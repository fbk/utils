package eu.fbk.utils.core;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Environment {

    private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);

    @Nullable
    private static List<String> propertyNames;

    private static Map<String, String> configuredProperties = new HashMap<>();

    private static Map<String, String> loadedProperties = new HashMap<>();

    private static Map<String, Optional<String>> frozenProperties = new ConcurrentHashMap<>();

    private static ExecutorService configuredPool = null;

    private static ExecutorService frozenPool = null;

    private static List<Plugin> frozenPlugins = null;

    @Nullable
    private static String frozenName;

    private static int frozenCores = 0;

    private static Map<String, ProcessBuilder> commands = new HashMap<String, ProcessBuilder>();

    static {
        // Initial properties initialized with some sensible defaults
        final Properties properties = new Properties();
        properties.setProperty("utils.environment.cores",
                "" + Runtime.getRuntime().availableProcessors());

        // Initial sources containing environment.properties and all the filenames mentioned in
        // system property environment.sources
        final Set<String> sources = Sets.newHashSet("environment.properties");
        sources.addAll(Splitter.on(',').omitEmptyStrings()
                .splitToList(System.getProperty("utils.environment.sources", "")));
        final List<String> queue = new LinkedList<>(sources);

        try {
            // Load properties from property sources
            while (!queue.isEmpty()) {

                // Pick the next source to load
                final String source = queue.remove(0);

                // Retrieve the URLs of classpath resources matching the source
                final List<URL> urls = new ArrayList<>();
                for (final String path : new String[] { "META-INF/" + source, source }) {
                    for (final Enumeration<URL> e = Environment.class.getClassLoader()
                            .getResources(path); e.hasMoreElements(); ) {
                        urls.add(e.nextElement());
                    }
                }

                // For each classpath resource, load properties and scan for new sources to load
                for (final URL url : urls) {
                    final Properties urlProperties = new Properties();
                    try (Reader in = new InputStreamReader(url.openStream(), Charsets.UTF_8)) {
                        urlProperties.load(in);
                        properties.putAll(urlProperties);
                        LOGGER.debug("Loaded {} properties from '{}'", urlProperties.size(), url);
                    } catch (final Throwable ex) {
                        LOGGER.warn("Could not load properties from '" + url + "' - ignore", ex);
                    }
                    for (final String urlSource : Splitter.on(',').omitEmptyStrings().splitToList(
                            urlProperties.getProperty("utils.environment.sources", ""))) {
                        if (sources.add(urlSource)) {
                            queue.add(urlSource);
                        }
                    }
                }
            }

        } catch (final IOException ex) {
            // Log warning and proceed
            LOGGER.warn("Could not complete loading of properties from classpath resources", ex);
        }

        for (final Map.Entry<?, ?> entry : properties.entrySet()) {
            loadedProperties.put((String) entry.getKey(), (String) entry.getValue());
        }
        for (final Map.Entry<?, ?> entry : System.getProperties().entrySet()) {
            loadedProperties.put((String) entry.getKey(), (String) entry.getValue());
        }
        for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
            final String key = entry.getKey().toString().toLowerCase().replace('_', '.');
            loadedProperties.put(key, entry.getValue());
        }
    }

    public static void configurePool(@Nullable final ExecutorService pool) {
        synchronized (Environment.class) {
            if (frozenPool != null) {
                throw new IllegalStateException("Thread pool already in use");
            }
            configuredPool = pool; // to be frozen later
        }
    }

    public static void configureProperty(final String name, @Nullable final String value) {
        Objects.requireNonNull(name);
        synchronized (Environment.class) {
            if (frozenPlugins != null && name.startsWith("plugin,")) {
                throw new IllegalStateException("Plugin configuration already loaded");
            }
            if (frozenProperties.containsKey(name)) {
                throw new IllegalStateException("Property " + name + " already in use (value "
                        + frozenProperties.get(name) + ")");
            }
            propertyNames = null; // invalidate
            if (value == null) {
                configuredProperties.remove(name);
            } else {
                configuredProperties.put(name, value);
            }
        }
    }

    public static String getName() {
        if (frozenName == null) {
            frozenName = getProperty("utils.environment.name", "application");
        }
        return frozenName;
    }

    public static int getCores() {
        if (frozenCores <= 0) {
            frozenCores = Integer.parseInt(getProperty("utils.environment.cores"));
        }
        return frozenCores;
    }

    public static ExecutorService getPool() {
        if (frozenPool == null) {
            synchronized (Environment.class) {
                if (frozenPool == null) {
                    frozenPool = configuredPool;
                    if (frozenPool == null) {
                        frozenPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                                .setDaemon(true).setPriority(Thread.NORM_PRIORITY)
                                .setNameFormat(getName() + "-%03d").build());
                    }
                    LOGGER.debug("Using pool {}", frozenPool);
                }
            }
        }
        return frozenPool;
    }

    public static void run(final Iterable<? extends Runnable> runnables) {

        final List<Runnable> runnableList = ImmutableList.copyOf(runnables);
        final int parallelism = Math.min(Environment.getCores(), runnableList.size());

        final CountDownLatch latch = new CountDownLatch(parallelism);
        final AtomicReference<Throwable> exception = new AtomicReference<Throwable>();
        final AtomicInteger index = new AtomicInteger(0);

        final List<Runnable> threadRunnables = new ArrayList<Runnable>();
        for (int i = 0; i < parallelism; ++i) {
            threadRunnables.add(new Runnable() {

                @Override
                public void run() {
                    try {
                        while (true) {
                            final int i = index.getAndIncrement();
                            if (i >= runnableList.size() || exception.get() != null) {
                                break;
                            }
                            runnableList.get(i).run();
                        }
                    } catch (final Throwable ex) {
                        exception.set(ex);
                    } finally {
                        latch.countDown();
                    }
                }

            });
        }

        try {
            for (int i = 1; i < parallelism; ++i) {
                Environment.getPool().submit(threadRunnables.get(i));
            }
            if (!threadRunnables.isEmpty()) {
                threadRunnables.get(0).run();
            }
            latch.await();
            if (exception.get() != null) {
                throw exception.get();
            }
        } catch (final Throwable ex) {
            Throwables.propagate(ex);
        }
    }

    @Nullable
    public static String getProperty(final String name) {
        Objects.requireNonNull(name);
        Optional<String> holder = frozenProperties.get(name);
        if (holder == null) {
            synchronized (Environment.class) {
                holder = frozenProperties.get(name);
                if (holder == null) {
                    String value;
                    if (configuredProperties.containsKey(name)) {
                        value = configuredProperties.get(name);
                    } else {
                        value = loadedProperties.get(name);
                    }
                    holder = Optional.ofNullable(value);
                    frozenProperties.put(name, holder);
                    if (value != null) {
                        LOGGER.debug("Using {} = {}", name, value);
                    }
                }
            }
        }
        return holder.orElse(null);
    }

    @Nullable
    public static String getProperty(final String name, @Nullable final String valueIfNull) {
        final String value = getProperty(name);
        return value != null ? value : valueIfNull;
    }

    public static List<String> getPropertyNames() {
        synchronized (Environment.class) {
            if (propertyNames == null) {
                propertyNames = new ArrayList<>();
                propertyNames.addAll(loadedProperties.keySet());
                for (final String property : configuredProperties.keySet()) {
                    if (!loadedProperties.containsKey(property)) {
                        propertyNames.add(property);
                    }
                }
                Collections.sort(propertyNames);
            }
        }
        return propertyNames;
    }

    public static ProcessBuilder getProcessBuilder(final String command, final String... args) {
        synchronized (commands) {
            ProcessBuilder template = commands.get(command);
            if (template == null) {
                final String property = "utils.environment.cmd." + command;
                String cmd = getProperty(property);
                if (cmd != null) {
                    try {
                        template = new ProcessBuilder();
                        cmd = cmd.trim();
                        final String[] tokens = tokenize(cmd, Pattern.compile("[\\s]+|(=)"));
                        int i = 0;
                        for (; i + 3 <= tokens.length; i += 3) {
                            if (tokens[i + 1].equals("=")) {
                                template.environment().put(tokens[i], tokens[i + 2]);
                            }
                        }
                        template.command().addAll(Arrays.asList(tokens).subList(i, tokens.length));
                        LOGGER.debug("Using {} for command {}", cmd, command);
                    } catch (final Throwable ex) {
                        LOGGER.warn("Ignoring invalid property " + property + " = " + cmd, ex);
                        template = null;
                    }
                }
                template = template != null ? template : new ProcessBuilder(command);
                commands.put(command, template);
            }
            final ProcessBuilder result = new ProcessBuilder(new ArrayList<>(template.command()));
            result.environment().putAll(template.environment());
            result.command().addAll(Arrays.asList(args));
            return result;
        }
    }

    public static Map<String, String> getPlugins(final Class<?> baseClass) {

        Objects.requireNonNull(baseClass);

        if (frozenPlugins == null) {
            loadPlugins();
        }

        final Map<String, String> map = new HashMap<>();
        for (final Plugin plugin : frozenPlugins) {
            if (baseClass.isAssignableFrom(plugin.factory.getReturnType())) {
                map.put(plugin.names.get(0), plugin.description);
            }
        }
        return map;
    }

    public static <T> T newPlugin(final Class<T> baseClass, final String name,
            final String... args) {

        Objects.requireNonNull(baseClass);
        Objects.requireNonNull(name);
        if (Arrays.asList(args).contains(null)) {
            throw new NullPointerException();
        }

        if (frozenPlugins == null) {
            loadPlugins();
        }

        for (final Plugin plugin : frozenPlugins) {
            if (baseClass.isAssignableFrom(plugin.factory.getReturnType())
                    && plugin.names.contains(name)) {
                try {
                    return baseClass.cast(plugin.factory.invoke(null, name, args));
                } catch (final IllegalAccessException ex) {
                    throw new Error("Unexpected error (!)", ex); // checked when
                    // loading
                    // plugins
                } catch (final InvocationTargetException ex) {
                    final Throwable cause = ex.getCause();
                    throw cause instanceof RuntimeException ? (RuntimeException) cause
                            : new RuntimeException(ex);
                }
            }
        }

        throw new IllegalArgumentException(
                "Unknown " + baseClass.getSimpleName() + " plugin '" + name + "'");
    }

    static String[] tokenize(final String string, final Pattern delimiter) {
        final List<String> tokens = new ArrayList<>();
        final Matcher matcher = delimiter.matcher(string);
        int start = 0;
        int index = 0;
        while (matcher.find(index)) {
            final char startCh = string.charAt(start);
            int end = matcher.start();
            if (startCh == '\'' || startCh == '"') {
                if (end - start < 2) {
                    index = matcher.start() + 1;
                    continue;
                }
                final char endCh = string.charAt(end - 1);
                int numEscapes = 0;
                for (int i = end - 2; i >= start && string.charAt(i) == '\\'; --i) {
                    ++numEscapes;
                }
                if (endCh != startCh || numEscapes % 2 == 1) {
                    index = matcher.start() + 1;
                    continue;
                }
                ++start;
                --end;
            }
            tokens.add(string.substring(start, end));
            for (int i = 1; i <= matcher.groupCount(); ++i) {
                final String group = matcher.group(i);
                if (group != null) {
                    tokens.add(group);
                }
            }
            index = matcher.end();
            start = matcher.end();
        }
        int end = string.length();
        if (end - start >= 2) {
            final char startCh = string.charAt(start);
            final char endCh = string.charAt(end - 1);
            if (startCh == endCh && startCh == '\'' || startCh == '"') {
                ++start;
                --end;
            }
        }
        tokens.add(start < end ? string.substring(start, end) : "");
        return tokens.toArray(new String[tokens.size()]);
    }

    @SuppressWarnings("unchecked")
    private static void loadPlugins() {
        synchronized (Environment.class) {
            if (frozenPlugins != null) {
                return;
            }
            final Set<String> disabledNames = new HashSet<>();
            final List<Plugin> plugins = new ArrayList<>();
            for (final Map<String, String> map : new Map[] { loadedProperties,
                    configuredProperties }) {
                for (final Map.Entry<String, String> entry : map.entrySet()) {
                    final String name = entry.getKey();
                    final String value = entry.getValue();
                    if (name.startsWith("plugin.enable.") || name.startsWith("plugin,enable,")) {
                        final List<String> names = Arrays
                                .asList(name.substring("plugin.enable.".length()).split("[.,]"));
                        if (value.equalsIgnoreCase("true")) {
                            disabledNames.removeAll(names);
                        } else {
                            disabledNames.addAll(names);
                        }
                    } else if (name.startsWith("plugin,") || name.startsWith("plugin.")) {
                        try {
                            final String s = name.substring("plugin.".length());
                            String[] tokens = s.split(",");
                            if (tokens.length == 1) {
                                final String[] allTokens = s.split("\\.");
                                for (int i = 0; i < allTokens.length; ++i) {
                                    if (Character.isUpperCase(allTokens[i].charAt(0))) {
                                        tokens = new String[allTokens.length - i];
                                        tokens[0] = String.join(".",
                                                Arrays.copyOfRange(allTokens, 0, i + 1));
                                        System.arraycopy(allTokens, i + 1, tokens, 1,
                                                allTokens.length - i - 1);
                                    }
                                }
                            }
                            final String className = tokens[0];
                            final String methodName = tokens[1];
                            final List<String> pluginNames = Arrays
                                    .asList(Arrays.copyOfRange(tokens, 2, tokens.length));
                            final Class<?> clazz = Class.forName(className);
                            final Method method = clazz.getDeclaredMethod(methodName, String.class,
                                    String[].class);
                            method.setAccessible(true);
                            plugins.add(new Plugin(pluginNames, value, method));
                        } catch (final Throwable ex) {
                            LOGGER.warn("Invalid plugin definition " + name + " - ignoring", ex);
                        }
                    }
                }
            }
            for (final Iterator<Plugin> i = plugins.iterator(); i.hasNext(); ) {
                final List<String> names = i.next().names;
                for (final String name : names) {
                    if (disabledNames.contains(name)) {
                        i.remove();
                        break;
                    }
                }
            }
            frozenPlugins = plugins;
        }
    }

    private static final class Plugin {

        public final List<String> names;

        public final String description;

        public final Method factory;

        Plugin(final List<String> names, final String description, final Method factory) {
            this.names = names;
            this.description = description;
            this.factory = factory;
        }

    }

}

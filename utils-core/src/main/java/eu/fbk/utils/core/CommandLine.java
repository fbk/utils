package eu.fbk.utils.core;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class CommandLine {

    private final List<String> args;

    private final List<String> options;

    private final Map<String, List<String>> optionValues;

    private CommandLine(final List<String> args, final Map<String, List<String>> optionValues) {

        final List<String> options = Lists.newArrayList();
        for (final String letterOrName : optionValues.keySet()) {
            if (letterOrName.length() > 1) {
                options.add(letterOrName);
            }
        }

        this.args = args;
        this.options = Ordering.natural().immutableSortedCopy(options);
        this.optionValues = optionValues;
    }

    public <T> List<T> getArgs(final Class<T> type) {
        return convert(this.args, type);
    }

    public <T> T getArg(final int index, final Class<T> type) {
        return convert(this.args.get(index), type);
    }

    public <T> T getArg(final int index, final Class<T> type, final T defaultValue) {
        try {
            return convert(this.args.get(index), type);
        } catch (final Throwable ex) {
            return defaultValue;
        }
    }

    public int getArgCount() {
        return this.args.size();
    }

    public List<String> getOptions() {
        return this.options;
    }

    public boolean hasOption(final String letterOrName) {
        return this.optionValues.containsKey(letterOrName);
    }

    public <T> List<T> getOptionValues(final String letterOrName, final Class<T> type) {
        final List<String> strings = Objects.firstNonNull(this.optionValues.get(letterOrName),
                ImmutableList.<String>of());
        return convert(strings, type);
    }

    @Nullable
    public <T> T getOptionValue(final String letterOrName, final Class<T> type) {
        final List<String> strings = this.optionValues.get(letterOrName);
        if (strings == null || strings.isEmpty()) {
            return null;
        }
        if (strings.size() > 1) {
            throw new Exception("Multiple values for option '" + letterOrName + "': "
                    + Joiner.on(", ").join(strings), null);
        }
        try {
            return convert(strings.get(0), type);
        } catch (final Throwable ex) {
            throw new Exception("'" + strings.get(0) + "' is not a valid " + type.getSimpleName(),
                    ex);
        }
    }

    @Nullable
    public <T> T getOptionValue(final String letterOrName, final Class<T> type,
            @Nullable final T defaultValue) {
        final List<String> strings = this.optionValues.get(letterOrName);
        if (strings == null || strings.isEmpty() || strings.size() > 1) {
            return defaultValue;
        }
        try {
            return convert(strings.get(0), type);
        } catch (final Throwable ex) {
            return defaultValue;
        }
    }

    public int getOptionCount() {
        return this.options.size();
    }

    @SuppressWarnings("unchecked")
    private static <T> T convert(final String string, final Class<T> type) {
        try {
            if (Path.class.isAssignableFrom(type)) {
                return (T) Paths.get(string);
            }
            return Conversion.convert(string, type);
        } catch (final Throwable ex) {
            throw new Exception("'" + string + "' is not a valid " + type.getSimpleName(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> convert(final List<String> strings, final Class<T> type) {
        if (type == String.class) {
            return (List<T>) strings;
        }
        final List<T> list = Lists.newArrayList();
        for (final String string : strings) {
            list.add(convert(string, type));
        }
        return ImmutableList.copyOf(list);
    }

    private static Object call(final Object object, final String methodName,
            final Object... args) {
        final boolean isStatic = object instanceof Class<?>;
        final Class<?> clazz = isStatic ? (Class<?>) object : object.getClass();
        for (final Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)
                    && isStatic == Modifier.isStatic(method.getModifiers())
                    && method.getParameterTypes().length == args.length) {
                try {
                    return method.invoke(isStatic ? null : object, args);
                } catch (final InvocationTargetException ex) {
                    Throwables.propagate(ex.getCause());
                } catch (final IllegalAccessException ex) {
                    throw new IllegalArgumentException("Cannot invoke " + method, ex);
                }
            }
        }
        throw new IllegalArgumentException("Cannot invoke " + methodName);
    }

    public static void fail(final Throwable throwable) {
        if (throwable instanceof Exception) {
            if (throwable.getMessage() == null) {
                System.exit(0);
            } else {
                System.err.println("SYNTAX ERROR: " + throwable.getMessage());
            }
            System.exit(-2);
        } else {
            System.err.println("EXECUTION FAILED: " + throwable.getMessage());
            throwable.printStackTrace();
            System.exit(-1);
        }
    }

    public static Parser parser() {
        return new Parser();
    }

    public static final class Parser {

        @Nullable
        private String name;

        @Nullable
        private String header;

        @Nullable
        private String footer;

        @Nullable
        private Logger logger;

        private final Options options;

        private final Set<String> mandatoryOptions;

        public Parser() {
            this.name = null;
            this.header = null;
            this.footer = null;
            this.options = new Options();
            this.mandatoryOptions = new HashSet<>();
        }

        /**
         * Sets the name of the executable tool to call from the command line. The name could be
         * the filename of the script invoking Java, or the base java command for running the
         * application. This information is used to generate the help message.
         *
         * @param name the application name, possibly null to show nothing
         * @return this {@code Parser} object, for call chaining
         */
        public Parser withName(@Nullable final String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the text to be displayed before the option list in the help message.
         *
         * @param header the header text, possibly null to show nothing
         * @return this {@code Parser} object, for call chaining
         */
        public Parser withHeader(@Nullable final String header) {
            this.header = header;
            return this;
        }

        /**
         * Sets the text to be displayed after the option list in the help message.
         *
         * @param footer the footer text, possibly null to show nothing
         * @return this {@code Parser} object, for call chaining
         */
        public Parser withFooter(@Nullable final String footer) {
            this.footer = footer;
            return this;
        }

        /**
         * Sets the logger object controlled by verbosity level options.
         *
         * @param logger the controlled logger object, possibly null to disable verbosity level
         *               options
         * @return this {@code Parser} object, for call chaining
         */
        public Parser withLogger(@Nullable final Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Defines an option taking zero arguments (a flag). At least one among the short and long
         * name should be specified.
         *
         * @param shortName   the short name (one letter) associated to the option, if any
         * @param longname    the long name associated to the option, if any
         * @param description the description associated to the option, or null to hide the option in the
         *                    help message
         * @return this {@code Parser} object, for call chaining
         */
        public Parser withOption(@Nullable final String shortName, @Nullable final String longName,
                @Nullable final String description) {

            checkOptionNames(shortName, longName);

            final Option option = new Option(shortName == null ? null : shortName, longName, false,
                    description);
            this.options.addOption(option);
            return this;
        }

        /**
         * Defines an option taking one argument.
         *
         * @param shortName   the short name (one letter) associated to the option, if any
         * @param longName    the long name associated to the option, if any
         * @param description the description associated to the option, or null to hide the option in the
         *                    help message
         * @param argName     the name of the argument to display in the help message, mandatory
         * @param argType     the type associated to the option argument, optional
         * @param argRequired true if option value(s) are required
         * @param multiValue  true if the option accepts multiple values
         * @param mandatory   true if the option and its value must be necessarily specified on the
         *                    command line
         * @return this {@code Parser} object, for call chaining
         */
        public Parser withOption(@Nullable final String shortName, @Nullable final String longName,
                @Nullable final String description, final String argName,
                @Nullable final Type argType, final boolean argRequired, final boolean multiValue,
                final boolean mandatory) {

            checkOptionNames(shortName, longName);
            Preconditions.checkNotNull(argName);
            if (argName.isEmpty()) {
                throw new IllegalArgumentException("Empty argName string");
            }

            final Option option = new Option(shortName == null ? null : shortName.toString(),
                    longName, true, description);
            option.setArgName(argName);
            option.setOptionalArg(!argRequired);
            option.setArgs(multiValue ? Short.MAX_VALUE : 1);
            option.setType(argType.toClass());
            this.options.addOption(option);

            if (mandatory) {
                this.mandatoryOptions.add(longName);
            }

            return this;
        }

        public CommandLine parse(final String... args) {

            try {
                // Add additional options
                if (this.logger != null) {
                    this.options.addOption(null, "debug", false, "enable verbose output");
                    this.options.addOption(null, "trace", false, "enable very verbose output");
                }
                this.options.addOption("v", "version", false,
                        "display version information and terminate");
                this.options.addOption("h", "help", false,
                        "display this help message and terminate");

                // Parse options
                org.apache.commons.cli.CommandLine cmd = null;
                try {
                    cmd = new DefaultParser().parse(this.options, args);
                } catch (final Throwable ex) {
                    System.err.println("SYNTAX ERROR: " + ex.getMessage());
                    printHelp();
                    throw new Exception(null);
                }

                try {
                    // Handle verbose mode via reflection, depending on the SLF4J backend used
                    final String loggerClassName = this.logger.getClass().getName();
                    if (loggerClassName.equals("ch.qos.logback.classic.Logger")) {
                        final Class<?> levelClass = Class.forName("ch.qos.logback.classic.Level");
                        final Object level = call(levelClass, "valueOf", cmd.hasOption("trace")
                                ? "TRACE" : cmd.hasOption("debug") ? "DEBUG" : "INFO");
                        call(this.logger, "setLevel", level);
                    } else if (loggerClassName.equals("org.apache.log4j.Logger")) {
                        final Class<?> levelClass = Class.forName("org.apache.log4j.Level");
                        final Object level = call(levelClass, "valueOf", cmd.hasOption("trace")
                                ? "TRACE" : cmd.hasOption("debug") ? "DEBUG" : "INFO");
                        call(this.logger, "setLevel", level);
                    } else if (loggerClassName.equals("org.apache.logging.slf4j.Log4jLogger")) {
                        final Class<?> managerClass = Class
                                .forName("org.apache.logging.log4j.LogManager");
                        final Object ctx = call(managerClass, "getContext", false);
                        final Object config = call(ctx, "getConfiguration");
                        final Object logConfig = call(config, "getLoggerConfig",
                                this.logger.getName());
                        final Class<?> levelClass = Class
                                .forName("org.apache.logging.log4j.Level");
                        final Object level = call(levelClass, "valueOf", cmd.hasOption("trace")
                                ? "TRACE" : cmd.hasOption("debug") ? "DEBUG" : "INFO");
                        call(logConfig, "setLevel", level);
                        call(ctx, "updateLoggers");
                    }

                } catch (final Throwable ex) {
                    // ignore
                }

                // Handle version and help commands. Throw an exception to halt execution
                if (cmd.hasOption('v')) {
                    printVersion();
                    throw new Exception(null);

                } else if (cmd.hasOption('h')) {
                    printHelp();
                    throw new Exception(null);
                }

                // Check that mandatory options have been specified
                for (final String name : this.mandatoryOptions) {
                    if (!cmd.hasOption(name)) {
                        System.err.println("SYNTAX ERROR: missing mandatory option " + name);
                        printHelp();
                        throw new Exception(null);
                    }
                }

                // Extract options and their arguments
                final Map<String, List<String>> optionValues = Maps.newHashMap();
                for (final Option option : cmd.getOptions()) {
                    final List<String> valueList = Lists.newArrayList();
                    final String[] values = cmd.getOptionValues(option.getLongOpt());
                    if (values != null) {
                        for (final String value : values) {
                            if (option.getType() instanceof Type) {
                                Type.validate(value, (Type) option.getType());
                            }
                            valueList.add(value);
                        }
                    }
                    final List<String> valueSet = ImmutableList.copyOf(valueList);
                    optionValues.put(option.getLongOpt(), valueSet);
                    if (option.getOpt() != null) {
                        optionValues.put(option.getOpt(), valueSet);
                    }
                }

                // Create and return the resulting CommandLine object
                return new CommandLine(ImmutableList.copyOf(cmd.getArgList()), optionValues);

            } catch (final Throwable ex) {
                throw new Exception(ex.getMessage(), ex);
            }
        }

        private void checkOptionNames(@Nullable final String shortName,
                @Nullable final String longName) {
            if (shortName == null && longName == null) {
                throw new IllegalArgumentException(
                        "At least one among short and long option names should be specified");
            }
            if (longName != null && longName.length() <= 1) {
                throw new IllegalArgumentException(
                        "Long option name should be longer than one character");
            }
        }

        private void printVersion() {
            String version = "(development)";
            final URL url = CommandLine.class.getClassLoader()
                    .getResource("META-INF/maven/eu.fbk.nafview/nafview/pom.properties");
            if (url != null) {
                try {
                    final InputStream stream = url.openStream();
                    try {
                        final Properties properties = new Properties();
                        properties.load(stream);
                        version = properties.getProperty("version").trim();
                    } finally {
                        stream.close();
                    }

                } catch (final IOException ex) {
                    version = "(unknown)";
                }
            }
            final String name = Objects.firstNonNull(this.name, "Version");
            System.out.println(String.format("%s %s\nJava %s bit (%s) %s\n", name, version,
                    System.getProperty("sun.arch.data.model"), System.getProperty("java.vendor"),
                    System.getProperty("java.version")));
        }

        private void printHelp() {
            final HelpFormatter formatter = new HelpFormatter();
            final PrintWriter out = new PrintWriter(System.out);
            final String name = Objects.firstNonNull(this.name, "java");
            formatter.printUsage(out, 80, name, this.options);
            if (this.header != null) {
                out.println();
                formatter.printWrapped(out, 80, this.header);
            }
            out.println();
            formatter.printOptions(out, 80, this.options, 2, 2);
            if (this.footer != null) {
                out.println();
                out.println(this.footer);
                // formatter.printWrapped(out, 80, this.footer);
            }
            out.flush();
        }
    }

    public static final class Exception extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public Exception(final String message) {
            super(message);
        }

        public Exception(final String message, final Throwable cause) {
            super(message, cause);
        }

    }

    public enum Type {

        STRING,

        INTEGER,

        POSITIVE_INTEGER,

        NON_NEGATIVE_INTEGER,

        FLOAT,

        POSITIVE_FLOAT,

        NON_NEGATIVE_FLOAT,

        FILE,

        FILE_EXISTING,

        DIRECTORY,

        DIRECTORY_EXISTING;

        public boolean validate(final String string) {
            // Polymorphism not used for performance reasons
            return validate(string, this);
        }

        public Class toClass() {
            return toClass.get(this);
        }

        public static HashMap<Type, Class> toClass = new HashMap<>();

        static {
            toClass.put(Type.STRING, String.class);

            toClass.put(Type.INTEGER, Integer.class);
            toClass.put(Type.POSITIVE_INTEGER, Integer.class);
            toClass.put(Type.NON_NEGATIVE_INTEGER, Integer.class);

            toClass.put(Type.FLOAT, Float.class);
            toClass.put(Type.POSITIVE_FLOAT, Float.class);
            toClass.put(Type.NON_NEGATIVE_FLOAT, Float.class);

            toClass.put(Type.FILE, File.class);
            toClass.put(Type.FILE_EXISTING, File.class);
            toClass.put(Type.DIRECTORY, File.class);
            toClass.put(Type.DIRECTORY_EXISTING, File.class);
        }

        private static boolean validate(final String string, final Type type) {

            if (type == Type.INTEGER || type == Type.POSITIVE_INTEGER
                    || type == Type.NON_NEGATIVE_INTEGER) {
                try {
                    final long n = Long.parseLong(string);
                    if (type == Type.POSITIVE_INTEGER) {
                        return n > 0L;
                    } else if (type == Type.NON_NEGATIVE_INTEGER) {
                        return n >= 0L;
                    }
                } catch (final Throwable ex) {
                    return false;
                }

            } else if (type == Type.FLOAT || type == Type.POSITIVE_FLOAT
                    || type == Type.NON_NEGATIVE_FLOAT) {
                try {
                    final double n = Double.parseDouble(string);
                    if (type == Type.POSITIVE_FLOAT) {
                        return n > 0.0;
                    } else if (type == Type.NON_NEGATIVE_FLOAT) {
                        return n >= 0.0;
                    }
                } catch (final Throwable ex) {
                    return false;
                }

            } else if (type == FILE) {
                final File file = new File(string);
                return !file.exists() || file.isFile();

            } else if (type == FILE_EXISTING) {
                final File file = new File(string);
                return file.exists() && file.isFile();

            } else if (type == DIRECTORY) {
                final File dir = new File(string);
                return !dir.exists() || dir.isDirectory();

            } else if (type == DIRECTORY_EXISTING) {
                final File dir = new File(string);
                return dir.exists() && dir.isDirectory();
            }

            return true;
        }

    }

}

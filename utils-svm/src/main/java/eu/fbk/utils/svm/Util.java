package eu.fbk.utils.svm;

import com.google.common.base.Charsets;
import com.google.common.collect.*;
import eu.fbk.utils.core.Environment;
import eu.fbk.utils.core.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

// TODO: change class name based on its final contents

public final class Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    public static final Properties PROPERTIES;

    static {
        // TODO: this feature should migrate to Environment
        PROPERTIES = new Properties();
        try {
            final List<URL> urls = new ArrayList<>();
            final ClassLoader cl = Environment.class.getClassLoader();
            for (final String p : new String[] { "META-INF/naftools.properties",
                    "naftools.properties" }) {
                for (final Enumeration<URL> e = cl.getResources(p); e.hasMoreElements(); ) {
                    urls.add(e.nextElement());
                }
            }
            for (final URL url : urls) {
                final Reader in = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));
                try {
                    PROPERTIES.load(in);
                    LOGGER.debug("Loaded configuration from '" + url + "'");
                } catch (final Throwable ex) {
                    LOGGER.warn("Could not load configuration from '" + url + "' - ignoring", ex);
                } finally {
                    in.close();
                }
            }
        } catch (final IOException ex) {
            LOGGER.warn("Could not complete loading of configuration from classpath resources", ex);
        }
    }

    public static Properties parseProperties(final String string) {
        final Properties properties = new Properties();
        final int len = string.length();
        String key = null;
        int index = 0;
        while (true) {
            final int newIndex = string.indexOf('=', index);
            if (newIndex < 0) {
                if (key != null && len > index) {
                    properties.setProperty(key, string.substring(index, len).trim());
                }
                break;
            }
            int end = newIndex - 1;
            while (end >= 0 && Character.isWhitespace(string.charAt(end))) {
                --end;
            }
            if (end > 0) {
                int start = end;
                while (start > 0 && !Character.isWhitespace(string.charAt(start - 1))) {
                    --start;
                }
                if (key != null && start > index) {
                    properties.setProperty(key, string.substring(index, start).trim());
                }
                key = string.substring(start, end + 1);
            }
            index = newIndex + 1;
        }
        return properties;
    }

    public static void deleteRecursively(final Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
                    throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    public static Path openVFS(final Path rootPath, final boolean clean) throws IOException {
        if (Files.isDirectory(rootPath)) {
            if (clean) {
                deleteRecursively(rootPath);
                Files.createDirectory(rootPath);
            }
            return rootPath;
        }
        if (rootPath.toString().endsWith(".zip")
                && rootPath.getFileSystem() == FileSystems.getDefault()) {
            boolean exists = Files.exists(rootPath);
            if (clean && exists) {
                Files.delete(rootPath);
                exists = false;
            }
            if (!exists) {
                try (OutputStream stream = Files.newOutputStream(rootPath,
                        StandardOpenOption.CREATE)) {
                    stream.write(new byte[] { 80, 75, 05, 06, 00, 00, 00, 00, 00, 00, 00, 00, 00,
                            00, 00, 00, 00, 00, 00, 00, 00, 00 });
                }
            }
            final FileSystem fs = FileSystems.newFileSystem(rootPath, null);
            return fs.getRootDirectories().iterator().next();
        }
        Files.createDirectories(rootPath);
        return rootPath;
    }

    public static void closeVFS(final Path rootPath) throws IOException {
        if (rootPath.getParent() == null && rootPath.getFileSystem() != FileSystems.getDefault()) {
            rootPath.getFileSystem().close();
        }
    }

    public static Function<String, String> fileRenamer(final String fromPath, final String toPath,
            @Nullable final String toExtension, final boolean junkPaths) {

        return new Function<String, String>() {

            @Override
            public String apply(final String path) {
                String name;
                if (!path.startsWith(fromPath) || junkPaths) {
                    name = Paths.get(path).getFileName().toString();
                } else {
                    name = path.substring(fromPath.length());
                }
                if (toExtension != null) {
                    while (true) {
                        final int index = name.lastIndexOf('.');
                        if (index < 0 || name.length() - index > 4) {
                            break;
                        }
                        name = name.substring(0, index);
                    }
                    name = name + (toExtension.startsWith(".") ? "" : ".") + toExtension;
                }
                return toPath + name;
            }

        };
    }

    @Nullable
    public static List<Path> fileMatch(@Nullable final Iterable<Path> filesOrDirs,
            @Nullable final Iterable<String> extensions, final boolean recursive) {
        try {
            return fileMatch(filesOrDirs, extensions, recursive, false);
        } catch (final IOException ex) {
            throw new Error(ex);
        }
    }

    @Nullable
    public static List<Path> fileMatch(@Nullable final Iterable<Path> filesOrDirs,
            @Nullable final Iterable<String> extensions, final boolean recursive,
            final boolean indirect) throws IOException {

        if (!indirect && filesOrDirs == null) {
            return null;
        }

        final List<Path> files = Lists.newArrayList();
        if (filesOrDirs != null) {
            for (final Path path : filesOrDirs) {
                if (!Files.exists(path)) {
                    throw new IllegalArgumentException("No such file or directory: " + path);
                } else if (Files.isRegularFile(path)) {
                    files.add(path);
                } else {
                    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult preVisitDirectory(final Path dir,
                                final BasicFileAttributes attrs) throws IOException {
                            return recursive || dir.equals(path) ? FileVisitResult.CONTINUE
                                    : FileVisitResult.SKIP_SUBTREE;
                        }

                        @Override
                        public FileVisitResult visitFile(final Path file,
                                final BasicFileAttributes attrs) throws IOException {
                            if (extensions == null) {
                                files.add(file);
                            } else {
                                for (final String extension : extensions) {
                                    if (file.toString().endsWith(extension)) {
                                        files.add(file);
                                        break;
                                    }
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }

                    });
                }
            }
        }

        List<Path> result = files;
        if (indirect) {
            if (filesOrDirs == null) {
                try (BufferedReader reader = new BufferedReader(
                        IO.utf8Reader(IO.buffer(System.in)))) {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        result.add(Paths.get(line.trim()));
                    }
                }
            } else {
                result = Lists.newArrayList();
                for (final Path file : files) {
                    for (final String line : Files.readAllLines(file, Charsets.UTF_8)) {
                        files.add(Paths.get(line.trim()));
                    }
                }
            }
        }

        Collections.sort(result);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(final Class<T> clazz, final Object... elements) {
        final T[] array = newArray(clazz, elements.length);
        for (int i = 0; i < elements.length; ++i) {
            array[i] = (T) elements[i];
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(final Class<T> clazz, final int length) {
        return (T[]) Array.newInstance(clazz, length);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[][] newArray(final Class<T> clazz, final int length1, final int length2) {
        final Class<T[]> elementClass = (Class<T[]>) newArray(clazz, 0).getClass();
        final T[][] array = newArray(elementClass, length1);
        if (length2 > 0) {
            for (int i = 0; i < length1; ++i) {
                array[i] = newArray(clazz, length2);
            }
        }
        return array;
    }

    /**
     * Clean an illegal IRI string, trying to make it legal (as per RFC 3987).
     *
     * @param string the IRI string to clean
     * @return the cleaned IRI string (possibly the input unchanged) upon success
     * @throws IllegalArgumentException in case the supplied input cannot be transformed into a legal IRI
     */
    @Nullable
    public static String cleanIRI(@Nullable final String string) throws IllegalArgumentException {

        // Handle null input
        if (string == null) {
            return null;
        }

        // Illegal characters should be percent encoded. Illegal IRI characters are all the
        // character that are not 'unreserved' (A-Z a-z 0-9 - . _ ~ 0xA0-0xD7FF 0xF900-0xFDCF
        // 0xFDF0-0xFFEF) or 'reserved' (! # $ % & ' ( ) * + , / : ; = ? @ [ ])
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < string.length(); ++i) {
            final char c = string.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= '?' && c <= '[' || c >= '&' && c <= ';' || c == '#'
                    || c == '$' || c == '!' || c == '=' || c == ']' || c == '_' || c == '~'
                    || c >= 0xA0 && c <= 0xD7FF || c >= 0xF900 && c <= 0xFDCF || c >= 0xFDF0
                    && c <= 0xFFEF) {
                builder.append(c);
            } else if (c == '%' && i < string.length() - 2
                    && Character.digit(string.charAt(i + 1), 16) >= 0
                    && Character.digit(string.charAt(i + 2), 16) >= 0) {
                builder.append('%'); // preserve valid percent encodings
            } else {
                builder.append('%').append(Character.forDigit(c / 16, 16))
                        .append(Character.forDigit(c % 16, 16));
            }
        }

        // Return the cleaned IRI (no Java validation as it is an IRI, not a URI)
        return builder.toString();
    }

    public static String[] hardTokenize(final String text) {
        if (text.length() == 0) {
            return new String[0];
        }
        final List<String> list = new ArrayList<String>();
        char currentChar = text.charAt(0);
        char previousChar = currentChar;
        int start = 0;
        boolean isCurrentCharLetterOrDigit;
        boolean isPreviousCharLetterOrDigit;
        if (!Character.isLetterOrDigit(currentChar)) {
            if (!Character.isWhitespace(currentChar)) {
                list.add(new String(new char[] { currentChar }));
            }
        }
        for (int i = 1; i < text.length(); i++) {
            currentChar = text.charAt(i);
            isCurrentCharLetterOrDigit = Character.isLetterOrDigit(currentChar);
            isPreviousCharLetterOrDigit = Character.isLetterOrDigit(previousChar);
            if (isCurrentCharLetterOrDigit) {
                if (!isPreviousCharLetterOrDigit) {
                    start = i;
                }
            } else {
                if (isPreviousCharLetterOrDigit) {
                    list.add(text.substring(start, i));
                    if (!Character.isWhitespace(currentChar)) {
                        list.add(new String(new char[] { currentChar }));
                    }
                } else {
                    if (!Character.isWhitespace(currentChar)) {
                        list.add(new String(new char[] { currentChar }));
                    }
                }
            }
            previousChar = currentChar;
        }
        if (Character.isLetterOrDigit(previousChar)) {
            list.add(text.substring(start, text.length()));
        }
        return list.toArray(new String[list.size()]);
    }

    public static double coverage(@Nullable final Iterable<?> covered,
            @Nullable final Iterable<?> covering) {
        final Set<Object> coveringSet = covering == null ? ImmutableSet.of() //
                : ImmutableSet.copyOf(covering);
        final Set<Object> coveredSet = covered == null ? ImmutableSet.of() //
                : ImmutableSet.copyOf(covered);
        if (coveredSet.isEmpty()) {
            return coveringSet.isEmpty() ? 1.0 : 0.0;
        } else {
            final int num = Sets.intersection(coveringSet, coveredSet).size();
            final int den = coveredSet.size();
            return (double) num / (double) den;
        }
    }

    // BEGIN

    public static <T, E extends T> T[][] align(final Class<T> clazz, final Iterable<E> objects1,
            final Iterable<E> objects2, final boolean functional, final boolean invFunctional,
            final boolean emitUnaligned, final BiFunction<? super E, ? super E, ?> matcher) {

        final Table<E, E, AlignPair<E>> table = HashBasedTable.create();
        for (final E object1 : objects1) {
            for (final E object2 : objects2) {
                final Object similarity = matcher.apply(object1, object2);
                table.put(object1, object2, new AlignPair<E>(object1, object2, similarity));
            }
        }

        final Set<E> set1 = emitUnaligned ? Sets.newHashSet(objects1) : null;
        final Set<E> set2 = emitUnaligned ? Sets.newHashSet(objects2) : null;

        final List<T[]> pairs = Lists.newArrayList();

        while (!table.isEmpty()) {
            final AlignPair<E> bestPair = Ordering.natural().max(table.values());
            if (bestPair.similarity == null) {
                break; // no more matches
            }
            final T[] pair = newArray(clazz, bestPair.object1, bestPair.object2);
            pairs.add(pair);
            if (functional) {
                table.rowKeySet().remove(bestPair.object1);
            }
            if (invFunctional) {
                table.columnKeySet().remove(bestPair.object2);
            }
            if (!functional && !invFunctional) {
                table.remove(bestPair.object1, bestPair.object2);
            }
            if (emitUnaligned) {
                set1.remove(bestPair.object1);
                set2.remove(bestPair.object2);
            }
        }

        if (emitUnaligned) {
            for (final E object1 : set1) {
                pairs.add(newArray(clazz, object1, null));
            }
            for (final E object2 : set2) {
                pairs.add(newArray(clazz, null, object2));
            }
        }

        return pairs.toArray(newArray(clazz, pairs.size(), -1));
    }

    private static final class AlignPair<T> implements Comparable<AlignPair<?>> {

        final T object1;

        final T object2;

        @Nullable
        Object similarity;

        public AlignPair(final T object1, final T object2, final Object similarity) {
            this.object1 = object1;
            this.object2 = object2;
            this.similarity = similarity;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public int compareTo(final AlignPair other) {
            if (this.similarity == null) {
                return other.similarity == null ? 0 : -1;
            } else if (other.similarity == null) {
                return 1;
            } else if (this.similarity instanceof Comparable<?>) {
                return ((Comparable<Object>) this.similarity).compareTo(other.similarity);
            } else if (this.similarity instanceof Iterable<?>) {
                return Ordering
                        .natural()
                        .lexicographical()
                        .compare((Iterable<Comparable>) this.similarity,
                                (Iterable<Comparable>) other.similarity);
            } else {
                throw new IllegalArgumentException("Could not compare similarities "
                        + this.similarity + ", " + other.similarity);
            }
        }

    }

}

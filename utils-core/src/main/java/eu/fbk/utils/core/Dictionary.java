package eu.fbk.utils.core;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;

public final class Dictionary<T> implements Iterable<T> {

    private final Map<T, Integer> map;

    private final List<T> list;

    private Dictionary(final Map<T, Integer> map, final List<T> list) {
        this.map = map;
        this.list = list;
    }

    private static boolean isTextFormat(final String filename) {
        final String name = filename.toLowerCase();
        return name.endsWith(".tsv") || name.endsWith(".txt") || name.contains(".tsv.")
                || name.contains(".txt.");
    }

    private static <T> Function<String, T> valueOfFunction(final Class<T> clazz) {

        Executable executable = null;

        for (final String name : new String[] { "valueOf", "fromString" }) {
            try {
                final Method method = clazz.getDeclaredMethod(name, String.class);
                if (Modifier.isStatic(method.getModifiers())
                        && clazz.isAssignableFrom(method.getReturnType())) {
                    executable = method;
                    break;
                }
            } catch (final Throwable ex) {
                // ignore
            }
        }

        try {
            executable = clazz.getConstructor(String.class);
        } catch (final Throwable ex) {
            // ignore
        }

        if (executable == null) {
            throw new IllegalArgumentException(
                    "Don't know how to transform strings into instances of " + clazz);
        }

        final Executable theExecutable = executable;
        return new Function<String, T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T apply(final String string) {
                try {
                    if (theExecutable instanceof Method) {
                        return (T) ((Method) theExecutable).invoke(null, string);
                    } else {
                        return ((Constructor<T>) theExecutable).newInstance(string);
                    }
                } catch (final IllegalAccessException | InstantiationException ex) {
                    throw new Error(ex);
                } catch (final InvocationTargetException ex) {
                    Throwables.throwIfUnchecked(ex.getCause());
                    throw new RuntimeException(ex.getCause());
                }
            }

        };
    }

    public static <T> Dictionary<T> create() {
        return new Dictionary<T>(new HashMap<T, Integer>(), new ArrayList<T>());
    }

    public static <T> Dictionary<T> create(final Dictionary<T> dictionary) {
        return new Dictionary<T>(new HashMap<T, Integer>(dictionary.map),
                new ArrayList<T>(dictionary.list));
    }

    public static <T> Dictionary<T> readFrom(final Class<T> elementClass, final Path path)
            throws IOException {
        try (InputStream stream = IO.buffer(Files.newInputStream(path))) {
            if (isTextFormat(path.toString())) {
                return readFrom(elementClass, IO.utf8Reader(stream));
            } else {
                return readFrom(elementClass, stream);
            }
        }
    }

    public static <T> Dictionary<T> readFrom(final Class<T> elementClass, final InputStream stream)
            throws IOException {
        final Dictionary<T> dictionary = create();
        final ObjectInputStream ois = new ObjectInputStream(stream);
        final int size = ois.readInt();
        try {
            for (int i = 0; i < size; ++i) {
                final T element = elementClass.cast(ois.readObject());
                dictionary.map.put(element, i);
                dictionary.list.add(element);
            }
        } catch (final ClassNotFoundException ex) {
            throw new IOException("Invalid file content", ex);
        }
        return dictionary;
    }

    public static <T> Dictionary<T> readFrom(final Class<T> elementClass, final Reader reader)
            throws IOException {
        final Dictionary<T> dictionary = create();
        final BufferedReader in = reader instanceof BufferedReader ? (BufferedReader) reader
                : new BufferedReader(reader);
        final Function<String, T> valueOfFunction = valueOfFunction(elementClass);
        String line;
        while ((line = in.readLine()) != null) {
            final T element = valueOfFunction.apply(line.trim());
            dictionary.map.put(element, dictionary.list.size());
            dictionary.list.add(element);
        }
        return dictionary;
    }

    public void writeTo(final Path path) throws IOException {
        try (OutputStream stream = IO.buffer(Files.newOutputStream(path,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE))) {
            if (isTextFormat(path.toString())) {
                writeTo(IO.utf8Writer(stream));
            } else {
                writeTo(stream);
            }
        }
    }

    public void writeTo(final OutputStream stream) throws IOException {
        final ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeInt(this.list.size());
        for (final T element : this.list) {
            oos.writeObject(element);
        }
        oos.flush();
    }

    public void writeTo(final Writer writer) throws IOException {
        for (int i = 0; i < this.list.size(); ++i) {
            writer.append(this.list.get(i).toString()).append('\n');
        }
        writer.flush();
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public int size() {
        return this.list.size();
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.unmodifiableIterator(this.list.iterator());
    }

    @Nullable
    public Integer indexFor(final T element) {
        Integer index = this.map.get(element);
        if (index == null && !(this.list instanceof ImmutableList<?>)) {
            index = this.list.size();
            this.list.add(element);
            this.map.put(element, index);
        }
        return index;
    }

    @Nullable
    public T elementFor(final int index) {
        try {
            return this.list.get(index);
        } catch (final IndexOutOfBoundsException ex) {
            throw new IllegalArgumentException(
                    "No element for index " + index + " (size is " + size() + ")");
        }
    }

    public Dictionary<T> freeze() {
        if (this.list instanceof ImmutableList<?>) {
            return this;
        } else {
            return new Dictionary<T>(ImmutableMap.copyOf(this.map),
                    ImmutableList.copyOf(this.list));
        }
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Dictionary<?>)) {
            return false;
        }
        final Dictionary<?> other = (Dictionary<?>) object;
        return this.map.equals(other.map) && this.list.equals(other.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.map, this.list);
    }

    @Override
    public String toString() {
        return this.map.toString();
    }

}
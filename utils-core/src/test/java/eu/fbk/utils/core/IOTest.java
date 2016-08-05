package eu.fbk.utils.core;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.io.CharStreams;

import org.junit.Assert;
import org.junit.Test;

public class IOTest {

    @Test
    public void testReadWrite() throws IOException {
        // Environment.configureProperty("utils.environment.cmd.gzip", "pigz");
        final Path path = Files.createTempFile("utils", ".gz");
        try {
            try (Writer w = IO.utf8Writer(IO.buffer(IO.write(path.toString())))) {
                w.write("hello");
            }
            try (Reader r = IO.utf8Reader(IO.buffer(IO.read(path.toString())))) {
                final String s = CharStreams.toString(r);
                Assert.assertEquals("hello", s);
            }
        } finally {
            Files.deleteIfExists(path);
        }
    }

}

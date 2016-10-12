package eu.fbk.utils.core;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.regex.Pattern;

public class EnvironmentTest {

    @Ignore
    @Test
    public void testGetProcessBuilder() {
        Environment.configureProperty("utils.environment.cmd.gzip", "a=b pigz -k");

        final ProcessBuilder gzip = Environment.getProcessBuilder("gzip");
        Assert.assertEquals("b", gzip.environment().get("a"));
        Assert.assertEquals(ImmutableList.of("pigz", "-k"), gzip.command());

        final ProcessBuilder bzip2 = Environment.getProcessBuilder("bzip2");
        Assert.assertEquals(ImmutableList.of("bzip2"), bzip2.command());
    }

    @Ignore
    @Test
    public void testTokenize() {
        final String[] tokens = Environment.tokenize("x=aa y=' b  '  'z'=test\" \"abc\"",
                Pattern.compile("[ \t]+|(=)"));
        Assert.assertArrayEquals(
                new String[] { "x", "=", "aa", "y", "=", " b  ", "z", "=", "test\"", "abc" },
                tokens);
    }

    @Test
    public void test() {
        Assert.assertEquals("value", Environment.getProperty("utils.environment.test"));
        Assert.assertEquals("value1", Environment.getProperty("utils.environment.test1"));
        Assert.assertEquals("value2", Environment.getProperty("utils.environment.test2"));
    }

}

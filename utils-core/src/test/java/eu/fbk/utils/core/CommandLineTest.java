package eu.fbk.utils.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.utils.core.CommandLine.Type;

public class CommandLineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineTest.class);

    public static void main(final String... args) {
        try {
            final CommandLine cmd = CommandLine.parser().withName("command-line-test")
                    .withOption("m", "message", "the message to display", "MSG", Type.STRING, true,
                            false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            String message = cmd.getOptionValue("m", String.class, "hello world!");
            LOGGER.info("{} (info)", message);
            LOGGER.debug("{} (debug)", message);
            LOGGER.trace("{} (trace)", message);

        } catch (final Throwable ex) {
            CommandLine.fail(ex);
        }
    }

}

package eu.fbk.utils.lsa.util;

import org.apache.commons.cli.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: giuliano
 * Date: 3/4/13
 * Time: 9:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class SVD {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>SVD</code>.
     */
    static Logger logger = Logger.getLogger(SVD.class.getName());

    public static final int DEFAULT_DIM = 100;

    public static final String DEFAULT_FILE_ROOT = System.getProperty("user.dir") + File.separator + "X";

    public static final String SVDLIBC_HOME = System.getProperty("user.dir") + File.separator + "SVDLIBC";

    Configuration config;

    public SVD(String matrixFile, String rootFile, int dim) {
        try {
            String name = "configuration/config.xml";
            logger.info("configuration file " + name);
            DefaultConfigurationBuilder defaultConfigurationBuilder = new DefaultConfigurationBuilder(name);
            defaultConfigurationBuilder.setBasePath(".");
            config = defaultConfigurationBuilder.getConfiguration();
            String svdlibcHome = config.getString("SVDLIBC_HOME");
            if (!svdlibcHome.endsWith(File.separator)) {
                svdlibcHome += File.separator;
            }
            String svdlibcCommand = svdlibcHome + "svd";
            logger.debug(svdlibcCommand);
            /*org.apache.commons.exec.CommandLine cmdLine = new org.apache.commons.exec.CommandLine(svdlibcCommand);
            cmdLine.addArgument("-d " + dim);
			cmdLine.addArgument("-o " + rootFile);
			cmdLine.addArgument(matrixFile);

			//DefaultExecutor executor = new DefaultExecutor();
			//int exitValue = executor.execute(cmdLine);
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

			ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
			Executor executor = new DefaultExecutor();
			executor.setExitValue(1);
			executor.setWatchdog(watchdog);
			executor.execute(cmdLine, resultHandler);
			logger.debug("waiting...");
			// some time later the result handler callback was invoked so we
			// can safely request the exit value
			int exitValue = 0;resultHandler.waitFor();
			logger.debug("finished");*/

            String str;
            String commandLine = svdlibcCommand + " -r sb -d " + dim + " -o " + rootFile + " " + matrixFile;
            logger.debug(commandLine);
            Process proc = Runtime.getRuntime().exec(commandLine);
            logger.debug(proc);
            // get its output (your input) stream

            DataInputStream dis = new DataInputStream(proc.getInputStream());

            try {
                while ((str = dis.readLine()) != null) {
                    logger.debug(str);
                }
            } catch (IOException e) {
                System.exit(0);
            }

        } catch (Exception e) {
            logger.error(e);
        }

    }

    public static void main(String[] args) {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);

        Options options = new Options();
        try {
            Option matrixFileOpt = OptionBuilder.withArgName("commonswiki-dump").hasArg()
                    .withDescription("file from which to read the matrix to factorize").isRequired()
                    .withLongOpt("matrix-file").create("m");
            Option rootFileOpt = OptionBuilder.withArgName("string").hasArg().withDescription(
                    "Root of files in which to store resulting U,S,V (default is " + DEFAULT_FILE_ROOT + ")")
                    .withLongOpt("root-file").create("o");
            Option dimOpt = OptionBuilder.withArgName("int").hasArg()
                    .withDescription("Desired SVD triples (default is " + DEFAULT_DIM + ")").withLongOpt("dimension")
                    .create("d");

            options.addOption("h", "help", false, "print this message");
            options.addOption("v", "version", false, "output version information and exit");

            options.addOption(matrixFileOpt);
            options.addOption(rootFileOpt);
            options.addOption(dimOpt);

            CommandLineParser parser = new PosixParser();
            CommandLine line = parser.parse(options, args);

            String rootFile = DEFAULT_FILE_ROOT;
            if (line.hasOption("root-file")) {
                rootFile = line.getOptionValue("root-file");
            }
            int dim = DEFAULT_DIM;
            if (line.hasOption("dim")) {
                dim = Integer.parseInt(line.getOptionValue("dim"));
            }
            new SVD(line.getOptionValue("matrix-file"), rootFile, dim);
        } catch (ParseException e) {
            // oops, something went wrong
            System.out.println("Parsing failed: " + e.getMessage() + "\n");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(400, "java -cp dist/jcore.jar eu.fbk.utils.lsa.util.SVD", "\n", options, "\n", true);
        }
    }
}

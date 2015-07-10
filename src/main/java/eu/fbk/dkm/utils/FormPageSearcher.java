/*
 * Copyright (2013) Fondazione Bruno Kessler (http://www.fbk.eu/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.fbk.dkm.utils;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created with IntelliJ IDEA. User: giuliano Date: 1/22/13 Time: 6:05 PM To change this template
 * use File | Settings | File Templates.
 *
 * java -cp dist/thewikimachine.jar org.fbk.cit.hlt.thewikimachine.index.FormPageSearcher -i
 * /data/models/wikipedia/en/20130604/enwiki-20130604-form-page-index/ -t
 *
 */
public class FormPageSearcher extends FreqSetSearcher {

    private static final String FORM_FIELD_NAME = "form";

    public static final String ENTRY_FIELD_NAME = "entry";

    public final static int DEFAULT_NOTIFICATION_POINT = 10000;

    /**
     * Define a static logger variable so that it references the Logger instance named
     * <code>FormPageSearcher</code>.
     */
    static Logger logger = Logger.getLogger(FormPageSearcher.class.getName());

    public FormPageSearcher(final String indexName) throws IOException {
        super(indexName, FORM_FIELD_NAME, ENTRY_FIELD_NAME);
        logger.trace(toString(10));
    }

    public static void main(final String args[]) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "configuration/log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);
        final Options options = new Options();
        try {
            OptionBuilder.withArgName("index");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("open an index with the specified name");
            OptionBuilder.isRequired();
            OptionBuilder.withLongOpt("index");
            final Option indexNameOpt = OptionBuilder.create("i");
            OptionBuilder.withArgName("interactive-mode");
            OptionBuilder.withDescription("enter in the interactive mode");
            OptionBuilder.withLongOpt("interactive-mode");
            final Option interactiveModeOpt = OptionBuilder.create("t");
            OptionBuilder.withArgName("search");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("search for the specified key");
            OptionBuilder.withLongOpt("search");
            final Option searchOpt = OptionBuilder.create("s");
            OptionBuilder.withArgName("key-freq");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("read the keys' frequencies from the specified file");
            OptionBuilder.withLongOpt("key-freq");
            final Option freqFileOpt = OptionBuilder.create("f");
            OptionBuilder.withArgName("minimum-freq");
            // Option keyFieldNameOpt =
            // OptionBuilder.withArgName("key-field-name").hasArg().withDescription("use the specified name for the field key").withLongOpt("key-field-name").create("k");
            // Option valueFieldNameOpt =
            // OptionBuilder.withArgName("value-field-name").hasArg().withDescription("use the specified name for the field value").withLongOpt("value-field-name").create("v");
            final Option minimumKeyFreqOpt = OptionBuilder
                    .hasArg()
                    .withDescription(
                            "minimum key frequency of cached values (default is "
                                    + DEFAULT_MIN_FREQ + ")").withLongOpt("minimum-freq")
                    .create("m");
            OptionBuilder.withArgName("int");
            final Option notificationPointOpt = OptionBuilder
                    .hasArg()
                    .withDescription(
                            "receive notification every n pages (default is "
                                    + DEFAULT_NOTIFICATION_POINT + ")")
                    .withLongOpt("notification-point").create("b");
            options.addOption("h", "help", false, "print this message");
            options.addOption("v", "version", false, "output version information and exit");

            options.addOption(indexNameOpt);
            options.addOption(interactiveModeOpt);
            options.addOption(searchOpt);
            options.addOption(freqFileOpt);
            // options.addOption(keyFieldNameOpt);
            // options.addOption(valueFieldNameOpt);
            options.addOption(minimumKeyFreqOpt);
            options.addOption(notificationPointOpt);

            final CommandLineParser parser = new PosixParser();
            final CommandLine line = parser.parse(options, args);

            if (line.hasOption("help") || line.hasOption("version")) {
                throw new ParseException("");
            }

            int minFreq = DEFAULT_MIN_FREQ;
            if (line.hasOption("minimum-freq")) {
                minFreq = Integer.parseInt(line.getOptionValue("minimum-freq"));
            }

            int notificationPoint = DEFAULT_NOTIFICATION_POINT;
            if (line.hasOption("notification-point")) {
                notificationPoint = Integer.parseInt(line.getOptionValue("notification-point"));
            }

            final FormPageSearcher pageFormSearcher = new FormPageSearcher(
                    line.getOptionValue("index"));
            pageFormSearcher.setNotificationPoint(notificationPoint);
            /*
             * logger.debug(line.getOptionValue("key-field-name") + "\t" +
             * line.getOptionValue("value-field-name")); if (line.hasOption("key-field-name")) {
             * pageFormSearcher.setKeyFieldName(line.getOptionValue("key-field-name")); } if
             * (line.hasOption("value-field-name")) {
             * pageFormSearcher.setValueFieldName(line.getOptionValue("value-field-name")); }
             */
            if (line.hasOption("key-freq")) {
                pageFormSearcher.loadCache(line.getOptionValue("key-freq"), minFreq);
            }
            if (line.hasOption("search")) {
                logger.debug("searching " + line.getOptionValue("search") + "...");
                final FreqSetSearcher.Entry[] result = pageFormSearcher.search(line
                        .getOptionValue("search"));
                logger.info(Arrays.toString(result));
            }
            if (line.hasOption("interactive-mode")) {
                pageFormSearcher.interactive();
            }
        } catch (final ParseException e) {
            // oops, something went wrong
            if (e.getMessage().length() > 0) {
                System.out.println("Parsing failed: " + e.getMessage() + "\n");
            }
            final HelpFormatter formatter = new HelpFormatter();
            formatter
                    .printHelp(
                            400,
                            "java -cp dist/thewikimachine.jar org.fbk.cit.hlt.thewikimachine.index.FormPageSearcher",
                            "\n", options, "\n", true);
        }
    }

}

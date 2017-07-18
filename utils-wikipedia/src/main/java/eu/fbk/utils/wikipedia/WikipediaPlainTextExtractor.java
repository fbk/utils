/*
 * Copyright (2013) Fondazione Bruno Kessler (http://www.fbk.eu/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.fbk.utils.wikipedia;

import de.tudarmstadt.ukp.wikipedia.parser.Content;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Section;
import eu.fbk.twm.utils.Defaults;
import eu.fbk.twm.utils.ExtractorParameters;
import eu.fbk.twm.utils.WikipediaExtractor;
import eu.fbk.twm.wiki.xmldump.AbstractWikipediaExtractor;
import eu.fbk.twm.wiki.xmldump.WikipediaTextExtractor;
import eu.fbk.twm.wiki.xmldump.util.ReversePageMap;
import eu.fbk.twm.wiki.xmldump.util.WikiMarkupParser;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WikipediaPlainTextExtractor extends AbstractWikipediaExtractor implements WikipediaExtractor {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>WikipediaTextExtractor</code>.
     */
    static Logger logger = Logger.getLogger(WikipediaTextExtractor.class.getName());

    private PrintWriter textWriter;

    private ReversePageMap reverseRedirectPageMap;

    public boolean isSkipTitle() {
        return skipTitle;
    }

    public void setSkipTitle(boolean skipTitle) {
        this.skipTitle = skipTitle;
    }

    private boolean skipTitle = false;

    MyWikiModel wikiModel;

    //private PageMap redirectPageMap;

    public WikipediaPlainTextExtractor(int numThreads, int numPages, Locale locale) {
        super(numThreads, numPages, locale);
    }

    @Override
    public void start(ExtractorParameters extractorParameters) {
        try {
            //redirectPageMap = new PageMap(new File(extractorParameters.getWikipediaRedirFileName()));
            //logger.info(redirectPageMap.size() + " redirect pages");

            reverseRedirectPageMap = new ReversePageMap(new File(extractorParameters.getWikipediaRedirFileName()));
            logger.info(reverseRedirectPageMap.size() + " reverse redirect pages");

            textWriter = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(extractorParameters.getWikipediaTextFileName()), "UTF-8")));
            wikiModel = new MyWikiModel("http://www.mywiki.com/wiki/${image}", "http://www.mywiki.com/wiki/${title}");
        } catch (IOException e) {
            logger.error(e);
        }
        startProcess(extractorParameters.getWikipediaXmlFileName());
    }

    @Override
    public void filePage(String text, String title, int wikiID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void contentPage(String text, String title, int wikiID) {

        try {
//            String rawText = wikiModel.render(text);
//            rawText = WikipediaText.br2nl(rawText, false, new Whitelist());
//            rawText = StringEscapeUtils.unescapeHtml4(rawText);
////        rawText = rawText.replaceAll("\\n{2,}", "\n\n");
//            rawText = rawText.replaceAll("\\s+", " ");
//            rawText = rawText.trim();
//            synchronized (this) {
//                textWriter.println(rawText);
//            }
            WikiMarkupParser wikiMarkupParser = WikiMarkupParser.getInstance();
            String[] prefixes = { imagePrefix, filePrefix };
            ParsedPage parsedPage = wikiMarkupParser.parsePage(text, prefixes);

            StringBuilder sb = new StringBuilder();

            for (Section section : parsedPage.getSections()) {
                List<Content> list = section.getContentList();
                for (int i = 0; i < list.size(); i++) {
                    String rawContent = list.get(i).getText();
                    sb.append(rawContent);
                    sb.append(" ");
                }
            }

            synchronized (this) {
                String ret = sb.toString();
                ret = ret.replaceAll("\\s+", " ");
                ret = ret.trim();
                textWriter.println(ret);
            }

        } catch (Exception e) {
            logger.error("Error processing page " + title + " (" + wikiID + ")");
        }
    }

    @Override
    public void disambiguationPage(String text, String title, int wikiID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void categoryPage(String text, String title, int wikiID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void templatePage(String text, String title, int wikiID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void redirectPage(String text, String title, int wikiID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void portalPage(String text, String title, int wikiID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void projectPage(String text, String title, int wikiID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void endProcess() {
        super.endProcess();
        textWriter.flush();
        textWriter.close();
    }

    public static void main(String args[]) throws IOException {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "configuration/log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);

        Options options = new Options();
        try {
            Option wikipediaDumpOpt = OptionBuilder.withArgName("file").hasArg().withDescription("wikipedia xml dump file").isRequired()
                    .withLongOpt("wikipedia-dump").create("d");
            Option outputDirOpt = OptionBuilder.withArgName("dir").hasArg().withDescription("output directory in which to store output files")
                    .isRequired().withLongOpt("output-dir").create("o");
            Option numThreadOpt = OptionBuilder.withArgName("int").hasArg()
                    .withDescription("number of threads (default " + Defaults.DEFAULT_THREADS_NUMBER
                            + ")").withLongOpt("num-threads").create("t");
            Option numPageOpt = OptionBuilder.withArgName("int").hasArg().withDescription("number of pages to process (default all)")
                    .withLongOpt("num-pages").create("p");
            Option notificationPointOpt = OptionBuilder
                    .withArgName("int").hasArg().withDescription("receive notification every n pages (default " + Defaults.DEFAULT_NOTIFICATION_POINT
                            + ")").withLongOpt("notification-point").create("b");

            options.addOption(null, "text-only", false, "skipt title in file");

            options.addOption("h", "help", false, "print this message");
            options.addOption("v", "version", false, "output version information and exit");

            options.addOption(wikipediaDumpOpt);
            options.addOption(outputDirOpt);
            options.addOption(numThreadOpt);
            options.addOption(numPageOpt);
            options.addOption(notificationPointOpt);
            CommandLineParser parser = new PosixParser();
            CommandLine line = parser.parse(options, args);
            logger.debug(line);

            if (line.hasOption("help") || line.hasOption("version")) {
                throw new ParseException("");
            }

            int numThreads = Defaults.DEFAULT_THREADS_NUMBER;
            boolean textOnly = line.hasOption("text-only");
            if (line.hasOption("num-threads")) {
                numThreads = Integer.parseInt(line.getOptionValue("num-threads"));
            }

            int numPages = Defaults.DEFAULT_NUM_PAGES;
            if (line.hasOption("num-pages")) {
                numPages = Integer.parseInt(line.getOptionValue("num-pages"));
            }

            int notificationPoint = Defaults.DEFAULT_NOTIFICATION_POINT;
            if (line.hasOption("notification-point")) {
                notificationPoint = Integer.parseInt(line.getOptionValue("notification-point"));
            }

            ExtractorParameters extractorParameters = new ExtractorParameters(line.getOptionValue("wikipedia-dump"),
                    line.getOptionValue("output-dir"));
            WikipediaPlainTextExtractor wikipediaPageParser = new WikipediaPlainTextExtractor(numThreads, numPages, extractorParameters.getLocale());
            wikipediaPageParser.setNotificationPoint(notificationPoint);
//            wikipediaPageParser.setSkipTitle(textOnly);
            wikipediaPageParser.start(extractorParameters);

            logger.info("extraction ended " + new Date());

        } catch (ParseException e) {
            // oops, something went wrong
            System.out.println("Parsing failed: " + e.getMessage() + "\n");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(400, "java -cp dist/thewikimachine.jar org.fbk.cit.hlt.thewikimachine.xmldump.WikipediaTextExtractor", "\n", options,
                    "\n", true);
        }
    }

}
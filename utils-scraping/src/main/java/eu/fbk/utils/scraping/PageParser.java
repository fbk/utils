package eu.fbk.utils.scraping;

import jodd.jerry.Jerry;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * User: alessio
 */
public class PageParser {

    private static final Logger logger = LoggerFactory.getLogger(PageParser.class);

    private final ArrayList<String> htmlPatterns;
    //    private final ArrayList<String> imgPatterns;
    private final Pattern pipe = Pattern.compile("\\|");

    static public String cleanHTML(String input) {
        String text = Jsoup.parse(input).text();
        text = StringEscapeUtils.unescapeHtml(text);
        return text.trim();
    }

    public static String saveFileToString(String fileName) throws IOException {
        if (fileName == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    sb.append(line).append('\n');
                }
            }
        }
        return sb.toString();
    }

    public static ArrayList<String> saveRowsFromFile(String fileName) throws IOException {
        if (fileName == null) {
            return null;
        }

        ArrayList<String> ret = new ArrayList<String>();
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    ret.add(line);
                }
            }
        }
        return ret;
    }

    public void addPattern(String pattern) {
        htmlPatterns.add(pattern);
    }

    public PageParser(ArrayList<String> htmlPatterns/*, ArrayList<String> imgPatterns*/) {
        this.htmlPatterns = htmlPatterns;
//        this.imgPatterns = imgPatterns;
    }

//    public String parseImage(String htmlArticle, String base) {
//        String article = "";
//        String image = "";
//
//        logger.trace(" -- Starting image");
//
//        if (imgPatterns == null) {
//            return image;
//        }
//
//        logger.trace(" -- Started Jerry");
//        Jerry subDoc = null;
//        try {
//            subDoc = Jerry.jerry(htmlArticle);
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        }
//        logger.trace(" -- Ended Jerry");
//
//        if (subDoc != null) {
//            int baseSize = subDoc.$("base").size();
//            if (baseSize > 0) {
//                base = subDoc.$("base").get(0).getAttribute("href");
//            }
//
//            mainLoop:
//            do {
//                int size;
//                int imgSize;
//
//                for (String thisTemplate : imgPatterns) {
//                    logger.trace(" -- Pattern row: " + thisTemplate);
//                    String[] parts = pipe.split(thisTemplate);
//                    if (parts.length == 0) {
//                        continue;
//                    }
//
//                    String templateOK = parts[0];
//                    size = subDoc.$(templateOK).size();
//                    if (size > 0) {
//
//                        logger.trace(" -- Template found (" + size + "): " + templateOK);
//                        for (int i = 0; i < size; i++) {
//                            article += subDoc.$(templateOK).get(i).getInnerHtml();
//                        }
//
//                        // Remove other tags
//                        for (int i = 1; i < parts.length; i++) {
//                            String tag = parts[i];
//                            logger.trace(" -- Removing " + tag);
//                            Jerry subSubDoc = Jerry.jerry(article);
//                            subSubDoc.$(tag).remove();
//                            article = subSubDoc.html();
//                        }
//
//                        subDoc = Jerry.jerry(article);
//                        imgSize = subDoc.$("img").size();
//                        if (imgSize > 0) {
//
//                            // Fix per Il Fatto Quotidiano
//                            if (subDoc.$("img").get(0).hasAttribute("data-lazy-src")) {
//                                image = subDoc.$("img").get(0).getAttribute("data-lazy-src");
//                            } else {
//                                image = subDoc.$("img").get(0).getAttribute("src");
//                            }
//                            break mainLoop;
//                        }
//                    } else {
//                        logger.trace(" -- Template not found: " + templateOK);
//                    }
//                }
//                break;
//
//            } while (true);
//        }
//
//        if (base != null && base.length() > 0 && image != null && image.length() > 0) {
//            try {
//                image = new URL(new URL(base), image).toString();
//            } catch (Exception ignored) {
//
//            }
//        }
//
//        logger.trace(" -- Image: " + image);
//        return image;
//    }

    // TODO: rivedere refactoring con Alessio.
    public String parseHTML(String htmlArticle) {
        logger.trace(" -- Starting article");

        if (htmlPatterns == null) {
            return "";
        }

        logger.trace(" -- Started Jerry");
        Jerry subDoc = null;
        try {
            subDoc = Jerry.jerry(htmlArticle);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        logger.trace(" -- Ended Jerry");

        final StringBuilder article = new StringBuilder();
        String articleStr = "";

        if (subDoc != null) {
            mainLoop:
            do {
                int size;
                for (String thisTemplate : htmlPatterns) {
                    article.delete(0, article.length());
                    logger.trace(" -- Pattern row: " + thisTemplate);
                    final String[] parts = pipe.split(thisTemplate);
                    if (parts.length == 0) {
                        continue;
                    }

                    String templateOK = parts[0];
                    size = subDoc.$(templateOK).size();
                    if (size > 0) {
                        logger.trace(" -- Template found (" + size + "): " + templateOK);
                        for (int i = 0; i < size; i++) {
                            article.append(subDoc.$(templateOK).get(i).getInnerHtml()).append(" ");
                        }

                        // Remove other tags
                        for (int i = 1; i < parts.length; i++) {
                            String tag = parts[i];
                            logger.trace(" -- Removing " + tag);
                            Jerry subSubDoc = Jerry.jerry(article);
                            subSubDoc.$(tag).remove();
                            article.delete(0, article.length());
                            article.append(subSubDoc.html()).append(" ");
                        }

                        articleStr = article.toString();
                        articleStr = cleanHTML(articleStr);
                        if (logger.isTraceEnabled()) {
                            logger.trace(" -- Article: " + articleStr);
                        }
                        if (articleStr.trim().length() != 0) {
                            break mainLoop;
                        }
                    } else {
                        logger.trace(" -- Template not found: " + templateOK);
                    }
                }

                break;

            } while (true);
        }

        return articleStr;
    }

    public static void main(String[] args) {
//        String logConfig = System.getProperty("log-config");
//        if (logConfig == null) {
//            logConfig = "log-config.txt";
//        }
//
//        PropertyConfigurator.configure(logConfig);
//
//        CommandLineParser parser = new PosixParser();
//        Options options = new Options();
//        options.addOption(
//                OptionBuilder.withDescription("HTML file").isRequired().hasArg().withArgName("file").create("f"));
//        options.addOption(
//                OptionBuilder.withDescription("Pattern file").isRequired().hasArg().withArgName("file").create("p"));
//        options.addOption(OptionBuilder.withDescription("Image pattern file").hasArg().withArgName("file").create("i"));
//
//        options.addOption("h", "help", false, "Print this message");
//
//        CommandLine commandLine = null;
//
//        try {
//            commandLine = parser.parse(options, args);
//            if (commandLine.hasOption("help")) {
//                throw new ParseException("");
//            }
//        } catch (ParseException exp) {
//            System.out.println();
//            if (exp.getMessage().length() > 0) {
//                System.out.println("ERR: " + exp.getMessage());
//                System.out.println();
//            }
//            HelpFormatter formatter = new HelpFormatter();
//            formatter.printHelp(
//                    400,
//                    "java -mx4g " + Thread.currentThread().getStackTrace()[1].getClassName(),
//                    "\n", options, "\n", true
//            );
//            System.out.println();
//            System.exit(0);
//        }
//
//        String htmlFile = commandLine.getOptionValue("f");
//        String patternFile = commandLine.getOptionValue("p");
//        String imageFile = null;
//        if (commandLine.hasOption("i")) {
//            imageFile = commandLine.getOptionValue("i");
//        }
//
//        try {
//            String html = saveFileToString(htmlFile);
//            ArrayList<String> patterns = saveRowsFromFile(patternFile);
//            ArrayList<String> imgPatterns = saveRowsFromFile(imageFile);
//
//            PageParser p = new PageParser(patterns/*, imgPatterns*/);
//            String article = p.parseHTML(html);
////            String image = p.parseImage(html, "");
//
//            System.out.println(article);
////            System.out.println(image);
//        } catch (IOException e) {
//            logger.error(e.getMessage());
//        }
    }
}

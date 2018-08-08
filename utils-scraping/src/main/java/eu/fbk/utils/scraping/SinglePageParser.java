package eu.fbk.utils.scraping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by alessio on 08/06/16.
 */

public class SinglePageParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SinglePageParser.class);

    public static String parseContent(String link) {
        try {
            LOGGER.trace("Opening URL");
            URLpage p = new URLpage(link);
            String content = p.getContent();
            LOGGER.trace("Content fetched");

            if (content == null) {
                return null;
            }

            return content;
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }
        return null;

    }

    public static String parse(String link, ArrayList<String> patterns) {
        try {

            String content = parseContent(link);
            //todo: fare in modo che questo oggetto venga caricato solamente una volta per ciascun rss
            PageParser parser = new PageParser(patterns);

            String article = parser.parseHTML(content);

            return article;

        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }
        return null;
    }
}

/*
 * Copyright (2011) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.analysis.stemmer;

import eu.fbk.utils.analysis.stemmer.ext.HindiStemmer;
import eu.fbk.utils.analysis.stemmer.ext.PorterStemmer;
import eu.fbk.utils.analysis.stemmer.ext.SnowballWrapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Locale;

/**
 * TO DO
 *
 * @author Claudio Giuliano, Steve Dodier-Lazaro
 * @version %I%, %G%
 * @since 1.0
 */
public class StemmerFactory {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>StemmerFactory</code>.
     */
    static Logger logger = Logger.getLogger(StemmerFactory.class.getName());

    //
    public static Stemmer getInstance(String lang) throws StemmerNotFoundException {
        return getInstance(new Locale(lang));
    } // end getInstance

    //
    public static Stemmer getInstance(Locale locale) throws StemmerNotFoundException {
        Stemmer stemmer = null;
        logger.debug("instantiating a stemmer for " + locale + "...");
        String actualLang = null;
        try {
            // We use a Locale object in order to try to guess the language name for all valid ISO codes
            //Locale locale = new Locale(lang);
            actualLang = locale.getDisplayLanguage(Locale.US).toLowerCase();
            logger.debug("actualLang " + actualLang);

            // If it fails, we back up to the lang string provided by the user
            //if(actualLang.isEmpty())
            //	actualLang = lang.toLowerCase();

            logger.debug("ISO Language Code " + locale);
            logger.debug("language string " + actualLang);
            if (actualLang.equals("english")) {
                stemmer = PorterStemmer.getInstance();
            } else if (actualLang.equals("hindi")) {
                stemmer = HindiStemmer.getInstance();
            } else if (actualLang.equals("polish")) {
                stemmer = HindiStemmer.getInstance();
            } else {
                stemmer = SnowballWrapper.getInstance(actualLang);
            }
            //TODO: ADD A CALL TO OTHER STEMMERS IF THEY ARE NOT IN SNOWBALL
        } catch (Exception e) {
            throw new StemmerNotFoundException(actualLang);
        }

        return stemmer;
    } // end getInstance

    //
    public static void main(String args[]) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }
        PropertyConfigurator.configure(logConfig);

        Stemmer stemmer = StemmerFactory.getInstance(args[0]);

        // java StemmerFactory lang term+

        for (int i = 1; i < args.length; i++) {
            String stem = stemmer.stem(args[i]);
            logger.info(args[i] + "\t" + stem);
            String stemn = stemmer.stemNgram(args[i]);
            logger.info(args[i] + "\t" + stemn);

        }
    } // end main

} // end class StemmerFactory
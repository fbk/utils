/*
 * Copyright (2011) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.analysis.stemmer.ext;

import eu.fbk.utils.analysis.stemmer.AbstractStemmer;
import eu.fbk.utils.analysis.stemmer.Stemmer;
import eu.fbk.utils.analysis.stemmer.StemmerNotFoundException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

/**
 * TO DO
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public class PolishStemmer extends AbstractStemmer implements Stemmer {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>PolishStemmer</code>.
     */
    static Logger logger = Logger.getLogger(PolishStemmer.class.getName());

    /*
    private PolishStemmer()
    {

    } // end constructor
    */
    //
    public static PolishStemmer getInstance() throws StemmerNotFoundException {
        return new PolishStemmer();
    } // end getInstance

    //
    public String stem(String s) {
        return s;
    } // end stem

    //
    public String toString() {
        return "Snowball Stemmer";
    } // end toString

    //
    public void interactive() throws Exception {
        DecimalFormat df = new DecimalFormat("000,000,000.#");
        InputStreamReader reader = null;
        BufferedReader myInput = null;
        long begin = 0, end = 0;
        while (true) {
            System.out.println("\nPlease write a query and type <return> to continue (CTRL C to exit):");

            reader = new InputStreamReader(System.in);
            myInput = new BufferedReader(reader);
            //String query = myInput.readLine().toString().replace(' ', '_');
            String query = myInput.readLine().toString();
            String stem = null;
            begin = System.nanoTime();
            stem = stem(query);
            end = System.nanoTime();
            logger.info(query + "\t" + stem + "\t" + df.format(end - begin) + " ns");
            begin = System.nanoTime();

        } // end while
    } // end interactive

    //
    public static void main(String args[]) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }
        PropertyConfigurator.configure(logConfig);

        PolishStemmer myStemmer = PolishStemmer.getInstance();
        //PorterStemmer porterStemmer = PorterStemmer.getInstance();

        if (args.length == 0) {
            //System.out.println("java PolishStemmer term+");
            myStemmer.interactive();

        }
    } // end main

} // end class PolishStemmer
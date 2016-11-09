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

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

//
public abstract class AbstractStemmer implements Stemmer {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>PorterStemmer</code>.
     */
    static Logger logger = Logger.getLogger(AbstractStemmer.class.getName());

    //
    private static Pattern spacePattern = Pattern.compile(" ");

    //
    protected LRUMap cache;

    //
    private int size;

    /**
     * Default cache size (1000).
     */
    public static final int DEFAULT_CACHE_SIZE = 1000;

    //
    protected DecimalFormat df;

    //
    public AbstractStemmer() {
        this(DEFAULT_CACHE_SIZE);

    } // end constructor

    //
    public AbstractStemmer(int size) {
        //cache = new LRUMap(size);
        df = new DecimalFormat("000,000,000.#");
    } // end constructor

    //
    public void setCacheSize(int size) {
        this.size = size;
    } // end setCacheSize

    //
    public int getCacheSize() {
        return size;
    } // end getCacheSize

    /**
     * Stems a term
     *
     * @param term the term
     * @returns the stem
     */
    public abstract String stem(String term);

    /**
     * Stems a list of terms read from the specified reader.
     *
     * @param r the reader
     */
    public void process(Reader r) throws IOException {
        long begin = 0, end = 0, time = 0;
        int count = 0;
        LineNumberReader lnr = new LineNumberReader(r);
        String line = null;
        String s = null;
        while ((line = lnr.readLine()) != null) {
            begin = System.nanoTime();
            s = stem(line);
            end = System.nanoTime();
            time += end - begin;
            count++;
        } // end while
        lnr.close();
        logger.info(count + " total " + df.format(time) + " ns");
        logger.info("avg " + df.format((double) time / count) + " ns");
    } // end process

    /**
     * Stems a tokenized ngram.
     *
     * @param s the	ngram
     * @return the stemmed ngram.
     */
    public String stemNgram(String s) {
        String[] t = spacePattern.split(s);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < t.length; i++) {
            if (i > 0) {
                sb.append("_");
            }

            sb.append(stem(t[i]));
        }

        return sb.toString();
    } // end stem

} // end class AbstractStemmer

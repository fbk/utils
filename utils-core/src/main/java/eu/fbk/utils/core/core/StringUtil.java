/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.core.core;

import eu.fbk.utils.core.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * This class provides a skeletal implementation of the <code>Set</code>
 * interface to minimize the effort required to implement this interface.
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public class StringUtil {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>StringUtil</code>.
     */
    static Logger logger = LoggerFactory.getLogger(StringUtil.class);

    //
    private final static DecimalFormat tf = new DecimalFormat("000,000,000.#");

    //
    private final static DecimalFormat df = new DecimalFormat("###,###,###,###");

    //
    private final static Pattern tabPattern = Pattern.compile("\t");

    /**
     * Splits the specified string around matches of the given
     * delimiting char.
     *
     * @param str the	string
     * @param ch  the delimiting char
     * @return the list of strings computed by splitting the string around
     * matches of the given char
     */
    public static String[] split(String str, char ch, int size) {
        String[] a = new String[size];
        //long b = System.nanoTime();
        int j = str.indexOf(ch), k = 0, i = 0;
        //logger.info("j = " + j);
        while (j != -1) {
            a[i++] = str.substring(k, j);

            //logger.info(k + "\t" + j + "\t'" + str.substring(k, j) + "'");
            str = str.substring(j + 1, str.length());
            //logger.info(k + "\t" + j + "\t'" + str + "'");
            j = str.indexOf(ch);
            ///logger.info("j = " + j);
        }

        a[i++] = str.substring(j + 1, str.length());
        //logger.info(k + "\t" + j + "\t'" + str + "'");

        return a;
    } // end split

    /**
     * Splits the specified string around matches of the given
     * delimiting char.
     *
     * @param str the	string
     * @param ch  the delimiting char
     * @return the list of strings computed by splitting the string around
     * matches of the given char
     */
    public static List<String> split(String str, char ch) {
        List<String> list = new ArrayList<String>();
        //long b = System.nanoTime();
        int j = str.indexOf(ch), k = 0;
        //logger.info("j = " + j);
        while (j != -1) {
            list.add(str.substring(k, j));

            //logger.info(k + "\t" + j + "\t'" + str.substring(k, j) + "'");
            str = str.substring(j + 1, str.length());
            //logger.info(k + "\t" + j + "\t'" + str + "'");
            j = str.indexOf(ch);
            ///logger.info("j = " + j);
        }

        str = str.substring(j + 1, str.length());
        list.add(str);
        //logger.info(k + "\t" + j + "\t'" + str + "'");

        return list;
    } // end split

    /**
     * Splits the specified string around matches of the given
     * delimiting char.
     *
     * @param str the	string
     * @param ch  the delimiting char
     * @return the list of strings computed by splitting the string around
     * matches of the given char
     */
    public static String[] splitST(String str, String delim) {
        StringTokenizer st = new StringTokenizer(str, delim);
        int size = st.countTokens();
        String[] a = new String[size];
        int i = 0;
        while (st.hasMoreTokens()) {
            a[i++] = st.nextToken();
        }

        return a;
    } // end splitST

    //
    public static void testMySplit1(File file, int size) throws Exception {
        logger.info("testMySplit1");
        long begin = 0, end = 0, time = 0;
        begin = System.nanoTime();
        String s = FileUtils.read(file, size);
        end = System.nanoTime();
        time = end - begin;

        //logger.info(file + " read in " + tf.format(time));
        begin = System.nanoTime();
        LineNumberReader lnr = new LineNumberReader(new StringReader(s));
        String line = null;
        List<String> m = null;
        int c = 0;
        while ((line = lnr.readLine()) != null) {
            m = split(line, '\t');
        } // end while
        lnr.close();
        end = System.nanoTime();
        time = end - begin;

        logger.info(file + " (" + df.format(size) + ") parsed in " + tf.format(time));

    } // end testMySplit1

    //
    public static void testFastSplit(File file, int size) throws Exception {
        logger.info("testFastSplit");
        long begin = 0, end = 0, time = 0;
        begin = System.nanoTime();
        String s = FileUtils.read(file, size);
        end = System.nanoTime();
        time = end - begin;

        //logger.info(file + " read in " + tf.format(time));
        begin = System.nanoTime();
        LineNumberReader lnr = new LineNumberReader(new StringReader(s));
        String line = null;
        String[] a = null, b = null;
        int c = 0;
        while ((line = lnr.readLine()) != null) {
            a = split(line, '\t', 6);
        } // end while
        lnr.close();
        end = System.nanoTime();
        time = end - begin;

        logger.info(file + " (" + df.format(size) + ") parsed in " + tf.format(time));

    } // end testFastSplit

    //
    public static void testSTSplit(File file, int size) throws Exception {
        logger.info("testSTSplit");
        long begin = 0, end = 0, time = 0;
        begin = System.nanoTime();
        String s = FileUtils.read(file, size);
        end = System.nanoTime();
        time = end - begin;

        //logger.info(file + " read in " + tf.format(time));
        begin = System.nanoTime();
        LineNumberReader lnr = new LineNumberReader(new StringReader(s));
        String line = null;
        String[] a = null, b = null;
        int c = 0;
        while ((line = lnr.readLine()) != null) {
            a = splitST(line, "\t");
        } // end while
        lnr.close();
        end = System.nanoTime();
        time = end - begin;

        logger.info(file + " (" + df.format(size) + ") parsed in " + tf.format(time));

    } // end testSTSplit

    //
    public static void testRegexSplit(File file, int size) throws Exception {
        logger.info("testRegexSplit");
        long begin = 0, end = 0, time = 0;
        begin = System.nanoTime();
        String s = FileUtils.read(file, size);
        end = System.nanoTime();
        time = end - begin;

        //logger.info(file + " read in " + tf.format(time));
        begin = System.nanoTime();
        LineNumberReader lnr = new LineNumberReader(new StringReader(s));
        String line = null;
        String[] a = null, b = null;
        int c = 0;
        while ((line = lnr.readLine()) != null) {
            a = tabPattern.split(line);
        } // end while
        lnr.close();
        end = System.nanoTime();
        time = end - begin;

        logger.info(file + " (" + df.format(size) + ") parsed in " + tf.format(time));

    } // end testRegexSplit

    //
    public static void testMySplitFast(File file, int size) throws Exception {
        logger.info("testMySplitFast");
        long begin = 0, end = 0, time = 0;
        begin = System.nanoTime();
        String s = FileUtils.read(file, size);
        end = System.nanoTime();
        time = end - begin;

        //logger.info(file + " read in " + tf.format(time));
        begin = System.nanoTime();
        List<String> l = split(s, '\n');
        String[] m = null;
        for (int i = 0; i < l.size(); i++) {
            m = split(l.get(i), '\t', 6);
        } // end for i
        end = System.nanoTime();
        time = end - begin;

        logger.info(file + " (" + df.format(size) + ") parsed in " + tf.format(time));

    } // end testMySplitFast

    //
    public static void testMySplit(File file, int size) throws Exception {
        logger.info("testMySplit");
        long begin = 0, end = 0, time = 0;
        begin = System.nanoTime();
        String s = FileUtils.read(file, size);
        end = System.nanoTime();
        time = end - begin;

        //logger.info(file + " read in " + tf.format(time));
        begin = System.nanoTime();
        List<String> l = split(s, '\n');
        List<String> m = null;
        for (int i = 0; i < l.size(); i++) {
            m = split(l.get(i), '\t');
        } // end for i
        end = System.nanoTime();
        time = end - begin;

        logger.info(file + " (" + df.format(size) + ") parsed in " + tf.format(time));

    } // end testMySplit

    //
    public static void main(String args[]) throws Exception {

        if (args.length != 2) {

            logger.info("java -mx1024M eu.fbk.utils.core.core.StringUtil file size");
            System.exit(-1);
        }
        File file = new File(args[0]);
        int size = Integer.parseInt(args[1]);

        StringUtil.testMySplit(file, size);

        StringUtil.testRegexSplit(file, size);

        StringUtil.testMySplit1(file, size);

        StringUtil.testSTSplit(file, size);

        StringUtil.testFastSplit(file, size);

        StringUtil.testMySplitFast(file, size);
    } // end main

} // end class StringUtil

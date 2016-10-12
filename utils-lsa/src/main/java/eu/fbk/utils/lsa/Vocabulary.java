/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.lsa;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class maps terms and their document frequency
 * within a corpus. The items are stored in alphabetical
 * order.
 * <p>
 * <code>(freq \t term)+</code>
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 * @see
 */
public class Vocabulary {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>Vocabulary</code>.
     */
    static Logger logger = Logger.getLogger(Vocabulary.class.getName());

    //
    private static Pattern tabPattern = Pattern.compile("\t");

    /**
     * to do.
     */
    private SortedMap<String, TermFrequency> map;

    /**
     * Constructs a <code>Vocabulary</code> object.
     */
    public Vocabulary() {
        map = new TreeMap<String, TermFrequency>();
    } // end constructor

    /**
     * Add a token to the index Vocabulary
     *
     * @param token the token.
     */
    public void add(String token) {
        //logger.debug("Vocabulary.add : " + token);
        TermFrequency tf = map.get(token);

        if (tf == null) {
            ////System.out.print("\"" + token +  "\"\n");
            tf = new TermFrequency();
            map.put(token, tf);
        } else {
            tf.inc();
        }

        //logger.debug("added " + token + " (" + tf + ")");
    } // end add

    /**
     * Add a token to the index Vocabulary
     *
     * @param token the token.
     */
    public int get(String token) {
        //logger.debug("Vocabulary.add : " + f);
        TermFrequency tf = map.get(token);

        if (tf == null) {
            return 0;
        }

        return tf.get();
    } // end add

    //
    public int size() {
        return map.size();
    } // end size

    /**
     * Writes the feature termIndex into the specified
     * output stream in a format suitable for loading
     * into a <code>Map</code> using the
     * {@link #load(Reader) load} method.
     *
     * @param out a <code>Writer</code> object to
     *            provide the underlying stream.
     * @throws IOException if writing this feature termIndex
     *                     to the specified  output stream
     *                     throws an <code>IOException</code>.
     */
    public void write(Writer out, int cutoff) throws IOException {
        PrintWriter pw = new PrintWriter(out);

        int count = 0, removedCount = 0;
        Iterator it = keySet().iterator();
        int[] freqCount = new int[10];
        int freqOther = 0;
        while (it.hasNext()) {
            String key = (String) it.next();
            int freq = get(key);
            if (freq > cutoff) {
                pw.println(freq + "\t" + key);
            } else {
                removedCount++;
            }

            if (freq < 10) {
                freqCount[freq]++;
            } else {
                freqOther++;
            }

            count++;
        }
        logger.info("removedCount: " + removedCount + ", count: " + count);

        for (int i = 1; i < freqCount.length; i++) {
            logger.info("f(" + i + ")=" + freqCount[i]);
        }
        logger.info("f(freq>10)=" + freqOther);

        pw.flush();
        pw.close();
    } // end write

    //
    public void stat() throws IOException {

        Iterator it = keySet().iterator();
        int[] freqCount = new int[11];
        while (it.hasNext()) {
            String key = (String) it.next();
            int freq = get(key);

            if (freq <= 10) {
                freqCount[freq]++;
            } else {
                freqCount[0]++;
            }

        }

        double c = 0;
        for (int i = 1; i < freqCount.length; i++) {
            c += (double) freqCount[i] / size();
            logger.info("F(" + i + ")=" + freqCount[i] + " (" + c + ")");
        }
        c += (double) freqCount[0] / size();
        logger.info("F(freq>10)=" + freqCount[0] + " (" + c + ")");

    } // end stat

    /**
     * Writes the feature termIndex into the specified
     * output stream in a format suitable for loading
     * into a <code>Map</code> using the
     * {@link #load(Reader) load} method.
     *
     * @param out a <code>Writer</code> object to
     *            provide the underlying stream.
     * @throws IOException if writing this feature termIndex
     *                     to the specified  output stream
     *                     throws an <code>IOException</code>.
     */
    public void write(Writer out) throws IOException {
        PrintWriter pw = new PrintWriter(out);

        Iterator it = map.entrySet().iterator();
        logger.info("writing termIndex " + map.entrySet().size());

        while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            // token index
            pw.println(me.getValue() + "\t" + me.getKey());
        }
        pw.flush();
        pw.close();
    } // end write

    /**
     * Reads the feature termIndex from the specified input stream.
     * <p>
     * This method processes input in terms of lines. A natural
     * line of input is terminated either by a set of line
     * terminator  characters (\n or \r or  \r\n) or by the end
     * of the file. A natural line  may be either a blank line,
     * a comment line, or hold some part  of a id-feature pair.
     * Lines are read from the input stream until  end of file
     * is reached.
     * <p>
     * A natural line that contains only white space characters
     * is  considered blank and is ignored. A comment line has
     * an ASCII  '#' as its first non-white  space character;
     * comment lines are also ignored and do not encode id-feature
     * information.
     * <p>
     * The id contains all of the characters in the line starting
     * with the first non-white space character and up to, but
     * not  including, the first '\t'. All remaining characters
     * on the line become part of  the associated feature string;
     * if there are no remaining  characters, the feature is the
     * empty string "".
     *
     * @param in a <code>Reader</code> object to
     *           provide the underlying stream.
     * @throws IOException if reading this feature termIndex
     *                     from the specified  input stream
     *                     throws an <code>IOException</code>.
     */
    public void read(Reader in) throws IOException {
        long begin = System.currentTimeMillis();
        logger.info("reading termIndex...");

        LineNumberReader lnr = new LineNumberReader(in);
        int i = 0;
        String line;
        String[] s;
        //Integer id;
        while ((line = lnr.readLine()) != null) {
            line = line.trim();
            if (!line.startsWith("#")) {
                //s = line.split("\t");
                s = tabPattern.split(line);

                // token index
                Object o = map.put(s[1], new TermFrequency(Integer.parseInt(s[0])));
                if (o != null) {
                    logger.warn(i + " returned " + o + ", " + s[1] + ", " + s[0]);
                }
                i++;
            }
        }
        lnr.close();

        logger.debug(i + " terms read (" + map.size() + ")");
        long end = System.currentTimeMillis();
        logger.info("took " + (end - begin) + " ms");

    } // end read

    /**
     * Returns a <code>String</code> object representing this
     * <code>Word</code>.
     *
     * @return a string representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            sb.append(me.getKey());
            sb.append("\t");
            sb.append(me.getValue());
            sb.append("\n");
        }
        return sb.toString();
    } // end toString

    //
    public void lowPassFilter(int cutoff) {

        Iterator it = keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (get(key) >= cutoff) {
                map.remove(key);
            }
        }
    } // end lowPassFilter

    //
    public void highPassFilter(int cutoff) {

        Iterator it = keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (get(key) <= cutoff) {
                map.remove(key);
            }
        }

    } // end lowPassFilter

    //
    public Set entrySet() {
        return map.entrySet();
    } // end entrySet

    //
    public Set<String> keySet() {
        return map.keySet();
    } // end keySet

    //
    public class TermFrequency {

        //
        private int tf;

        //
        public TermFrequency() {
            tf = 1;
        } // end constructor

        //
        public TermFrequency(int tf) {
            this.tf = tf;
        } // end constructor

        //
        public int get() {
            return tf;
        } // end get

        //
        public int inc() {
            return tf++;
        } // end inc

        //
        public String toString() {
            return Integer.toString(tf);
        } // end toString

    } // end class TermFrequency

} // end class Vocabulary
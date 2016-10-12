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
 * This class maps items into indexes. The items are stored in
 * alphabetical order.
 * <p>
 * <code>(index \t item)+</code>
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public class Index {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>Index</code>.
     */
    static Logger logger = Logger.getLogger(Index.class.getName());

    /**
     * to do.
     */
    private SortedMap<String, Integer> map;

    /**
     * to do.
     */
    private SortedMap<Integer, String> inverseMap;

    /**
     * to do.
     */
    private int count;

    //
    private static Pattern tabPattern = Pattern.compile("\t");

    /**
     * Constructs a <code>Index</code> object.
     */
    public Index() {
        this(0);
    } // end constructor

    /**
     * Constructs a <code>Index</code> object.
     */
    public Index(int count) {
        logger.info("Index " + count);
        map = new TreeMap<String, Integer>();
        inverseMap = new TreeMap<Integer, String>();
        this.count = count;
    } // end constructor

    //
    public int size() {
        return map.size();
    } // end size

    /**
     * Returns the <i>index</i> of the specified item and adds
     * the item to the termIndex if it is not present yet.
     *
     * @param item the item.
     * @return the <i>index</i> of the specified item.
     */
    public int add(String item) {
        //logger.debug("Index.put : " + item + "(" + count + ")");
        Integer index = map.get(item);

        if (index == null) {
            index = new Integer(count++);
            map.put(item, index);
            inverseMap.put(index, item);
        }

        return index.intValue();
    } // end get

    /**
     * Returns the <i>index</i> of the specified item and adds
     * the item to the termIndex if it is not present yet.
     *
     * @param item the item.
     * @return the <i>index</i> of the specified item.
     */
    public int get(String item) {
        //logger.debug("Index.get : " + item + "(" + count + ")");
        Integer index = map.get(item);

        if (index == null) {
            return -1;
        }

        return index.intValue();
    } // end get

    //
    public String get(int i) {
        return inverseMap.get(new Integer(i));
    }

    //
    public Set<String> itemSet() {
        return map.keySet();
    } // end termSet

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
            sb.append(me.getValue());
            sb.append("\t");
            sb.append(me.getKey());
            sb.append("\n");
        }
        return sb.toString();
    } // end toString

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
            // item index
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
        logger.info("reading vocabulary...");

        LineNumberReader lnr = new LineNumberReader(in);

        String line;
        String[] s;
        Integer id;
        while ((line = lnr.readLine()) != null) {
            line = line.trim();
            if (!line.startsWith("#")) {
                //s = line.split("\t");
                s = tabPattern.split(line);

                if (s.length > 1) {
                    // item index
                    Integer index = new Integer(s[0]);
                    Object o = map.put(s[1], index);
                    inverseMap.put(index, s[1]);
                    if (o != null) {
                        logger.warn(count + " returned " + o + ", " + s[1] + ", " + s[0]);
                    }
                    // SETTARE COUNT
                    count++;

                }
            }
        }
        lnr.close();

        logger.debug(count + " terms read (" + map.size() + ")");
        long end = System.currentTimeMillis();
        logger.info("took " + (end - begin) + " ms to read " + count + " terms");

    } // end read

} // end class Index
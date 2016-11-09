/*
 * Copyright (2011) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.core.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides utils for reading and writing to files
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public class FileUtils {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>FileUtils</code>.
     */
    static Logger logger = LoggerFactory.getLogger(FileUtils.class);

    //
    private static DecimalFormat tf = new DecimalFormat("000,000,000.#");

    //
    private static DecimalFormat df = new DecimalFormat("###,###,###,###");

    /**
     * Returns the text contained in the specified file.
     *
     * @param name the system-dependent file name.
     * @return the text conatined in the file
     */
    public static String read(String name) throws IOException {
        return read(new File(name));
    } // end read

    /**
     * Returns the text contained in the specified file.
     *
     * @param f the file from which read the text
     * @return the text conatined in the file
     */
    public static String read(File f, int size) throws IOException {
        StringBuilder sb = new StringBuilder();
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line = null;
        int count = 0;
        // run the rest of the file
        while ((line = lnr.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
            if (count > size) {
                break;
            }
            count++;
        } // end while
        lnr.close();
        return sb.toString();
    } // end read

    /**
     * Returns the text contained in the specified file.
     *
     * @param f the file from which read the text
     * @return the text conatined in the file
     */
    public static String read(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line = null;
        // run the rest of the file
        while ((line = lnr.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        } // end while
        lnr.close();
        return sb.toString();
    } // end read

    /**
     *
     */
    public static Set<String> readSet(File f) throws IOException {
        return readSet(f, false);
    } // end readSet

    /**
     *
     */
    public static Set<String> readSet(File f, boolean lowercase) throws IOException {
        logger.info("reading from " + f + "...");
        long begin = System.nanoTime();
        Set<String> set = new HashSet<String>();
        LineNumberReader lr = new LineNumberReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line = null;
        while ((line = lr.readLine()) != null) {
            //logger.info(line);
            if (lowercase) {
                set.add(line.trim().toLowerCase());
            } else {
                set.add(line.trim());
            }
        } // end while
        lr.close();

        long end = System.nanoTime();
        logger.info(set.size() + " items read  in " + tf.format(end - begin) + " ns");

        return set;
    } // end readSet

} // end class FileUtils

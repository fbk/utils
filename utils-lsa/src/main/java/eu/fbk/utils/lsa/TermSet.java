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

import eu.fbk.utils.analysis.stemmer.Stemmer;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class is contain a set that can be read and write to/from a stream.
 *
 * @author Claudio Giuliano
 * @version 1.0
 * @since 1.0
 */
public class TermSet {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>TermSet</code>.
     */
    static Logger logger = Logger.getLogger(TermSet.class.getName());
    public final static boolean DEFAULT_LOWERCASE = true;
    public final static int DEFAULT_COLUMN = 0;
    protected static Pattern tabPattern = Pattern.compile("\t");
    protected Set<String> set;
    int maxSize;
    int column;
    boolean lowercase;
    Stemmer stemmer;

    public TermSet() {
        this(Integer.MAX_VALUE);
    }

    public TermSet(int maxSize) {
        this.maxSize = maxSize;
        lowercase = DEFAULT_LOWERCASE;
        column = DEFAULT_COLUMN;
        set = new HashSet<>();
    }

    public Stemmer getStemmer() {
        return stemmer;
    }

    public void setStemmer(Stemmer stemmer) {
        this.stemmer = stemmer;
    }

    public boolean getLowercase() {
        return lowercase;
    }

    public void setLowercase(boolean lowercase) {
        this.lowercase = lowercase;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean contains(String w) {
        //return set.contains(w.toLowerCase());
        return set.contains(w);
    } // end contains

    //
    public int size() {
        return set.size();
    } // end size

    public void read(Reader in, Stemmer stemmer) throws IOException {
        this.stemmer = stemmer;
        read(in);
    }

    public void read(Reader in) throws IOException {
        logger.debug("reading term set...");

        LineNumberReader lnr = new LineNumberReader(in);
        String line;
        String[] s;
        while ((line = lnr.readLine()) != null && set.size() < maxSize) {
            line = line.trim();
            if (!line.startsWith("#") && line.length() > 0) {
                s = tabPattern.split(line);
                if (s.length > column) {
                    if (lowercase) {
                        s[column] = s[column].trim().toLowerCase();
                    }
                    if (stemmer != null) {
                        //logger.debug(line.toLowerCase().trim() + " -> " + stemmer.stem(line.toLowerCase().trim()));
                        set.add(stemmer.stem(s[column]));
                    } else {
                        set.add(s[column]);
                    }
                }
            }
        }
        lnr.close();
        logger.debug(set.size() + " terms read");
    }

    //
    public void write(Writer writer) throws IOException {
        logger.info("writing term set...");
        PrintWriter pw = new PrintWriter(writer);
        // iterates over the training set
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String s = it.next();
            pw.println(s);
            pw.flush();

        } // end while
        pw.close();

    } // end 	write

    //
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            String s = (String) it.next();
            sb.append(s);
            sb.append("\n");

        } // end while

        return sb.toString();
    } // end toString

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        long begin = System.currentTimeMillis();

        PropertyConfigurator.configure(logConfig);

        if (args.length != 4) {
            System.out.println("Usage: java -mx1024M eu.fbk.utils.lsa.TermSet file size column lowercase");
            System.exit(1);
        }

        TermSet set = new TermSet();
        set.setMaxSize(Integer.parseInt(args[1]));
        set.setColumn(Integer.parseInt(args[2]));
        set.setLowercase(Boolean.parseBoolean(args[3]));
        Reader reader = new InputStreamReader(new FileInputStream(args[0]), "UTF-8");
        set.read(reader);
        logger.info("set:\n" + set.toString());
        logger.info("size: " + set.size());
        logger.info("max size: " + set.getMaxSize());
        logger.info("column: " + set.getColumn());
        logger.info("lowercase: " + set.getLowercase());
    } // end main

} // end class TermSet
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
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class create a map between term indexes and term document frequency.
 *
 * @author Claudio Giuliano
 * @version 1.0
 * @since 1.0
 */
public class InverseVocabulary {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>InverseVocabulary</code>.
     */
    public static Logger logger = Logger.getLogger(InverseVocabulary.class.getName());

    //
    protected Map<Integer, Integer> map;

    //
    protected static Pattern tabPattern = Pattern.compile("\t");

    //
    public InverseVocabulary(File row, File df) throws IOException {
        map = new HashMap<Integer, Integer>();
        read(row, df);
    } // end constructor

    /**
     * Returns the frequency of the term with the specified index
     * in the row file.
     *
     * @param index the index of the term
     * @return the frequency of the term with the specified index
     */
    public int get(int index) {
        Integer value = map.get(index);
        if (value == null) {
            return 0;
        }
        return value;
    } // end get

    //
    public int size() {
        return map.size();
    } // end size

    //
    protected void read(File row, File df) throws IOException {
        logger.info("reading " + row + ", " + df + "...");
        //
        //LineNumberReader rowReader = new LineNumberReader(new FileReader(row));
        LineNumberReader rowReader = new LineNumberReader(new InputStreamReader(new FileInputStream(row), "UTF-8"));
        //LineNumberReader dfReader = new LineNumberReader(new FileReader(df));
        LineNumberReader dfReader = new LineNumberReader(new InputStreamReader(new FileInputStream(df), "UTF-8"));
        String rowLine = null, dfLine = null;
        String[] rowArray = null, dfArray = null;

        //Integer id;
        while (((rowLine = rowReader.readLine()) != null) && ((dfLine = dfReader.readLine()) != null)) {
            rowArray = tabPattern.split(rowLine);
            dfArray = tabPattern.split(dfLine);
            if (rowArray.length == 2 && dfArray.length == 2) {
                if (!rowArray[1].equals(dfArray[1])) {
                    logger.error(rowArray[1] + " != " + dfArray[1]);
                    logger.error("at row " + rowLine);
                    logger.error("at df " + dfLine);
                    System.exit(0);
                }
                map.put(new Integer(rowArray[0]), new Integer(dfArray[0]));
            }
        }
        rowReader.close();
        dfReader.close();

    } // end read

    //
    public void write(Writer writer) throws IOException {
        PrintWriter pw = new PrintWriter(writer);
        Iterator<Integer> it = map.keySet().iterator();
        // iterates over the inverse map
        while (it.hasNext()) {
            Integer i = it.next();
            Integer f = map.get(i);
            pw.print(i);
            pw.print("\t");
            pw.print(f);
            pw.print("\n");

        } // end while
        pw.close();
    } // end 	write

    //
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator<Integer> it = map.keySet().iterator();
        while (it.hasNext()) {
            Integer i = it.next();
            Integer f = map.get(i);
            sb.append(i);
            sb.append("\t");
            sb.append(f);
            sb.append("\n");

        } // end while

        return sb.toString();
    } // end toString

    //
    public void interactive() throws Exception {
        InputStreamReader reader = null;
        BufferedReader myInput = null;
        while (true) {
            System.out.println("\nPlease write a term index and type <return> to continue (CTRL C to exit):");

            reader = new InputStreamReader(System.in);
            myInput = new BufferedReader(reader);
            //String query = myInput.readLine().toString().replace(' ', '_');
            String query = myInput.readLine().toString();
            logger.info("page");
            int i = Integer.parseInt(query);
            int f = get(i);
            logger.info("f(" + i + ") = " + f);

        } // end while
    } // end

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        long begin = System.currentTimeMillis();

        PropertyConfigurator.configure(logConfig);

        if (args.length != 2) {
            System.out.println("Usage: java -mx1024M eu.fbk.utils.lsa.InverseVocabulary row df");
            System.exit(1);
        }

        File row = new File(args[0]);
        File df = new File(args[1]);
        InverseVocabulary inverseVocabulary = new InverseVocabulary(row, df);
        inverseVocabulary.interactive();
    } // end main

} // end class InverseVocabulary
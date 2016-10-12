/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.lsa.util;

import eu.fbk.utils.lsa.BOW;
import eu.fbk.utils.lsa.LSM;
import eu.fbk.utils.math.Vector;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.net.MalformedURLException;
import java.util.regex.Pattern;

/**
 * This class maps a CSV file in an input file for clustring: (id\svec)+
 * The user must specify the id and text column.
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public class Text2Vector {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>Text2Vector  </code>.
     */
    static Logger logger = Logger.getLogger(Text2Vector.class.getName());

    //
    private LSM lsm;

    //
    public Text2Vector(LSM lsm, File in, File out, int i, int j, String sep) throws IOException, MalformedURLException {
        long begin = System.currentTimeMillis();
        this.lsm = lsm;
        run(in, out, i, j, sep);
        long end = System.currentTimeMillis();
        System.out.println("time required " + (end - begin) + " ms");

    } // end constructor

    //
    private void run(File in, File out, int i, int j, String sep) throws IOException {
        logger.info("reading " + in + "...");
        //Pattern sepPattern = Pattern.compile(sep);
        Pattern spacePattern = Pattern.compile(" ");
        Pattern sepPattern = Pattern.compile(" ");
        int c = 1;
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(out)));
        LineNumberReader lnr = new LineNumberReader(new FileReader(in));
        String line = null;
        while ((line = lnr.readLine()) != null) {
            String[] s = sepPattern.split(line);
            //logger.debug(c + "\t" + s.length);
            if (j < s.length) {
                //logger.debug(c + "\t" + s.length + "\t" + s[i]);
                //logger.info(i + "\t" + j + "\t" + s[i] + "\t" + s[j]);
                //String[] t = spacePattern.split(line);
                BOW bow = new BOW(s);
                Vector d = lsm.mapDocument(bow);
                Vector pd = lsm.mapPseudoDocument(d);
                pw.print(s[i]);
                pw.print(" ");
                //pw.print(d.toString());
                pw.print(pd.toString());
                pw.print("\n");
                //logger.info(s[0] + "\t" + bow.size());

            }
            c++;
        } // end while
        lnr.close();
        pw.flush();
        pw.close();
    } // end read

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);

        if (args.length != 7) {
            System.out.println(
                    "Usage: java -mx2G eu.fbk.utils.lsa.util.Text2Vector lsa-root lsa-dim in-csv-file out-file id-col text-col separator");
            System.exit(1);
        }

        File Ut = new File(args[0] + "-Ut");
        File Sk = new File(args[0] + "-S");
        File r = new File(args[0] + "-row");
        File c = new File(args[0] + "-col");
        File df = new File(args[0] + "-df");
        double threshold = 0.5;//Double.parseDouble(args[1]);
        int size = 20;//Integer.parseInt(args[2]);
        int dim = Integer.parseInt(args[1]);
        boolean rescaleIdf = false;//Boolean.parseBoolean(args[4]);
        File in = new File(args[2]);
        File out = new File(args[3]);
        int i = Integer.parseInt(args[4]);
        int j = Integer.parseInt(args[5]);
        //LSM lsm = null;
        LSM lsm = new LSM(Ut, Sk, r, c, df, dim, rescaleIdf);
        //LSSimilarity lss = new LSSimilarity(lsm, size);

        new Text2Vector(lsm, in, out, i, j, args[6]);
    } // end main

} // end Text2Vector  

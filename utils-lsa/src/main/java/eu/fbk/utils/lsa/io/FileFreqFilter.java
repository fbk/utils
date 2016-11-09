/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.lsa.io;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * This class filters a keyword list according
 * the specified frequency cutoff.
 * <p>
 * This is the third step in task 2.
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public class FileFreqFilter {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>FileFreqFilter</code>.
     */
    static Logger logger = Logger.getLogger(FileFreqFilter.class.getName());

    //
    private static Pattern tabPattern = Pattern.compile("\t");

    //
    public FileFreqFilter(File in, File out, int cutoff) throws Exception {
        //PrintWriter pw = new PrintWriter(new FileWriter(out));
        PrintWriter pw = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out), "UTF-8")));

        pw.println("# Created by FileFreqFilter, " + new Date());
        pw.println("# input: " + in);
        pw.println("# output: " + out);
        pw.println("# cutoff: " + cutoff);
        pw.flush();

        int[] freqStat = new int[11];
        //LineNumberReader lnr = new LineNumberReader(new FileReader(in));
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(in), "UTF-8"));
        String line = null;
        int count = 0, all = 0;
        String[] s = null;
        int freq = 0;
        while ((line = lnr.readLine()) != null) {
            //String[] s = line.split("\t");
            s = tabPattern.split(line);

            if (s.length == 2) {
                freq = Integer.parseInt(s[0]);

                // cambiare qui
                //if (s[1].matches("k\\d") || freq >= cutoff)
                if (freq >= cutoff) {
                    //logger.info("line: \"" + line + "\"");
                    count++;

                    // to be read by WordSet
                    pw.println(s[1]);
                    //pw.println(line);

                }

                if (freq <= 10) {
                    freqStat[freq]++;
                } else {
                    freqStat[0]++;
                }
                //if (all > 1000000)
                //	break;

                if ((all % 100000) == 0) {
                    System.out.print(".");
                }

                all++;

            }
        } // end for i

        lnr.close();
        System.out.print("\n");
        logger.info(count + "/" + all);

        double c = 0;
        for (int i = 1; i < freqStat.length; i++) {
            if (freqStat[i] > 0) {
                c += (double) freqStat[i] / all;
                logger.info("F(" + i + ")=" + freqStat[i] + " (" + c + ")");
            }
        }
        if (freqStat[0] > 0) {
            c += (double) freqStat[0] / all;
            logger.info("F(freq>10)=" + freqStat[0] + " (" + c + ")");
        }

        pw.flush();
        pw.close();
    } // end constructor

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        long begin = System.currentTimeMillis();

        PropertyConfigurator.configure(logConfig);

        if (args.length != 3) {
            System.out.println(getHelp());
            System.exit(1);
        }

        File in = new File(args[0]);
        File out = new File(args[1]);
        int cutoff = Integer.parseInt(args[2]);
        new FileFreqFilter(in, out, cutoff);

		/*
		 ScoreTermMap[] map = ts.compareAll(terms);
		 
		 for (int i=0;i<terms.length;i++)
		 {
			 System.out.println("***\n");
			 System.out.println(map[i]);
		 } // end for i
		 */

        long end = System.currentTimeMillis();
        System.out.println("frequency filter applied in " + (end - begin) + " ms");
    } // end main

    /**
     * Returns a command-line help.
     * <p>
     * return a command-line help.
     */
    private static String getHelp() {
        StringBuffer sb = new StringBuffer();

        // License
        //sb.append(License.get());

        // Usage
        sb.append("Usage: java -mx1024M com.rt.task2.FileFreqFilter input output f\n\n");

        // Arguments
        sb.append("Arguments:\n");
        sb.append("\tinput\t-> file from which to read the df file \n");
		/*sb.append("\tcutoff\t-> similarity cutoff\n");
		sb.append("\tsize\t\t-> number of similar terms to return\n");
		sb.append("\tin-file\t\t-> input domain/keyword file\n");
		*/
        sb.append("\toutput\t-> output file\n");
        sb.append("\tf\t-> frequency cutoff\n");

        // Arguments
        //sb.append("Arguments:\n");

        return sb.toString();
    } // end getHelp

} // end FileFreqFilter

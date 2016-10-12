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
import eu.fbk.utils.lsa.LSSimilarity;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

//
public class NgramComparator {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>NgramComparator </code>.
     */
    static Logger logger = Logger.getLogger(NgramComparator.class.getName());

    //
    public NgramComparator(String page, LSSimilarity lss) throws IOException, MalformedURLException {
        logger.info("parsing " + page + "...");
        BOW bow = new BOW(getText(new File(page)));
        //logger.info("bow " + bow);
        logger.info("size bow " + bow.size());

        List<String[]> list = getRows(new File(page + ".np"));
        logger.info("size list " + list.size());

        PrintWriter pw = new PrintWriter(new FileWriter(page + ".allscore"));
        for (int j = 0; j < list.size(); j++) {
            logger.info("comparing line " + j + " " + list.get(j)[0]);
            /*
			
			// output max
			float f = compare(bow, list.get(j), lss);
			
			pw.print(f);
			for (int i=0;i<list.get(j).length;i++)
				pw.print("\t" + list.get(j)[i]);
			*/
            // output all

            float[] f = compareAll(bow, list.get(j), lss);

            pw.print(list.get(j)[0]);

            for (int i = 1; i < list.get(j).length; i++) {
                pw.print("\t" + list.get(j)[i]);
                pw.print("=" + f[i - 1]);

            }

            pw.print("\n");

        } // end for j
        pw.flush();
        pw.close();

        //logger.info(toText(page));

    } // end constructor

    //
    private URL[] getConceptURL(String[] s) throws MalformedURLException {
        URL[] url = new URL[s.length - 1];
        for (int j = 0; j < s.length - 1; j++) {
            url[j] = new URL("http://en.wikipedia.org/wiki/" + s[j + 1]);
        } // end for j
        return url;
    } // end getConceptURL

    //
    private float[] compareAll(BOW bow, String[] s, LSSimilarity lss) throws IOException, MalformedURLException {
        URL[] concept = getConceptURL(s);
        BOW[] bows = new BOW[concept.length];
        float[] f = new float[concept.length];
        for (int i = 0; i < concept.length; i++) {
            //logger.info("parsing concept " + concept[i]);
            bows[i] = new BOW(toText(concept[i]));
            //logger.info("concept " + bows[i]);
            //logger.info("size concept " + i + " " + bows[i].size());
            f[i] = lss.compare(bow, bows[i]);
            logger.info(i + ", " + concept[i] + ", " + f[i]);
            //logger.info(i + " = " + f[i]);
        }

        return f;
    } // end compareAll

    //
    private float compare(BOW bow, String[] s, LSSimilarity lss) throws IOException, MalformedURLException {
        URL[] concept = getConceptURL(s);
        BOW[] bows = new BOW[concept.length];
        float[] f = new float[concept.length];
        for (int i = 0; i < concept.length; i++) {
            //logger.info("parsing concept " + concept[i]);
            bows[i] = new BOW(toText(concept[i]));
            //logger.info("concept " + bows[i]);
            //logger.info("size concept " + i + " " + bows[i].size());
            f[i] = lss.compare(bow, bows[i]);
            logger.info(i + ", " + concept[i] + ", " + f[i]);
            //logger.info(i + " = " + f[i]);
        }

        int i = maxIndex(f);
        if (i != -1) {
            logger.info("max = " + concept[i] + ", " + f[i]);

            return f[i];
        }

        return 0;
    } // end compare

    //
    private int maxIndex(float[] f) {
        float max = 0;
        int maxIndex = -1;
        for (int i = 0; i < f.length; i++) {
            if (f[i] > max) {
                max = f[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    } // end maxIndex

    //
    private List<String[]> getRows(File f) throws IOException {
        List<String[]> list = new ArrayList<String[]>();
        LineNumberReader lnr = new LineNumberReader(new FileReader(f));

        String line;
        while ((line = lnr.readLine()) != null) {
            line = line.trim();
            String[] s = line.split("\t");
            list.add(s);
        } // end while
        lnr.close();
        return list;
    } // end list

    //
    private String getText(File f) throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader in = new BufferedReader(new FileReader(f));

        int ch;
        while ((ch = in.read()) != -1) {
            //System.out.println((char) ch + " " + ch);
            sb.append((char) ch);
        } // end while
        in.close();
        return sb.toString();
    } // end getText

    //
    public String toText(URL url) {
        Parser parser = null;
        StringBuilder sb = new StringBuilder();
        try {
            URLConnection con = url.openConnection();

            parser = new Parser(con);
            //NodeList list = parser.parse(null);
            NodeList list = parser.extractAllNodesThatMatch(new TagNameFilter("P"));
            // do something with your list of nodes.
            SimpleNodeIterator it = list.elements();
            while (it.hasMoreNodes()) {
                Node node = it.nextNode();
                sb.append(node.toPlainTextString());
                sb.append("\n");

            }
        } catch (EncodingChangeException ece) {
            logger.error(ece);
            //... do whatever necessary to reset your state here
            //try
            {
                // reset the parser
                parser.reset();
                // try again with the encoding now in force
                // parser.parse(...);
            }
            //catch (ParserException pe)
            //{
            //logger.error(pe);
            //}

        } catch (ParserException pe) {
            logger.error(pe);
        } catch (IOException e) {
            logger.error(e);
        }

        logger.info(url + "\t" + sb.length());
        return sb.toString();
    } // end toText

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);

        if (args.length != 6) {
            System.out.println(
                    "Usage: java -mx512M eu.fbk.utils.lsa.util.NgramComparator input threshold size dim idf file");
            System.exit(1);
        }

        File Ut = new File(args[0] + "-Ut");
        File Sk = new File(args[0] + "-S");
        File r = new File(args[0] + "-row");
        File c = new File(args[0] + "-col");
        File df = new File(args[0] + "-df");
        double threshold = Double.parseDouble(args[1]);
        int size = Integer.parseInt(args[2]);
        int dim = Integer.parseInt(args[3]);
        boolean rescaleIdf = Boolean.parseBoolean(args[4]);

        LSM lsm = new LSM(Ut, Sk, r, c, df, dim, rescaleIdf);
        LSSimilarity lss = new LSSimilarity(lsm, size);

        new NgramComparator(args[5], lss);
    } // end main
} // end NgramComparator 

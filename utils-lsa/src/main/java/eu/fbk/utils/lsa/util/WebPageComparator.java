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
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

//
public class WebPageComparator {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>WebPageComparator</code>.
     */
    static Logger logger = Logger.getLogger(WebPageComparator.class.getName());

    //
    public WebPageComparator(URL page, URL[] concept, LSSimilarity lss) throws IOException {
        logger.info("parsing " + page + "...");
        BOW bow = new BOW(toText(page));
        logger.info("size bow " + bow.size());
        BOW[] bows = new BOW[concept.length];
        for (int i = 0; i < concept.length; i++) {
            logger.info("parsing concept " + concept[i]);
            bows[i] = new BOW(toText(concept[i]));
            logger.info("size concept " + i + " " + bows[i].size());
            float f = lss.compare(bow, bows[i]);
            logger.info(i + " = " + f);
        }

        //logger.info(toText(page));
        interactive(concept, bows, lss);
    } // end constructor

    //
    public void interactive(URL[] concept, BOW[] bows, LSSimilarity lss) throws IOException {
        InputStreamReader reader = null;
        BufferedReader myInput = null;
        while (true) {
            System.out.println("\nPlease write a query and type <return> to continue (CTRL C to exit):");

            reader = new InputStreamReader(System.in);
            myInput = new BufferedReader(reader);
            String query = myInput.readLine().toString().toLowerCase();

            BOW bow = new BOW(toText(new URL(query)));
            logger.info("size bow " + bow.size());
            logger.info("bow " + bow);
            SortedMap<Float, String> map = new TreeMap<Float, String>(new Comparator<Float>() {

                public int compare(Float o1, Float o2) {
                    //Float f1 = (Float) o1;
                    //Float f2 = (Float) o2;
                    //float diff = f2 - f1;
                    if (o1.floatValue() == o2.floatValue()) {
                        return 0;
                    } else if (o1.floatValue() < o2.floatValue()) {
                        return -1;
                    }

                    return 1;
                }

            } // end FloatComparator

            );
            for (int i = 0; i < bows.length; i++) {

                if (bows[i].size() > 0) {
                    //logger.info("size concept " + i + " " + bows[i].size());
                    float f = lss.compare(bow, bows[i]);
                    //logger.info(i + ":" + concept[i] + " = " + f);
                    //System.out.println(i + "\t" + concept[i] + "\t" + f + "\t(" + bows[i].size() + ")");
                    map.put(f, concept[i].toString());

                }
            } // end for i

            PrintWriter pw = new PrintWriter(new FileWriter("output.html"));
            pw.println("<html><body><table>");
            logger.info("map size " + map.size());
            Iterator<Float> it = map.keySet().iterator();
            int j = 0;
            while (it.hasNext()) {
                Float f = it.next();
                String s = map.get(f);
                pw.println("<tr><td>" + (++j) + "</td><td>" + f + "</td><td><a href=\"" + s + "\">" + s
                        + "</a></td></tr>");
                System.out.println(j + "\t" + f + "\t" + s);
            }
            pw.println("</table></body></html>");
            pw.flush();
            pw.close();
        } // end while(true)
    }  //end interactive

    //
    public class FloatComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            Float f1 = (Float) o1;
            Float f2 = (Float) o2;
            float diff = f2 - f1;
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            }

            return 0;
        }

        public boolean equals(Object obj) {
            return true;
        }
    } // end FloatComparator

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

        logger.info(url + "\n" + sb.length());
        return sb.toString();
    } // end toText

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);

        if (args.length <= 6) {
            System.out.println(
                    "Usage: java -mx512M eu.fbk.utils.lsa.util.WebPageComparator input threshold size dim idf page concepts+");
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

        URL page = new URL(args[5]);
        URL[] concept = new URL[args.length - 6];
        for (int i = 0; i < concept.length; i++) {
            concept[i] = new URL("http://it.wikipedia.org/wiki/" + args[i + 6]);
        }

        new WebPageComparator(page, concept, lss);
    } // end main
} // end WebPageComparator

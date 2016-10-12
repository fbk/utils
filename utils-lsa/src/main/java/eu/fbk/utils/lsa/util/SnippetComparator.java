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

import eu.fbk.utils.core.io.FolderScanner;
import eu.fbk.utils.lsa.BOW;
import eu.fbk.utils.lsa.LSM;
import eu.fbk.utils.lsa.LSSimilarity;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

//
public class SnippetComparator {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>SnippetComparator  </code>.
     */
    static Logger logger = Logger.getLogger(SnippetComparator.class.getName());

    //
    private double qualityThreshold;

    //
    private Random rnd;

    //
    private List<BOW> vectorList;

    //
    private List<String> textList;

    //
    private List<File> idList;

    //
    private Map<Integer, Integer> clusterMap;

    //
    public List<SortedSet<Integer>> clusterList;

    //
    public SnippetComparator(String dir, LSSimilarity lss, File doc, double qualityThreshold)
            throws IOException, MalformedURLException {

        this.qualityThreshold = qualityThreshold;
        logger.info("*** reading " + dir + "...");
        long begin = System.currentTimeMillis();
        textList = new ArrayList<String>();
        vectorList = new ArrayList<BOW>();
        idList = new ArrayList<File>();

        BOW bow = new BOW(getText(doc));
        //logger.info("bow " + bow);
        logger.info("size bow " + bow.size());

        SortedMap<Float, String> map = new TreeMap<Float, String>();

        FolderScanner fs = new FolderScanner(new File(dir));

        int count = 0;
        while (fs.hasNext()) {
            Object[] files = fs.next();

            for (int i = 0; i < files.length; i++) {
                logger.info(files[i]);
                String texti = getText((File) files[i]);
                BOW bowi = new BOW(texti);

                idList.add((File) files[i]);
                vectorList.add(bowi);
                textList.add(texti);

                //float[] f = lss.compare2(bow, bowi);
                //map.put(f[1], f[0] + "\t" + files[i] + "\t" + texti);
                count++;
            } // end for i
        } // end while
    /*
		logger.info("map list " + map.size() + " (" + count + ")");
		
		PrintWriter pw = new PrintWriter(new FileWriter(dir + ".html"));
		pw.println("<htlm><body><table border=\"1\">");
		Iterator<Float> it = map.keySet().iterator();
		int j = 0;
		while (it.hasNext())
		{
			Float f = it.next();
			String[] t = map.get(f).split("\t");
			
			pw.print("<tr><td>");
			pw.print(map.size() - j);
			pw.print("</td><td>");
			pw.print(f);
			pw.print("</td><td>");
			pw.print(t[0]);
			pw.print("</td><td>");
			pw.print(t[2]);
			pw.print("</td><td>");
			pw.print(t[1]);
			pw.println("</td></tr>");
			j++;
		}
		pw.println("</table></body></htlm>");
		pw.flush();
		pw.close();
		
		//logger.info(toText(dir));
	*/
        calculateMatrix(lss);

        //List<SortedSet<Integer>> clusterList = getClusterList();
        final File[] label = getID();
        final String[] text = getText();

        long end = System.currentTimeMillis();
        System.out.println("term similarity calculated in " + (end - begin) + " ms");
        logger.info("writing " + dir + "1.html");
        PrintWriter pw = new PrintWriter(new FileWriter(dir + "-" + qualityThreshold + ".html"));
        /////PrintWriter xw = new PrintWriter(new FileWriter("/Users/giuliano/Public/jlsa/tmp/ouput/" + qualityThreshold + "/Claudio_Giuliano.xml"));
        PrintWriter xw = new PrintWriter(new FileWriter(
                "/Users/giuliano/Public/jlsa/cos-tmp/output/" + qualityThreshold + "/Claudio_Giuliano.xml"));

        //PrintWriter sw = new PrintWriter(new FileWriter(stat, true));

        //sw.print(clusterList.size() + "\t");
        //sw.close();

        xw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xw.println("<clustering>");

        pw.println("<html><body><table border=\"1\">");

        for (int i = 0; i < clusterList.size(); i++) {
            Set<Integer> cluster = clusterList.get(i);
            //System.out.print("{");
            StringBuilder sb = new StringBuilder();
            xw.println("\t<entity id=\"" + i + "\">");
            sb.append("\t<tr><td>" + i + "</td><td><ul>\n");
            boolean b = false;
            Iterator<Integer> it = cluster.iterator();
            while (it.hasNext()) {
                Integer j = it.next();
                ////System.out.print(" " + c[j] + ":" + words[c[j]]);
                //System.out.print(" " + j + ":" + label[j]);

                {

                    int q = label[j].getName().lastIndexOf("_");
                    int m = label[j].getName().indexOf(".");
                    String l = label[j].getName().substring(q + 1, m);
                    xw.println("\t\t<doc rank=\"" + l + "\"/>");

                    //sb.append("\t\t<td>" + label[j] + "</td>\n");
                    sb.append("\t\t<li><font color=\"red\">" + label[j] + "</font>" + filter(text[j]) + "</li>\n");
                    b = true;
                }

            } // end while

            //System.out.print("}\n");
            if (b) {
                xw.println("\t</entity>");
                //xw.print(sb.toString());
                pw.println("\t</ul></td></tr>");
            }

        } // end for i
        pw.println("</table></body></html>");
        xw.print("\t</clustering>");

        xw.flush();
        xw.close();
        pw.flush();
        pw.close();

    } // end constructor

    //
    public String filter(String s) {
        StringBuilder sb = new StringBuilder();
        char ch;
        for (int i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
            if (ch == '&') {
                sb.append("&amp;");
            } else if (ch == '<') {
                sb.append("&gt;");
            } else if (ch == '>') {
                sb.append("&lt;");
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    } // end filter

    //
    public String[] getText() {
        String[] t = new String[textList.size()];
        for (int i = 0; i < textList.size(); i++) {
            t[i] = textList.get(i);
        }
        return t;
    } // end getText

    //
    public File[] getID() {
        File[] id = new File[idList.size()];
        for (int i = 0; i < idList.size(); i++) {
            id[i] = idList.get(i);
        }
        return id;
    } // end getID

    // my QT implementation here
    private void calculateMatrix(LSSimilarity lss) {
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        double sumDot = 0, ssumDot = 0, averageDot = 0;
        int countDot = 0;

        clusterList = new ArrayList<SortedSet<Integer>>();

        //
        // ATTENZIONE
        // dovrebbe essere <int, List<int>>
        // un indice (person) puo' essere in piu' cluster
        //

        clusterMap = new HashMap<Integer, Integer>();
        logger.info("calculateMatrix: " + vectorList.size());
        for (int i = 0; i < vectorList.size(); i++) {
            BOW vi = vectorList.get(i);
            //double dotii = vi.dotProduct(vi);
            for (int j = i + 1; j < vectorList.size(); j++) {

                BOW vj = vectorList.get(j);
				/*	Vector vj = vectorList.get(j);
				double dotij = vi.dotProduct(vj);
				double dotjj = vj.dotProduct(vj);
				double myDot = dotij / Math.sqrt(dotii * dotjj);
				*/
                //double dot = kernelMatrix[i][j];

                /////double dot = lss.compare(vi, vj);
                double dot = lss.compare2(vi, vj)[0];
				/*
				 if (myDot != dot)
				 {
					 logger.warn(myDot + " != " + dot + " at " + i + "," + j);
				 }
				 */
                sumDot += dot;
                ssumDot += Math.pow(dot, 2);
                countDot++;

                if (dot >= max) {
                    max = dot;
                }

                if (dot <= min) {
                    min = dot;
                }
                //dot = myDot;
                if (dot >= qualityThreshold) {
                    Integer clusterIndex = clusterMap.get(i);
                    if (clusterIndex == null) {
                        SortedSet<Integer> cluster = new TreeSet<Integer>();
                        clusterList.add(cluster);
                        int index = clusterList.size() - 1;

                        cluster.add(i);
                        cluster.add(j);
                        clusterMap.put(i, index);
                        clusterMap.put(j, index);
                        //System.out.println("a) K(" + idList.get(i) + ", " +  idList.get(j) + ") = " + dot + "\tcluster_" + index + "=" + clusterToString(cluster) + ", (" + cluster.size() + ")");
                    } else {
                        SortedSet<Integer> cluster = clusterList.get(clusterIndex);
                        cluster.add(j);
                        clusterMap.put(j, clusterIndex);
                        //System.out.println("b) K(" + idList.get(i) + ", " +  idList.get(j) + ") = " + dot + "\tcluster_" + clusterIndex + "=" + clusterToString(cluster) + ", (" + cluster.size() + ")");

                    } // end inner if

                } // end outer if
                else {
                    //System.out.println("c) K(" + idList.get(i) + ", " +  idList.get(j) + ") = " + dot);
                }

            } // end for j
            //System.out.print(".");

            Integer clusterIndex = clusterMap.get(i);
            if (clusterIndex == null) {
                SortedSet<Integer> cluster = new TreeSet<Integer>();
                clusterList.add(cluster);
                int index = clusterList.size() - 1;
                cluster.add(i);
                clusterMap.put(i, index);
                //System.out.println("d) " + idList.get(i));

            }
            System.out.print(i + " ");
        } // end for i
        System.out.print("\n");

        double mean = sumDot / countDot;
        //double variance = ((countDot * ssumDot) - Math.pow(sumDot, 2)) / (countDot * (countDot - 1));
        double variance = (((double) 1 / countDot) * ssumDot) - Math.pow(mean, 2);

        double standardDeviation = Math.sqrt(variance);
        logger.info("min: " + min + ", max: " + max + ", mean: " + mean + ", var: " + variance + ", std dev: "
                + standardDeviation);
    } // end calculateMatrix

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
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);

        if (args.length != 5) {
            System.out.println(
                    "Usage: java -mx2G eu.fbk.utils.lsa.util.SnippetComparator input dim snippet-dir doc-id qualityThreshold");
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
        File doc = new File(args[3]);

        LSM lsm = new LSM(Ut, Sk, r, c, df, dim, rescaleIdf);
        LSSimilarity lss = new LSSimilarity(lsm, size);
        double qt = Double.parseDouble(args[4]);

        new SnippetComparator(args[2], lss, doc, qt);
    } // end main
} // end SnippetComparator  

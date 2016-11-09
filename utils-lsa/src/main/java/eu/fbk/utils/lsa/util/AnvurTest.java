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
import eu.fbk.utils.math.SparseVector;
import eu.fbk.utils.math.Vector;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.text.BreakIterator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * usato per convertire il file di sara:
 * Abusing	abuse.n	cruel and violent treatment	Abuse
 * in directory con nome del frame e con contenuto un file per
 * ogni lex unit.
 */
public class AnvurTest {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>AnvurTest</code>.
     */
    static Logger logger = Logger.getLogger(AnvurTest.class.getName());

    //
    static List<String[]> readText(File f) throws IOException {
        logger.debug("reading text: " + f + "...");
        List<String[]> list = new ArrayList<String[]>();
        //LineNumberReader lnr = new LineNumberReader(new FileReader(f));
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

        String line = null;
        int count = 1;
        while ((line = lnr.readLine()) != null) {
            String[] s = StringEscapeUtils.unescapeHtml(line).split("\t");
            list.add(s);
        } // end while

        logger.debug(list.size() + " lines read from in " + f);
        lnr.close();
        return list;
    } // end readText

    //
    static String tokenize(String in) {

        //print each word in order
        BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(in);
        StringBuilder out = new StringBuilder();
        int start = boundary.first();

        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            out.append(" ");
            out.append(in.substring(start, end));
        }
        return out.toString();
    } // end tokenize

    //
    public static String buildName(String[] str, String f) {
        String[] s = f.split(",");
        int[] index = new int[s.length];
        for (int i = 0; i < s.length; i++) {
            index[i] = Integer.parseInt(s[i]);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < index.length; i++) {
            sb.append("-");
            sb.append(str[index[i]]);
        }
        return sb.toString();
    } // end buildName

    //
    public static Vector merge(Vector v1, Vector v2) {
        //v1.normalize();
        //v2.normalize();
        //logger.debug("v1\t" + v1);
        //logger.debug("v2\t" + v2);

        Vector m = new SparseVector();
        Iterator<Integer> it1 = v1.nonZeroElements();
        if (it1 != null) {
            while (it1.hasNext()) {
                int i = it1.next();
                m.add(i, v1.get(i));
            } // end while

        }

        Iterator<Integer> it2 = v2.nonZeroElements();
        if (it2 != null) {
            while (it2.hasNext()) {
                int i = it2.next();
                m.add(i + v1.size(), v2.get(i));
            } // end while

        }

        //logger.debug("merge\t" + v1.size() + "\t" + v2.size() + "\t" + m.size());
        //logger.debug(m);
        return m;
    } // end merge

    //
    public static String buildText(String[] str, String f) {
        String[] s = f.split(",");
        int[] index = new int[s.length];
        for (int i = 0; i < s.length; i++) {
            index[i] = Integer.parseInt(s[i]);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < index.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(str[index[i]]);

        }
        return sb.toString();
    } // end buildText

    //
    public static void run(LSM enLsm, LSM itLsm, String tsv, String fields) throws Exception {
        //
        DecimalFormat dec = new DecimalFormat("#.00");

        //"author_check"0,	"authors"1,	"title"2,	"year"3,	"pubtype"4,	"publisher"5,	"journal"6,	"volume"7,	"number"8,	"pages"9,	"abstract"10,	"nauthors",	"citedby"
        String[] labels = {
                "authors", "title", "year", "pubtype", "publisher", "journal", "volume", "number", "pages", "abstract",
                "nauthors", "citedby"
        };
        String name = buildName(labels, fields);

        File bwf = new File(tsv + name + "-bow-en-it.txt");
        PrintWriter bw = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bwf), "UTF-8")));
        File bdf = new File(tsv + name + "-bow-en-it.csv");
        PrintWriter bd = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bdf), "UTF-8")));
        File lwf = new File(tsv + name + "-ls-en-it.txt");
        PrintWriter lw = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lwf), "UTF-8")));
        File ldf = new File(tsv + name + "-ls-en-it.csv");
        PrintWriter ld = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ldf), "UTF-8")));
        File blwf = new File(tsv + name + "-bow+ls-en-it.txt");
        PrintWriter blw = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(blwf), "UTF-8")));
        File bldf = new File(tsv + name + "-bow+ls-en-it.csv");
        PrintWriter bld = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bldf), "UTF-8")));
        File logf = new File(tsv + name + "-en-it.log");
        PrintWriter log = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logf), "UTF-8")));

        List<String[]> list = readText(new File(tsv));

        // author_check	authors	title	year	pubtype	publisher	journal	volume	number	pages	abstract	nauthors	citedby

        //header
        for (int i = 0; i < list.size(); i++) {
            String[] s1 = list.get(i);
            String t1 = s1[0].toLowerCase();
            bw.print("\t");
            lw.print("\t");
            blw.print("\t");
            bw.print(i + "(" + s1[0] + ")");
            lw.print(i + "(" + s1[0] + ")");
            blw.print(i + "(" + s1[0] + ")");
        } // end for i

        bw.print("\n");
        lw.print("\n");
        blw.print("\n");
        for (int i = 0; i < list.size(); i++) {
            //logger.debug(i + "\t");
            String[] s1 = list.get(i);
            String t1 = buildText(s1, fields);

            BOW bow1 = new BOW(t1);
            //logger.debug(bow1);

            Vector enD1 = enLsm.mapDocument(bow1);
            enD1.normalize();
            log.println("enD1:" + enD1);

            Vector enPd1 = enLsm.mapPseudoDocument(enD1);
            enPd1.normalize();
            log.println("enPd1:" + enPd1);

            Vector enM1 = merge(enPd1, enD1);
            log.println("enM1:" + enM1);

            Vector itD1 = itLsm.mapDocument(bow1);
            itD1.normalize();
            log.println("itD1:" + itD1);

            Vector itPd1 = itLsm.mapPseudoDocument(itD1);
            itPd1.normalize();
            log.println("itPd1:" + itPd1);

            Vector itM1 = merge(itPd1, itD1);
            log.println("itM1:" + itM1);

            // write the orginal line
            for (int j = 0; j < s1.length; j++) {
                bd.print(s1[j]);
                bd.print("\t");
                ld.print(s1[j]);
                ld.print("\t");
                bld.print(s1[j]);
                bld.print("\t");

            }
            // write the bow, ls, and bow+ls vectors
            bd.print(enD1);
            bd.print("\t");
            bd.println(itD1);
            ld.print(enPd1);
            ld.print("\t");
            ld.println(itPd1);
            bld.print(enM1);
            bld.print("\t");
            bld.println(itM1);

		/*
			bw.print(i + "(" + s1[0] + ")");
			lw.print(i + "(" + s1[0] + ")");			
			blw.print(i + "(" + s1[0] + ")");			
			for (int j=0;j<i+1;j++)
			{
				bw.print("\t");
				lw.print("\t");				
				blw.print("\t");				
			} // end for j
			
			// calculate the kernel matrix
			for (int j=i+1;j<list.size();j++)
			{
				//logger.debug(i + "\t" + j);
				String[] s2 = list.get(j);
				
				String t2 = buildText(s2, fields);
				BOW bow2 = new BOW(t2);
				
				log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") t1:" + t1);
				log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") t2:" + t2);
				log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") bow1:" + bow1);
				log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") bow2:" + bow2);
				
				Vector d2 = enLsm.mapDocument(bow2);
				d2.normalize();
				log.println("d2:" + d2);
				
				Vector pd2 = enLsm.mapPseudoDocument(d2);
				pd2.normalize();
				log.println("pd2:" + pd2);
				
				Vector m2 = merge(pd2, d2);
				log.println("m2:" + m2);
				
				float cosVSM = enD1.dotProduct(d2) / (float) Math.sqrt(enD1.dotProduct(enD1) * d2.dotProduct(d2));
				float cosLSM = enPd1.dotProduct(pd2) / (float) Math.sqrt(enPd1.dotProduct(enPd1) * pd2.dotProduct(pd2));
				float cosBOWLSM = enM1.dotProduct(m2) / (float) Math.sqrt(enM1.dotProduct(enM1) * m2.dotProduct(m2));
				bw.print("\t");
				bw.print(dec.format(cosVSM));
				lw.print("\t");				
				lw.print(dec.format(cosLSM));
				blw.print("\t");				
				blw.print(dec.format(cosBOWLSM));
				
				log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") bow\t" + cosVSM);
				log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") ls:\t" + cosLSM);
				log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") bow+ls:\t" + cosBOWLSM);
			} // end for j
			
			bw.print("\n");
			lw.print("\n");
			blw.print("\n");
			*/
        } // end for i

        logger.debug("wrote " + bwf);
        logger.debug("wrote " + bwf);
        logger.debug("wrote " + bdf);
        logger.debug("wrote " + lwf);
        logger.debug("wrote " + ldf);
        logger.debug("wrote " + blwf);
        logger.debug("wrote " + bldf);
        logger.debug("wrote " + logf);

        ld.close();
        bd.close();
        bld.close();
        bw.close();
        lw.close();
        blw.close();

        log.close();

    } // end run

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);

        if (args.length != 8) {
            System.out.println(args.length);
            System.out.println(
                    "Usage: java -mx2G eu.fbk.utils.lsa.util.AnvurTest root-lsa-en root-lsa-it threshold-lsa size-lsa dim-lsa idf-lsa in-file-tsv fields-tsv\n\n");
            System.exit(1);
        }

        //
        DecimalFormat dec = new DecimalFormat("#.00");

        File enUt = new File(args[0] + "-Ut");
        File enSk = new File(args[0] + "-S");
        File enr = new File(args[0] + "-row");
        File enc = new File(args[0] + "-col");
        File endf = new File(args[0] + "-df");
        File itUt = new File(args[1] + "-Ut");
        File itSk = new File(args[1] + "-S");
        File itr = new File(args[1] + "-row");
        File itc = new File(args[1] + "-col");
        File itdf = new File(args[1] + "-df");

        double threshold = Double.parseDouble(args[2]);
        int size = Integer.parseInt(args[3]);
        int dim = Integer.parseInt(args[4]);
        boolean rescaleIdf = Boolean.parseBoolean(args[5]);

        LSM enLsm = new LSM(enUt, enSk, enr, enc, endf, dim, rescaleIdf);
        LSM itLsm = new LSM(itUt, itSk, itr, itc, itdf, dim, rescaleIdf);

        File in = new File(args[6]);

        if (in.isFile()) {
            run(enLsm, itLsm, args[6], args[7]);
        } else if (in.isDirectory()) {
            FolderScanner fs = new FolderScanner(in);
            fs.setFiler(new TsvFilter());
            int count = 0;
            while (fs.hasNext()) {
                Object[] files = fs.next();
                System.out.println((count++) + " : " + files.length);
                for (int i = 0; i < files.length; i++) {

                    String name = ((File) files[i]).getAbsolutePath();
                    System.out.println(name);
                    run(enLsm, itLsm, name, args[7]);
                } // end for i
            } // end while
        }

    } // end main

} // end AnvurTest

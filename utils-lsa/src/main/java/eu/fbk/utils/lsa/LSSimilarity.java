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
import eu.fbk.utils.analysis.stemmer.StemmerFactory;
import eu.fbk.utils.analysis.stemmer.StemmerNotFoundException;
import eu.fbk.utils.analysis.tokenizer.HardTokenizer;
import eu.fbk.utils.analysis.tokenizer.Tokenizer;
import eu.fbk.utils.math.Vector;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * TO DO
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 * @see TextSimilarity
 */
public class LSSimilarity implements TextSimilarity {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>LSSimilarity</code>.
     */
    static Logger logger = Logger.getLogger(LSSimilarity.class.getName());

    //
    private LSM lsm;

    //
    private int size;

    protected static DecimalFormat df = new DecimalFormat("000,000,000.#");

    Stemmer stemmer;

    public LSSimilarity(LSM lsm, int size) {
        this(lsm, size, null);
    }

    public LSSimilarity(LSM lsm, int size, Stemmer stemmer) {
        this.lsm = lsm;
        this.size = size;
        this.stemmer = stemmer;
    } // end constructor

    /**
     *
     */
    public float compare(String term1, String term2) throws TermNotFoundException {
        Vector x1 = lsm.mapTerm(term1);
        Vector x2 = lsm.mapTerm(term2);
        float cos = x1.dotProduct(x2) / (float) Math.sqrt(x1.dotProduct(x1) * x2.dotProduct(x2));

        return cos;
    } // end compare

    /**
     * dot[0] is cosine
     * dot[1] is lsa
     */
    public float[] compare2(BOW bow1, BOW bow2) {
        Vector d1 = lsm.mapDocument(bow1);
        Vector d2 = lsm.mapDocument(bow2);
        Vector pd1 = lsm.mapPseudoDocument(d1);
        Vector pd2 = lsm.mapPseudoDocument(d2);

        float[] dot = new float[2];
        dot[0] = d1.dotProduct(d2) / (float) Math.sqrt(d1.dotProduct(d1) * d2.dotProduct(d2));
        //float dotLSM = pd1.dotProduct(pd2) / (float) Math.sqrt(d1.dotProduct(pd2) * d2.dotProduct(pd2));
        dot[1] = pd1.dotProduct(pd2) / (float) Math.sqrt(pd1.dotProduct(pd1) * pd2.dotProduct(pd2));

        return dot;
    } // end compare2

    /**
     *
     */
    public float compare(BOW bow1, BOW bow2) {
        Vector d1 = lsm.mapDocument(bow1);
        Vector d2 = lsm.mapDocument(bow2);
        Vector pd1 = lsm.mapPseudoDocument(d1);
        Vector pd2 = lsm.mapPseudoDocument(d2);

        //float cosVSM = d1.dotProduct(d2) / (float) Math.sqrt(d1.dotProduct(d2) * d2.dotProduct(d2));
        //float dotLSM = pd1.dotProduct(pd2) / (float) Math.sqrt(d1.dotProduct(pd2) * d2.dotProduct(pd2));
        float dotLSM = pd1.dotProduct(pd2) / (float) Math.sqrt(pd1.dotProduct(pd1) * pd2.dotProduct(pd2));

        return dotLSM;
    } // end compare

    private String[] tokenize(String s) {
        logger.debug(s);
        Tokenizer tokenizer = HardTokenizer.getInstance();
        String[] t = tokenizer.stringArray(s);
        String[] r = new String[t.length];
        logger.debug(stemmer);
        if (stemmer != null) {
            for (int i = 0; i < t.length; i++) {
                r[i] = stemmer.stem(t[i].toLowerCase());
                logger.debug(t[i] + "\t" + r[i]);
            }
        }
        return r;
    }

    public void interactive() throws IOException {
        InputStreamReader reader = null;
        BufferedReader myInput = null;
        while (true) {
            logger.info("\nPlease write a query and type <return> to continue (CTRL C to exit):");

            reader = new InputStreamReader(System.in);
            myInput = new BufferedReader(reader);
            //String query = myInput.readLine().toString().toLowerCase();
            String query = myInput.readLine().toString();

            if (query.contains("\t")) {
                // compare two terms
                String[] s = query.split("\t");
                long begin = System.nanoTime();

                BOW bow1 = new BOW(tokenize(s[0]));
                BOW bow2 = new BOW(tokenize(s[1]));

                //BOW bow1 = new BOW(s[0].toLowerCase().replaceAll("category:", "_").split("[_ ]"));
                //bow1.add(s[0]);
                //BOW bow2 = new BOW(s[1].toLowerCase().replaceAll("category:", "_").split("[_ ]"));
                //bow2.add(s[1]);

                Vector d1 = lsm.mapDocument(bow1);
                //logger.info("d1:" + d1);

                Vector d2 = lsm.mapDocument(bow2);
                //logger.info("d2:" + d2);

                Vector pd1 = lsm.mapPseudoDocument(d1);
                //logger.info("pd1:" + pd1);

                Vector pd2 = lsm.mapPseudoDocument(d2);
                //logger.info("pd2:" + pd2);

                float cosVSM = d1.dotProduct(d2) / (float) Math.sqrt(d1.dotProduct(d1) * d2.dotProduct(d2));
                float cosLSM = pd1.dotProduct(pd2) / (float) Math.sqrt(pd1.dotProduct(pd1) * pd2.dotProduct(pd2));
                long end = System.nanoTime();
                logger.info("bow1:" + bow1);
                logger.info("bow2:" + bow2);

                logger.info("time required " + df.format(end - begin) + " ns");

                logger.info("<\"" + s[0] + "\",\"" + s[1] + "\"> = " + cosLSM + " (" + cosVSM + ")");

            } else {
                //return the similar terms

                try {
                    query = query.toLowerCase();
                    logger.debug("query " + query);
                    long begin = System.nanoTime();
                    ScoreTermMap map = new ScoreTermMap(query, size);
                    Vector vec1 = lsm.mapTerm(query);

                    String term = null;
                    Iterator<String> it = lsm.terms();
                    while (it.hasNext()) {
                        term = it.next();
                        Vector vec2 = lsm.mapTerm(term);
                        float cos = vec1.dotProduct(vec2) / (float) Math
                                .sqrt(vec1.dotProduct(vec1) * vec2.dotProduct(vec2));
                        map.put(cos, term);
                    }
                    long end = System.nanoTime();
                    logger.info(map.toString());
                    logger.info("time required " + df.format(end - begin) + " ns");

                } catch (TermNotFoundException e) {
                    logger.error(e);
                }

            }

        } // end while(true)
    }  //end interactive

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        long begin = System.currentTimeMillis();

        PropertyConfigurator.configure(logConfig);

        if (args.length < 5) {
            logger.info(getHelp());
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

        Stemmer stemmer = null;
        if (args.length == 6) {
            try {
                stemmer = StemmerFactory.getInstance(args[5]);
            } catch (StemmerNotFoundException e) {
                logger.error(e);
            }
        }

        LSM lsm = new LSM(Ut, Sk, r, c, df, dim, rescaleIdf);
        LSSimilarity lss = new LSSimilarity(lsm, size, stemmer);
        lss.interactive();

        // example
        float cos = lss.compare("word", "term");

        long end = System.currentTimeMillis();
        logger.info("term similarity calculated in " + (end - begin) + " ms");
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
        sb.append(
                "Usage: java -cp dist/jcore.jar -mx2G eu.fbk.utils.lsa.LSSimilarity input threshold size dim idf [lang]\n\n");

        // Arguments
        sb.append("Arguments:\n");
        sb.append("\tinput\t\t-> root of files from which to read the model\n");
        sb.append("\tthreshold\t-> similarity threshold\n");
        sb.append("\tsize\t\t-> number of similar terms to return\n");
        sb.append("\tdim\t\t-> number of dimensions\n");
        sb.append("\tidf\t\t-> if true rescale using the idf\n");
        sb.append("\tlang\t\t-> input language\n");

        // Arguments
        //sb.append("Arguments:\n");

        return sb.toString();
    } // end getHelp

} // end class LSSimilarity
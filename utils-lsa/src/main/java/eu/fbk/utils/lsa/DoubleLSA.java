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

import eu.fbk.utils.lsa.io.DenseBinaryMatrixFileReader;
import eu.fbk.utils.lsa.io.DenseTextVectorFileReader;
import eu.fbk.utils.math.DoubleVector;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;

/**
 * Maps text into the latent semantic space.
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public class DoubleLSA {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>DoubleLSA</code>.
     */
    static Logger logger = Logger.getLogger(DoubleLSA.class.getName());

    /**
     * Matrix created by SVD
     */
    protected double[][] Uk;

    /**
     * Matrix created by SVD
     */
    protected double[] Sk;

    /**
     * Diagonal matrix such that each element
     * is the inverse document frequency of a
     * term.
     */
    protected double[] Iidf;

    //
    protected Index termIndex;

    //
    protected Index documentIndex;

    protected static DecimalFormat df = new DecimalFormat("000,000,000.#");

    //
    //protected int size;

    //
    protected int documentNumber;

    //
    private int dim;

    /**
     * Constructs a <code>DoubleLSA</code> object.
     */
    public DoubleLSA(File UtFile, File SFile, File rowFile, File colFile, File dfFile, int dim, boolean rescaleIdf)
            throws IOException {
        this(UtFile, SFile, rowFile, colFile, dfFile, dim, rescaleIdf, false);
    } // end constructor

    /**
     * Constructs a <code>DoubleLSA</code> object.
     */
    public DoubleLSA(File UtFile, File SFile, File rowFile, File colFile, File dfFile, int dim, boolean rescaleIdf,
            boolean normalize) throws IOException {
        init(UtFile, SFile, rowFile, colFile, dfFile, dim, rescaleIdf, normalize);
    } // end constructor

    /**
     * Constructs a <code>DoubleLSA</code> object.
     */
    public DoubleLSA(String root, int dim, boolean rescaleIdf) throws IOException {
        this(root, dim, rescaleIdf, false);
    } // end loadLSM

    /**
     * Constructs a <code>DoubleLSA</code> object.
     */
    public DoubleLSA(String root, int dim, boolean rescaleIdf, boolean normalize) throws IOException {
        //logger.info("reading ls model...");
        this.dim = dim;
        File Ut = new File(root + "-Ut");
        File Sk = new File(root + "-S");
        File r = new File(root + "-row");
        File c = new File(root + "-col");
        File df = new File(root + "-df");
        init(Ut, Sk, r, c, df, dim, rescaleIdf, normalize);
    } // end loadLSM

    /**
     * Constructs a <code>DoubleLSA</code> object.
     */
    private void init(File UtFile, File SFile, File rowFile, File colFile, File dfFile, int dim, boolean rescaleIdf,
            boolean normalize) throws IOException {

        this.dim = dim;

        // read term index
        logger.info("reading term index from " + rowFile + "...");
        termIndex = new Index();
        //termIndex.read(new FileReader(rowFile));
        termIndex.read(new InputStreamReader(new FileInputStream(rowFile), "UTF-8"));

        // read document index
        logger.info("reading document index from " + colFile + "...");
        documentIndex = new Index();
        //documentIndex.read(new FileReader(colFile));
        documentIndex.read(new InputStreamReader(new FileInputStream(colFile), "UTF-8"));

        // number of documents
        int l = documentNumber = documentIndex.itemSet().size();
        logger.info(l + " documents");

        // read term frequency
        logger.info("reading term frequency from " + dfFile + "...");
        Vocabulary voc = new Vocabulary();
        //voc.read(new FileReader(dfFile));
        voc.read(new InputStreamReader(new FileInputStream(dfFile), "UTF-8"));
        createIdf(voc, l);

        // read S matrix
        logger.info("reading S matrix from " + SFile + "...");
        DenseTextVectorFileReader SVectorFileReader = new DenseTextVectorFileReader(SFile, dim);
        Sk = SVectorFileReader.readDouble();
        logger.info("Sk[" + Sk.length + "]");

        // read Uk matrix
        logger.info("reading Uk matrix from " + UtFile + "...");
        DenseBinaryMatrixFileReader matrixFileReader = new DenseBinaryMatrixFileReader(UtFile, dim);
        Uk = matrixFileReader.readDouble(true);

        //print("Uk");

        // Multiply Uk for Sk
        rescale();

        print("Uk rescaled");

        //
        //normalize();

        //print("Uk rescaled & normalized");

        if (rescaleIdf) {
            idf();
            print("Uk idf");
        }

        if (normalize) {
            normalize();
            print("Uk norm");
        }

        //print("Uk idf & normalized ");

        //termByTerm(voc);
    } // end constructor

    //
    public int termCount() {
        return termIndex.size();
    } // end termCount

    //
    public int documentCount() {
        return documentNumber;
    } // end documentCount

    //
    public int getDimension() {
        return dim;
    } // end getDimension

    //
    protected void print(String msg) {
        logger.info("\n" + msg);

        if (Uk.length < 50 && Uk[0].length < 50) {

            for (int i = 0; i < Uk.length; i++) {
                for (int j = 0; j < Uk[i].length; j++) {
                    if (j != 0) {
                        System.out.print(" ");
                    }

                    System.out.print(Uk[i][j]);
                }
                System.out.print("\n");
            }
            return;
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (j != 0) {
                    System.out.print("\t");
                }

                System.out.print(Uk[i][j]);
            }

            System.out.print("\t...\t");
            for (int j = (Uk[i].length - 3); j < Uk[i].length; j++) {
                if (j != 0) {
                    System.out.print("\t");
                }

                System.out.print(Uk[i][j]);
            }
            System.out.print("\n");
        }

        System.out.print("...\n");
        for (int i = (Uk.length - 3); i < Uk.length; i++) {
            for (int j = 0; j < 3; j++) {
                if (j != 0) {
                    System.out.print("\t");
                }
                System.out.print(Uk[i][j]);
            }

            System.out.print("\t...\t");
            for (int j = (Uk[i].length - 3); j < Uk[i].length; j++) {
                if (j != 0) {
                    System.out.print("\t");
                }

                System.out.print(Uk[i][j]);
            }
            System.out.print("\n");
        }
    } // end print

    //
    public static final double LOG2 = Math.log(2);

    //
    public double log2(double d) {
        return Math.log(d) / LOG2;
    } //

    /**
     *
     */
    public double[] getVector(String term) {
        int i = termIndex.get(term);
        logger.debug(term + " " + i);

        if (i == -1) {
            return null;
        }

        //return Uk.viewRow(i);
        return Uk[i];
    } // end getVector

    //
    private void createIdf(Vocabulary voc, int l) {
        long begin = System.currentTimeMillis();
        logger.info("creating idf matrix...");

        Iidf = new double[voc.entrySet().size()];

        //logger.debug("Iidf.size: " + Iidf.length);
        // iterates over the types
        Iterator it = voc.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            String term = (String) me.getKey();

            Vocabulary.TermFrequency tf = (Vocabulary.TermFrequency) me.getValue();
            int index = termIndex.get(term);

            Iidf[index] = log2((double) l / tf.get());

            //logger.info(index + ": " + l + "/"+ tf.get() + " = " + Iidf[index]);

        } // end while

        //for (int i=0;i<Iidf.length;i++)
        //	logger.info(i + " " + Iidf[i]);

        long end = System.currentTimeMillis();
        logger.info("took " + (end - begin) + " ms");

    } // end createIdf

    /**
     * Multiply Uk for Sk.
     */
    private void rescale() {
        long begin = System.currentTimeMillis();
        logger.info("rescale: Uk[" + Uk.length + " X " + Uk[0].length + "] * Sk[" + Uk[0].length + " X " + Uk[0].length
                + "]");

        // rows (error)
        for (int i = 0; i < Uk.length; i++) {
            // col
            for (int j = 0; j < Uk[i].length; j++) {
                Uk[i][j] *= Sk[j];
            } // end for j
        } // end for i

        long end = System.currentTimeMillis();
        logger.info("took " + (end - begin) + " ms");

    } // end rescale

    /**
     *
     */
    private void normalize() {
        long begin = System.currentTimeMillis();
        logger.info("normalize: IN[" + Iidf.length + " X " + Iidf.length + "] * Uk[" + Uk.length + " X " + Uk[0].length
                + "]");

        // Uk.rows = N
        for (int i = 0; i < Uk.length; i++) {
            double sum = 0;
            for (int j = 0; j < Uk[i].length; j++) {
                sum += Math.pow(Uk[i][j], 2);
            }

            //  normalization
            sum = Math.sqrt(sum);
            for (int j = 0; j < Uk[i].length; j++) {
                Uk[i][j] /= sum;
            }
        } // end for i

        long end = System.currentTimeMillis();
        logger.info("took " + (end - begin) + " ms");

    } // end normalize

    //
    public Iterator<String> terms() {
        return termIndex.itemSet().iterator();
    } // end terms

    //
    public Iterator<String> documents() {
        return documentIndex.itemSet().iterator();
    } // end terms

    /**
     * Returns the idf of the specified term if present in the index; -1 otherwise.
     */
    public double getIdf(String term) {
        int index = termIndex.get(term);
        if (index == -1) {
            return 0;
        }

        return Iidf[index];
    } // end getIdf

    /**
     *
     */
    private void idf() {
        long begin = System.currentTimeMillis();
        logger.info(
                "idf: Iidf[" + Iidf.length + " X " + Iidf.length + "] * Uk[" + Uk.length + " X " + Uk[0].length + "]");

        // Uk.rows = N
        for (int i = 0; i < Uk.length; i++) {

            // I^idf X (Uk * Sk)
            for (int j = 0; j < Uk[i].length; j++) {
                //logger.info("before " + Uk[i][j]);
                Uk[i][j] *= Iidf[i];
                //logger.info("after " + Uk[i][j]);

            }
        } // end for i

        long end = System.currentTimeMillis();
        logger.info("took " + (end - begin) + " ms");

    } // end idf

    /**
     * Returns the index of this term in the VSM.
     */
    public int termIndex(String term) throws TermNotFoundException {
        return termIndex.get(term);

    } // end termIndex

    /**
     * Returns a term in the VSM
     */
    public DoubleVector mapTerm(String term) throws TermNotFoundException {
        int i = termIndex.get(term);

        if (i == -1) {
            throw new TermNotFoundException(term);
        }

        DoubleVector nodes = new DoubleVector(Uk[i]);
        return nodes;
    } // end mapTerm

    /**
     * Returns a document in the VSM.
     */
    public DoubleVector mapDocument(BOW bow, boolean b) {
        //logger.info("mapDocument " + b);
        int[] indexes = new int[bow.size()];
        double[] values = new double[bow.size()];
        String term;
        int index;
        int tf;
        double tfIdf;
        int current = 0;
        Iterator<String> it = bow.termSet().iterator();
        for (int i = 0; it.hasNext(); i++) {
            term = it.next();
            index = termIndex.get(term);

            if (index != -1) {
                tf = bow.getFrequency(term);
                tfIdf = 1.0 + Math.log10(tf);
                if (b) {
                    tfIdf *= Iidf[index];
                }

                //logger.info(term + " ==> " + index + ", tf.idf = " + tf + "(" + (1.0 + Math.log10(tf)) + ") * " + Iidf[index] + " = " + tfIdf);
                indexes[i] = index;
                values[i] = tfIdf;
                current++;
            } else {
                indexes[i] = index;
                values[i] = 0;
            }

        } // end for
        /*if (current < indexes.length)
		{
			int[] shorterIndexes = new int[current];
			double[]  shorterValues = new double[current];
			System.arraycopy(
			
		}*/
        return new DoubleVector(indexes, values);

    } // end map

    /**
     * Returns a document in the VSM.
     */
    public DoubleVector mapDocument(BOW bow) {
        return mapDocument(bow, true);
    } // end map

    /**
     * Returns a document in the latent semantic space.
     */
    public DoubleVector mapPseudoDocument(DoubleVector doc) {
        //logger.info("mapPseudoDocument " + doc);
        // N = Uk.rows();
        //float[] pdoc = new float[Uk[0].length];
        DoubleVector nodes = new DoubleVector(Uk[0].length);
        //logger.info("Uk.size " + Uk.length + " X " + Uk[0].length);
        //logger.info("doc.size " + doc.size());
        //logger.info("pdoc.size " + pdoc.length);
        int index;
        for (int i = 0; i < Uk[0].length; i++) {
            //Iterator<Integer> it = doc.nonZeroElements();

            for (int j = 0; j < doc.length(); j++) {
                //Integer index = it.next().intValue();
                index = doc.indexes[j];
                //logger.info(i + ", i: " + index);
                //logger.info(i + ", v:" + doc.get(index));
                //logger.info(i + ", Uk: " + Uk[index][i]);
                //pdoc[i] +=  Uk[index][i] * doc.get(index);
                nodes.values[i] += Uk[index][i] * doc.values[j];
            } // end for j
        } // end for i

        //logger.info("pdoc.size " + pdoc.length);
        return nodes;
    } // end mapPseudoDocument

    public double compare(String term1, String term2) throws TermNotFoundException {
        DoubleVector x1 = mapTerm(term1);
        DoubleVector x2 = mapTerm(term2);
        return x1.dot(x2) / Math.sqrt(x1.dot(x1) * x2.dot(x2));
    }

    public double compare(BOW bow1, BOW bow2) {
        DoubleVector d1 = mapDocument(bow1);
        DoubleVector d2 = mapDocument(bow2);
        DoubleVector pd1 = mapPseudoDocument(d1);
        DoubleVector pd2 = mapPseudoDocument(d2);
        return pd1.dot(pd2) / Math.sqrt(pd1.dot(pd1) * pd2.dot(pd2));
    }

    public void interactive() throws IOException {
        InputStreamReader reader = null;
        BufferedReader myInput = null;
        while (true) {
            logger.info("\nPlease write a query and type <return> to continue (CTRL C to exit):");
            reader = new InputStreamReader(System.in);
            myInput = new BufferedReader(reader);
            String query = myInput.readLine().toString();

            if (query.contains("\t")) {
                // compare two terms
                String[] s = query.split("\t");
                long begin = System.nanoTime();

                BOW bow1 = new BOW(s[0].toLowerCase().replaceAll("category:", "_").split("[_ ]"));
                BOW bow2 = new BOW(s[1].toLowerCase().replaceAll("category:", "_").split("[_ ]"));

                DoubleVector d1 = mapDocument(bow1);
                //logger.info("d1:" + d1);

                DoubleVector d2 = mapDocument(bow2);
                //logger.info("d2:" + d2);

                DoubleVector pd1 = mapPseudoDocument(d1);
                //logger.info("pd1:" + pd1);

                DoubleVector pd2 = mapPseudoDocument(d2);
                //logger.info("pd2:" + pd2);

                double cosVSM = pd1.dot(d2) / Math.sqrt(d1.dot(d1) * d2.dot(d2));
                double cosLSM = pd1.dot(pd2) / Math.sqrt(pd1.dot(pd1) * pd2.dot(pd2));
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
                    ScoreTermMap map = new ScoreTermMap(query, 20);
                    DoubleVector vec1 = mapTerm(query);

                    String term = null;
                    Iterator<String> it = terms();
                    while (it.hasNext()) {
                        term = it.next();
                        DoubleVector vec2 = mapTerm(term);
                        double cos = vec1.dot(vec2) / Math.sqrt(vec1.dot(vec1) * vec2.dot(vec2));
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
    }

    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        long begin = System.currentTimeMillis();

        PropertyConfigurator.configure(logConfig);

        if (args.length != 5) {
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

        DoubleLSA lsi = new DoubleLSA(Ut, Sk, r, c, df, dim, rescaleIdf);

        lsi.interactive();

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
        sb.append("Usage: java -cp dist/jcore.jar -mx2G eu.fbk.utils.lsa.DoubleLSA input threshold size dim idf\n\n");

        // Arguments
        sb.append("Arguments:\n");
        sb.append("\tinput\t\t-> root of files from which to read the model\n");
        sb.append("\tthreshold\t-> similarity threshold\n");
        sb.append("\tsize\t\t-> number of similar terms to return\n");
        sb.append("\tdim\t\t-> number of dimensions\n");
        sb.append("\tidf\t\t-> if true rescale using the idf\n");
        //sb.append("\tterm\t\t-> input term\n");

        // Arguments
        //sb.append("Arguments:\n");

        return sb.toString();
    } // end getHelp

} // end class DoubleLSA

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
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;

/**
 * Maps text into the latent semantic space.
 * <p>
 * This class is equals to LSA but uses Node instead of Vector.
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.1
 */
public abstract class AbstractLSI {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>AbstractLSI</code>.
     */
    static Logger logger = Logger.getLogger(AbstractLSI.class.getName());

    /**
     * Matrix created by SVD
     */
    protected float[][] Uk;

    /**
     * Matrix created by SVD
     */
    protected float[] Sk;

    /**
     * Diagonal matrix such that each element
     * is the inverse document frequency of a
     * term.
     */
    protected float[] Iidf;

    //
    protected Index termIndex;

    //
    protected Index documentIndex;

    protected static DecimalFormat df = new DecimalFormat("000,000,000.#");

    //
    protected int documentNumber;

    //
    protected int dim;

    /**
     * Constructs a <code>AbstractLSI</code> object.
     */
    public AbstractLSI(File UtFile, File SFile, File rowFile, File colFile, File dfFile, int dim, boolean rescaleIdf)
            throws IOException {
        this(UtFile, SFile, rowFile, colFile, dfFile, dim, rescaleIdf, false);
    } // end constructor

    /**
     * Constructs a <code>AbstractLSI</code> object.
     */
    public AbstractLSI(File UtFile, File SFile, File rowFile, File colFile, File dfFile, int dim, boolean rescaleIdf,
            boolean normalize) throws IOException {
        init(UtFile, SFile, rowFile, colFile, dfFile, dim, rescaleIdf, normalize);
    } // end constructor

    /**
     * Constructs a <code>AbstractLSI</code> object.
     */
    public AbstractLSI(String root, int dim, boolean rescaleIdf) throws IOException {
        this(root, dim, rescaleIdf, false);
    } // end loadLSM

    /**
     * Constructs a <code>AbstractLSI</code> object.
     */
    public AbstractLSI(String root, int dim, boolean rescaleIdf, boolean normalize) throws IOException {
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
     * Constructs a <code>AbstractLSI</code> object.
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
        Sk = SVectorFileReader.read();
        logger.info("Sk[" + Sk.length + "]");

        // read Uk matrix
        logger.info("reading Uk matrix from " + UtFile + "...");
        DenseBinaryMatrixFileReader matrixFileReader = new DenseBinaryMatrixFileReader(UtFile, dim);
        Uk = matrixFileReader.read(true);

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
    public float[] getVector(String term) {
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
        logger.info("creating idf vector...");

        Iidf = new float[voc.entrySet().size()];

        //logger.debug("Iidf.size: " + Iidf.length);
        // iterates over the types
        Iterator it = voc.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            String term = (String) me.getKey();

            Vocabulary.TermFrequency tf = (Vocabulary.TermFrequency) me.getValue();
            int index = termIndex.get(term);

            Iidf[index] = (float) log2((double) l / tf.get());

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
                Uk[i][j] /= (float) sum;
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
    public float getIdf(String term) {
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

} // end class AbstractLSI

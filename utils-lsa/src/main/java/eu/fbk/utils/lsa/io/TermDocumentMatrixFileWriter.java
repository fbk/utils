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

import eu.fbk.utils.analysis.stemmer.Stemmer;
import eu.fbk.utils.lsa.Index;
import eu.fbk.utils.lsa.TermSet;
import eu.fbk.utils.lsa.Vocabulary;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * This class creates the term-by-document matrix, row index,
 * col index and document frequency to be used by
 * <a href="http://tedlab.mit.edu/~dr/SVDLIBC/"> SVDLIBC</a>
 * to create a LSA model.
 * <p/>
 * The input is a file where each line is tokenized document
 * (tokens are separated by spaces), the first token is the document name.
 * <p/>
 * The output is stored in the specified directory matrix in
 * <a href="http://tedlab.mit.edu/~dr/SVDLIBC/SVD_F_SB.html">
 * sparse binary format</a>
 * The file created follow the following name convetion:
 * <p/>
 * <pre>
 * X-matrix:				term-by-document matrix
 * X-matrix-tf-idf:	term-by-document matrix rescaled with tf-idf
 * X-col:						col index
 * X-row:						row index
 * X-df:						document frequency
 * </pre>
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.1
 */
public class TermDocumentMatrixFileWriter {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>TermDocumentMatrixFileWriter</code>.
     */
    static Logger logger = Logger.getLogger(TermDocumentMatrixFileWriter.class.getName());

    /**
     * The term index
     */
    protected Index termIndex;

    //
    protected static Pattern spacePattern = Pattern.compile(" ");

    /**
     * The document index
     */
    protected Index documentIndex;

    /**
     * The matrix writer.
     */
    protected MatrixFileWriter matrixWriter;

    //
    protected int columnCount;

    //
    protected Vocabulary corpusVocabulary;

    //
    protected TermSet stopwordSet, keywordSet;

    //
    protected int totalKW;

    //
    protected int[] lengthFreq;

    //
    protected boolean indexAllTokens, saveMatrix;

    Stemmer stemmer;

    public TermDocumentMatrixFileWriter(File root, String matrixName, File stopwordFile, File keywordFile, int n,
            boolean saveMatrix) {
        this(root, matrixName, stopwordFile, keywordFile, n, saveMatrix, null);
    }

    /**
     * Constructs a reader.
     */
    public TermDocumentMatrixFileWriter(File root, String matrixName, File stopwordFile, File keywordFile, int n,
            boolean saveMatrix, Stemmer stemmer) {
        this.indexAllTokens = indexAllTokens;
        this.stemmer = stemmer;

        this.saveMatrix = saveMatrix;
        try {
            totalKW = 0;
            keywordSet = new TermSet();
            //keywordSet.read(new FileReader(keywordFile));
            keywordSet.read(new BufferedReader(new InputStreamReader(new FileInputStream(keywordFile), "UTF-8")));

            //logger.info("keyword to be indexed: " + keywordSet.size());
            logger.info(keywordSet.size() + " keywords read from " + keywordFile);

            stopwordSet = new TermSet();
            //stopwordSet.read(new FileReader(stopwordFile));

            stopwordSet.read(new BufferedReader(new InputStreamReader(new FileInputStream(stopwordFile), "UTF-8")),
                    stemmer);

            //logger.info(stopwordFile + " (" + stopwordSet.size() + ")");
            logger.info(stopwordSet.size() + " stopwords read from " + stopwordFile);

            lengthFreq = new int[101];
            columnCount = 0;

            File matrixFile = new File(matrixName + "-matrix");
            File rowFile = new File(matrixName + "-row");
            File colFile = new File(matrixName + "-col");
            File dfFile = new File(matrixName + "-df");

            termIndex = new Index();
            documentIndex = new Index();
            if (saveMatrix) {
                matrixWriter = new SparseBinaryMatrixFileWriter(matrixFile);
            }

            corpusVocabulary = new Vocabulary();

            long begin = System.currentTimeMillis();
            //logger.debug((File) files[i]);

            //LineNumberReader lnr = new LineNumberReader(new FileReader(root));
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(root), "UTF-8"));
            //LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(root), "UTF-8"));
            String line = null;
            int lineCount = 1;
            Date start = new Date();
            Date start1 = new Date();
            while ((line = lnr.readLine()) != null) {
                if (lineCount > n) {
                    break;
                }

                addDocument(spacePattern.split(line));

                if ((lineCount % 100000) == 0) {
                    //logger.info("*");
                    Date end = new Date();
                    logger.info(
                            lineCount + "\t" + ((double) (end.getTime() - start1.getTime()) / 1000) + " total s (" + end
                                    + "), voc size:" + corpusVocabulary.size() + ", term index size:" + termIndex.size()
                                    + ", totalKW: " + totalKW);
                    //logger.info("corpusVocabulary size: " + corpusVocabulary.size());
                    start1 = new Date();
                    start = new Date();
                } else if ((lineCount % 10000) == 0) {
                    //logger.info("*");
                    Date end = new Date();
                    logger.info(lineCount + "\t" + ((double) (end.getTime() - start.getTime()) / 1000)
                            + " total s,  voc size:" + corpusVocabulary.size());
                    start = new Date();
                } else if ((lineCount % 500) == 0) {
                    System.out.print(".");
                }

                if ((lineCount % 1000000) == 0) {
                    logger.info("keyword stat");
                    //corpusVocabulary.stat();
                    logger.info("doc stat");
                    //					lengthFreq
                    double c = 0;
                    for (int i = 1; i < lengthFreq.length; i++) {

                        if (lengthFreq[i] > 0) {
                            c += (double) lengthFreq[i] / lineCount;
                            logger.info("F(" + i + ")=" + lengthFreq[i] + " (" + c + ")");
                        }

                    }

                    if (lengthFreq[0] > 0) {
                        c += (double) lengthFreq[0] / lineCount;
                        logger.info("F(freq>100)=" + lengthFreq[0] + " (" + c + ")");
                    }

                }
                lineCount++;
            } // end while
            long end = System.currentTimeMillis();
            System.out.print("\n");
            logger.info(root + " processed in " + (end - begin) + " ms");

            if (saveMatrix) {
                //termIndex.write(new FileWriter(rowFile));
                termIndex.write(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rowFile), "UTF-8")));

                //documentIndex.write(new FileWriter(colFile));
                documentIndex.write(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(colFile), "UTF-8")));

                //
                matrixWriter.close();
            }

            //
            //corpusVocabulary.write(new FileWriter(dfFile));
            corpusVocabulary.write(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dfFile), "UTF-8")));

            logger.info("columnCount: " + columnCount);
        } catch (Exception e) {
            logger.error(e);
        }
    } // end constructor

    //
    private void addDocument(String[] array) throws IOException {
        //logger.debug("readSentence");
        Vocabulary documentVocabulary = new Vocabulary();
        //logger.info((array.length - 1) + " tokens");
        totalKW += (array.length - 1);
        String token = null;
        for (int i = 1; i < array.length; i++) {
            //todo: add stemming here
            if (stemmer != null) {
                token = stemmer.stem(array[i].toLowerCase());
            } else {
                token = array[i].toLowerCase();
            }

            //logger.info(i + "\t" + token);
            if (isWord(token)) {
                if (keywordSet.size() == 0) {
                    if (stopwordSet.size() == 0) {
                        //logger.debug("1 adding " + token);
                        documentVocabulary.add(token);
                    } else if (!stopwordSet.contains(token)) {
                        //logger.debug("2 adding " + token);
                        documentVocabulary.add(token);
                    }
                } else if (keywordSet.contains(token)) {
                    //logger.debug("3 adding " + token);
                    documentVocabulary.add(token);
                } // end inner if

            } // end if isWord
        } // end for i

        if (array.length <= 100) {
            lengthFreq[array.length - 1]++;
        } else {
            lengthFreq[0]++;
        }

        if (documentVocabulary.size() == 0) {
            return;
        }

        if (saveMatrix) {
            int documentID = documentIndex.add(array[0]);
        }

        int size = documentVocabulary.entrySet().size();

        int[] indexes = new int[size];
        float[] values = new float[size];
        int j = 0;
        String term = null;
        int freq = 0;
        //int ti = 0;
        // iterates over the types
        //logger.debug("iterates over the types");
        //Iterator it = documentVocabulary.entrySet().iterator();
        Iterator<String> it = documentVocabulary.keySet().iterator();
        while (it.hasNext()) {
            //Map.Entry me = (Map.Entry) it.next();
            //String term = (String) me.getKey();
            //Vocabulary.TermFrequency tf = (Vocabulary.TermFrequency) me.getValue();
            term = it.next();
            if (saveMatrix) {
                freq = documentVocabulary.get(term);
                //ti = termIndex.add(term);
                // term index
                indexes[j] = termIndex.add(term);

                // tf
                //values[j] = (float) (1 + Math.log10(freq));
                values[j] = (float) freq / size;

            }

            corpusVocabulary.add(term);
            j++;
        } // end while

        columnCount++;

        // SHOULD THE TWO VECTORS BE ORDERED ON INDEXES?
        // the order should be insignificant in svdlibc
        if (saveMatrix) {
            matrixWriter.writeColumn(indexes, values);
        }

    } // end addDocument

    //
    private boolean isWord(String s) {

        if (s.length() < 2) {
            return false;
        }

        char ch = s.charAt(0);

        if (!Character.isLetter(ch)) {
            //logger.info((int) ch + " isWord '" + s + "' false");
            return false;
        }

        for (int i = 1; i < s.length(); i++) {
            ch = s.charAt(i);

            if (!Character.isLetterOrDigit(ch) && ch != '-') {
                //logger.info((int) ch + " isWord '" + s + "' false");
                return false;
            }

        }
        //logger.info("\tisWord '" + s + "' true");
        return true;
    } // end isWord

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        long begin = System.currentTimeMillis();

        PropertyConfigurator.configure(logConfig);

        if (args.length != 6) {
            logger.info(getHelp());
            System.exit(1);
        }
        File root = new File(args[0]);
        File stopwordFile = new File(args[1]);
        File keywordFile = new File(args[2]);
        String output = args[3];
        int n = Integer.parseInt(args[4]);
        boolean b = Boolean.parseBoolean(args[5]);
        new TermDocumentMatrixFileWriter(root, output, stopwordFile, keywordFile, n, b);

        long end = System.currentTimeMillis();
        logger.info("corpus readDocumentList in " + (end - begin) + " ms");
    } // end main

    /**
     * Returns a command-line help.
     * <p/>
     * return a command-line help.
     */
    private static String getHelp() {
        StringBuffer sb = new StringBuffer();

        // License
        ////sb.append(License.get());

        // Usage
        sb.append(
                "Usage: java -mx1024M com.rt.task2.TermDocumentMatrixFileWriter input stowordSet keywordSet output n b\n\n");

        // Arguments
        sb.append("Arguments:\n");
        sb.append("\tinput\t\t-> file from which to read the input corpus (txt format)\n");
        sb.append("\tkeywords\t-> file from which to read the stopwords (one stopword per line)\n");
        sb.append("\tkeywords\t-> file from which to read the keywords to index (one keyword per line)\n");
        sb.append(
                "\toutput\t\t-> root of files in which to store resulting term-by-document matrix (in sparse binary format), row index, col index and document frequency\n");
        sb.append("\tn\t\t-> number of documents to process\n");
        sb.append("\tb\t\t-> true to save the matrix; false to save the term document frequency only\n");
        // Arguments
        //sb.append("Arguments:\n");

        return sb.toString();
    } // end getHelp

} // end TermDocumentMatrixFileWriter

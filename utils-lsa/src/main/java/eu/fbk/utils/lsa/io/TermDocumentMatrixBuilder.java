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

import eu.fbk.utils.lsa.Index;
import eu.fbk.utils.lsa.TermSet;
import eu.fbk.utils.lsa.Vocabulary;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class creates the term-by-document matrix, row index,
 * col index and document frequency to be used by
 * <a href="http://tedlab.mit.edu/~dr/SVDLIBC/"> SVDLIBC</a>
 * to create a LSA model.
 * <p>
 * The input is a file where each line is tokenized document
 * (tokens are separated by spaces), the first token is the document name.
 * <p>
 * The output is stored in the specified directory matrix in
 * <a href="http://tedlab.mit.edu/~dr/SVDLIBC/SVD_F_SB.html">
 * sparse binary format</a>
 * The file created follow the following name convetion:
 * <p>
 * <pre>
 * X-matrix:				term-by-document matrix
 * X-matrix-tf-idf:	term-by-document matrix rescaled with tf-idf
 * X-col:						col index
 * X-row:						row index
 * X-df:						document frequency
 * </pre>
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public abstract class TermDocumentMatrixBuilder {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>TermDocumentMatrixBuilder</code>.
     */
    static Logger logger = Logger.getLogger(TermDocumentMatrixBuilder.class.getName());

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
    protected File matrixFile, rowFile, colFile, dfFile;

    /**
     * Constructs a reader.
     */
    public TermDocumentMatrixBuilder(String matrixName, File stopwordFile, File keywordFile) throws IOException {
        totalKW = 0;
        keywordSet = new TermSet();
        keywordSet.read(new FileReader(keywordFile));
        logger.info("keyword to be indexed: " + keywordSet.size());

        stopwordSet = new TermSet();
        stopwordSet.read(new FileReader(stopwordFile));
        logger.info(stopwordFile + "(" + stopwordSet.size() + ")");

        lengthFreq = new int[101];
        columnCount = 0;

        matrixFile = new File(matrixName + "-matrix");
        rowFile = new File(matrixName + "-row");
        colFile = new File(matrixName + "-col");
        dfFile = new File(matrixName + "-df");

        termIndex = new Index();
        documentIndex = new Index();
        matrixWriter = new SparseBinaryMatrixFileWriter(matrixFile);
        corpusVocabulary = new Vocabulary();

    } // end constructor

    /**
     * Closes the readers.
     */
    public void close() throws IOException {
        //
        termIndex.write(new FileWriter(rowFile));

        //
        documentIndex.write(new FileWriter(colFile));

        //
        matrixWriter.close();

        //
        corpusVocabulary.write(new FileWriter(dfFile));

    } // end close

    //
    public abstract void read(File root) throws IOException;

    //
    protected void addDocument(String[] array) throws IOException {
        //logger.debug("readSentence");
        Vocabulary documentVocabulary = new Vocabulary();

        totalKW += (array.length - 1);
        String token = null;
        String[] t = null;
        for (int i = 1; i < array.length; i++) {
            token = array[i].toLowerCase();

            if (isWord(token)) {
                if (keywordSet.size() == 0) {
                    if (stopwordSet.size() == 0) {
                        logger.debug("1 adding " + token);
                        documentVocabulary.add(token);
                    } else if (!stopwordSet.contains(token)) {
                        logger.debug("2 adding " + token);
                        documentVocabulary.add(token);
                    }
                } else if (keywordSet.contains(token)) {
                    logger.debug("3 adding " + token);
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

        int documentID = documentIndex.get(array[0]);
        ///System.out.print(documentID + " \"" + sent +  "\"\n");

        int size = documentVocabulary.entrySet().size();

        int[] indexes = new int[size];
        float[] values = new float[size];
        int j = 0;

        // iterates over the types
        //logger.debug("iterates over the types");
        Iterator it = documentVocabulary.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            String term = (String) me.getKey();
            Vocabulary.TermFrequency tf = (Vocabulary.TermFrequency) me.getValue();

            int ti = termIndex.add(term);
            indexes[j] = ti;

            values[j] = (float) (1 + Math.log(tf.get()));
            corpusVocabulary.add(term);
            j++;
        } // end while

        columnCount++;
        matrixWriter.writeColumn(indexes, values);

    } // end addDocument

    //
    //
    private boolean isWord(String s) {
        if (s.length() < 2) {
            return false;
        }

        int ch = (int) s.charAt(0);

        if (!Character.isLetter(ch)) {
            //logger.info((int) ch + " isWord '" + s + "' false");
            return false;
        }

        for (int i = 1; i < s.length(); i++) {
            ch = (int) s.charAt(i);

            if (!Character.isLetterOrDigit(ch) || ch == '-') {
                //logger.info((int) ch + " isWord '" + s + "' false");
                return false;
            }

        }
        //logger.info("\tisWord '" + s + "' true");
        return true;
    } // end isWord

} // end TermDocumentMatrixBuilder

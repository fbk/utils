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

import java.io.File;
import java.io.IOException;

/**
 * Writes in Sparse Binary Matrix File.
 * <p>
 * Format:
 * <p><pre>
 * numRows numCols totalNonZeroValues
 * 	for each column:
 * 		numNonZeroValues
 * 			for each non-zero value in the column:
 * 				rowIndex value
 * </pre>
 * <p>
 * All values are 4-byte integers except value, which is a 4-byte
 * float. All are in network byte order.
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public class SparseBinaryMatrixFileWriter extends MatrixFileWriter implements MatrixWriter {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>SparseBinaryMatrixFileWriter</code>.
     */
    static Logger logger = Logger.getLogger(SparseBinaryMatrixFileWriter.class.getName());

    //
    //private RandomAccessFile raf;

    /**
     * The number of rows.
     */
    private int nr;

    /**
     * The number of columns.
     */
    private int nc;

    /**
     * The total number of non zero values.
     */
    private int nz;

    /**
     * Constructs a sparse binary matrix writer.
     *
     * @param file the file where to write the matrix.
     */
    public SparseBinaryMatrixFileWriter(File f) throws IOException {
        super(f);

        nr = 0;
        nc = 0;
        nz = 0;

        // a place holder
        writeHeader();

    } // end constructor

    /**
     * Writes the number of rows, columns and
     * non zero values. All values are 4-byte
     * integers.
     */
    private void writeHeader() throws IOException {
        raf.seek(0);

        // write the number of rows
        raf.writeInt(nr + 1);

        // write the number of columns
        raf.writeInt(nc);

        // write the total number of non zero values
        raf.writeInt(nz);

        logger.debug("matrix: " + (nr + 1) + " " + nc + " " + nz);
    } // end writeHeader

    /**
     * Writes the number of non zero values for a column and
     * or each non-zero value in the column: row index and
     * value. All values are 4-byte integers except value,
     * which is a 4-byte float. All are in network byte
     * order.
     *
     * @param indexes the column indexes.
     * @param values  the column values.
     */
    public void writeColumn(int[] indexes, float[] values) throws IOException {
        // increment the number of columns
        nc++;

        raf.seek(raf.length());

        // write the number of non zero values
        raf.writeInt(indexes.length);
        //logger.debug(Integer.toString(indexes.length));

        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] > nr)
            // increment the number of columns
            {
                nr = indexes[i];
            }

            // write the row index
            raf.writeInt(indexes[i]);

            // write the value
            raf.writeFloat(values[i]);

            //logger.debug(indexes[i] + " " + values[i]);
            // increment the number of non zero values
            nz++;
        } // end for i

    } // end writeColumn

    //
    public void close() throws IOException {
        writeHeader();
        raf.close();
    } // end close

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        long begin = System.currentTimeMillis();

        PropertyConfigurator.configure(logConfig);

        if (args.length != 1) {
            System.out.println("Usage: java -mx512M org.fbk.it.hlt.jlsi.data.SparseBinaryMatrixFileWriter file");
            System.exit(1);
        }

        File file = new File(args[0]);
        MatrixFileWriter mfw = new SparseBinaryMatrixFileWriter(file);

        int[] i0 = { 0, 1 };
        float[] v0 = { 1, 2 };
        mfw.writeColumn(i0, v0);

        int[] i1 = { 0, 2, 3 };
        float[] v1 = { 3, 4, 5 };
        mfw.writeColumn(i1, v1);

        int[] i2 = { 2, 4, 5 };
        float[] v2 = { 6, 7, 8 };
        mfw.writeColumn(i2, v2);
        mfw.close();

        long end = System.currentTimeMillis();
        System.out.println("matrix wrote in " + (end - begin) + " ms");
    } // end main

} // end SparseBinaryMatrixFileWriter
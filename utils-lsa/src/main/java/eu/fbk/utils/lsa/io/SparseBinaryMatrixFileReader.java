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
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * This class read a matrix in sparse binary
 * format froma a file.
 * <p>
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public class SparseBinaryMatrixFileReader {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>SparseBinaryMatrixFileReader</code>.
     */
    static Logger logger = Logger.getLogger(SparseBinaryMatrixFileReader.class.getName());

    //
    private RandomAccessFile raf;

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

    //
    private FileChannel channel;

    /**
     * Constructs a sparse binary matrix writer.
     *
     * @param in the file where to write the matrix.
     */
    public SparseBinaryMatrixFileReader(File in, File row, File df) throws IOException {
        raf = new RandomAccessFile(in, "r");

        channel = raf.getChannel();

        nr = 0;
        nc = 0;
        nz = 0;

        readHeader();
        Entry[][] mat = read();

        close();

    } // end constructor

    /**
     *
     */
    public Entry[][] read() throws IOException {
        Entry[][] mat = new Entry[nc][];
        for (int i = 0; i < nc; i++) {
            Entry[] col = readColumn();
            //logger.debug(i +  " " + col.length);

            mat[i] = col;
        } // end for i

        return mat;
    } // end read

    /**
     * Reads the number of rows, columns and
     * non zero values. All values are 4-byte
     * integers.
     */
    private void readHeader() throws IOException {
        raf.seek(0);

        // read the number of rows
        nr = raf.readInt();

        // read the number of columns
        nc = raf.readInt();

        // read the number of non zero elements
        nz = raf.readInt();

        logger.debug("read matrix: " + nr + " " + nc + " " + nz);
    } // end readHeader

    //
    private static final double LOG2 = Math.log(2);

    //
    private double log2(double d) {
        return Math.log(d) / LOG2;
    } //

    //
    public Entry[] readColumn() throws IOException {
        //float[] col = new float[nr];

        // read the number of elements stored in this column
        int l = raf.readInt();
        Entry[] col = new Entry[l];

        long position = raf.getFilePointer();
        long size = l * 4 * 2;

        MappedByteBuffer mbb = channel.map(FileChannel.MapMode.READ_ONLY, position, size);
        logger.info("l = " + l);
        for (int i = 0; i < l; i++) {
            //col[mbb.getInt()] = mbb.getFloat();
            col[i] = new Entry(mbb.getInt(), mbb.getFloat());
            logger.info(col[i].index() + "\t" + col[i].value());
        }

        // next column
        raf.seek(position + size);
        return col;
    } // end readColumn

    //
    public Entry[] readColumnArray() throws IOException {
        // read the number of elements stored in this column
        int l = raf.readInt();
        Entry[] array = new Entry[l];

        long position = raf.getFilePointer();
        long size = l * 4 * 2;

        MappedByteBuffer mbb = channel.map(FileChannel.MapMode.READ_ONLY, position, size);

        for (int i = 0; i < l; i++) {
            array[i] = new Entry(mbb.getInt(), mbb.getFloat());
        }

        // next column
        raf.seek(position + size);
        return array;
    } // end readColumnArray

    //
    public void close() throws IOException {
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

        if (args.length != 3) {
            System.out.println("Usage: java -mx1024M eu.fbk.utils.lsa.io.SparseBinaryMatrixFileReader in row df");
            System.exit(1);
        }

        File in = new File(args[0]);

        File row = new File(args[1]);
        File df = new File(args[2]);

        SparseBinaryMatrixFileReader tfidf = new SparseBinaryMatrixFileReader(in, row, df);

        long end = System.currentTimeMillis();
        System.out.println("matrix read in " + (end - begin) + " ms");
    } // end main

    //
    public class Entry {

        //
        private int index;

        //
        private float value;

        //
        public Entry(int index, float value) {
            this.index = index;
            this.value = value;
        } // end constructor

        //
        public int index() {
            return index;
        }    // end index

        //
        public float value() {
            return value;
        }    // end value

    } // end class Entry

} // end SparseBinaryMatrixFileReader
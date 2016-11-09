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

import eu.fbk.utils.lsa.InverseVocabulary;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * This class multiplies the term-by-document matrix in sparse binary
 * format and the inverse document frequency diagonal matrix.
 * <p>
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public class TFIDF {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>TFIDF</code>.
     */
    static Logger logger = Logger.getLogger(TFIDF.class.getName());

    //
    private RandomAccessFile inRAF, outRAF;

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

    //
    private InverseVocabulary inverseVocabulary;

    /**
     * Constructs a sparse binary matrix writer.
     *
     * @param file the file where to write the matrix.
     */
    public TFIDF(File in, File out, File row, File df) throws IOException {
        inRAF = new RandomAccessFile(in, "r");
        outRAF = new RandomAccessFile(out, "rw");
        channel = inRAF.getChannel();

        inverseVocabulary = new InverseVocabulary(row, df);

        nr = 0;
        nc = 0;
        nz = 0;

        readHeader();
        writeHeader();

        if (nr != inverseVocabulary.size()) {
            logger.error(nr + " != " + inverseVocabulary);
            System.exit(-1);
        }

        convert();
        close();

    } // end constructor

    /**
     * Reads the number of rows, columns and
     * non zero values. All values are 4-byte
     * integers.
     */
    private void readHeader() throws IOException {
        inRAF.seek(0);

        // read the number of rows
        nr = inRAF.readInt();

        // read the number of columns
        nc = inRAF.readInt();

        // read the number of non zero elements
        nz = inRAF.readInt();

        logger.debug("read matrix: " + nr + " " + nc + " " + nz);
    } // end readHeader

    /**
     * Writes the number of rows, columns and
     * non zero values. All values are 4-byte
     * integers.
     */
    private void writeHeader() throws IOException {
        outRAF.seek(0);

        // write the number of rows
        outRAF.writeInt(nr);

        // write the number of columns
        outRAF.writeInt(nc);

        // write the total number of non zero values
        outRAF.writeInt(nz);

        logger.debug("write matrix: " + nr + " " + nc + " " + nz);
    } // end writeHeader

    /**
     *
     */
    public void convert() throws IOException {
        for (int i = 0; i < nc; i++) {
            Entry[] col = readColumn();
            if ((i % 10000) == 0) {
                System.out.print(".");
            }
            //logger.debug(i +  " " + col.length);
            writeColumn(col);
        } // end for i

        System.out.print("\n");
    } // end read

    //
    private static final double LOG2 = Math.log(2);

    //
    private double log2(double d) {
        return Math.log(d) / LOG2;
    } //

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
    public void writeColumn(Entry[] col) throws IOException {
        //outRAF.seek(outRAF.length());

        // write the number of non zero values
        outRAF.writeInt(col.length);
        //logger.debug(Integer.toString(indexes.length));

        for (int i = 0; i < col.length; i++) {
            // write the row index
            int j = col[i].index();
            //float df = (float) log2((double) nc / inverseVocabulary.get(j));
            //float df = (float) Math.log10((double) nc / inverseVocabulary.get(j));
            // to avoid a division-by-zero
            float df = (float) Math.log10((double) nc / (1 + inverseVocabulary.get(j)));
            //logger.info(df + " = log(" + nc + " / " + inverseVocabulary.get(j) + ")");

            float tf = col[i].value();

            //logger.info(tf + " = " + tf);
            //
            // ATTENZIONE DEVO PRENDERE IL 1+LOG_10(tf)
            // OPPURE tf/numero_di_parole_doc
            //
            float w = tf * df;
            //logger.debug(w + "[" + i + "] = " + tf + " * " + df);

            outRAF.writeInt(j);

            // write the value
            outRAF.writeFloat(w);
        } // end for i

    } // end writeColumn

    //
    public Entry[] readColumn() throws IOException {
        //float[] col = new float[nr];

        // read the number of elements stored in this column
        int l = inRAF.readInt();
        Entry[] col = new Entry[l];

        long position = inRAF.getFilePointer();
        long size = l * 4 * 2;

        MappedByteBuffer mbb = channel.map(FileChannel.MapMode.READ_ONLY, position, size);

        for (int i = 0; i < l; i++) {
            //col[mbb.getInt()] = mbb.getFloat();
            col[i] = new Entry(mbb.getInt(), mbb.getFloat());
        }

        // next column
        inRAF.seek(position + size);
        return col;
    } // end readColumn

    //
    public Entry[] readColumnArray() throws IOException {
        // read the number of elements stored in this column
        int l = inRAF.readInt();
        Entry[] array = new Entry[l];

        long position = inRAF.getFilePointer();
        long size = l * 4 * 2;

        MappedByteBuffer mbb = channel.map(FileChannel.MapMode.READ_ONLY, position, size);

        for (int i = 0; i < l; i++) {
            array[i] = new Entry(mbb.getInt(), mbb.getFloat());
        }

        // next column
        inRAF.seek(position + size);
        return array;
    } // end readColumnArray

    //
    public void close() throws IOException {
        inRAF.close();
        outRAF.close();
    } // end close

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        long begin = System.currentTimeMillis();

        PropertyConfigurator.configure(logConfig);

        if (args.length != 4) {
            System.out.println("Usage: java -mx1024M eu.fbk.utils.lsa.io.TFIDF in out row df");
            System.exit(1);
        }

        File in = new File(args[0]);
        File out = new File(args[1]);
        File row = new File(args[2]);
        File df = new File(args[3]);

        TFIDF tfidf = new TFIDF(in, out, row, df);

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

} // end TFIDF
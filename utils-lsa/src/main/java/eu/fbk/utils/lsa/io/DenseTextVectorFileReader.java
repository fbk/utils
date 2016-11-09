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
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * Reads a vector in dense text format from a file.
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public class DenseTextVectorFileReader {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>DenseTextVectorFileReader</code>.
     */
    static Logger logger = Logger.getLogger(DenseTextVectorFileReader.class.getName());

    //
    private LineNumberReader lnr;

    /**
     * The number of rows.
     */
    private int nr;

    /**
     * Constructs a sparse binary vector reader.
     *
     * @param file the file from which read the vector.
     */
    public DenseTextVectorFileReader(File f) throws IOException {
        lnr = new LineNumberReader(new FileReader(f));

        nr = 0;

        readHeader();

    } // end constructor

    /**
     * Constructs a sparse binary vector reader.
     *
     * @param file the file from which read the vector.
     */
    public DenseTextVectorFileReader(File f, int dim) throws IOException {
        lnr = new LineNumberReader(new FileReader(f));

        nr = 0;

        readHeader();

        if (dim < nr) {
            nr = dim;
        }
    } // end constructor

    //
    private void readHeader() throws IOException {
        String line = lnr.readLine();

        if (line != null) {
            // ATTENZIONE QUESTO NON ERE ESEGUITO
            nr = Integer.parseInt(line);
        }

        logger.debug("vector: " + nr);
    } // end readHeader

    //
    public double[] readDouble() throws IOException {
        double[] vector = new double[nr];

        for (int i = 0; i < nr; i++) {
            //System.out.print(i + " ");
            String line = lnr.readLine();
            if (line != null) {
                // ATTENZIONE QUESTO NON ERE ESEGUITO
                vector[i] = Float.parseFloat(line);
            }
        } // end for i

        return vector;
    } // end read

    //
    public float[] read() throws IOException {
        float[] vector = new float[nr];

        for (int i = 0; i < nr; i++) {
            //System.out.print(i + " ");
            String line = lnr.readLine();
            if (line != null) {
                // ATTENZIONE QUESTO NON ERE ESEGUITO
                vector[i] = Float.parseFloat(line);
            }
        } // end for i

        return vector;
    } // end read

    //
    public void close() throws IOException {
        lnr.close();
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
            System.out.println("Usage: java -mx1024M com.rt.task2.DenseTextVectorFileReader file");
            System.exit(1);
        }

        File file = new File(args[0]);

        DenseTextVectorFileReader vectorFileReader = new DenseTextVectorFileReader(file);
        float[] vector = vectorFileReader.read();

        logger.debug("vector: " + vector.length);
        System.out.println(vector.length);
        for (int i = 0; i < vector.length; i++) {
            if (i != 0) {
                System.out.print(" ");
            }
            System.out.print(vector[i]);
        } // end for i
        System.out.print("\n");

        long end = System.currentTimeMillis();
        //System.out.println("vector wrote in " + (end - begin) + " ms");
    } // end main

} // end DenseTextVectorFileReader
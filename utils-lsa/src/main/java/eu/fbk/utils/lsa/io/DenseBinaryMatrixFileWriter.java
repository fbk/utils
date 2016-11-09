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

import java.io.*;

//import cern.colt.matrix.impl.*;

/**
 * Write a matrix in dense binary format in a file.
 * <p>
 * Format:
 * <p><pre>
 * numRows numCols
 * 	for each row:
 * 		for each column:
 * 			value
 * </pre><p>
 * numRows and numCols are 4-byte integers. value is a 4-byte float.
 * All are in network byte order.
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public class DenseBinaryMatrixFileWriter //implements MatrixWriter
{

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>DenseBinaryMatrixFileWriter</code>.
     */
    static Logger logger = Logger.getLogger(DenseBinaryMatrixFileWriter.class.getName());

    //
    private DataOutputStream outputStream;

    /**
     * Constructs a dense binary matrix writer.
     *
     * @param file the file where to write the matrix.
     */
    public DenseBinaryMatrixFileWriter(File f) throws IOException {
        outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));

    } // end constructor

    /**
     *
     */
    public void write(float[][] matrix, boolean b) throws IOException {
        if (!b) {
            write(matrix);
            return;
        }

        int nr = matrix[0].length;
        int nc = matrix.length;
        logger.debug("matrix^T " + nr + " x " + nc);

        outputStream.writeInt(nr);
        outputStream.writeInt(nc);

        for (int i = 0; i < nr; i++) {
            //System.out.print(i + " ");
            for (int j = 0; j < nc; j++) {
                // read the value
                outputStream.writeFloat(matrix[j][i]);
                //logger.debug(i + ", " + j + " = " + matrix[j][i]);
            } // end for j
        } // end for i

        //return matrix;
    } // end write

    /**
     *
     */
    public void write(float[][] matrix) throws IOException {
        int nr = matrix.length;
        int nc = matrix[0].length;
        logger.debug("matrix " + nr + " x " + nc);
        outputStream.writeInt(nr);
        outputStream.writeInt(nc);

        for (int i = 0; i < nr; i++) {
            //System.out.print(i + " ");
            for (int j = 0; j < nc; j++) {
                // read the value
                outputStream.writeFloat(matrix[i][j]);
                //logger.debug(i + ", " + j + " = " + matrix[i][j]);
            } // end for j
        } // end for i

        //return matrix;
    } // end write

    //
    public void close() throws IOException {
        outputStream.close();
    } // end close

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        long begin = System.currentTimeMillis();

        PropertyConfigurator.configure(logConfig);

        if (args.length != 2) {
            System.out.println("Usage: java -mx1024M com.rt.task2.DenseBinaryMatrixFileWriter file boolean");
            System.exit(1);
        }

        File file = new File(args[0]);
        boolean b = Boolean.parseBoolean(args[1]);

        DenseBinaryMatrixFileWriter matrixFileReader = new DenseBinaryMatrixFileWriter(file);
        float[][] matrix = {
                {
                        1, 2, 3, 4, 5
                },
                {
                        6, 7, 8, 9, 10
                },
                {
                        11, 12, (float) 13.1, (float) 13.2, (float) 13.3
                }
        };

        matrixFileReader.write(matrix, b);
        matrixFileReader.close();

        //logger.debug("matrix: " + matrix.rows() + " X " + matrix.columns());
/*
        for (int i=0;i<matrix.rows();i++)
		{
			for (int j=0;i<matrix.columns();j++)
			{
				
				System.out.print(matrix.getQuick(i, j) + " ");
			} // end for j
			System.out.print("\n");
		} // end for i		
		*/
        long end = System.currentTimeMillis();
        System.out.println("matrix wrote in " + (end - begin) + " ms");
    } // end main

} // end DenseBinaryMatrixFileWriter
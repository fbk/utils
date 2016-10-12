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
 * Reads in a matrix in dense binary format from a file.
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public class DenseBinaryMatrixFileReader //implements MatrixFileReader
{

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>DenseBinaryMatrixFileReader</code>.
     */
    static Logger logger = Logger.getLogger(DenseBinaryMatrixFileReader.class.getName());

    //
    private DataInputStream inputStream;

    //
    private int dim = Integer.MAX_VALUE;

    //
    public DenseBinaryMatrixFileReader(File f) throws IOException {
        inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));

    } // end constructor

    //
    public DenseBinaryMatrixFileReader(File f, int dim) throws IOException {
        inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
        this.dim = dim;
    } // end constructor

    //
    private static void printAll(float[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (j != 0) {
                    System.out.print(" ");
                }
                System.out.print(matrix[i][j]);
            } // end for j
            System.out.print("\n");
        } // end for i
    } // end printAll

    //
    private static void print(float[][] Uk, String msg) {
        System.out.println("\n" + msg);

        if ((Uk.length <= 10) && (Uk[0].length <= 10)) {
            printAll(Uk);
            return;
        }

        // header
        for (int j = 0; j < 3; j++) {
            //if (j != 0)
            System.out.print("\t");
            System.out.print(j);
        }

        System.out.print("\t...");
        for (int j = (Uk[0].length - 3); j < Uk[0].length; j++) {
            //if (j != 0)
            System.out.print("\t");
            System.out.print(j);
        }
        System.out.print("\n");

        // matrix
        for (int i = 0; i < 3; i++) {
            System.out.print(i);
            for (int j = 0; j < 3; j++) {
                //if (j != 0)
                System.out.print("\t");
                System.out.print(Uk[i][j]);
            }

            System.out.print("\t...");
            for (int j = (Uk[i].length - 3); j < Uk[i].length; j++) {
                //if (j != 0)
                System.out.print("\t");
                System.out.print(Uk[i][j]);
            }
            System.out.print("\n");
        }

        System.out.print("...\n");
        for (int i = (Uk.length - 3); i < Uk.length; i++) {
            System.out.print(i);
            for (int j = 0; j < 3; j++) {
                //if (j != 0)
                System.out.print("\t");
                System.out.print(Uk[i][j]);
            }

            System.out.print("\t...");
            for (int j = (Uk[i].length - 3); j < Uk[i].length; j++) {
                //if (j != 0)
                System.out.print("\t");
                System.out.print(Uk[i][j]);
            }
            System.out.print("\n");
        }
    } // end print

    public double[][] readDouble(boolean b) throws IOException {
        if (!b) {
            return readDouble();
        }

        int nr = inputStream.readInt();
        int nc = inputStream.readInt();
        logger.debug("matrix " + nr + " x " + nc);

        if (dim < nr) {
            nr = dim;
            logger.debug("read only the first " + nr + " components");
        }

        double[][] matrix = new double[nc][nr];
        for (int i = 0; i < nr; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < nc; j++) {
                // read the value
                matrix[j][i] = inputStream.readFloat();
            } // end for j
        } // end for i

        return matrix;
    } // end read

    /**
     *
     */
    public float[][] read(boolean b) throws IOException {
        if (!b) {
            return read();
        }

        int nr = inputStream.readInt();
        int nc = inputStream.readInt();
        logger.debug("matrix " + nr + " x " + nc);

        if (dim < nr) {
            nr = dim;
            logger.debug("read only the first " + nr + " components");
        }

        float[][] matrix = new float[nc][nr];
        for (int i = 0; i < nr; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < nc; j++) {
                // read the value
                matrix[j][i] = inputStream.readFloat();
                /*
				// introduced to solve a bug in the db format in SVDLIB
				float f = inputStream.readFloat();
				if (j == 0)
					matrix[nc-1][i] = 0;
				else
					matrix[j-1][i] = f;
				*/
                //logger.debug(i + ", " + j + " = " + matrix[i][j]);
            } // end for j
        } // end for i

        return matrix;
    } // end read

    /**
     *
     */
    public double[][] readDouble() throws IOException {

        int nr = inputStream.readInt();
        int nc = inputStream.readInt();
        logger.debug("matrix " + nr + " x " + nc);

        if (dim < nr) {
            nr = dim;
            logger.debug("read only the first " + nr + " components");
        }

        double[][] matrix = new double[nr][nc];
        for (int i = 0; i < nr; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < nc; j++) {
                // read the value
                matrix[i][j] = inputStream.readFloat();
            } // end for j
        } // end for i

        return matrix;
    } // end read

    /**
     *
     */
    public float[][] read() throws IOException {

        int nr = inputStream.readInt();
        int nc = inputStream.readInt();
        logger.debug("matrix " + nr + " x " + nc);

        if (dim < nr) {
            nr = dim;
            logger.debug("read only the first " + nr + " components");
        }

        float[][] matrix = new float[nr][nc];
        for (int i = 0; i < nr; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < nc; j++) {
                // read the value
                matrix[i][j] = inputStream.readFloat();
				/*
				// introduced to solve a bug in the db format in SVDLIB
				float f = inputStream.readFloat();
				if (j == 0)
					matrix[i][nc-1] = 0;
				else
					matrix[i][j-1] = f;
					*/
                //logger.debug(i + ", " + j + " = " + matrix[i][j]);
            } // end for j
        } // end for i

        return matrix;
		/*
		for (int i=0;i<10;i++)
		{
			logger.info(i + " " + inputStream.readFloat());
		}
		return null;*/
    } // end read

    //
    public void close() throws IOException {
        inputStream.close();
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
            System.out.println("Usage: java -mx1024M eu.fbk.utils.lsa.io.DenseBinaryMatrixFileReader file boolean");
            System.exit(1);
        }

        File file = new File(args[0]);
        boolean b = Boolean.parseBoolean(args[1]);

        DenseBinaryMatrixFileReader matrixFileReader = new DenseBinaryMatrixFileReader(file);
        float[][] matrix = matrixFileReader.read(b);
        //logger.debug("matrix: " + matrix.length + " X " + matrix[0].length);

        //PrintWriter pw = new PrintWriter(new FileWriter("prova.txt"));
        //System.out.println(matrix.length + " " + matrix[0].length);
        print(matrix, matrix.length + " " + matrix[0].length);
        //pw.println(matrix.length + " " + matrix[0].length);
        //pw.flush();
	/*
		for (int i=0;i<matrix.length;i++)
		{
			for (int j=0;j<matrix[i].length;j++)
			{
				if (j != 0)
					System.out.print(" ");
					//pw.print(" ");
				System.out.print(matrix[i][j]);
				//pw.print(matrix[i][j]);
			} // end for j
			System.out.print("\n");
			//pw.print("\n");
			//pw.flush();
		} // end for i		
		 */
        //pw.flush();
        //pw.close();

        long end = System.currentTimeMillis();
        //System.out.println("matrix read in " + (end - begin) + " ms");
    } // end main

} // end DenseBinaryMatrixFileReader
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
import java.util.regex.Pattern;

/**
 * Reads a dense text matrix from a file and writes
 * it in dense binary format in a new file.
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public class DenseText2DenseBinary //implements MatrixFileReader
{

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>DenseText2DenseBinary</code>.
     */
    static Logger logger = Logger.getLogger(DenseText2DenseBinary.class.getName());

    //
    protected static Pattern spacePattern = Pattern.compile(" ");

    //
    private LineNumberReader lnr;

    /**
     * The number of rows.
     */
    private int nr;

    //
    private DataOutputStream outputStream;

    /**
     * The number of rows.
     */
    private int nc;

    //
    public DenseText2DenseBinary(File input, File output) throws IOException {
        outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)));
        lnr = new LineNumberReader(new FileReader(input));

        nr = 0;
        nc = 0;
        readHeader();

    } // end constructor

    //
    private void readHeader() throws IOException {
        String line = lnr.readLine();

        if (line != null) {
            //String[] s = line.split(" ");
            String[] s = spacePattern.split(line);

            nr = Integer.parseInt(s[0]);
            nc = Integer.parseInt(s[1]);

            outputStream.writeInt(nr);
            outputStream.writeInt(nc);

        }

        logger.debug("matrix: " + nr + " X " + nc);
    } // end readHeader

    //
    public void read() throws IOException {
        for (int i = 0; i < nr; i++) {
            System.out.print(i);
            String line = lnr.readLine();
            System.out.print(". ");
            if (line != null) {
                //String[] s = line.split(" ");
                String[] s = spacePattern.split(line);

                for (int j = 0; j < s.length; j++) {
                    //matrix.setQuick(j, i, (double) Float.parseFloat(s[j]));
                    outputStream.writeFloat(Float.parseFloat(s[j]));
                }
            }
            outputStream.flush();
        } // end for i
    } // end read

    //
    public void read1() throws IOException {
        for (int i = 0; i < nr; i++) {
            System.out.print(i);
            String line = lnr.readLine();
            System.out.print(". ");
            int c = write(line);
            if (c != nc) {
                logger.error("counter " + c + " != " + nc);
                System.exit(-1);
            }
            outputStream.flush();
        } // end for i

    } // end read

    //
    private int write(String s) throws IOException {
        //logger.info("s: '" +s +"'");
        //List<Float> list = new ArrayList<Float>();
        int c = 0;
        int j = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == 32) {
                //list.add(Float.parseFloat(s.substring(j, i)));
                //logger.info(j + ":" + i + ":" +s.substring(j, i));
                outputStream.writeFloat(Float.parseFloat(s.substring(j, i)));
                c++;
                j = i + 1;
            }
        }
        // last
        //logger.info(j + ":" + s.length() + ":" +s.substring(j, s.length()));
        outputStream.writeFloat(Float.parseFloat(s.substring(j, s.length())));
        c++;

        //logger.info("counter " + c);
        return c;
    } // end split

    //
    public void close() throws IOException {
        lnr.close();
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
            System.out.println("Usage: java -mx1024M eu.fbk.utils.lsa.io.DenseText2DenseBinary input output");
            System.exit(1);
        }

        File input = new File(args[0]);
        File output = new File(args[1]);

        DenseText2DenseBinary matrixFileReader = new DenseText2DenseBinary(input, output);
        matrixFileReader.read1();
        matrixFileReader.close();
        //print(matrix, matrix.rows() + " X " + matrix.columns());
        long end = System.currentTimeMillis();
        logger.info("matrix wrote in " + (end - begin) + " ms");
    } // end main

} // end DenseText2DenseBinary
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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Abstract class for writing to matrices.
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public abstract class MatrixFileWriter implements MatrixWriter {

    //
    protected RandomAccessFile raf;

    /**
     * Constructs a sparse binary matrix writer.
     *
     * @param file the file where to write the matrix.
     */
    protected MatrixFileWriter(File f) throws IOException {
        raf = new RandomAccessFile(f, "rw");
    } // end constructor

    /**
     * Closes this matrix file writer. A closed matrix
     * file writer cannot perform input or output operations
     * and cannot be reopened.
     */
    public abstract void close() throws IOException;

} // end interface MatrixFileWriter
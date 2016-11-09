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

import java.io.IOException;

/**
 * Interface for writing to matrices.
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public interface MatrixWriter {

    /**
     * Writes a matrix column in a file.
     *
     * @param indexes the column indexes.
     * @param values  the column values.
     */
    public void writeColumn(int[] indexes, float[] values) throws IOException;

} // end interface MatrixWriter
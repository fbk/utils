/*
 * Copyright (2011) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.analysis.stemmer;

/**
 * TO DO
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public interface Stemmer {

    //
    public abstract String stem(String s);

    //
    public abstract String stemNgram(String s);

} // end interface Stemmer
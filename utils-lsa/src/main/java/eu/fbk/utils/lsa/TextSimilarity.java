/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.lsa;

/**
 * TO DO
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 * @see TextSimilarity
 */
public interface TextSimilarity {

    /**
     *
     */
    public float compare(String term1, String term2) throws TermNotFoundException;

    /**
     *
     */
    public float compare(BOW bow1, BOW bow2); //throws TermNotFoundException;

    /**
     *
     */
    //public ScoreTermMap[] compareAll(String[] terms) throws TermNotFoundException;

    /**
     *
     */
    //public ScoreTermMap compareAll(String t) throws TermNotFoundException;

} // end interface TextSimilarity
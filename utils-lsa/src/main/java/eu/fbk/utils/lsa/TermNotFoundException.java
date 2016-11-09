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
 * @see TermNotFoundException
 */
public class TermNotFoundException extends Exception {

    //
    private String t;

    //
    private static final long serialVersionUID = 42L;

    /**
     * Constructs a <code>TermNotFoundException</code> object.
     */
    public TermNotFoundException(String t) {
        this.t = t;
    } // end constructor

    /**
     * Returns a <code>String</code> object representing this
     * <code>Word</code>.
     *
     * @return a string representation of this object.
     */
    public String toString() {
        return t + " not found.";
    } // end toString
} // end class TermNotFoundException
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

//
public class StemmerNotFoundException extends Exception {

    //
    private static final long serialVersionUID = 5024396602591514749L;

    //
    public StemmerNotFoundException() {
        super();
    } // end constructor

    //
    public StemmerNotFoundException(String lang) {
        super("A stemmer for " + lang + " was not found");
    } // end constructor

} // end class StemmerNotFoundException

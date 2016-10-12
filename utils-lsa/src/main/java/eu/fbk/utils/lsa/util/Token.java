/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.lsa.util;

import org.apache.log4j.Logger;

//
public class Token {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>Token</code>.
     */
    static Logger logger = Logger.getLogger(Token.class.getName());

    //
    public static String normalize(String s) {
        if (s.length() == 0) {
            return s;
        }
        char c = s.charAt(0);
        if (Character.isUpperCase(c)) {
            return s.substring(0, 1) + s.substring(1, s.length()).toLowerCase();
        }

        return s.toLowerCase();
    } // end normalize

} // end class Token
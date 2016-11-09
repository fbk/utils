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

//

import java.io.File;
import java.io.FileFilter;

//
public class TsvFilter implements FileFilter {

    /**
     * Returns <code>true</code> if the specified file
     * is a directory or a txt files; <code>true</code>
     * otherwise.
     *
     * @param name the file name.
     * @return <code>true</code> if the specified file
     * is a directory or a txt files; <code>true</code>
     * otherwise.
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f.getName());
        if (extension != null) {
            if (extension.toLowerCase().equals("tsv")) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    } // end accept

    /**
     * Returns the description of this filter.
     *
     * @returns the description of this filter.
     */
    public String getDescription() {
        return "Just Text";
    } // end getDescription

    /**
     * Returns the extension of the specified file name.
     *
     * @param name the file name.
     * @return the extension.
     */
    private String getExtension(String name) {
        int i = name.lastIndexOf('.') + 1;

        if ((i == -1) && (i == name.length())) {
            return "";
        }

        return name.substring(i);
    } // end getExtension

} // end class TsvFilter
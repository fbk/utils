/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.core.io;

import java.io.File;
import java.io.FileFilter;

/**
 * This class provides a filter on txt files.
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public class TxtFilter implements FileFilter {

    // Accept all directories and all .test files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        if (f.getName().toLowerCase().endsWith(".txt") || f.getName().toLowerCase().endsWith(".txt.gz")) {
            return true;
        }

        return false;
    } // end accept

    // The description of this filter
    public String getDescription() {
        return "Just txt File";
    }

} // end class

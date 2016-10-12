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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Scans recursively a directory. Successive calls to the
 * <code>next</code> method return successive arrays of
 * objects <code>File</code>.
 * <p>
 * The following code fragment, in which <code>root</code> is
 * the starging directory, illustrates how to use a folder
 * scanner.
 * <p>
 * <pre>
 * 		FolderScanner fs = new FolderScanner(root);
 * 		while (fs.hasNext())
 *        {
 * 			Object[] files = fs.next();
 *
 * 			for (int i=0;i<files.length;i++)
 * 				System.out.println((File) files[i]);
 *        }
 * </pre>
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public final class FolderScanner {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>FolderScanner</code>.
     */
    static Logger logger = LoggerFactory.getLogger(FolderScanner.class);

    /**
     * The name root path.
     */
    private File root;

    /**
     * The stack used for manage recursive scanning.
     */
    private Stack<File> stack;

    /**
     * The filter used to consider only file
     * with a specified extension.
     */
    private FileFilter filter;

    /**
     * Create a folder scanner.
     *
     * @param root the root directory.
     */
    public FolderScanner(File root) {
        //System.out.println("FolderScanner.FolderScanner");
        this.root = root;

        stack = new Stack<File>();
        stack.push(root);

    } // end constructor

    /**
     * Sets a file filter for this scanner
     *
     * @param root the root directory.
     */
    public void setFiler(FileFilter filter) {
        //System.out.println("FolderScanner.setFiler");
        this.filter = filter;
    } // end setFiler

    /**
     * Sets a file filter for this scanner
     *
     * @param root the root directory.
     */
    public void setFilter(FileFilter filter) {
        //System.out.println("FolderScanner.setFilter");
        this.filter = filter;
    } // end setFiler

    /**
     * Returns <code>true</code> if the scanner has more
     * directories. (In other words, returns <code>true</code>
     * if <code>next</code> would return an array of files
     * rather than return <code>null</code>.)
     *
     * @return <code>true</code> if the scanner has more elements.
     */
    public boolean hasNext() {
        //System.out.println("FolderScanner.hasNext");

        if (!stack.empty()) {
            return true;
        }

        return false;
    } // end hasNext

    /**
     * Returns the next array of files in the iteration.
     *
     * @return the next array of files in the iteration.
     */
    public Object[] next() {
        //System.out.println("FolderScanner.next");
        File dir = null;

        if (!stack.empty()) {
            List<File> res = new ArrayList<File>();
            try {
                dir = stack.pop();

                File[] ls = null;
                if (filter == null) {
                    ls = dir.listFiles();
                } else {
                    ls = dir.listFiles(filter);
                }

                for (int i = 0; i < ls.length; i++) {
                    if (ls[i].isFile()) {
                        res.add(ls[i]);
                    } else if (ls[i].isDirectory()) {
                        stack.push(ls[i]);
                    }
                } // end for
            } catch (Exception e) {
                logger.error("Exception thrown \"FolderScanner.next\" " + e);
            }

            return res.toArray();
        } // end if

        return null;
    } // end next

    //
    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("Usage: java -mx512M org.fbk.it.hlt.jlsi.util.FolderScanner in");
            System.exit(1);
        }

        FolderScanner fs = new FolderScanner(new File(args[0]));

        int count = 0;
        while (fs.hasNext()) {
            Object[] files = fs.next();
            System.out.println((count++) + " : " + files.length);
            for (int i = 0; i < files.length; i++) {
                System.out.println(files[i]);
            }
        }

    }

}
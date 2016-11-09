/*
 * Copyright (2012) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.core.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

/**
 * An object that maps elements to indexes.
 * A <code>TreeIndexSet</code> cannot contain duplicate
 * elements; each element can map to at most one index.
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @see IndexSet
 * @since 1.0
 */
public class TreeIndexSet<E> extends IndexSet<E>
        implements Serializable, Cloneable, Iterable<E>, Collection<E>, Set<E> {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>TreeIndexSet</code>.
     */
    static Logger logger = LoggerFactory.getLogger(TreeIndexSet.class);

    //
    private static final long serialVersionUID = 41L;

    /**
     * Constructs a <code>TreeIndexSet</code> object.
     */
    public TreeIndexSet(boolean threadSafe) {
        this(threadSafe, 0);

    } // end constructor

    /**
     * Constructs a <code>TreeIndexSet</code> object.
     */
    public TreeIndexSet() {
        this(0);
    } // end constructor

    /**
     * Constructs a <code>TreeIndexSet</code> object.
     *
     * @parm from    the first index
     */
    public TreeIndexSet(boolean threadSafe, int from) {
        super(from);
        //logger.info("TreeIndexSet " + count);
        if (threadSafe) {
            map = Collections.synchronizedMap(new TreeMap<E, Entry>());
        } else {
            map = new TreeMap<E, Entry>();
        }

    } // end constructor

    /**
     * Constructs a <code>TreeIndexSet</code> object.
     *
     * @parm from    the first index
     */
    public TreeIndexSet(int from) {
        super(from);
        //logger.info("TreeIndexSet " + count);

        map = new TreeMap<E, Entry>();
    } // end constructor

    //
    public static void main(String args[]) throws Exception {

        if (args.length == 0) {
            logger.info("java eu.fbk.utils.core.core.TreeIndexSet element+");
            System.exit(-1);
        }

        TreeIndexSet<String> set = new TreeIndexSet<String>();
        logger.info("element\tadded\tindex");
        for (int i = 0; i < args.length; i++) {
            boolean b = set.add(args[i]);
            logger.info(args[i] + "\t" + b + "\t" + set.getIndex(args[i]));
        } // end for i

    } // end main

} // end class TreeIndexSet
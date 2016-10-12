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
import java.util.HashMap;
import java.util.Set;

/**
 * An object that maps elements to indexes.
 * A <code>HashIndexSet</code> cannot contain duplicate
 * elements; each element can map to at most one index.
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 * @see IndexSet
 */
public class HashIndexSet<E> extends IndexSet<E>
        implements Serializable, Cloneable, Iterable<E>, Collection<E>, Set<E> {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>HashIndexSet</code>.
     */
    static Logger logger = LoggerFactory.getLogger(HashIndexSet.class);

    //
    private static final long serialVersionUID = 41L;

    /**
     * Constructs a <code>HashIndexSet</code> object.
     */
    public HashIndexSet() {
        this(0);
    } // end constructor

    /**
     * Constructs a <code>HashIndexSet</code> object.
     *
     * @parm from    the first index
     */
    public HashIndexSet(int from) {
        super(from);
        //logger.info("HashIndexSet " + count);

        //map = new TreeMap<String, Entry>();
        map = new HashMap<E, Entry>();

    } // end constructor

    //
    public static void main(String args[]) throws Exception {

        if (args.length == 0) {
            logger.info("java eu.fbk.utils.core.core.HashIndexSet element+");
            System.exit(-1);
        }

        HashIndexSet<String> set = new HashIndexSet<String>();
        logger.info("element\tadded\tindex");
        for (int i = 0; i < args.length; i++) {
            boolean b = set.add(args[i]);
            logger.info(args[i] + "\t" + b + "\t" + set.getIndex(args[i]));
        } // end for i

    } // end main

} // end class HashIndexSet
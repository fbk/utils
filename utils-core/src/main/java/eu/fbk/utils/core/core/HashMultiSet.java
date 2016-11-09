/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
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
import java.util.HashMap;
import java.util.Set;

/**
 * Implements the <code>Set</code> interface where elements
 * can be repeated, the method <code>getFrequency<code>
 * returns the frequency of the element into the set.
 * <p>
 * The implementation is backed by a hash table (actually
 * a HashMap instance). It makes no guarantees as to the
 * iteration order of the set; in particular, it does not
 * guarantee that the order will remain constant over time.
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public class HashMultiSet<E> extends MultiSet<E>
        implements Serializable, Cloneable, Iterable<E>, Collection<E>, Set<E> {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>HashMultiSet</code>.
     */
    static Logger logger = LoggerFactory.getLogger(HashMultiSet.class);

    //
    private static final long serialVersionUID = 42L;

    //
    public HashMultiSet(boolean threadSafe) {
        super();
        if (threadSafe) {
            map = Collections.synchronizedMap(new HashMap<E, Counter>());
        } else {
            map = new HashMap<E, Counter>();
        }

    } // end constructor

    //
    public HashMultiSet() {
        super();
        map = new HashMap<E, Counter>();
    } // end constructor

    //
    public static void main(String args[]) throws Exception {

        if (args.length == 0) {
            logger.info("java eu.fbk.utils.core.core.TreeMultiSet element+");
            System.exit(-1);
        }

        HashMultiSet<String> set = new HashMultiSet<String>();
        logger.info("element\tadded\tfreq");
        for (int i = 0; i < args.length; i++) {
            boolean b = set.add(args[i]);
            logger.info(args[i] + "\t" + b + "\t" + set.getFrequency(args[i]));
        } // end for i

    } // end main

} // end class HashMultiSet
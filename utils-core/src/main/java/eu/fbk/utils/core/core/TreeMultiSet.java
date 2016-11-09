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
import java.util.Set;
import java.util.TreeMap;

/**
 * Implements the <code>Set</code> interface  where elements
 * can be repeated, the method <code>getFrequency<code> returns
 * the frequency of the element into the set.
 * <p>
 * This implemetation is backed by a TreeMap instance. This class
 * guarantees that the sorted set will be in ascending element
 * order, sorted according to the natural order of the elements
 * (see Comparable), or by the comparator provided at set creation
 * time, depending on which constructor is used.
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public class TreeMultiSet<E> extends MultiSet<E>
        implements Serializable, Cloneable, Iterable<E>, Collection<E>, Set<E> {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>TreeMultiSet</code>.
     */
    static Logger logger = LoggerFactory.getLogger(TreeMultiSet.class);

    //
    private static final long serialVersionUID = 43L;

    //
    public TreeMultiSet(boolean threadSafe) {
        super();
        if (threadSafe) {
            map = Collections.synchronizedMap(new TreeMap<E, Counter>());
        } else {
            map = new TreeMap<E, Counter>();
        }
    } // end constructor

    //
    public TreeMultiSet() {
        super();
        map = new TreeMap<E, Counter>();
    } // end constructor

    //
    public static void main(String args[]) throws Exception {

        if (args.length == 0) {
            logger.info("java eu.fbk.utils.core.core.TreeMultiSet element+");
            System.exit(-1);
        }

        TreeMultiSet<String> set = new TreeMultiSet<String>();
        logger.info("element\tadded\tfreq");
        for (int i = 0; i < args.length; i++) {
            boolean b = set.add(args[i]);
            logger.info(args[i] + "\t" + b + "\t" + set.getFrequency(args[i]));
        } // end for i
    } // end main

} // end class TreeMultiSet
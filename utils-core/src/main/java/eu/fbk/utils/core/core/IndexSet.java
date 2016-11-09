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

import java.util.*;

/**
 * This class provides a skeletal implementation of the <code>Set</code>
 * interface to minimize the effort required to implement an index
 * set.
 * Each element in the <code>IndexSet</code> has a unique index,
 * The first element added to the <code>IndexSet</code> has index 0
 * if not specified.
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @see MultiSet
 * @since 1.0
 */
public abstract class IndexSet<E> implements Iterable<E>, Collection<E>, Set<E> {

    /**
     * to do.
     */
    protected Map<E, Entry> map;

    /**
     * to do.
     */
    private int count;

    /**
     * Constructs a <code>IndexSet</code> object.
     *
     * @param from the first index.
     */
    public IndexSet(int from) {
        count = from;
    } // end constructor

    /**
     * Constructs a <code>IndexSet</code> object.
     */
    public IndexSet() {
        this(0);
    } // end constructor

    //
    public boolean isEmpty() {
        return map.isEmpty();
    } // end isEmpty

    //
    public boolean remove(Object o) {
        if (map.remove(o) == null) {
            return true;
        }

        return false;
    } // end remove

    //
    public boolean removeAll(Collection<?> c) {
        Iterator<?> it = c.iterator();
        boolean b = true;
        while (it.hasNext()) {
            b &= remove(it.next());
        } // end while

        return b;
    } // end removeAll

    //
    public boolean retainAll(Collection<?> c) {
        Iterator<E> it = iterator();
        boolean b = true;
        while (it.hasNext()) {
            E e = it.next();
            if (!c.contains(e)) {
                b &= remove(e);
            }
        } // end while

        return b;
    } // end retainAll

    //
    public boolean addAll(Collection<? extends E> c) {
        Iterator it = c.iterator();
        boolean b = true;
        while (it.hasNext()) {
            //E o = (E) it.next();
            b &= add((E) it.next());
        } // end while
        return b;
    } // end addAll

    //
    public void addAll(Set<E> set) {
        Iterator<E> it = set.iterator();
        while (it.hasNext()) {
            add(it.next());
        } // end while

    } // end addAll

    /**
     * Adds the specified element to this set if it is not already present
     * (optional operation). If this set already contains the specified
     * element, the call leaves this set unchanged and return false.
     *
     * @param o element to be added to this set.
     * @return <code>true</code> if this set did not already contain
     * the specified element.
     */
    public boolean add(E o) {
        //logger.debug("IndexSet.put : " + o + "(" + count + ")");
        Entry entry = map.get(o);

        if (entry == null) {
            entry = new Entry(count++, 1);
            map.put(o, entry);
            return true;
        }

        entry.inc();
        return false;
    } // end add

    /**
     * Adds the specified element to this set if it is not already present
     * (optional operation). If this set already contains the specified
     * element, the call leaves this set unchanged.
     *
     * @param o element to be added to this set.
     * @return the element's <i>index</i> if this set contains the specified
     * element; -1 othewise.
     */
    /*public int add(E o)
	 {
	 //logger.debug("IndexSet.put : " + o + "(" + count + ")");
	 Entry entry = map.get(o);
	 
	 if (entry == null)
	 {			
	 entry = new Entry(count++, 1);
	 map.put(o, entry);
	 return entry.getIndex();
	 }
	 
	 entry.inc();
	 return entry.getIndex();
	 } // end add*/

    //
    public boolean contains(Object o) {
        Entry e = map.get(o);
        if (e == null) {
            return false;
        }

        return true;
    } // end contains

    //
    public boolean containsAll(Collection<?> c) {
        Iterator<E> it = iterator();
        boolean b = true;
        while (it.hasNext()) {
            b &= c.contains(it.next());
        } // end while

        return b;
    } // end retainAll
	
	/*
	 public Collection values()
	 {
	 return map.values();
	 } // end values
	 */

    //
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    } // end iterator

    //
    public int size() {
        return map.size();
    } // end size

    /**
     * Returns the element's <i>index</i> if this set contains the specified
     * element.
     *
     * @param o element to be added to this set.
     * @return the element's <i>index</i> if this index contains the specified
     * feature; -1 othewise.
     */
    public int getIndex(E o) {
        Entry entry = map.get(o);

        if (entry == null) {
            return -1;
        }

        return entry.getIndex();
    } // end getIndex

    //
    public Set<E> keySet() {
        return map.keySet();
    } // end featureSet

    //
    public void clear() {
        count = 0;
        map.clear();
    } // end clear

    //
    public Object[] toArray() {
        return map.keySet().toArray();
    } // end toArray

    //
    public <T> T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    } // end

    /**
     * Returns a <code>String</code> object representing this
     * <code>Word</code>.
     *
     * @return a string representation of this object.
     */
    public String toString1() {
        StringBuffer sb = new StringBuffer();

        E o = null;
        Entry entry = null;
        Iterator<E> it = map.keySet().iterator();
        while (it.hasNext()) {
            o = it.next();
            entry = map.get(o);
            sb.append(o);
            sb.append("\t");
            sb.append(entry);
            sb.append("\n");
        }
        return sb.toString();
    } // end toString1

    /**
     * Returns a <code>String</code> object representing this
     * <code>Word</code>.
     *
     * @return a string representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        SortedMap<Integer, String> smap = new TreeMap<Integer, String>();
        E o = null;
        Entry entry = null;
        Iterator<E> it = map.keySet().iterator();
        while (it.hasNext()) {
            o = it.next();
            entry = map.get(o);

            smap.put(entry.getFreq(), o + "\t" + entry);
        }

        Iterator<Integer> it1 = smap.keySet().iterator();
        int freq = 0;
        while (it1.hasNext()) {
            freq = it1.next();

            sb.append(smap.get(freq));
            sb.append("\n");
        }
        return sb.toString();

    } // end toString

    //
    class Entry {

        //
        private int index, freq;

        //
        Entry(int index, int freq) {
            this.index = index;
            this.freq = freq;
        } // end constructor

        //
        public int getIndex() {
            return index;
        } // end getIndex

        //
        public int getFreq() {
            return freq;
        } // end getFreq

        //
        public String toString() {
            return index + "\t" + freq;
        } // end getFreq

        //
        public void inc() {
            freq++;
        } // end inc
    } // end class Entry
} // end class IndexSet
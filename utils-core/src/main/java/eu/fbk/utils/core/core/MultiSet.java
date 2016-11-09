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

import java.util.*;

/**
 * This class provides a skeletal implementation of the <code>Set</code>
 * interface to minimize the effort required to implement a multi set.
 * A multiset (or bag) is a generalization of the notion of a set in
 * which elements are allowed to appear more than once.
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @see IndexSet
 * @since 1.0
 */
public abstract class MultiSet<E> implements Iterable<E>, Collection<E>, Set<E> {

    //
    protected Map<E, Counter> map;

    //
    protected int count;

    //
    protected MultiSet() {
        count = 0;
    } // end constructor

    //
    public void clear() {
        map.clear();
    } // end clear

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

    //
    public boolean add(E o, int freq) {
        count++;
        Counter c = map.get(o);
        if (c == null) {
            //unique++;
            map.put(o, new Counter(freq));
            return true;
        }

        c.inc(freq);
        return false;
    } // end add

    //
    public boolean add(E o) {
        count++;
        Counter c = map.get(o);
        if (c == null) {
            //unique++;
            map.put(o, new Counter(1));
            return true;
        }

        c.inc();
        return false;
    } // end add

    //
    public boolean contains(Object o) {
        Counter c = map.get(o);
        if (c == null) {
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
    public int getFrequency(E o) {
        //logger.debug("get: " + ngram + ", " + toChar(ngram));
        Counter c = map.get(o);
        if (c == null) {
            return 0;
        }

        return c.get();

    } // end get

    //
    public Object[] toArray() {
        return map.keySet().toArray();
    } // end toArray

    //
    public <T> T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    } // end

	/*
	 public static String toChar(String w)
	 {
	 StringBuilder sb = new StringBuilder();
	 int ch = 0;
	 for (int i=0;i<w.length();i++)
	 {
	 ch = w.charAt(i);
	 if (i > 0)
	 sb.append(" ");
	 sb.append(ch);
	 }
	 
	 sb.append("\n");
	 
	 for (int i=0;i<w.length();i++)
	 {
	 ch = w.charAt(i);
	 if (i > 0)
	 sb.append(" ");
	 sb.append((char) ch);
	 }		
	 return sb.toString();
	 } // end toChar
	 */

    /**
     * Returns the number of elements in the set.
     *
     * @return the number of elements in the set
     */
    public int size() {
        return map.size();
    } // end size

    /**
     * Returns the total number of items in the set.
     *
     * @return the total number of items in the set.
     * <p/>
     * public int count()
     * {
     * return unique;
     * } // end count
     */

    //
    public SortedMap<Integer, List<E>> toSortedMap() {
        SortedMap<Integer, List<E>> smap = new TreeMap<Integer, List<E>>(
                new Comparator<Integer>() {

                    public int compare(Integer e1, Integer e2) {
                        return e2.compareTo(e1);
                    }
                });

        Iterator<E> it = map.keySet().iterator();
        while (it.hasNext()) {
            E o = it.next();
            Counter c = map.get(o);
            List<E> list = smap.get(c.get());
            if (list == null) {
                list = new ArrayList<E>();
                list.add(o);
                smap.put(c.get(), list);
            } else {
                list.add(o);
            }
        }

        return smap;
    } // end toSortedMap

    //
    public String toString(int t) {
        SortedMap<Integer, List<E>> smap = toSortedMap();
        StringBuilder sb = new StringBuilder();
        //sb.append("(");
        double fc = 0, fi = 0;
        Iterator<Integer> it = smap.keySet().iterator();
        for (int i = 0; it.hasNext(); i++) {
            Integer f = it.next();

            List<E> list = smap.get(f);
            fi = (double) f / count;
            if (f.intValue() < t) {
                break;
            }
            fc += fi;
            sb.append(i);
            sb.append("\t");
            sb.append(f);
            sb.append("\t");
            sb.append(fi);
            sb.append("\t");
            sb.append(fc);
            sb.append("\t");
            sb.append(list);
            sb.append("\n");

        }
        //sb.append("...)");
        return sb.toString();

    } // end toString

    //
    public String toString() {
        SortedMap<Integer, List<E>> smap = toSortedMap();
        StringBuilder sb = new StringBuilder();
        //sb.append("(");
        double fc = 0, fi = 0;
        Iterator<Integer> it = smap.keySet().iterator();
        for (int i = 0; it.hasNext(); i++) {
            Integer f = it.next();
            List<E> list = smap.get(f);
            fi = ((double) f / count) * list.size();
            fc += fi;
            sb.append(i);
            sb.append("\t");
            sb.append(f);
            sb.append("\t");
            sb.append(fi);
            sb.append("\t");
            sb.append(fc);
            sb.append("\t");
            sb.append(list.size());
            sb.append("\t");
            sb.append(list);
            sb.append("\n");

        }
        //sb.append("...)");
        sb.append(map.size() + " unique elements (" + count + ")");
        sb.append("\n");
        return sb.toString();

    } // end toString

    //
    class Counter {

        //
        int count;

        //
        public Counter(int count) {
            this.count = count;
        } // end constructor

        //
        public void inc() {
            count++;
        } // end inc

        //
        public void inc(int l) {
            count += l;
        } // end inc

        //
        public int get() {
            return count;
        } // end get

        //
        public String toString() {
            return Integer.toString(count);
        } // end toString

    } // end class Counter

} // end class MultiSet
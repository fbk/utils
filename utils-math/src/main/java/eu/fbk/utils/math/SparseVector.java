/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.math;

import eu.fbk.utils.mylibsvm.svm_node;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * TODO
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public class SparseVector implements Vector {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>SparseVector</code>.
     */
    static Logger logger = Logger.getLogger(SparseVector.class.getName());

    //
    protected SortedMap<Integer, Float> map;

    //
    protected int size;

    //
    public SparseVector() {
        map = new TreeMap<Integer, Float>();
        size = 0;
    } // end constructor

    // TODO: replace s.split(" ")
    public static SparseVector parse(String s, float low, float high) {
        SparseVector vec = new SparseVector();

        String[] t = s.split(" ");
        for (int i = 0; i < t.length; i++) {
            String[] u = t[i].split(":");
            int index = Integer.parseInt(u[0]);
            float value = Float.parseFloat(u[1]);
            if ((value >= low) && (value < high)) {
                vec.add(index, value);
            }
        } // end for i

        return vec;
    } // end parse

    //
    public static SparseVector parse(String s) {
        SparseVector vec = new SparseVector();

        String[] t = s.split(" ");
        for (int i = 0; i < t.length; i++) {
            String[] u = t[i].split(":");
            int index = Integer.parseInt(u[0]);
            float value = Float.parseFloat(u[1]);
            vec.add(index, value);
        } // end for i

        return vec;
    } // end parse

    //
    public Vector merge(Vector v) {
        Vector m = new SparseVector();

        Iterator<Integer> it1 = nonZeroElements();
        int i = 0;
        while (it1.hasNext()) {
            i = it1.next();
            m.add(i, v.get(i));
        } // end while

        Iterator<Integer> it2 = v.nonZeroElements();
        while (it2.hasNext()) {
            i = it2.next();
            m.add(i + v.size(), v.get(i));
        } // end while

        return m;
    } // end merge

    //
    public void add(int index, float value) throws IndexOutOfBoundsException {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }

        if (index >= size) {
            size = index + 1;
        }

        Float currentValue = map.get(index);
        if (currentValue == null) {
            map.put(index, value);
            //logger.info("added1 " + index + ":" + value + ", size: " + size);
            return;
        }

        map.put(index, currentValue.floatValue() + value);

        //logger.info("added2 " + index + ":" + value + ", size: " + size);
        //return map.get(index);
    } // end add

    //
    public float get(int index) throws IndexOutOfBoundsException {
        //logger.info("get " + index);

        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        Float currentValue = map.get(index);
        if (currentValue != null) {
            return currentValue.floatValue();
        }

        return 0;

    } // end get

    //
    public boolean existsIndex(int index) {
        //logger.debug("existsIndex: " + index);
        if (map.get(index) != null) {
            return true;
        }

        return false;
    } // end existsIndex

    //
    public void set(int index, float value) throws IndexOutOfBoundsException {
        //logger.debug("set " + index + ", " + value);

        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }

        if (index > size) {
            size = index + 1;
        }

        Float currentValue = map.get(index);
        if (currentValue == null) {
            map.put(index, value);
            return;
        }

        map.put(index, value);
        return;

    } // end set

    //
    public int size() {
        return size;
    } // end size

    //
    public int elementCount() {
        return map.size();
    } // end elementCount

    //
    public Iterator<Float> iterator() {
        return map.values().iterator();
    } // end iterator

    //
    public boolean isSparse() {
        return true;
    } // end isSparse

    //
    public boolean isDense() {
        return false;
    } // end isDense

    //
    public Iterator<Integer> nonZeroElements() {
        return map.keySet().iterator();
    } // end indexes

    //
    public Vector copy() {
        //logger.info("SparseVector.merge");
        SparseVector vector = new SparseVector();

        Iterator<Integer> it = map.keySet().iterator();
        while (it.hasNext()) {
            Integer index = it.next();
            Float value = map.get(index);
            vector.add(index.intValue(), value.floatValue());
        } // end while

        return vector;
    } // end toString

    /*
    public Vector merge(Vector anotherVector)
    {
        //logger.info("SparseVector.merge");
        SparseVector vector = new SparseVector();


        Iterator it = map.keySet().iterator();
        while (it.hasNext())
        {
            Integer index = (Integer) it.next();
            Float value = map.get(index);

        } // end while

        return vector;
    } // end toString
    */
    //
    public String toString() {
        //logger.info("SparseVector.toString");

        StringBuilder sb = new StringBuilder();
        Iterator it = map.keySet().iterator();

        // first element
        if (it.hasNext()) {
            Integer index = (Integer) it.next();
            Float value = map.get(index);
            sb.append(index);
            sb.append(":");
            sb.append(value);
        } // end if

        while (it.hasNext()) {
            Integer index = (Integer) it.next();
            Float value = map.get(index);
            sb.append(" ");
            sb.append(index);
            sb.append(":");
            sb.append(value);
        } // end while

        return sb.toString();
    } // end toString

    //
    public String toString(int fromIndex) {
        //logger.info("SparseVector.toString");

        StringBuilder sb = new StringBuilder();
        Iterator it = map.keySet().iterator();

        // first element
        if (it.hasNext()) {
            Integer index = (Integer) it.next();
            Float value = map.get(index);
            sb.append(fromIndex + index);
            sb.append(":");
            sb.append(value);
        } // end if

        while (it.hasNext()) {
            Integer index = (Integer) it.next();
            Float value = map.get(index);
            sb.append(" ");
            sb.append(fromIndex + index);
            sb.append(":");
            sb.append(value);
        } // end while

        return sb.toString();
    } // end toString

    //
    public Node[] toNodeArray() {
        Node[] node = new Node[elementCount()];

        Iterator it = map.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Integer index = (Integer) it.next();
            Float value = map.get(index);
            node[i] = new Node();
            node[i].index = index.intValue();
            node[i].value = value.floatValue();
            i++;
        } // end while

        return node;
    } // end toNodeArray

    //
    public Node[] toNodeArray(int fromIndex) {
        Node[] node = new Node[elementCount()];

        Iterator it = map.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Integer index = (Integer) it.next();
            Float value = map.get(index);
            node[i] = new Node();
            node[i].index = fromIndex + index.intValue();
            node[i].value = value.floatValue();
            i++;
        } // end while

        return node;

    } // end toNodeArray

    //
    public svm_node[] toSvmNodeArray() {
        svm_node[] node = new svm_node[elementCount()];

        Iterator it = map.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Integer index = (Integer) it.next();
            Float value = map.get(index);
            node[i] = new svm_node();
            node[i].index = index.intValue();
            node[i].value = value.floatValue();
            i++;
        } // end while

        return node;
    } // end toSvmNodeArray

    //
    public svm_node[] toSvmNodeArray(int fromIndex) {
        svm_node[] node = new svm_node[elementCount()];

        Iterator it = map.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Integer index = (Integer) it.next();
            Float value = map.get(index);
            node[i] = new svm_node();
            node[i].index = fromIndex + index.intValue();
            node[i].value = value.floatValue();
            i++;
        } // end while

        return node;

    } // end toSvmNodeArray

    //
    public float dotProduct(SparseVector v) {
        //logger.info("dotProduct(SparseVector) " + v);
        Iterator<Integer> it = null;
        if (v.elementCount() < elementCount()) {
            it = v.nonZeroElements();
        } else {
            it = nonZeroElements();
        }
        float d = 0;

        while (it.hasNext()) {
            int index = it.next().intValue();
            try {
                float value1 = get(index);
                float value2 = v.get(index);
                d += value1 * value2;
            } catch (IndexOutOfBoundsException e) {
                // do nothing
            }
        } // end while

        return d;
    } // end dotProduct

    //
    public float dotProduct(Vector v) {
        //logger.debug("dotProduct(Vector) " + v);
        if (v.getClass() == this.getClass()) {
            return dotProduct((SparseVector) v);
        }

        Iterator<Integer> it = v.nonZeroElements();
        float d = 0;
        while (it.hasNext()) {
            int index = it.next().intValue();
            try {
                float value1 = get(index);
                float value2 = v.get(index);
                d += value1 * value2;
            } catch (IndexOutOfBoundsException e) {
                // do nothing
            }
        } // end while

        return d;
    } // end dotProduct

    //
    public float norm() {
        float norm = 0;

        Iterator<Integer> it = map.keySet().iterator();
        while (it.hasNext()) {
            Integer index = it.next();
            Float value = map.get(index);

            norm += Math.pow(value, 2);
        }

        return (float) Math.sqrt(norm);
    } // end norm

    //
    public void normalize() {
        float norm = norm();
        //logger.debug("norm before = " + norm);

        // CHECK THIS!
        if (norm == 0) {
            return;
        }

        Iterator<Integer> it = map.keySet().iterator();
        while (it.hasNext()) {
            Integer index = it.next();
            map.put(index, map.get(index).floatValue() / norm);
        }

        //logger.debug("norm after = " + norm());
    } // end normalize

    //
    public static void main(String args[]) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);
        // java eu.fbk.utils.lsa.math.SparseVector

        long begin = System.currentTimeMillis();
/*		
        SparseVector vec = new SparseVector();
		// java com.rt.util.SparseVector

		int max = 50000;
		for (int i=0;i<max;i++)
		{
			float r = Math.random();
			int j = (int) (r * max);
			//System.out.println(i + ":" + j + ":" + r);
			if (r < 0.0005)
				vec.add(i, r);
			
		} // end for i

		
		System.out.println(vec);
		System.out.println(vec.size());
	*/
        SparseVector v1 = new SparseVector();
        v1.add(0, 1);
        v1.add(1, 1);

        logger.info("v1.size: " + v1.size());
        logger.info("v1: " + v1);
        logger.info("v1.norm: " + v1.norm());

        v1.normalize();
        logger.info("n1: " + v1);
        logger.info("");

        SparseVector v2 = new SparseVector();
        v2.add(0, 1);
        logger.info(v2.size());
        v2.add(1, 1);
        v2.add(7, 1);
        v2.add(42, 1);
        v2.add(13, 1);
        v2.add(12, 1);
        v2.add(5, 1);
        v2.add(8, 1);
        v2.add(42, 1);
        logger.info(v2.size());

        logger.info("v2.size: " + v2.size());
        logger.info("v2: " + v2);
        logger.info("v2.norm: " + v2.norm());
        v2.normalize();
        logger.info("n2: " + v2);
        logger.info("");

        logger.info(v1 + " * " + v2 + " = " + v1.dotProduct(v2));

        float f1 = v1.dotProduct(v1);
        float f2 = v2.dotProduct(v2);
        float f = v1.dotProduct(v2) / (float) Math.sqrt(f1 * f2);
        logger.info("f = " + f);

        logger.info("size = " + v2.size());
        Iterator<Integer> it = v2.nonZeroElements();
        for (int j = 0; it.hasNext(); j++) {
            Integer index = it.next().intValue();
            Float value = v2.get(index.intValue());
            logger.info(index + ":" + value);
        }
        long end = System.currentTimeMillis();
        System.out.println("time " + (end - begin) + " ms");
    } // end main
} // end class SparseVector
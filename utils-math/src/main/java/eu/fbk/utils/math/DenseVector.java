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

/**
 * This class implements a dense vector.
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public class DenseVector implements Vector {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>DenseVector</code>.
     */
    static Logger logger = Logger.getLogger(DenseVector.class.getName());

    //
    protected float[] vector;

    //
    public DenseVector(int size) throws IllegalArgumentException {
        if (size <= 0) {
            throw new IllegalArgumentException();
        }

        vector = new float[size];
    } // end constructor

    //
    public DenseVector(float[] vector) {
        this.vector = vector;
    } // end constructor

    //
    public void add(int index, float value) throws IndexOutOfBoundsException {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }

        if (index > vector.length) {
            throw new IndexOutOfBoundsException();
        }

        vector[index] += value;
    } // end add

    //
    public float get(int index) throws IndexOutOfBoundsException {
        //logger.info("get " + index);

        if (index < 0 || index > vector.length) {
            throw new IndexOutOfBoundsException();
        }

        return vector[index];

    } // end get

    //
    public boolean isSparse() {
        return false;
    } // end isSparse

    //
    public boolean isDense() {
        return true;
    } // end isDense

    //
    public Vector merge(Vector v) {
        if (v.isDense()) {
            return mergeDense((DenseVector) v);
        }

        return mergeSparse((SparseVector) v);
    } // end merge

    //
    protected Vector mergeSparse(SparseVector v) {
        Vector m = new SparseVector();

        for (int i = 0; i < size(); i++) {
            m.add(i, get(i));
        }

        Iterator<Integer> it2 = v.nonZeroElements();
        int i = 0;
        while (it2.hasNext()) {
            i = it2.next();
            m.add(i + size(), v.get(i));
        } // end while

        return m;

    } // end mergeSparse

    //
    protected Vector mergeDense(DenseVector v) {
        Vector m = new DenseVector(size() + v.size());

        for (int i = 0; i < size(); i++) {
            m.add(i, get(i));
        }

        for (int i = 0; i < v.size(); i++) {
            m.add(i + size(), v.get(i));
        }
        return m;
    } // end mergeDense

    //
    public boolean existsIndex(int index) {
        //logger.debug("existsIndex: " + index);
        if (index >= 0 && index < vector.length) {
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

        if (index > vector.length) {
            throw new IndexOutOfBoundsException();
        }

        vector[index] = value;
    } // end set

    //
    public int size() {
        return vector.length;
    } // end vector.length

    //
    public int elementCount() {
        return size();
    } // end elementCount

    //
    public Iterator<Float> iterator() {
        return new ValueIterator(vector);
    } // end iterator

    //
    public Iterator<Integer> nonZeroElements() {
        return new IndexIterator(vector.length);
    } // end nonZeroElements

    //
    public String toString(int fromIndex) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < vector.length; i++) {
            if (vector[i] != 0) {
                if (i > 0) {
                    sb.append(" ");
                }

                sb.append(fromIndex + i);
                sb.append(":");
                sb.append(vector[i]);

            }

        } // end while

        return sb.toString();
    } // end toString

    //
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < vector.length; i++) {
            if (vector[i] != 0) {
                if (i > 0) {
                    sb.append(" ");
                }

                sb.append(i);
                sb.append(":");
                sb.append(vector[i]);

            }

        } // end while

        return sb.toString();
    } // end toString

    //
    public Node[] toNodeArray() {
        Node[] node = new Node[vector.length];

        for (int i = 0; i < vector.length; i++) {
            node[i] = new Node();
            node[i].index = i;
            node[i].value = vector[i];
        }

        return node;
    } // end toNodeArray

    //
    public Node[] toNodeArray(int fromIndex) {
        Node[] node = new Node[vector.length];

        for (int i = 0; i < vector.length; i++) {
            node[i] = new Node();
            node[i].index = fromIndex + i;
            node[i].value = vector[i];
        }

        return node;
    } // end toNodeArray

    //
    public svm_node[] toSvmNodeArray() {
        svm_node[] node = new svm_node[vector.length];

        for (int i = 0; i < vector.length; i++) {
            node[i] = new svm_node();
            node[i].index = i;
            node[i].value = vector[i];
        }

        return node;
    } // end toNodeArray

    //
    public svm_node[] toSvmNodeArray(int fromIndex) {
        svm_node[] node = new svm_node[vector.length];

        for (int i = 0; i < vector.length; i++) {
            node[i] = new svm_node();
            node[i].index = fromIndex + i;
            node[i].value = vector[i];
        }

        return node;
    } // end toNodeArray

    public float[] toArray() {
        return vector;
    } // end toArray

    //
    public float dotProduct(DenseVector anotherDenseVector) {
        //logger.info("dotProduct(DenseVector) " + anotherDenseVector);
        float[] anotherVector = anotherDenseVector.toArray();

        int len = vector.length;
        if (len > anotherVector.length) {
            len = anotherVector.length;
        }

        float d = 0;

        for (int i = 0; i < len; i++) {
            d += vector[i] * anotherVector[i];
        }

        return d;
    } // end dotProduct

    //
    public float dotProduct(Vector anotherVector) {
        //logger.info("dotProduct(Vector) " + anotherVector);
        if (anotherVector.getClass() == this.getClass()) {
            return dotProduct((DenseVector) anotherVector);
        }

        float d = 0;
        int len = vector.length;
        if (vector.length > anotherVector.size()) {
            Iterator<Integer> it = anotherVector.nonZeroElements();
            while (it.hasNext()) {
                int index = it.next().intValue();
                d += vector[index] * anotherVector.get(index);
            }
        } else {
            for (int i = 0; i < len; i++) {
                d += vector[i] * anotherVector.get(i);
            }

        }

        return d;
    } // end dotProduct

    //
    public float norm() {
        float norm = 0;

        for (int i = 0; i < vector.length; i++) {
            norm += Math.pow(vector[i], 2);
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

        for (int i = 0; i < vector.length; i++) {
            vector[i] /= norm;
        }

        //logger.debug("norm after = " + norm());
    } // end normalize

    //
    class ValueIterator implements Iterator {

        //
        private float[] vector;

        //
        private int i;

        //
        public ValueIterator(float[] vector) {
            this.vector = vector;
            i = 0;
        } // end constructor

        //
        public boolean hasNext() {
            return i < vector.length;
        } // end hasNext

        //
        public Object next() {
            return new Float(vector[i++]);
        } // end next

        //
        public void remove() {
            // do nothing
        } // end remove

    } // end IndexIterator

    //
    class IndexIterator implements Iterator {

        //
        private int len;

        //
        private int i;

        //
        public IndexIterator(int len) {
            this.len = len;
            i = 0;
        } // end constructor

        //
        public boolean hasNext() {
            return i < len;
        } // end hasNext

        public Object next() {
            return new Integer(i++);
        } // end next

        //
        public void remove() {
            // do nothing
        } // end remove

    } // end IndexIterator

    //
    public static void main(String args[]) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);

        long begin = System.currentTimeMillis();
/*		
        DenseVector vector = new DenseVector();
		// java org.fbk.it.hlt.termsim.DenseVector

		int max = 50000;
		for (int i=0;i<max;i++)
		{
			double r = Math.random();
			int j = (int) (r * max);
			//System.out.println(i + ":" + j + ":" + r);
			if (r < 0.0005)
				vector.add(i, r);
			
		} // end for i

		
		System.out.println(vector);
		System.out.println(vector.vector.length());
	*/

        DenseVector v1 = new DenseVector(2);
        v1.add(0, 1);
        v1.add(1, 1);

        logger.info("v1.size: " + v1.size());
        logger.info("v1: " + v1);
        logger.info("v1.norm: " + v1.norm());

        v1.normalize();
        logger.info("n1: " + v1);
        logger.info("");

        //DenseVector v2 = new DenseVector(43);
        SparseVector v2 = new SparseVector();
        v2.add(0, 1);
        v2.add(1, 1);
        v2.add(7, 1);
        v2.add(42, 1);
        v2.add(13, 1);
        v2.add(12, 1);
        v2.add(5, 1);
        v2.add(8, 1);
        v2.add(42, 1);

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

        long end = System.currentTimeMillis();
        System.out.println("time " + (end - begin) + " ms");
    } // end main
} // end class DenseVector

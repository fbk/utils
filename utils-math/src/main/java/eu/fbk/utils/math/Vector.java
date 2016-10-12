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

import java.util.Iterator;

/**
 * Interface for vectors holding float elements.
 * <p>
 * A vector has a growable number of cells (its size). Elements
 * are accessed via zero based indexes. Legal indexes are of the
 * form [0..size()-1]. Any attempt to access an element at a
 * coordinate index<0 || index>=size() will throw an
 * IndexOutOfBoundsException.
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public interface Vector {

    /**
     * Inserts the specified element at the specified position
     * in this vector. Shifts the element currently at that
     * position (if any) and any subsequent elements to the
     * right (adds one to their indices).
     *
     * @param index index at which the specified element
     *              is to be inserted.
     * @param value value to be inserted.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index < 0).
     */
    public abstract void add(int index, float value) throws IndexOutOfBoundsException;

    /**
     * Returns the element at the specified position in this vector.
     *
     * @param index index of element to return.
     * @return the element at the specified position in this vector.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index < 0 || index > size()).
     */
    public abstract float get(int index) throws IndexOutOfBoundsException;

    /**
     * Returns a boolean denoting whether this index already
     * exists in the vector.
     *
     * @param index index of element to return.
     * @return <code>true</code> if and only if the index exists; <code>false</code> otherwise
     */
    public abstract boolean existsIndex(int index) throws IndexOutOfBoundsException;

    /**
     * Replaces the element at the specified position in this vectro
     * with the specified element (optional operation).
     *
     * @param index index of element to return.
     * @param value value to be inserted.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index < 0 || index > size()).
     */
    public abstract void set(int index, float value) throws IndexOutOfBoundsException;

    /**
     * Returns the size of this vector.
     *
     * @return the size of this vector.
     */
    public abstract int size();

    /**
     * Returns the number of non-zero elements in this vector
     *
     * @return the number of non-zero elements in this vector.
     */
    public abstract int elementCount();

    /**
     * Returns an iterator over the elements in this vector in
     * proper sequence.
     *
     * @return an iterator over the elements in this vector
     * in proper sequence.
     */
    public abstract Iterator<Float> iterator();

    /**
     * Returns an iterator over the non-zero elements in this
     * vector in proper sequence.
     *
     * @return an iterator over the non-zero elements in this
     * vector in proper sequence.
     */
    public Iterator<Integer> nonZeroElements();

    /**
     * Returns an iterator over the elements in this vector in
     * proper sequence (optional operation).
     *
     * @return an iterator over the elements in this vector
     * in proper sequence.
     */
    public abstract float dotProduct(Vector v);

    /**
     * Returns the norm of this vector (optional operation).
     *
     * @return norm of this vector;
     */
    public abstract float norm();

    /**
     * Normalizes this vector (optional operation).
     */
    public abstract void normalize();

    //
    public abstract svm_node[] toSvmNodeArray(int fromIndex);

    //
    public abstract svm_node[] toSvmNodeArray();

    //
    public abstract Node[] toNodeArray(int fromIndex);

    //
    public abstract Node[] toNodeArray();

    //
    //public abstract void copy(Vector v);

    //
    public abstract boolean isSparse();

    //
    public abstract boolean isDense();

    //
    public abstract Vector merge(Vector v);

    //
    public abstract String toString(int fromIndex);

} // end interface Vector
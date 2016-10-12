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

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.Serializable;

public class FloatVector implements Serializable {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>FloatVector</code>.
     */
    static Logger logger = Logger.getLogger(FloatVector.class.getName());

    public int[] indexes;

    public static final char COLON = ':';

    public static final char SPACE = ' ';

    private static final long serialVersionUID = 42L;

    public float[] values;

    public FloatVector(int[] indexes, float[] values) {
        this.indexes = indexes;
        this.values = values;
    }

    public FloatVector(int size) {
        indexes = new int[size];
        values = new float[size];
    }

    public FloatVector(float[] values) {
        indexes = new int[values.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        this.values = values;
    }

    public int length() {
        return values.length;
    }

    public float dot(FloatVector y) {
        float sum = 0;
        int xlen = values.length;
        int ylen = y.length();
        int i = 0;
        int j = 0;
        while (i < xlen && j < ylen) {
            if (indexes[i] == y.indexes[j]) {
                sum += values[i++] * y.values[j++];
            } else {
                if (indexes[i] > y.indexes[j]) {
                    ++j;
                } else {
                    ++i;
                }
            }
        }
        return sum;
    }

    public void normalize() {
        float norm = norm();

        for (int i = 0; i < values.length; i++) {
            values[i] /= norm;
        }
    }

    public float norm() {
        float norm = 0;

        for (int i = 0; i < values.length; i++) {
            norm += Math.pow(values[i], 2);
        }

        return (float) Math.sqrt(norm);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (indexes.length > 0) {
            sb.append(indexes[0]);
            sb.append(COLON);
            sb.append(values[0]);

        }
        for (int i = 1; i < values.length; i++) {
            sb.append(SPACE);
            sb.append(indexes[i]);
            sb.append(COLON);
            sb.append(values[i]);
        }
        return sb.toString();
    }

    //
    public static void main(String args[]) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);
        // java eu.fbk.utils.lsa.math.SparseVector
    }
}

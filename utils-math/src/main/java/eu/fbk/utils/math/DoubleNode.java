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

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * A replacement for the class svm_node
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public class DoubleNode implements Serializable {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>DoubleNode</code>.
     */
    static Logger logger = Logger.getLogger(DoubleNode.class.getName());

    public int index;

    public static final char COLON = ':';

    private static final long serialVersionUID = 42L;

    //
    public double value;

    public DoubleNode() {
    }

    public DoubleNode(int index, double value) {
        this.index = index;
        this.value = value;
    }

    //
    static public void print(DoubleNode[] node) {
        if (node.length > 0) {
            logger.info(node[0].index + ":" + node[0].value);
        }
        for (int i = 1; i < node.length; i++) {
            logger.info(" " + node[i].index + ":" + node[i].value);
        }
        logger.info("\n");

    } // end print

    // factory method
    static public DoubleNode[] parse(String line) {
        StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
        int m = st.countTokens() / 2;
        DoubleNode[] x = new DoubleNode[m];
        for (int j = 0; j < m; j++) {
            x[j] = new DoubleNode();
            x[j].index = Integer.parseInt(st.nextToken());
            x[j].value = Double.valueOf(st.nextToken());
        }

        return x;
    } // end parse

    //
    static public String toString(DoubleNode[] node) {
        StringBuilder sb = new StringBuilder();
        if (node.length > 0) {
            sb.append(node[0].index);
            sb.append(COLON);
            sb.append(node[0].value);
        }
        for (int i = 1; i < node.length; i++) {
            sb.append(node[i].index);
            sb.append(COLON);
            sb.append(node[i].value);
        }
        //sb.append("\n");
        return sb.toString();
    } // end toString

    public static double dot(DoubleNode[] x, DoubleNode[] y) {
        double sum = 0;
        int xlen = x.length;
        int ylen = y.length;
        int i = 0;
        int j = 0;
        while (i < xlen && j < ylen) {
            if (x[i].index == y[j].index) {
                sum += x[i++].value * y[j++].value;
            } else {
                if (x[i].index > y[j].index) {
                    ++j;
                } else {
                    ++i;
                }
            }
        }
        return sum;
    }

    public static void normalize(DoubleNode[] x) {
        double norm = norm(x);

        for (int i = 0; i < x.length; i++) {
            x[i].value = x[i].value / norm;
        }
    }

    public static double norm(DoubleNode[] x) {
        double norm = 0;

        for (int i = 0; i < x.length; i++) {
            norm += Math.pow(x[i].value, 2);
        }

        return Math.sqrt(norm);
    }

} // end class DoubleNode

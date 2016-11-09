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
import java.util.Comparator;
import java.util.StringTokenizer;

/**
 * A replacement for the class svm_node
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public class Node implements Serializable, Comparator<Node>, Comparable<Node> {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>Node</code>.
     */
    static Logger logger = Logger.getLogger(Node.class.getName());

    //
    public int index;

    //
    public double value;

    //
    private static final long serialVersionUID = 42L;

    public Node() {
    }

    public Node(int index, double value) {
        this.index = index;
        this.value = value;
    }

    @Override
    public int compareTo(Node node) {
        double diff = node.index - index;
        if (diff > 0) {
            return -1;

        } else if (diff < 0) {
            return 1;
        }
        return 0;
    }

    //
    static public void print(Node[] node) {
        if (node.length > 0) {
            logger.info(node[0].index + ":" + node[0].value);
        }
        for (int i = 1; i < node.length; i++) {
            logger.info(" " + node[i].index + ":" + node[i].value);
        }
        logger.info("\n");

    } // end print

    // factory method
    static public Node[] parse(String line) {
        StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
        int m = st.countTokens() / 2;
        Node[] x = new Node[m];
        for (int j = 0; j < m; j++) {
            x[j] = new Node();
            x[j].index = Integer.parseInt(st.nextToken());
            x[j].value = Double.valueOf(st.nextToken());
        }

        return x;
    } // end parse

    //
    public String toString() {
        return index + ":" + value;
    }

    //
    static public String toString(Node[] node) {
        StringBuilder sb = new StringBuilder();
        if (node.length > 0) {
            sb.append(node[0].index + ":" + node[0].value);
        }
        for (int i = 1; i < node.length; i++) {
            sb.append(" " + node[i].index + ":" + node[i].value);
        }
        //sb.append("\n");
        return sb.toString();
    } // end toString

    //
    static public Node[] copyValueOf(byte[] b) {
        return null;
    } // end copyValueOf

    //
    static public byte[] toByteArray(Node[] node) {

        return null;
    } // end toByteArray

    public static double dot(Node[] x, Node[] y) {
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

    @Override
    public int compare(Node node, Node node2) {

        double diff = node2.index - node.index;
        if (diff > 0) {

            return -1;

        } else if (diff < 0) {

            return 1;
        }

        return 0;
    }

    public static void normalize(Node[] x, double norm) {

        for (int i = 0; i < x.length; i++) {
            x[i].value /= norm;
        }
    }

    public static void normalize(Node[] x) {
        double norm = norm(x);

        for (int i = 0; i < x.length; i++) {
            x[i].value = x[i].value / norm;
        }
    }

    public static double norm(Node[] x) {
        double norm = 0;

        for (int i = 0; i < x.length; i++) {
            norm += Math.pow(x[i].value, 2);
        }

        return Math.sqrt(norm);
    }
} // end class Node

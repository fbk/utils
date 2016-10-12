package eu.fbk.utils.mylibsvm;

import org.apache.log4j.Logger;

import java.util.StringTokenizer;

//
public class svm_node implements java.io.Serializable {

    //
    private static final long serialVersionUID = 5024396602591514749L;

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>svm_node</code>.
     */
    static Logger logger = Logger.getLogger(svm_node.class.getName());

    public int index;
    public double value;

    //
    static public void print(svm_node[] node) {
        if (node.length > 0) {
            logger.info(node[0].index + ":" + node[0].value);
        }
        for (int i = 1; i < node.length; i++) {
            logger.info(" " + node[i].index + ":" + node[i].value);
        }
        logger.info("\n");

    } // end print

    //
    static public svm_node[] parse(String line) {
        StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
        int m = st.countTokens() / 2;
        svm_node[] x = new svm_node[m];
        for (int j = 0; j < m; j++) {
            x[j] = new svm_node();
            x[j].index = Integer.parseInt(st.nextToken());
            x[j].value = Double.valueOf(st.nextToken());
        }

        return x;
    } // end parse

    //
    static public String toString(svm_node[] node, int k) {
        StringBuilder sb = new StringBuilder();
        if (node.length > 0) {
            sb.append((node[0].index + k) + ":" + node[0].value);
        }
        for (int i = 1; i < node.length; i++) {
            sb.append(" " + (node[i].index + k) + ":" + node[i].value);
        }
        //sb.append("\n");
        return sb.toString();
    } // end toString

    //
    static public String toString(svm_node[] node) {
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
    static public svm_node[] copyValueOf(byte[] b) {

        return null;
    } // end copyValueOf

    //
    static public byte[] toByteArray(svm_node[] node) {

        return null;
    } // end toByteArray

} // end class svn_node

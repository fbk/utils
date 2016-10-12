/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.lsa;

import eu.fbk.utils.math.Node;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Maps text into the latent semantic space.
 * <p>
 * This class is equals to LSA but uses Node instead of Vector.
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.1
 */
public class LSI extends AbstractLSI {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>LSI</code>.
     */
    static Logger logger = Logger.getLogger(LSI.class.getName());

    public LSI(String root, int dim, boolean rescaleIdf) throws IOException {
        super(root, dim, rescaleIdf);
    }

    public LSI(String root, int dim, boolean rescaleIdf, boolean normalize) throws IOException {
        super(root, dim, rescaleIdf, normalize);
    }

    public LSI(File UtFile, File SFile, File rowFile, File colFile, File dfFile, int dim, boolean rescaleIdf)
            throws IOException {
        super(UtFile, SFile, rowFile, colFile, dfFile, dim, rescaleIdf);
    }

    public LSI(File UtFile, File SFile, File rowFile, File colFile, File dfFile, int dim, boolean rescaleIdf,
            boolean normalize) throws IOException {
        super(UtFile, SFile, rowFile, colFile, dfFile, dim, rescaleIdf, normalize);
    }

    /**
     * Returns a term in the VSM
     */
    public Node[] mapTerm(String term) throws TermNotFoundException {
        int i = termIndex.get(term);

	    if (i == -1) {
		    throw new TermNotFoundException(term);
	    }

        Node[] nodes = new Node[Uk[i].length];
        for (int j = 0; j < Uk[i].length; j++) {
            nodes[j] = new Node(j, Uk[i][j]);
        }

        return nodes;
    } // end mapTerm

    /**
     * Returns a document in the VSM.
     */
    public Node[] mapDocumentOld(BOW bow, boolean b) {
        logger.info("mapDocument " + b);
        //Node[] nodes = new Node[bow.size()];
        List<Node> nodeList = new ArrayList<Node>();
        String term;

        int index;
        int tf;
        double tfIdf;
        Iterator<String> it = bow.termSet().iterator();
        for (int i = 0; it.hasNext(); i++) {
            term = it.next();
            index = termIndex.get(term);

            if (index != -1) {
                tf = bow.getFrequency(term);
                tfIdf = log2(tf);
                if (b) {
                    tfIdf *= Iidf[index];
                }

                logger.info(
                        "\t" + term + "\t" + index + "\ttf= " + tf + " (" + (log2(tf)) + ")\tidf=" + Iidf[index] + " ("
                                + tfIdf + ")");
                nodeList.add(new Node(index, tfIdf));
            } else {
                logger.debug(i + "\t" + term + "\t" + index);
            }
        } // end for

        Node[] nodes = nodeList.toArray(new Node[nodeList.size()]);

        Arrays.sort(nodes, new Comparator<Node>() {

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
        });

        return nodes;
    }

    /**
     * Returns a document in the VSM.
     */
    public Node[] mapDocument(BOW bow) {
        //logger.debug("mapDocument");
        SortedSet<Node> nodes = new TreeSet<Node>();
        String term;

        int index;
        //double tf;
        double tfIdf;
        Iterator<String> it = bow.termSet().iterator();
        for (int i = 0; it.hasNext(); i++) {
            term = it.next();
            index = termIndex.get(term);
            if (index != -1) {
                //tfIdf = bow.augmentedFrequency(term) * Iidf[index];
                //tfIdf = bow.logarithmicFrequency(term) * Iidf[index];
                tfIdf = bow.tf(term) * Iidf[index];
                //logger.info("\t"+term + "\t" + index + "\ttf= " + bow.augmentedFrequency(term) + "\tidf=" + Iidf[index] + " (" + tfIdf + ")");
                nodes.add(new Node(index, tfIdf));
            }
            //else {
            //	logger.debug(i+"\t"+term+"\t"+index);
            //}
        }
        return nodes.toArray(new Node[nodes.size()]);
    }

    public Node[] mapDocument(Map<String, Double> map) {
        //logger.debug("mapDocument");
        SortedSet<Node> nodes = new TreeSet<Node>();
        String term;
        int index;
        //double tf;
        double w;
        Iterator<String> it = map.keySet().iterator();
        for (int i = 0; it.hasNext(); i++) {
            term = it.next();
            index = termIndex.get(term);
            if (index != -1) {
                w = map.get(term);
                //logger.info("\t"+term + "\t" + index + "\ttf= " + bow.augmentedFrequency(term) + "\tidf=" + Iidf[index] + " (" + tfIdf + ")");
                nodes.add(new Node(index, w));
            }
        }
        return nodes.toArray(new Node[nodes.size()]);
    }

    /**
     * Returns a document in the VSM.
     */
    public Node[] mapDocumentOld(BOW bow) {
        return mapDocument(bow);
    }

    /**
     * Returns a document in the latent semantic space.
     */
    public Node[] mapPseudoDocument(Node[] doc) {
        //logger.info("mapPseudoDocument " + doc);
        // N = Uk.rows();
        //float[] pdoc = new float[Uk[0].length];
        Node[] nodes = new Node[Uk[0].length];
        //logger.info("Uk.size " + Uk.length + " X " + Uk[0].length);
        //logger.info("doc.size " + doc.size());
        //logger.info("pdoc.size " + pdoc.length);
        int index;
        for (int i = 0; i < Uk[0].length; i++) {
            //Iterator<Integer> it = doc.nonZeroElements();
            nodes[i] = new Node(i, 0);
            for (int j = 0; j < doc.length; j++) {
                //Integer index = it.next().intValue();
                index = doc[j].index;
                //logger.info(i + ", i: " + index);
                //logger.info(i + ", v:" + doc.get(index));
                //logger.info(i + ", Uk: " + Uk[index][i]);
                //pdoc[i] +=  Uk[index][i] * doc.get(index);
                nodes[i].value += Uk[index][i] * doc[j].value;
            } // end for j
        } // end for i

        //logger.info("pdoc.size " + pdoc.length);
        return nodes;
    }

    public double compare(String term1, String term2) throws TermNotFoundException {
        Node[] x1 = mapTerm(term1);
        Node[] x2 = mapTerm(term2);
        return Node.dot(x1, x2) / Math.sqrt(Node.dot(x1, x1) * Node.dot(x2, x2));
    }

    public double compare(BOW bow1, BOW bow2) {
        Node[] d1 = mapDocument(bow1);
        Node[] d2 = mapDocument(bow2);
        Node[] pd1 = mapPseudoDocument(d1);
        Node[] pd2 = mapPseudoDocument(d2);
        return Node.dot(pd1, pd2) / Math.sqrt(Node.dot(pd1, pd1) * Node.dot(pd2, pd2));
    }

    public void interactive() throws IOException {
        InputStreamReader reader = null;
        BufferedReader myInput = null;
        while (true) {
            logger.info("\nPlease write a query and type <return> to continue (CTRL C to exit):");
            reader = new InputStreamReader(System.in);
            myInput = new BufferedReader(reader);
            String query = myInput.readLine().toString();

            if (query.contains("\t")) {
                // compare two terms
                String[] s = query.split("\t");
                long begin = System.nanoTime();

                //BOW bow1 = new BOW(s[0].toLowerCase().replaceAll("category:", "_").split("[_ ]"));
                //BOW bow2 = new BOW(s[1].toLowerCase().replaceAll("category:", "_").split("[_ ]"));
                BOW bow1 = new BOW(s[0].toLowerCase());
                BOW bow2 = new BOW(s[1].toLowerCase());
                logger.info("bow1:" + bow1);
                logger.info("bow2:" + bow2);

                long end = System.nanoTime();
                logger.info("parsing time " + df.format(end - begin) + " ns");
                begin = System.nanoTime();
                Node[] d1 = mapDocument(bow1);
                logger.info("d1:" + Arrays.toString(d1));

                Node[] d2 = mapDocument(bow2);
                logger.info("d2:" + Arrays.toString(d2));

                Node[] pd1 = mapPseudoDocument(d1);
                logger.info("pd1:" + Arrays.toString(pd1));

                Node[] pd2 = mapPseudoDocument(d2);
                logger.info("pd2:" + Arrays.toString(pd2));

                double cosVSM = Node.dot(d1, d2) / Math.sqrt(Node.dot(d1, d1) * Node.dot(d2, d2));
                double cosLSM = Node.dot(pd1, pd2) / Math.sqrt(Node.dot(pd1, pd1) * Node.dot(pd2, pd2));
                end = System.nanoTime();
                logger.info("mapping time " + df.format(end - begin) + " ns");

                logger.info("<\"" + s[0] + "\",\"" + s[1] + "\"> = " + cosLSM + " (" + cosVSM + ")");

            } else {
                //return the similar terms

                try {
                    query = query.toLowerCase();
                    logger.debug("query " + query);
                    long begin = System.nanoTime();
                    ScoreTermMap map = new ScoreTermMap(query, 20);
                    Node[] vec1 = mapTerm(query);

                    String term = null;
                    Iterator<String> it = terms();
                    while (it.hasNext()) {
                        term = it.next();
                        Node[] vec2 = mapTerm(term);
                        double cos = Node.dot(vec1, vec2) / Math.sqrt(Node.dot(vec1, vec1) * Node.dot(vec2, vec2));
                        map.put(cos, term);
                    }
                    long end = System.nanoTime();

                    logger.info(map.toString());
                    logger.info("time required " + df.format(end - begin) + " ns");

                } catch (TermNotFoundException e) {
                    logger.error(e);
                }

            }

        } // end while(true)
    }

    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
	    if (logConfig == null) {
		    logConfig = "log-config.txt";
	    }

        long begin = System.currentTimeMillis();

        PropertyConfigurator.configure(logConfig);

        if (args.length != 5) {
            logger.info(getHelp());
            System.exit(1);
        }

        File Ut = new File(args[0] + "-Ut");
        File Sk = new File(args[0] + "-S");
        File r = new File(args[0] + "-row");
        File c = new File(args[0] + "-col");
        File df = new File(args[0] + "-df");
        double threshold = Double.parseDouble(args[1]);
        int size = Integer.parseInt(args[2]);
        int dim = Integer.parseInt(args[3]);
        boolean rescaleIdf = Boolean.parseBoolean(args[4]);

        LSI lsi = new LSI(Ut, Sk, r, c, df, dim, rescaleIdf);

        lsi.interactive();

        long end = System.currentTimeMillis();
        logger.info("term similarity calculated in " + (end - begin) + " ms");
    } // end main

    /**
     * Returns a command-line help.
     * <p>
     * return a command-line help.
     */
    private static String getHelp() {
        StringBuffer sb = new StringBuffer();

        // License
        //sb.append(License.get());

        // Usage
        sb.append("Usage: java -cp dist/jcore.jar -mx2G eu.fbk.utils.lsa.LSI input threshold size dim idf\n\n");

        // Arguments
        sb.append("Arguments:\n");
        sb.append("\tinput\t\t-> root of files from which to read the model\n");
        sb.append("\tthreshold\t-> similarity threshold\n");
        sb.append("\tsize\t\t-> number of similar terms to return\n");
        sb.append("\tdim\t\t-> number of dimensions\n");
        sb.append("\tidf\t\t-> if true rescale using the idf\n");
        //sb.append("\tterm\t\t-> input term\n");

        // Arguments
        //sb.append("Arguments:\n");

        return sb.toString();
    } // end getHelp
} // end class LSI

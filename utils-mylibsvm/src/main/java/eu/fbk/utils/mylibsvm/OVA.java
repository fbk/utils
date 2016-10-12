/*
 * Copyright (2011) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.mylibsvm;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * This class allows training a svn that used one-verus-all
 * multi class implementation.
 * The binary svm use the default livsvm all-vs-all implementation.
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 * @see svm_multiclass_predict
 */
public class OVA {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>OVA</code>.
     */
    static Logger logger = Logger.getLogger(OVA.class.getName());

    public OVA(File train, File test, String root, double w) throws IOException {
        double[] part = run(train, test, root, w);
    }

    // performs cross-validation
    public OVA(File dataset, int n, String root, double w) throws IOException {
        DecimalFormat decFormatter = new DecimalFormat("0.00");

        double[] res = new double[4];
        List<Pair> dataList = readDataset(dataset);
        for (int i = 0; i < n; i++) {
            logger.info("CROSS VALIDATION START " + i + "/" + n);
            File train = new File(dataset.getAbsolutePath() + "_train_" + n);
            File test = new File(dataset.getAbsolutePath() + "_test_" + n);
            split(dataList, train, test, i, n);
            double[] part = run(train, test, root, w);
            for (int k = 0; k < part.length; k++) {
                res[k] += part[k];
            }

            logger.info("CROSS VALIDATION END " + i + "/" + n);
        }

        double p = res[0] / (res[0] + res[1]);
        double r = res[0] / (res[0] + res[2]);
        double f1 = (2 * p * r) / (p + r);
        logger.info("tp\tfp\tfn\tsize\tp\tr\tf1");
        logger.info((int) res[0] + "\t" + (int) res[1] + "\t" + (int) res[2] + "\t" + (int) res[3] + "\t" + decFormatter
                .format(p) + "\t" + decFormatter.format(r) + "\t" + decFormatter.format(f1));

    }

    private void split(List<Pair> dataList, File train, File test, int j, int n) throws IOException {
        PrintWriter testWriter = new PrintWriter(new FileWriter(test));
        PrintWriter trainWriter = new PrintWriter(new FileWriter(train));

        for (int i = 0; i < dataList.size(); i++) {
            int k = i + j;
            if ((k % n) == 0) {
                testWriter.println(dataList.get(i).c + " " + dataList.get(i).e);
            } else {
                trainWriter.println(dataList.get(i).c + " " + dataList.get(i).e);
            }
        }
        testWriter.close();
        trainWriter.close();
    }

    public double[] run(File train, File test, String root, double w) throws IOException {
        List<Pair> trainList = readDataset(train);
        List<Pair> testList = readDataset(test);
        Set<String> set = classes(trainList);

        String[] y = new String[set.size()];
        List<List<Double[]>> d = new ArrayList<List<Double[]>>();
        /*double[] w = {
			2, 2.5, 1, 4, 2.5, 2.5, 2.5, 4, 4
		};*/
        Iterator<String> it = set.iterator();
        int i = 0;
        while (it.hasNext()) {
            String c = it.next();
            y[i] = c;
            logger.info(c);
            String trainFile = root + "_train_" + c;
            String testFile = root + "_test_" + c;
            String modelFile = root + "_mdl_" + c;
            String outFile = root + "_out_" + c;

            writeProblem(trainList, c, trainFile);
            writeProblem(testList, c, testFile);
            svm_train t = new svm_train();
            String[] argv = new String[8];
            argv[0] = "-t";
            argv[1] = "0";
            argv[2] = "-m";
            argv[3] = "2000";
            argv[4] = "-w1";
            //argv[5] =	 "2";
            argv[5] = new Double(w).toString();
            //argv[5] =	new Double(w[Integer.parseInt(c)-1]).toString();
            logger.info("class " + c + " => " + argv[5]);

            argv[6] = trainFile;
            argv[7] = modelFile;

            t.main(argv);

            svm_multiclass_predict p = new svm_multiclass_predict();
            String[] argc = new String[3];
            argc[0] = testFile;
            argc[1] = modelFile;
            argc[2] = outFile;
            p.main(argc);

            d.add(readOutput(new File(outFile)));
            i++;
        } // end while

        PrintWriter pw = new PrintWriter(new FileWriter(root + "_result"));
        List<Double> resList = new ArrayList<Double>();
        for (int j = 0; j < d.get(0).size(); j++) {
            int maxi = -1;
            double maxv = 0;
            for (int k = 0; k < y.length; k++) {
                //logger.info(j + ", " + k + ":" + y[k] + "\t" + d.get(k).get(j)[0] + "\t" + d.get(k).get(j)[1]);

                if (d.get(k).get(j)[0] > 0 && Math.abs(d.get(k).get(j)[1]) > maxv) {
                    maxi = k;
                    maxv = d.get(k).get(j)[1];
                }
            } // end for k
            if (maxi > -1) {
                //logger.info("res: " + y[maxi] + "\t" + maxv);
                pw.println(y[maxi] + "\t" + maxv);
                resList.add(new Double(y[maxi]));
            } else {
                //logger.info("res: 0\t0");
                pw.println("0\t0");
                resList.add(0.0);

            }
        }
        pw.close();

        return eval(resList, testList);
    }

    private double[] eval(List<Double> resList, List<Pair> testList) {
        DecimalFormat decFormatter = new DecimalFormat("0.00");
        int correct = 0;
        int total = 0;
        double tp = 0, fp = 0, fn = 0;

        double error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

        logger.info("eval " + resList.size() + ", " + testList.size());
        for (int i = 0; i < resList.size(); i++) {
            double target = Double.parseDouble(testList.get(i).c);
            double v = resList.get(i);

            if (v == target) {
                ++correct;
            }

            if (v == 0) {
                fn++;
            } else {
                if (v == target) {
                    tp++;
                } else {
                    fp++;
                    fn++;
                }

            }
            error += (v - target) * (v - target);
            sumv += v;
            sumy += target;
            sumvv += v * v;
            sumyy += target * target;
            sumvy += v * target;
            ++total;
        }

        logger.info("Accuracy = " + (double) correct / total * 100 +
                "% (" + correct + "/" + total + ") (classification)\n");

        double p = tp / (tp + fp);
        double r = tp / (tp + fn);
        double f1 = (2 * p * r) / (p + r);
        logger.info("===\ntp\tfp\tfn\tsize\tp\tr\tf1");
        logger.info((int) tp + "\t" + (int) fp + "\t" + (int) fn + "\t" + resList.size() + "\t" + decFormatter.format(p)
                + "\t" + decFormatter.format(r) + "\t" + decFormatter.format(f1) + "\n===");

        double[] res = new double[4];
        res[0] = tp;
        res[1] = fp;
        res[2] = fn;
        res[3] = total;
        return res;
    }

    private List<Double[]> readOutput(File f) throws IOException {
        List<Double[]> list = new ArrayList<Double[]>();
        LineNumberReader lr = new LineNumberReader(new FileReader(f));
        String line = null;

        while ((line = lr.readLine()) != null) {
            String[] s = line.split("\t");
            Double[] d = new Double[2];
            d[0] = Double.parseDouble(s[0]);
            d[1] = Double.parseDouble(s[1]);
            list.add(d);
			/*if (Double.parseDouble(s[0]) == 1)
				list.add(Double.parseDouble(s[1]));
			else
				list.add(0.0);*/
        }
        return list;

    }

    //
    class Pair {

        String c;
        String e;

        public Pair(String c, String e) {
            this.c = c;
            this.e = e;
        }

    }

    //
    private void writeProblem(List<Pair> list, String c, String name) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(name));

        for (int i = 0; i < list.size(); i++) {
            if (c.equals(list.get(i).c)) {
                pw.print("1");
            } else {
                pw.print("0");
            }

            pw.println(" " + list.get(i).e);
        } // end for i
        pw.close();
    }

    private Set<String> classes(List<Pair> list) {
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < list.size(); i++) {
            set.add(list.get(i).c);
        } // end for i
        return set;
    } //

    private List<Pair> readDataset(File f) throws IOException {
        List<Pair> list = new ArrayList<Pair>();
        LineNumberReader lr = new LineNumberReader(new FileReader(f));
        String line = null;

        while ((line = lr.readLine()) != null) {
            int i = line.indexOf(" ");
            String c = line.substring(0, i);
            String e = line.substring(i + 1, line.length());
            list.add(new Pair(c, e));
        }
        return list;
    }

    //
    public static void main(String args[]) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);
        //  time java -mx1G -cp dist/mylibsvm.jar:lib/log4j-1.2.8.jar mylibsvm.OVA
        // cross validation
        //new OVA(new File(args[0]), Integer.parseInt(args[1]), args[2], Double.parseDouble(args[3]));

        //
        new OVA(new File(args[0]), new File(args[1]), args[2], Double.parseDouble(args[3]));
    }
}

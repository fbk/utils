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
import java.util.StringTokenizer;

/**
 * This class is used by <code>OVA</code> to predict multiple
 * classes.
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 * @see OVA
 */
public class svm_multiclass_predict {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>svm_multiclass_predict</code>.
     */
    static Logger logger = Logger.getLogger(svm_multiclass_predict.class.getName());

    private static double atof(String s) {
        return Double.valueOf(s).doubleValue();
    }

    private static int atoi(String s) {
        return Integer.parseInt(s);
    }

    public static void predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability)
            throws IOException {
        DecimalFormat decFormatter = new DecimalFormat("0.00");
        double tp = 0, fp = 0, fn = 0, tn = 0;

        int correct = 0;
        int total = 0;
        double error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

        int svm_type = svm.svm_get_svm_type(model);
        int nr_class = svm.svm_get_nr_class(model);
        double[] prob_estimates = null;

        if (predict_probability == 1) {
            if (svm_type == svm_parameter.EPSILON_SVR ||
                    svm_type == svm_parameter.NU_SVR) {
                logger.info(
                        "Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="
                                + svm.svm_get_svr_probability(model) + "\n");
            } else {
                int[] labels = new int[nr_class];
                svm.svm_get_labels(model, labels);
                prob_estimates = new double[nr_class];
                output.writeBytes("labels");
                for (int j = 0; j < nr_class; j++) {
                    output.writeBytes(" " + labels[j]);
                }
                output.writeBytes("\n");
            }
        }
        while (true) {
            String line = input.readLine();
            if (line == null) {
                break;
            }

            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

            double target = atof(st.nextToken());
            int m = st.countTokens() / 2;
            svm_node[] x = new svm_node[m];
            for (int j = 0; j < m; j++) {
                x[j] = new svm_node();
                x[j].index = atoi(st.nextToken());
                x[j].value = atof(st.nextToken());
            }

            double v;
            if (predict_probability == 1 && (svm_type == svm_parameter.C_SVC || svm_type == svm_parameter.NU_SVC)) {
                v = svm.svm_predict_probability(model, x, prob_estimates);
                output.writeBytes(v + " ");
                for (int j = 0; j < nr_class; j++) {
                    output.writeBytes(prob_estimates[j] + " ");
                }
                output.writeBytes("\n");
            } else {
                // original
                /*double score = svm.svm_predict_score(model,x);
				
				if (score < 0)
					v = 1;
				else
					v = 0;*/
                double[] res = svm.svm_predict_score(model, x);
                v = res[1];
                //output.writeBytes(v+"\n");
                output.writeBytes(v + "\t" + res[0] + "\n");
            }

            if (v == target) {
                ++correct;
            }

            if (v == 0) {
                if (target == v) {
                    tn++;
                } else {
                    fn++;
                }
            } else {
                if (target == 0) {
                    fp++;
                } else if (target == v) {
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
        if (svm_type == svm_parameter.EPSILON_SVR ||
                svm_type == svm_parameter.NU_SVR) {
            logger.info("Mean squared error = " + error / total + " (regression)\n");
            logger.info("Squared correlation coefficient = " +
                    ((total * sumvy - sumv * sumy) * (total * sumvy - sumv * sumy)) /
                            ((total * sumvv - sumv * sumv) * (total * sumyy - sumy * sumy)) +
                    " (regression)\n");
        } else {
            double p = tp / (tp + fp);
            double r = tp / (tp + fn);
            double f1 = (2 * p * r) / (p + r);
            logger.info("Accuracy = " + (double) correct / total * 100 +
                    "% (" + correct + "/" + total + ") (classification)\n");
            logger.info("tp\tfp\tfn\tsize\tp\tr\tf1");
            logger.info(
                    (int) tp + "\t" + (int) fp + "\t" + (int) fn + "\t" + total + "\t" + decFormatter.format(p) + "\t"
                            + decFormatter.format(r) + "\t" + decFormatter.format(f1));

        }
    }

    private static void exit_with_help() {
        logger.error("usage: svm_multiclass_predict [options] test_file model_file output_file\n"
                + "options:\n"
                + "-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n");
        System.exit(1);
    }

    public static void main(String argv[]) throws IOException {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);

        int i, predict_probability = 0;

        // parse options
        for (i = 0; i < argv.length; i++) {
            if (argv[i].charAt(0) != '-') {
                break;
            }
            ++i;
            switch (argv[i - 1].charAt(1)) {
            case 'b':
                predict_probability = atoi(argv[i]);
                break;
            default:
                logger.error("Unknown option: " + argv[i - 1] + "\n");
                exit_with_help();
            }
        }
        if (i >= argv.length - 2) {
            exit_with_help();
        }
        try {
            BufferedReader input = new BufferedReader(new FileReader(argv[i]));
            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(argv[i + 2])));
            svm_model model = svm.svm_load_model(argv[i + 1]);
            if (predict_probability == 1) {
                if (svm.svm_check_probability_model(model) == 0) {
                    logger.error("Model does not support probabiliy estimates\n");
                    System.exit(1);
                }
            } else {
                if (svm.svm_check_probability_model(model) != 0) {
                    logger.info("Model supports probability estimates, but disabled in prediction.\n");
                }
            }
            predict(input, output, model, predict_probability);
            input.close();
            output.close();
        } catch (FileNotFoundException e) {
            exit_with_help();
        } catch (ArrayIndexOutOfBoundsException e) {
            exit_with_help();
        }
    }
}

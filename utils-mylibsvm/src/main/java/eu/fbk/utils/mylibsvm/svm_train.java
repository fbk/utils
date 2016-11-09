package eu.fbk.utils.mylibsvm;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.util.Vector;

public class svm_train {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>svm_train</code>.
     */
    static Logger logger = Logger.getLogger(svm_train.class.getName());

    private final static DecimalFormat df = new DecimalFormat(".00");

    private svm_parameter param;    // set by parse_command_line
    private svm_problem prob;    // set by read_problem
    private svm_model model;
    private String input_file_name;    // set by parse_command_line
    private String model_file_name;    // set by parse_command_line
    private String error_msg;
    private int cross_validation;
    private int nr_fold;

    private static void exit_with_help() {
        //java eu.fbk.utils.mylibsvm.svm_train
        logger.info(
                "Usage: svm_train [options] training_set_file [model_file]\n"
                        + "options:\n"
                        + "-s svm_type : set type of SVM (default 0)\n"
                        + "	0 -- C-SVC\n"
                        + "	1 -- nu-SVC\n"
                        + "	2 -- one-class SVM\n"
                        + "	3 -- epsilon-SVR\n"
                        + "	4 -- nu-SVR\n"
                        + "-t kernel_type : set type of kernel function (default 2)\n"
                        + "	0 -- linear: u'*v\n"
                        + "	1 -- polynomial: (gamma*u'*v + coef0)^degree\n"
                        + "	2 -- radial basis function: exp(-gamma*|u-v|^2)\n"
                        + "	3 -- sigmoid: tanh(gamma*u'*v + coef0)\n"
                        + "	4 -- precomputed kernel (kernel values in training_set_file)\n"
                        + "-d degree : set degree in kernel function (default 3)\n"
                        + "-g gamma : set gamma in kernel function (default 1/k)\n"
                        + "-r coef0 : set coef0 in kernel function (default 0)\n"
                        + "-c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)\n"
                        + "-n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)\n"
                        + "-p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)\n"
                        + "-m cachesize : set cache memory size in MB (default 100)\n"
                        + "-e epsilon : set tolerance of termination criterion (default 0.001)\n"
                        + "-h shrinking : whether to use the shrinking heuristics, 0 or 1 (default 1)\n"
                        + "-b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)\n"
                        + "-wi weight : set the parameter C of class i to weight*C, for C-SVC (default 1)\n"
                        + "-v n : n-fold cross validation mode\n"
                        + "-q : quiet mode (no outputs)\n"
        );
        System.exit(1);
    }

    private void do_cross_validation() {
        double tp = 0, fp = 0, fn = 0, tn = 0;
        int i;
        int total_correct = 0;
        double total_error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
        double[] target = new double[prob.l];
        double p = 0, r = 0, f1 = 0;

        int[][] confusionMatrix = new int[3][3];

        svm.svm_cross_validation(prob, param, nr_fold, target);
        if (param.svm_type == svm_parameter.EPSILON_SVR ||
                param.svm_type == svm_parameter.NU_SVR) {
            for (i = 0; i < prob.l; i++) {
                double y = prob.y[i];
                double v = target[i];
                total_error += (v - y) * (v - y);
                sumv += v;
                sumy += y;
                sumvv += v * v;
                sumyy += y * y;
                sumvy += v * y;
            }
            logger.info("Cross Validation Mean squared error = " + total_error / prob.l + "\n");
            logger.info("Cross Validation Squared correlation coefficient = " +
                    ((prob.l * sumvy - sumv * sumy) * (prob.l * sumvy - sumv * sumy)) /
                            ((prob.l * sumvv - sumv * sumv) * (prob.l * sumyy - sumy * sumy)) + "\n"
            );
        } else {
            for (i = 0; i < prob.l; i++) {
                if (target[i] == prob.y[i]) {
                    ++total_correct;
                }
                double y = prob.y[i];
                double v = target[i];
                if (v == 0) {
                    if (y == v) {
                        tn++;
                    } else {
                        fn++;
                    }
                } else {
                    if (y == 0) {
                        fp++;
                    } else if (y == v) {
                        tp++;
                    } else {
                        fp++;
                        fn++;
                    }
                }

                confusionMatrix[rescale((int) target[i])][rescale((int) prob.y[i])]++;
            }

            p = tp / (tp + fp);
            r = tp / (tp + fn);
            f1 = (2 * p * r) / (p + r);
            logger.info("Cross Validation Accuracy = " + 100.0 * total_correct / prob.l + "%\n");
            logger.info(tp + "\t" + fp + "\t" + fn + "\t" + df.format(p) + "\t" + df.format(r) + "\t" + df.format(f1));

            confusionMatrixToString(confusionMatrix);
            pr(confusionMatrix);

        }

    }

    int rescale(int a) {

        switch (a) {
        case 5:
            return 0;
        case 3:
            return 1;
        case 1:
            return 2;
        }
        return 0;
    }

    public String confusionMatrixToString(int[][] confusionMatrix) {

        for (int j = 0; j < confusionMatrix.length; j++) {
            System.out.print("\t" + (j - 1));
        }
        System.out.print("\n");
        for (int j = 0; j < confusionMatrix.length; j++) {
            System.out.print((j - 1));
            int sum = 0;
            for (int k = 0; k < confusionMatrix[j].length; k++) {
                System.out.print("\t" + confusionMatrix[j][k]);
                sum += confusionMatrix[j][k];
            }
            System.out.println("\t" + sum);
        }
        int tot = 0;
        for (int j = 0; j < confusionMatrix.length; j++) {
            int sum = 0;
            for (int k = 0; k < confusionMatrix[j].length; k++) {
                sum += confusionMatrix[k][j];
            }
            tot += sum;
            System.out.print("\t" + sum);
        }
        /*int sum=0;
		for (int j = 0; j < confusionMatrix.length; j++) {
			sum+=confusionMatrix[j][j];
		}*/
        System.out.print("\t" + tot + "\n");

        return null;
    }

    public void pr(int[][] confusionMatrix) {
        int[] row = new int[confusionMatrix.length];
        for (int j = 0; j < confusionMatrix.length; j++) {

            for (int k = 0; k < confusionMatrix[j].length; k++) {
                row[j] += confusionMatrix[j][k];
            }
        }
        int[] col = new int[confusionMatrix.length];
        for (int j = 0; j < confusionMatrix.length; j++) {
            int sum = 0;
            for (int k = 0; k < confusionMatrix[j].length; k++) {
                sum += confusionMatrix[k][j];
            }
            col[j] += sum;
        }

        double sp = 0;
        double sr = 0;

        System.out.println("c\tp\tr\tf1");

        for (int j = 0; j < confusionMatrix.length; j++) {
            double p = (double) confusionMatrix[j][j] / row[j];
            sp += p;
            double r = (double) confusionMatrix[j][j] / col[j];
            sr += r;
            double f1 = (2 * p * r) / (p + r);
            System.out.println((j - 1) + "\t" + df.format(p) + "\t" + df.format(r) + "\t" + df.format(f1));

        }
        sp /= confusionMatrix.length;
        sr /= confusionMatrix.length;
        double sf1 = (2 * sp * sr) / (sp + sr);
        System.out.println("macro\t" + df.format(sp) + "\t" + df.format(sr) + "\t" + df.format(sf1));

    }

    private void run(String argv[]) throws IOException {
        parse_command_line(argv);
        read_problem();
        error_msg = svm.svm_check_parameter(prob, param);

        if (error_msg != null) {
            logger.error("Error: " + error_msg + "\n");
            System.exit(1);
        }

        if (cross_validation != 0) {
            do_cross_validation();
        } else {
            model = svm.svm_train(prob, param);
            svm.svm_save_model(model_file_name, model);
        }
    }

    public static void main(String argv[]) throws IOException {

        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }
        System.out.println("ciao");
        PropertyConfigurator.configure(logConfig);

        svm_train t = new svm_train();
        t.run(argv);
    }

    private static double atof(String s) {
        double d = Double.valueOf(s).doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            logger.error("NaN or Infinity in input\n");
            System.exit(1);
        }
        return (d);
    }

    private static int atoi(String s) {
        return Integer.parseInt(s);
    }

    private void parse_command_line(String argv[]) {
        int i;

        param = new svm_parameter();
        // default values
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.degree = 3;
        param.gamma = 0;  // 1/k
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 100;
        param.C = 1;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];
        cross_validation = 0;

        // parse options
        for (i = 0; i < argv.length; i++) {
            if (argv[i].charAt(0) != '-') {
                break;
            }
            if (++i >= argv.length) {
                exit_with_help();
            }
            switch (argv[i - 1].charAt(1)) {
            case 's':
                param.svm_type = atoi(argv[i]);
                break;
            case 't':
                param.kernel_type = atoi(argv[i]);
                break;
            case 'd':
                param.degree = atoi(argv[i]);
                break;
            case 'g':
                param.gamma = atof(argv[i]);
                break;
            case 'r':
                param.coef0 = atof(argv[i]);
                break;
            case 'n':
                param.nu = atof(argv[i]);
                break;
            case 'm':
                param.cache_size = atof(argv[i]);
                break;
            case 'c':
                param.C = atof(argv[i]);
                break;
            case 'e':
                param.eps = atof(argv[i]);
                break;
            case 'p':
                param.p = atof(argv[i]);
                break;
            case 'h':
                param.shrinking = atoi(argv[i]);
                break;
            case 'b':
                param.probability = atoi(argv[i]);
                break;
            case 'q':
                svm.svm_print_string = new svm_print_interface() {

                    public void print(String s) {
                    }
                };
                i--;
                break;
            case 'v':
                cross_validation = 1;
                nr_fold = atoi(argv[i]);
                if (nr_fold < 2) {
                    logger.error("n-fold cross validation: n must >= 2\n");
                    exit_with_help();
                }
                break;
            case 'w':
                ++param.nr_weight;
            {
                int[] old = param.weight_label;
                param.weight_label = new int[param.nr_weight];
                System.arraycopy(old, 0, param.weight_label, 0, param.nr_weight - 1);
            }

            {
                double[] old = param.weight;
                param.weight = new double[param.nr_weight];
                System.arraycopy(old, 0, param.weight, 0, param.nr_weight - 1);
            }

            param.weight_label[param.nr_weight - 1] = atoi(argv[i - 1].substring(2));
            param.weight[param.nr_weight - 1] = atof(argv[i]);
            break;
            default:
                logger.error("Unknown option: " + argv[i - 1] + "\n");
                exit_with_help();
            }
        }

        // determine filenames

        if (i >= argv.length) {
            exit_with_help();
        }

        input_file_name = argv[i];

        if (i < argv.length - 1) {
            model_file_name = argv[i + 1];
        } else {
            int p = argv[i].lastIndexOf('/');
            ++p;  // whew...
            model_file_name = argv[i].substring(p) + ".model";
        }
    }

    // read in a problem (in svmlight format)

    private void read_problem() throws IOException {
        BufferedReader fp = new BufferedReader(new FileReader(input_file_name));
        Vector<Double> vy = new Vector<Double>();
        Vector<svm_node[]> vx = new Vector<svm_node[]>();
        int max_index = 0;

        while (true) {
            String line = fp.readLine();
            if (line == null) {
                break;
            }

            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

            vy.addElement(atof(st.nextToken()));
            int m = st.countTokens() / 2;
            svm_node[] x = new svm_node[m];
            for (int j = 0; j < m; j++) {
                x[j] = new svm_node();
                x[j].index = atoi(st.nextToken());
                x[j].value = atof(st.nextToken());
            }
            if (m > 0) {
                max_index = Math.max(max_index, x[m - 1].index);
            }
            vx.addElement(x);
        }

        prob = new svm_problem();
        prob.l = vy.size();
        prob.x = new svm_node[prob.l][];
        for (int i = 0; i < prob.l; i++) {
            prob.x[i] = vx.elementAt(i);
        }
        prob.y = new double[prob.l];
        for (int i = 0; i < prob.l; i++) {
            prob.y[i] = vy.elementAt(i);
        }

        if (param.gamma == 0 && max_index > 0) {
            param.gamma = 1.0 / max_index;
        }

        if (param.kernel_type == svm_parameter.PRECOMPUTED) {
            for (int i = 0; i < prob.l; i++) {
                if (prob.x[i][0].index != 0) {
                    logger.error("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
                    System.exit(1);
                }
                if ((int) prob.x[i][0].value <= 0 || (int) prob.x[i][0].value > max_index) {
                    logger.error("Wrong input format: sample_serial_number out of range\n");
                    System.exit(1);
                }
            }
        }

        fp.close();
    }
}

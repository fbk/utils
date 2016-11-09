package eu.fbk.utils.eval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by alessio on 04/02/16.
 */

public class ApproximateRandomization {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApproximateRandomization.class);
    private static DecimalFormat formatter = new DecimalFormat("###,###.####");

    private static void swap(double[] y, double[] z, Random rdm) {
        //logger.info("swap");
        int count = 0;
        for (int i = 0; i < y.length; i++) {
            double p = rdm.nextDouble();
            if (p < 0.5) {
                double t = y[i];
                y[i] = z[i];
                z[i] = t;
                count++;
            }
        } // end for i

        //logger.info("swapped " + count + " out of " + y.length);
    } // end swap

    static double calculateScore(double[] tmp) {
        double sum = 0.0;
        for (double v : tmp) {
            sum += v;
        }

        return sum / tmp.length;
    }

    static double test(int iterations, double[] a, double b[]) {
        // p - p-value. In general, the lowest the p-value,
        // the less probable it is that that the null
        // hypothesis holds. That is, the two systems are
        // are significantly different.

        double bs = calculateScore(a);
        double ps = calculateScore(b);
        double d = Math.abs(ps - bs);
        LOGGER.debug(
                "Original score bs, ps, d: " + formatter.format(bs * 100) + "%, " + formatter.format(ps * 100) + "%, "
                        + formatter.format(d * 100) + "%");

        double p = 0;
        double mean = 0;
        double variance = 0;
        double sum = 0;
        double ssum = 0;

        // c - number of times that the pseudostatistic is
        // greater or equal to the true statistic
        int c = 0;
        for (int i = 0; i < iterations; i++) {
            double[] x = new double[a.length];
            double[] y = new double[b.length];
//            Evaluator[] baselineEvalCopy = new Evaluator[baselineEval.length];
//            Evaluator[] preferredEvalCopy = new Evaluator[preferredEval.length];

            System.arraycopy(a, 0, x, 0, a.length);
            System.arraycopy(b, 0, y, 0, b.length);

            swap(x, y, new Random(i * 123));
            bs = calculateScore(x);
            ps = calculateScore(y);

            double di = Math.abs(ps - bs);
            sum += di;
            ssum += Math.pow(di, 2);
            //logger.debug("score at " + i + " bs, ps,d: " + formatter.format(bs) + ", " + formatter.format(ps) + ", " + formatter.format(di) + ", (" + formatter.format(d) + ")");

            if (di >= d) {
                c++;
            }

        } // end for i

        mean = sum / iterations;
        variance = (iterations * ssum - Math.pow(sum, 2)) / iterations * (iterations - 1);

        p = (double) (c + 1) / (iterations + 1);

        LOGGER.debug("Mean: " + mean + ", " + Math.sqrt(variance));
        LOGGER.debug(p + " = (" + c + " + 1) / (" + iterations + " + 1)");

        return p;
    }

    public static void main(String[] args) {
        double[] a = { 0.1, 0.2, 0.4, 0.34, 0.7 };
        double[] b = { 1.2, 1.1, 1.89, 1.7, 1.99 };
        swap(a, b, new Random(1234));
        System.out.println(Arrays.toString(a));
        System.out.println(Arrays.toString(b));
    }
}

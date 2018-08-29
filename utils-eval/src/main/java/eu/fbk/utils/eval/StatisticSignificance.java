package eu.fbk.utils.eval;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import eu.fbk.utils.core.CommandLine;
import org.apache.commons.math3.stat.inference.TTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alessio on 04/02/16.
 */

public class StatisticSignificance {

//    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticSignificance.class);
    private static NumberFormat formatter = new DecimalFormat("#0.00000");

    public static double[] convertDoubles(List<Double> doubles) {
        double[] ret = new double[doubles.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = doubles.get(i).doubleValue();
        }
        return ret;
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./student-test")
                    .withHeader("Apply Student's t-test to a file with two-column doubles")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false,
                            true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);

            File[] files;
            if (inputFile.isDirectory()) {
                files = inputFile.listFiles();
            } else {
                files = new File[1];
                files[0] = inputFile;
            }

            for (File file : files) {
                List<String> lines = Files.readLines(file, Charsets.UTF_8);

                List<Double> c1 = new ArrayList<>();
                List<Double> c2 = new ArrayList<>();

                for (String line : lines) {
                    line = line.trim();
                    if (line.length() == 0) {
                        continue;
                    }

                    String[] parts = line.split("\\s+");

                    double x = Double.parseDouble(parts[0]);
                    double y = Double.parseDouble(parts[1]);
                    c1.add(x);
                    c2.add(y);
                }

                double[] c1d = convertDoubles(c1);
                double[] c2d = convertDoubles(c2);

//                LOGGER.trace(Arrays.toString(c1d));
//                LOGGER.trace(Arrays.toString(c2d));

                TTest test = new TTest();
                double p = test.pairedTTest(c1d, c2d);
                double r = ApproximateRandomization.test(1000, c1d, c2d);
                System.out.println(formatter.format(p));
                System.out.println(formatter.format(r));
//                LOGGER.info("{} ---> t-test {}", file.getName(), formatter.format(p));
//                LOGGER.info("{} ---> appr-rand {}", file.getName(), formatter.format(r));
//                LOGGER.debug("");
            }

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}

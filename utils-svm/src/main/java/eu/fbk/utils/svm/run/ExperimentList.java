package eu.fbk.utils.svm.run;

import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.eval.ConfusionMatrix;
import eu.fbk.utils.svm.Classifier;
import eu.fbk.utils.svm.LabelledVector;
import eu.fbk.utils.svm.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alessio on 26/04/17.
 */

public class ExperimentList {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentList.class);

    public static void main(String[] args) {
        try {

            final CommandLine cmd = CommandLine.parser().withName("experiment-list")
                    .withOption("i", "vectors", "Input file with vectors", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("e", "experiments", "Input file with experiments", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File outputFile = cmd.getOptionValue("output", File.class);
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            File vectorFile = cmd.getOptionValue("vectors", File.class);
            List<String> lines;
            lines = Files.readAllLines(vectorFile.toPath());

            HashMap<String, LabelledVector> vectorIndex = new HashMap<>();
            List<LabelledVector> vectors = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.split("\\s+");
                final Vector.Builder builder = eu.fbk.utils.svm.Vector.builder();

                String id = parts[0];
                Integer label = Integer.parseInt(parts[1]);
                for (int i = 2; i < parts.length; i++) {
                    String part = parts[i];
                    String[] splitted = part.split(":");
                    String featName = "feat" + splitted[0];
                    Float featValue = Float.parseFloat(splitted[1]);
                    builder.set(featName, featValue);
                }
                LabelledVector vector = builder.setID(id).build().label(label);
                vectors.add(vector);
                vectorIndex.put(id, vector);
            }

            File experimentsFile = cmd.getOptionValue("experiments", File.class);
            List<String> configLines = Files.readAllLines(experimentsFile.toPath());
            for (String configLine : configLines) {
                configLine = configLine.trim();
                if (configLine.startsWith("#")) {
                    continue;
                }
                String[] configParts = configLine.split("\\s+");

                Integer numLabels = Integer.parseInt(configParts[1]);

                Float c = null;
                try {
                    c = Float.parseFloat(configParts[2]);
                } catch (Exception e) {
                    // ignored
                }
                Float gamma = null;
                try {
                    gamma = Float.parseFloat(configParts[3]);
                } catch (Exception e) {
                    // ignored
                }

                int numWeights = configParts.length - 4;
                if (numWeights != numLabels) {
                    throw new Exception("Incoherent information about weights");
                }

                float[] weights = new float[numWeights];
                for (int i = 4; i < configParts.length; i++) {
                    weights[i - 4] = Float.parseFloat(configParts[i]);
                }

                Classifier.Parameters parameters;
                String type = configParts[0];
                if (type.equals("0")) {
                    parameters = Classifier.Parameters.forSVMLinearKernel(numLabels, weights, c);
                } else if (type.equals("1")) {
                    parameters = Classifier.Parameters.forSVMPolyKernel(numLabels, weights, c, gamma, null, null);
                } else if (type.equals("2")) {
                    parameters = Classifier.Parameters.forSVMRBFKernel(numLabels, weights, c, gamma);
                } else {
                    throw new Exception("No type specified");
                }

                HashMap<String, Integer> results = new HashMap<>();
                ConfusionMatrix confusionMatrix = Classifier.crossValidate(parameters, vectors, 10, results);
                writer.append(configLine).append("\n");
                writer.append(confusionMatrix.toString()).append("\n");
                writer.append(Integer.toString(results.size())).append("\n");
                for (String key : results.keySet()) {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(key).append("\t").append(vectorIndex.get(key).getLabel()).append("\t").append(results.get(key));
                    writer.append(buffer.toString()).append("\n");
                }
            }

            writer.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}

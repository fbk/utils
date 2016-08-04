package eu.fbk.utils.svm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import eu.fbk.utils.core.Dictionary;
import eu.fbk.utils.eval.ConfusionMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_print_interface;
import libsvm.svm_problem;

import eu.fbk.rdfpro.util.Environment;
import eu.fbk.rdfpro.util.Hash;
import eu.fbk.rdfpro.util.IO;
import eu.fbk.rdfpro.util.Statements;

public abstract class Classifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(Classifier.class);

    private final Parameters parameters;

    private final String modelHash;

    private Classifier(final Parameters parameters, final String modelHash) {
        this.parameters = parameters;
        this.modelHash = modelHash;
    }

    public Parameters getParameters() {
        return this.parameters;
    }

    public final LabelledVector predict(final boolean withProbabilities, final Vector vector) {
        if (!getParameters().getAlgorithm().supportsProbabilities()) {
            throw new IllegalArgumentException("Probabilities not supported by algorithm "
                    + getParameters().getAlgorithm());
        }
        return doPredict(withProbabilities, vector);
    }

    public final List<LabelledVector> predict(final boolean withProbabilities,
            final Iterable<? extends Vector> vectors) {
        if (withProbabilities && !getParameters().getAlgorithm().supportsProbabilities()) {
            throw new IllegalArgumentException("Probabilities not supported by algorithm "
                    + getParameters().getAlgorithm());
        }
        return doPredict(withProbabilities, vectors);
    }

    abstract LabelledVector doPredict(final boolean withProbabilities, final Vector vector);

    List<LabelledVector> doPredict(final boolean withProbabilities,
            final Iterable<? extends Vector> vectors) {

        // simple implementation; could be overridden
        final ImmutableList.Builder<LabelledVector> builder = ImmutableList.builder();
        for (final Vector vector : vectors) {
            builder.add(doPredict(withProbabilities, vector));
        }
        return builder.build();
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object.getClass() != this.getClass()) {
            return false;
        }
        final Classifier other = (Classifier) object;
        return this.modelHash.equals(other.modelHash);
    }

    @Override
    public final int hashCode() {
        return this.modelHash.hashCode();
    }

    @Override
    public final String toString() {
        return this.parameters.getNumLabels() + "-classes " + getClass().getSimpleName()
                + ", model " + this.modelHash;
    }

    public final void writeTo(final Path path) throws IOException {

        final Path p = Util.openVFS(path, true);
        try {
            // Create entry map, storing parameters in entry 'parameters'
            final Properties properties = this.parameters.toProperties(null, null);
            try (BufferedWriter writer = Files.newBufferedWriter(p.resolve("parameters"))) {
                properties.store(writer, "");
            }

            // Store custom entries
            doWrite(p);

        } finally {
            Util.closeVFS(p);
        }
    }

    void doWrite(final Path path) throws IOException {
        // can be overridden by subclasses
    }

    public static Classifier readFrom(final Path path) throws IOException {

        final Path p = Util.openVFS(path, false);
        try {
            // Extract the parameters from entry 'parameters'
            final Properties properties = new Properties();
            try (BufferedReader reader = Files.newBufferedReader(p.resolve("parameters"))) {
                properties.load(reader);
            }
            final Parameters parameters = Parameters.forProperties(properties, null);

            // Select the implementation class and delegate to its doRead() static method
            final Class<? extends Classifier> implementationClass = implementationFor(parameters);
            if (implementationClass.equals(LibLinearClassifier.class)) {
                return LibLinearClassifier.doRead(parameters, p);
            } else if (implementationClass.equals(LibSvmClassifier.class)) {
                return LibSvmClassifier.doRead(parameters, p);
            } else {
                throw new IllegalArgumentException("No suitable implementation for parameters "
                        + parameters);
            }

        } finally {
            Util.closeVFS(p);
        }
    }

    public static Classifier train(final Parameters parameters,
            final Iterable<LabelledVector> trainingSet) throws IOException {

        Preconditions.checkArgument(Iterables.size(trainingSet) > 0, "No training examples");

        final Class<? extends Classifier> implementationClass = implementationFor(parameters);
        if (implementationClass.equals(LibLinearClassifier.class)) {
            return LibLinearClassifier.doTrain(parameters, trainingSet);
        } else if (implementationClass.equals(LibSvmClassifier.class)) {
            return LibSvmClassifier.doTrain(parameters, trainingSet);
        } else {
            throw new IllegalArgumentException("No suitable implementation for parameters "
                    + parameters);
        }
    }

    public static Classifier train(final Iterable<Parameters> parametersGrid,
            final Iterable<LabelledVector> trainingSet,
            final Comparator<ConfusionMatrix> comparator, final int maxVectors) throws IOException {

        Preconditions.checkArgument(Iterables.size(trainingSet) > 0, "No training examples");

        final List<List<LabelledVector>> partitions = Vector.split(trainingSet, 3, maxVectors);
        final List<Parameters> parametersList = ImmutableList.copyOf(parametersGrid);
        final Map<ConfusionMatrix, Parameters> map = Maps.newHashMap();

        final AtomicInteger index = new AtomicInteger(0);
        final List<ListenableFuture<?>> futures = Lists.newArrayList();
        final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Environment
                .getPool());
        for (int i = 0; i < Environment.getCores() / 2; ++i) {
            futures.add(executor.submit(new Callable<Object>() {

                @Override
                public Object call() throws IOException {
                    while (true) {
                        final int i = index.getAndIncrement();
                        if (i >= parametersList.size()) {
                            return null;
                        }
                        final Parameters parameters = parametersList.get(i);
                        final ConfusionMatrix matrix = crossValidate(parameters, partitions);
                        synchronized (map) {
                            map.put(matrix, parameters);
                        }
                        LOGGER.debug("Performances for parameters combination {}/{} - {}:\n{}",
                                i + 1, parametersList.size(), parameters, matrix);
                    }
                }

            }));
        }
        Futures.get(Futures.allAsList(futures), IOException.class);

        final ConfusionMatrix bestMatrix = Ordering.from(comparator).min(map.keySet());
        final Parameters bestParameters = map.get(bestMatrix);
        LOGGER.debug("Best parameter combination: {}", bestParameters);
        final Classifier bestClassifier = train(bestParameters, Ordering.natural()
                .immutableSortedCopy(trainingSet));
        return bestClassifier;
    }

    public static ConfusionMatrix crossValidate(final Parameters parameters,
            final Iterable<LabelledVector> trainingSet, final int numPartitions,
            final int maxVectors) throws IOException {

        Preconditions.checkArgument(numPartitions >= 2);
        Preconditions.checkArgument(Iterables.size(trainingSet) > 0, "No training examples");
        return crossValidate(parameters, Vector.split(trainingSet, numPartitions, maxVectors));
    }

    public static ConfusionMatrix crossValidate(final Parameters parameters,
            final Iterable<? extends Iterable<LabelledVector>> partitions) throws IOException {

        int size = 0;
        for (final Iterable<LabelledVector> partition : partitions) {
            size += Iterables.size(partition);
        }

        final List<Iterable<LabelledVector>> partitionList = ImmutableList.copyOf(partitions);
        final List<ListenableFuture<ConfusionMatrix>> futures = Lists.newArrayList();

        final ListeningExecutorService executor = size > 200000 ? MoreExecutors
                .newDirectExecutorService() : MoreExecutors.listeningDecorator(Environment
                .getPool());

        for (int i = 0; i < partitionList.size(); ++i) {
            final int index = i;
            futures.add(executor.submit(new Callable<ConfusionMatrix>() {

                @Override
                public ConfusionMatrix call() throws IOException {
                    final Iterable<LabelledVector> testSet = partitionList.get(index);
                    final List<LabelledVector> trainingSet = Lists.newArrayList();
                    for (int j = 0; j < partitionList.size(); ++j) {
                        if (j != index) {
                            Iterables.addAll(trainingSet, partitionList.get(j));
                        }
                    }
                    final Classifier classifier = train(parameters, trainingSet);
                    final List<LabelledVector> predictedSet = classifier.predict(false, testSet);
                    return LabelledVector.evaluate(testSet, predictedSet,
                            parameters.getNumLabels());
                }

            }));
        }

        return ConfusionMatrix.sum(Futures.get(Futures.allAsList(futures), IOException.class));
    }

    @Nullable
    private static Class<? extends Classifier> implementationFor(final Parameters parameters) {
        if (parameters.getAlgorithm().isLinear()) {
            return LibLinearClassifier.class;
        } else if (parameters.getAlgorithm().isSVM()) {
            return LibSvmClassifier.class;
        } else {
            return null;
        }
    }

    private static String computeHash(final Dictionary<String> dictionary, final String modelString) {
        final StringBuilder builder = new StringBuilder(modelString);
        for (final String feature : dictionary) {
            builder.append('\n').append(feature);
        }
        return Hash.murmur3(builder).toString();
    }

    private static boolean testNative(final String program, final String successMessage) {
        try {
            invokeNative(program, ImmutableList.of(), true);
            LOGGER.info(successMessage);
            return true;
        } catch (final Throwable ex) {
            return false;
        }
    }

    private static Map<String, Float> invokeNative(final String program,
            final Iterable<String> args, final boolean suppressOutput) throws IOException {

        // Invoke LIBSVM
        final List<String> command = new ArrayList<String>(Arrays.asList(Environment.getProperty(
                "cmd." + program.replace('-', '.'), program).split("\\s+")));
        Iterables.addAll(command, args);
        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        // processBuilder.environment().put("OMP_NUM_THREADS",
        // Integer.toString(Environment.getCores()));
        final Process process = processBuilder.start();

        try {
            // Launch a task to log STDERR at ERROR level
            Environment.getPool().submit(new Runnable() {

                @Override
                public void run() {
                    final BufferedReader in = new BufferedReader(new InputStreamReader(process
                            .getErrorStream(), Charset.forName("UTF-8")));
                    try {
                        String line;
                        while ((line = in.readLine()) != null) {
                            LOGGER.error("[{}] {}", program, line);
                        }
                    } catch (final Throwable ex) {
                        // ignore
                    } finally {
                        IO.closeQuietly(in);
                    }
                }

            });

            // Log LIBSVM output at DEBUG level and parse key = value bindings in it
            final Map<String, Float> result = Maps.newHashMap();
            final BufferedReader in = new BufferedReader(new InputStreamReader(
                    process.getInputStream(), Charset.forName("UTF-8")));
            String line;
            while ((line = in.readLine()) != null) {
                if (!suppressOutput) {
                    LOGGER.debug("[{}] {}", program, line);
                }
                try {
                    int startIndex = -1;
                    int equalIndex = -1;
                    for (int i = 0; i < line.length(); ++i) {
                        final char c = line.charAt(i);
                        if (equalIndex >= 0) {
                            final boolean last = i == line.length() - 1;
                            final boolean nonDigit = c != ' ' && c != '.' && c != '-'
                                    && !Character.isDigit(c);
                            if (last || nonDigit) {
                                final int endIndex = last && !nonDigit ? i + 1 : i;
                                final String key = line.substring(startIndex, equalIndex).trim();
                                final float value = Float.parseFloat(line.substring(
                                        equalIndex + 1, endIndex).trim());
                                result.put(key, value);
                                startIndex = -1;
                                equalIndex = -1;
                            }
                        } else if (c == '=' && i > 0 && line.charAt(i - 1) != '<'
                                && line.charAt(i - 1) != '=') {
                            equalIndex = i;
                        } else if (c == ',') {
                            startIndex = -1;
                        } else if (startIndex < 0) {
                            startIndex = i;
                        }
                    }
                } catch (final Throwable ex) {
                    LOGGER.warn("Could not parse output line:\n" + line, ex);
                }
            }
            return result;

        } finally {
            // Kill the process
            process.destroy();
        }
    }

    public static final class Parameters {

        private static final Float DEFAULT_C = 1.0f;

        private static final Float DEFAULT_BIAS = -1.0f;

        private static final Boolean DEFAULT_DUAL = Boolean.TRUE;

        private static final Float DEFAULT_COEFF = 0.0f;

        private static final Integer DEFAULT_DEGREE = 3;

        private static final Float DEFAULT_GAMMA = 1f;

        private final Algorithm algorithm;

        private final int numLabels;

        @Nullable
        private final float[] weights;

        private final float c;

        @Nullable
        private final Float bias;

        @Nullable
        private final Boolean dual;

        @Nullable
        private final Float gamma;

        @Nullable
        private final Float coeff;

        @Nullable
        private final Integer degree;

        private Parameters(final Algorithm algorithm, final int numLabels,
                @Nullable final float[] weights, @Nullable final Float c,
                @Nullable final Float bias, @Nullable final Boolean dual,
                @Nullable final Float gamma, @Nullable final Float coeff,
                @Nullable final Integer degree) {

            Preconditions.checkNotNull(algorithm);
            Preconditions.checkArgument(numLabels >= 2);
            Preconditions.checkArgument(weights == null || weights.length == numLabels);
            Preconditions.checkArgument(c == null || c > 0.0f);
            Preconditions.checkArgument(degree == null || degree >= 1);

            this.algorithm = algorithm;
            this.numLabels = numLabels;
            this.weights = weights;
            this.c = c != null ? c : DEFAULT_C;

            this.bias = algorithm.isLinear() ? bias != null ? bias : DEFAULT_BIAS : null;
            this.dual = algorithm == Algorithm.LINEAR_L2LOSS_L2REG
                    || algorithm == Algorithm.LINEAR_LRLOSS_L2REG ? dual != null ? dual
                    : DEFAULT_DUAL : null;
            this.gamma = algorithm == Algorithm.SVM_POLY_KERNEL
                    || algorithm == Algorithm.SVM_RBF_KERNEL
                    || algorithm == Algorithm.SVM_SIGMOID_KERNEL ? gamma != null ? gamma
                    : DEFAULT_GAMMA : null;
            this.coeff = algorithm == Algorithm.SVM_POLY_KERNEL
                    || algorithm == Algorithm.SVM_SIGMOID_KERNEL ? coeff != null ? coeff
                    : DEFAULT_COEFF : null;
            this.degree = algorithm == Algorithm.SVM_POLY_KERNEL ? degree != null ? degree
                    : DEFAULT_DEGREE : null;
        }

        public static Parameters forLinearLRLossL1Reg(final int numLabels,
                @Nullable final float[] weights, @Nullable final Float c,
                @Nullable final Float bias) {
            return new Parameters(Algorithm.LINEAR_LRLOSS_L1REG, numLabels, weights, c, bias,
                    null, null, null, null);
        }

        public static Parameters forLinearLRLossL2Reg(final int numLabels,
                @Nullable final float[] weights, @Nullable final Float c,
                @Nullable final Float bias, @Nullable final Boolean dual) {
            return new Parameters(Algorithm.LINEAR_LRLOSS_L2REG, numLabels, weights, c, bias,
                    dual, null, null, null);
        }

        public static Parameters forLinearL2LossL1Reg(final int numLabels,
                @Nullable final float[] weights, @Nullable final Float c,
                @Nullable final Float bias) {
            return new Parameters(Algorithm.LINEAR_L2LOSS_L1REG, numLabels, weights, c, bias,
                    null, null, null, null);
        }

        public static Parameters forLinearL2LossL2Reg(final int numLabels,
                @Nullable final float[] weights, @Nullable final Float c,
                @Nullable final Float bias, @Nullable final Boolean dual) {
            return new Parameters(Algorithm.LINEAR_L2LOSS_L2REG, numLabels, weights, c, bias,
                    dual, null, null, null);
        }

        public static Parameters forLinearL1LossL2Reg(final int numLabels,
                @Nullable final float[] weights, @Nullable final Float c,
                @Nullable final Float bias) {
            return new Parameters(Algorithm.LINEAR_L1LOSS_L2REG, numLabels, weights, c, bias,
                    null, null, null, null);
        }

        public static Parameters forSVMLinearKernel(final int numLabels,
                @Nullable final float[] weights, @Nullable final Float c) {
            return new Parameters(Algorithm.SVM_LINEAR_KERNEL, numLabels, weights, c, null, null,
                    null, null, null);
        }

        public static Parameters forSVMRBFKernel(final int numLabels,
                @Nullable final float[] weights, @Nullable final Float c,
                @Nullable final Float gamma) {
            return new Parameters(Algorithm.SVM_RBF_KERNEL, numLabels, weights, c, null, null,
                    gamma, null, null);
        }

        public static Parameters forSVMSigmoidKernel(final int numLabels,
                @Nullable final float[] weights, @Nullable final Float c,
                @Nullable final Float gamma, @Nullable final Float coeff) {
            return new Parameters(Algorithm.SVM_RBF_KERNEL, numLabels, weights, c, null, null,
                    gamma, coeff, null);
        }

        public static Parameters forSVMPolyKernel(final int numLabels,
                @Nullable final float[] weights, @Nullable final Float c,
                @Nullable final Float gamma, @Nullable final Float coeff,
                @Nullable final Integer degree) {
            return new Parameters(Algorithm.SVM_RBF_KERNEL, numLabels, weights, c, null, null,
                    gamma, coeff, degree);
        }

        public static Parameters forProperties(final Properties properties,
                @Nullable final String prefix) {
            final Properties p = properties;
            final String pr = prefix != null ? prefix : "";
            final Algorithm algorithm = Algorithm.valueOf(p.getProperty(pr + "algorithm")
                    .toUpperCase());
            final int numLabels = Integer.parseInt(p.getProperty(pr + "numLabels"));
            final float c = Float.parseFloat(p.getProperty(pr + "c"));
            final Float bias = Statements.convert(p.getProperty(pr + "bias"), Float.class);
            final Boolean dual = Statements.convert(p.getProperty(pr + "dual"), Boolean.class);
            final Float gamma = Statements.convert(p.getProperty(pr + "gamma"), Float.class);
            final Float coeff = Statements.convert(p.getProperty(pr + "coeff"), Float.class);
            final Integer degree = Statements.convert(p.getProperty(pr + "degree"), Integer.class);
            float[] weights = null;
            if (p.containsKey(pr + "weight.0")) {
                weights = new float[numLabels];
                for (int i = 0; i < numLabels; ++i) {
                    weights[i] = Statements
                            .convert(p.getProperty(pr + "weight." + i), Float.class);
                }
            }
            return new Parameters(algorithm, numLabels, weights, c, bias, dual, gamma, coeff,
                    degree);
        }

        public Algorithm getAlgorithm() {
            return this.algorithm;
        }

        public int getNumLabels() {
            return this.numLabels;
        }

        @Nullable
        public float[] getWeights() {
            return this.weights == null ? null : this.weights.clone();
        }

        public float getC() {
            return this.c;
        }

        @Nullable
        public Float getBias() {
            return this.bias;
        }

        @Nullable
        public Boolean getDual() {
            return this.dual;
        }

        @Nullable
        public Float getGamma() {
            return this.gamma;
        }

        @Nullable
        public Integer getDegree() {
            return this.degree;
        }

        @Nullable
        public Float getCoeff() {
            return this.coeff;
        }

        public List<Parameters> grid(final int maxCombinations, final float multiplier) {

            // Enumerate C values
            final int nc = this.gamma == null && (this.bias == null || this.bias < 0) ? maxCombinations
                    : (int) Math.sqrt(maxCombinations);
            final List<Float> cs = Lists.newArrayList(this.c);
            float m = multiplier;
            while (cs.size() < nc && m <= 10000000) {
                if (this.c * m <= 100000) {
                    cs.add(this.c * m);
                }
                if (cs.size() < nc && this.c / m >= 0.01) {
                    cs.add(this.c / m);
                }
                m *= 10f;
            }

            // Enumerate bias values
            final List<Float> biases = Lists.newArrayList(this.bias);
            m = 10f;
            if (this.bias != null && this.bias > 0) {
                final int nbias = maxCombinations / cs.size();
                while (biases.size() < nbias && m <= 1000000) {
                    if (this.bias * m <= 1000) {
                        biases.add(this.bias * m);
                    }
                    if (biases.size() < nbias && this.bias / m >= 0.001f) {
                        biases.add(this.bias / m);
                    }
                    m *= 10f;
                }
            }

            // Enumerate gamma values
            final List<Float> gammas = Lists.newArrayList(this.gamma);
            m = 10f;
            if (this.gamma != null) {
                final int ngamma = maxCombinations / cs.size();
                while (gammas.size() < ngamma && m <= 1000000) {
                    if (this.gamma * m <= 10) {
                        gammas.add(this.gamma * m);
                    }
                    if (gammas.size() < ngamma && this.gamma / m >= 0.00001f) {
                        gammas.add(this.gamma / m);
                    }
                    m *= 10f;
                }
            }

            // Build and return the parameters grid
            final List<Parameters> result = Lists.newArrayList(this);
            for (final float c : cs) {
                for (final Float bias : biases) {
                    for (final Float gamma : gammas) {
                        result.add(new Parameters(this.algorithm, this.numLabels, this.weights, c,
                                bias, this.dual, gamma, this.coeff, this.degree));
                    }
                }
            }
            return result;
        }

        @Override
        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof Parameters)) {
                return false;
            }
            final Parameters other = (Parameters) object;
            return this.algorithm == other.algorithm && this.numLabels == other.numLabels
                    && Arrays.equals(this.weights, other.weights) && this.c == other.c
                    && Objects.equal(this.bias, other.bias)
                    && Objects.equal(this.dual, other.dual)
                    && Objects.equal(this.gamma, other.gamma)
                    && Objects.equal(this.coeff, other.coeff)
                    && Objects.equal(this.degree, other.degree);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.algorithm, this.numLabels, Arrays.hashCode(this.weights),
                    this.c, this.bias, this.dual, this.gamma, this.coeff, this.degree);
        }

        @Override
        public String toString() {
            return MoreObjects
                    .toStringHelper(this.algorithm.toString().toLowerCase() + " classifier")
                    .omitNullValues().add("#labels", this.numLabels)
                    .add("weights", Arrays.toString(this.weights)).add("C", this.c)
                    .add("bias", this.bias).add("dual", this.dual).add("gamma", this.gamma)
                    .add("coeff", this.coeff).add("degree", this.degree).toString();
        }

        public Properties toProperties(@Nullable final Properties properties,
                @Nullable final String prefix) {
            final Properties p = properties != null ? properties : new Properties();
            final String pr = prefix != null ? prefix : "";
            p.setProperty(pr + "algorithm", this.algorithm.toString());
            p.setProperty(pr + "numLabels", Integer.toString(this.numLabels));
            p.setProperty(pr + "c", Float.toString(this.c));
            for (int i = 0; i < (this.weights == null ? 0 : this.weights.length); ++i) {
                p.setProperty(pr + "weight." + i, Float.toString(this.weights[i]));
            }
            if (this.bias != null) {
                p.setProperty(pr + "bias", Float.toString(this.bias));
            }
            if (this.dual != null) {
                p.setProperty(pr + "dual", Boolean.toString(this.dual));
            }
            if (this.gamma != null) {
                p.setProperty(pr + "gamma", Float.toString(this.gamma));
            }
            if (this.coeff != null) {
                p.setProperty(pr + "coeff", Float.toString(this.coeff));
            }
            if (this.degree != null) {
                p.setProperty(pr + "degree", Integer.toString(this.degree));
            }
            return p;
        }

    }

    public enum Algorithm {

        LINEAR_LRLOSS_L1REG(true, true, false),

        LINEAR_LRLOSS_L2REG(true, true, false),

        LINEAR_L2LOSS_L1REG(false, true, false),

        LINEAR_L2LOSS_L2REG(false, true, false),

        LINEAR_L1LOSS_L2REG(false, true, false),

        SVM_LINEAR_KERNEL(true, false, true),

        SVM_RBF_KERNEL(true, false, true),

        SVM_SIGMOID_KERNEL(true, false, true),

        SVM_POLY_KERNEL(true, false, true);

        private boolean supportsProbabilities;

        private boolean linear;

        private boolean svm;

        private Algorithm(final boolean supportsProbabilities, final boolean linear,
                final boolean svm) {
            this.supportsProbabilities = supportsProbabilities;
            this.linear = linear;
            this.svm = svm;
        }

        public boolean supportsProbabilities() {
            return this.supportsProbabilities;
        }

        public boolean isLinear() {
            return this.linear;
        }

        public boolean isSVM() {
            return this.svm;
        }

    }

    private static class LibLinearClassifier extends Classifier {

        private static final Ordering<Feature> FEATURE_ORDERING = new Ordering<Feature>() {

            @Override
            public int compare(final Feature left, final Feature right) {
                return left.getIndex() - right.getIndex();
            }

        };

        private static final boolean NATIVE_LIB_AVAILABLE = testNative("train",
                "Using native LIBLINEAR tools");

        private final Dictionary<String> dictionary;

        private final Model model;

        private LibLinearClassifier(final Parameters parameters, final String modelHash,
                final Dictionary<String> dictionary, final Model model) {
            super(parameters, modelHash);
            this.dictionary = Preconditions.checkNotNull(dictionary);
            this.model = model;
        }

        @Override
        LabelledVector doPredict(final boolean withProbabilities, final Vector vector) {
            final Feature[] features = encodeVector(this.dictionary, vector);
            if (withProbabilities) {
                final int numLabels = getParameters().getNumLabels();
                final double[] p = new double[numLabels];
                final int label = (int) Linear.predictProbability(this.model, features, p);
                final float[] probabilities = new float[numLabels];
                for (int i = 0; i < p.length; ++i) {
                    final int labelIndex = this.model.getLabels()[i];
                    probabilities[labelIndex] = (float) p[i];
                }
                return vector.label(label, probabilities);
            } else {
                final double label = Linear.predict(this.model, features);
                return vector.label((int) label);
            }
        }

        @Override
        void doWrite(final Path path) throws IOException {

            // Write the dictionary
            this.dictionary.writeTo(path.resolve("dictionary"));

            // Write the model
            try (BufferedWriter writer = Files.newBufferedWriter(path.resolve("model"))) {
                Linear.saveModel(writer, this.model);
            }
        }

        static Classifier doRead(final Parameters parameters, final Path path) throws IOException {

            // Read the dictionary
            final Dictionary<String> dictionary = Dictionary.readFrom(String.class,
                    path.resolve("dictionary"));

            // Read the model
            final String modelString = new String(Files.readAllBytes(path.resolve("model")),
                    Charsets.UTF_8);
            final Model model = Model.load(new StringReader(modelString));

            // Compute model hash
            final String modelHash = computeHash(dictionary, modelString);

            // Create and return the SVM
            return new LibLinearClassifier(parameters, modelHash, dictionary, model);
        }

        static Classifier doTrain(final Parameters parameters,
                final Iterable<LabelledVector> trainingSet) throws IOException {
            if (NATIVE_LIB_AVAILABLE) {
                return trainNative(parameters, trainingSet);
            } else {
                return trainJava(parameters, trainingSet);
            }
        }

        private static Classifier trainJava(final Parameters parameters,
                final Iterable<LabelledVector> trainingSet) throws IOException {

            // Prepare the svm_parameter object based on supplied parameters
            final Parameter parameter = encodeParameters(parameters);
            parameter.setEps(getDefaultEpsilon(parameters) * 0.1f);

            // Encode the training set as an svm_problem object, filling a dictionary meanwhile
            final Dictionary<String> dictionary = Dictionary.create();
            dictionary.indexFor("_unused"); // just to avoid using feature index 0
            final Problem problem = encodeProblem(dictionary, trainingSet, parameters);

            // Perform training
            final Model model = Linear.train(problem, parameter);

            // Compute model hash
            final StringWriter writer = new StringWriter();
            Linear.saveModel(writer, model);
            final String modelString = writer.toString();
            final String modelHash = computeHash(dictionary, modelString);

            // Build and return the SVM object
            return new LibLinearClassifier(parameters, modelHash, dictionary, model);
        }

        private static Classifier trainNative(final Parameters parameters,
                final Iterable<LabelledVector> trainingSet) throws IOException {

            Preconditions.checkNotNull(parameters);
            Preconditions.checkNotNull(trainingSet);
            Preconditions.checkArgument(Iterables.size(trainingSet) >= 2);

            // Encode the training set in a file, filling a dictionary meanwhile
            final Dictionary<String> dictionary = Dictionary.create();
            dictionary.indexFor("_unused"); // just to avoid using feature index 0
            final File trainingFile = File.createTempFile("training.", ".txt");
            trainingFile.deleteOnExit();
            try (Writer writer = IO
                    .utf8Writer(IO.buffer(IO.write(trainingFile.getAbsolutePath())))) {
                Vector.write(trainingSet, dictionary, writer);
            }

            // Define a file where to store the learned model
            final File modelFile = new File(trainingFile.getAbsolutePath() + ".model");

            // Call liblinear train
            final ImmutableList.Builder<String> argBuilder = ImmutableList.builder();
            argBuilder.add("-c").add(Double.toString(parameters.getC()));
            if (parameters.getWeights() != null) {
                for (int i = 0; i < parameters.getWeights().length; ++i) {
                    argBuilder.add("-w" + i).add(Float.toString(parameters.getWeights()[i]));
                }
            }
            if (parameters.getBias() != null) {
                argBuilder.add("-B").add(parameters.getBias().toString());
            }
            argBuilder.add("-s");
            switch (parameters.getAlgorithm()) {
            case LINEAR_LRLOSS_L1REG:
                argBuilder.add("6");
                break;
            case LINEAR_LRLOSS_L2REG:
                argBuilder.add(parameters.getDual() ? "7" : "0");
                break;
            case LINEAR_L2LOSS_L1REG:
                argBuilder.add("5");
                break;
            case LINEAR_L2LOSS_L2REG:
                argBuilder.add(parameters.getDual() ? "1" : "2");
                break;
            case LINEAR_L1LOSS_L2REG:
                argBuilder.add("3");
                break;
            default:
                throw new Error();
            }
            argBuilder.add("-e").add(Float.toString(getDefaultEpsilon(parameters) * 0.1f));
            argBuilder.add(trainingFile.getAbsolutePath());
            argBuilder.add(modelFile.getAbsolutePath());
            invokeNative("train", argBuilder.build(), false);

            // Load the model from the generated model file
            final String modelString = com.google.common.io.Files.toString(modelFile,
                    Charset.defaultCharset());
            final String modelHash = computeHash(dictionary, modelString);
            final Model model = Model.load(new StringReader(modelString));

            // Delete temporary files
            trainingFile.delete();
            modelFile.delete();

            // Build and return the Classifier object
            return new LibLinearClassifier(parameters, modelHash, dictionary, model);
        }

        private static float getDefaultEpsilon(final Parameters parameters) {
            final Algorithm alg = parameters.getAlgorithm();
            final boolean dual = Boolean.TRUE.equals(parameters.getDual());
            if (alg == Algorithm.LINEAR_LRLOSS_L2REG && !dual
                    || alg == Algorithm.LINEAR_L2LOSS_L2REG && !dual
                    || alg == Algorithm.LINEAR_L2LOSS_L1REG
                    || alg == Algorithm.LINEAR_LRLOSS_L1REG) {
                return 0.01f; // 0 2 5 6
            } else if (alg == Algorithm.LINEAR_L2LOSS_L2REG && dual
                    || alg == Algorithm.LINEAR_L1LOSS_L2REG
                    || alg == Algorithm.LINEAR_LRLOSS_L2REG && dual) {
                return 0.1f; // 1 3 7
            } else {
                throw new IllegalArgumentException("Invalid parameters " + parameters);
            }
        }

        private static Parameter encodeParameters(final Parameters parameters) {

            // Initialize the parameter object, setting algorithm, constant C and epsilon
            Parameter parameter;
            final Boolean dual = parameters.getDual();
            final float c = parameters.getC();
            switch (parameters.getAlgorithm()) {
            case LINEAR_LRLOSS_L1REG:
                parameter = new Parameter(SolverType.L1R_LR, c, 0.01f);
                break;
            case LINEAR_LRLOSS_L2REG:
                parameter = new Parameter(dual ? SolverType.L2R_LR_DUAL : SolverType.L2R_LR, c,
                        dual ? 0.1f : 0.01f);
                break;
            case LINEAR_L2LOSS_L1REG:
                parameter = new Parameter(SolverType.L1R_L2LOSS_SVC, c, 0.01f);
                break;
            case LINEAR_L2LOSS_L2REG:
                parameter = new Parameter(dual ? SolverType.L2R_L2LOSS_SVC_DUAL
                        : SolverType.L2R_L2LOSS_SVC, c, dual ? 0.1f : 0.01f);
                break;
            case LINEAR_L1LOSS_L2REG:
                parameter = new Parameter(SolverType.L2R_L1LOSS_SVC_DUAL, c, 0.1f);
                break;
            default:
                throw new Error();
            }

            // Set weights, if supplied
            final float[] weights = parameters.getWeights();
            if (weights != null) {
                final double[] weightValues = new double[weights.length];
                final int[] weightLabels = new int[weights.length];
                for (int i = 0; i < weights.length; ++i) {
                    weightLabels[i] = i;
                    weightValues[i] = weights[i];
                }
                parameter.setWeights(weightValues, weightLabels);
            }
            return parameter;
        }

        private static Problem encodeProblem(final Dictionary<String> dictionary,
                final Iterable<LabelledVector> vectors, final Parameters parameters) {
            final int size = Iterables.size(vectors);
            final Problem problem = new Problem();
            problem.l = size;
            problem.bias = MoreObjects.firstNonNull(parameters.getBias(), -1f).doubleValue();
            problem.x = new Feature[size][];
            problem.y = new double[size];
            int index = 0;
            final Set<String> features = Sets.newIdentityHashSet();
            for (final LabelledVector vector : vectors) {
                problem.x[index] = encodeVector(dictionary, vector);
                problem.y[index] = vector.getLabel();
                ++index;
                for (int i = 0; i < vector.size(); ++i) {
                    final String feature = vector.getFeature(i);
                    if (feature.charAt(0) != '_') {
                        features.add(feature);
                    }
                }
            }
            problem.n = features.size() + (problem.bias >= 0 ? 1 : 0);
            return problem;
        }

        private static Feature[] encodeVector(final Dictionary<String> dictionary,
                final Vector vector) {
            final int size = vector.size();
            Feature[] features = new Feature[size];
            int index = 0;
            for (int i = 0; i < size; ++i) {
                final String feature = vector.getFeature(i);
                if (feature.charAt(0) != '_') {
                    final Integer featureIndex = dictionary.indexFor(vector.getFeature(i));
                    if (featureIndex != null) {
                        features[index++] = new FeatureNode(featureIndex, vector.getValue(i));
                    }
                }
            }
            if (index < size) {
                features = Arrays.copyOfRange(features, 0, index);
            }
            Arrays.sort(features, FEATURE_ORDERING);
            return features;
        }

        static {
            Linear.disableDebugOutput();
        }

    }

    private static class LibSvmClassifier extends Classifier {

        private static final Ordering<svm_node> NODE_ORDERING = new Ordering<svm_node>() {

            @Override
            public int compare(final svm_node left, final svm_node right) {
                return left.index - right.index;
            }

        };

        private static final boolean NATIVE_LIB_AVAILABLE = testNative("svm-train",
                "Using native LIBSVM tools");

        private static final int SHRINKING = 1; // default value in libsvm docs

        private final Dictionary<String> dictionary;

        private final svm_model model;

        private LibSvmClassifier(final Parameters parameters, final String modelHash,
                final Dictionary<String> dictionary, final svm_model model) {
            super(parameters, modelHash);
            this.dictionary = dictionary.freeze();
            this.model = model;
        }

        @Override
        LabelledVector doPredict(final boolean withProbabilities, final Vector vector) {
            final svm_node[] nodes = encodeVector(this.dictionary, vector);
            if (withProbabilities) {
                final int numLabels = getParameters().getNumLabels();
                final double[] p = new double[numLabels];
                final int label = (int) svm.svm_predict_probability(this.model, nodes, p);
                final float[] probabilities = new float[numLabels];
                for (int i = 0; i < p.length; ++i) {
                    final int labelIndex = this.model.label[i];
                    probabilities[labelIndex] = (float) p[i];
                }
                return vector.label(label, probabilities);
            } else {
                final int label = (int) svm.svm_predict(this.model, nodes);
                return vector.label(label);
            }
        }

        @Override
        void doWrite(final Path path) throws IOException {

            // Write the dictionary
            this.dictionary.writeTo(path.resolve("dictionary"));

            // Write the model
            final File tmpFile = File.createTempFile("svm", ".bin");
            tmpFile.deleteOnExit();
            svm.svm_save_model(tmpFile.getAbsolutePath(), this.model);
            final String modelString = com.google.common.io.Files.toString(tmpFile,
                    Charset.defaultCharset());
            tmpFile.delete();
            Files.write(path.resolve("model"), modelString.getBytes(Charsets.UTF_8));
        }

        static Classifier doRead(final Parameters parameters, final Path path) throws IOException {

            // Read the dictionary
            final Dictionary<String> dictionary = Dictionary.readFrom(String.class,
                    path.resolve("dictionary"));

            // Read the model
            final String modelString = new String(Files.readAllBytes(path.resolve("model")),
                    Charsets.UTF_8);
            final svm_model model = svm.svm_load_model(new BufferedReader(new StringReader(
                    modelString)));

            // Compute model hash
            final String modelHash = computeHash(dictionary, modelString);

            // Create and return the SVM
            return new LibSvmClassifier(parameters, modelHash, dictionary, model);
        }

        static Classifier doTrain(final Parameters parameters,
                final Iterable<LabelledVector> trainingSet) throws IOException {

            if (NATIVE_LIB_AVAILABLE) {
                return trainNative(parameters, trainingSet);
            } else {
                return trainJava(parameters, trainingSet);
            }
        }

        private static Classifier trainJava(final Parameters parameters,
                final Iterable<LabelledVector> trainingSet) throws IOException {

            // Prepare the svm_parameter object based on supplied parameters
            final svm_parameter parameter = encodeParameters(parameters);

            // Encode the training set as an svm_problem object, filling a dictionary meanwhile
            final Dictionary<String> dictionary = Dictionary.create();
            final svm_problem problem = encodeProblem(dictionary, trainingSet);

            // Perform training
            final svm_model model = svm.svm_train(problem, parameter);

            // Compute model hash, by saving and reloading SVM model
            final File tmpFile = File.createTempFile("svm", ".bin");
            tmpFile.deleteOnExit();
            svm.svm_save_model(tmpFile.getAbsolutePath(), model);
            final String modelString = com.google.common.io.Files.toString(tmpFile,
                    Charset.defaultCharset());
            final String modelHash = computeHash(dictionary, modelString);
            final svm_model reloadedModel = svm.svm_load_model(new BufferedReader(
                    new StringReader(modelString)));
            tmpFile.delete();

            // Build and return the SVM object
            return new LibSvmClassifier(parameters, modelHash, dictionary, reloadedModel);
        }

        private static Classifier trainNative(final Parameters parameters,
                final Iterable<LabelledVector> trainingSet) throws IOException {

            Preconditions.checkNotNull(parameters);
            Preconditions.checkNotNull(trainingSet);
            Preconditions.checkArgument(Iterables.size(trainingSet) >= 2);

            // Encode the training set in a file, filling a dictionary meanwhile
            final Dictionary<String> dictionary = Dictionary.create();
            final File trainingFile = File.createTempFile("svmdata.", ".txt");
            trainingFile.deleteOnExit();
            try (Writer writer = IO
                    .utf8Writer(IO.buffer(IO.write(trainingFile.getAbsolutePath())))) {
                Vector.write(trainingSet, dictionary, writer);
            }

            // Define a file where to store the learned model
            final File modelFile = new File(trainingFile.getAbsolutePath() + ".model");

            // Call svm-train
            final ImmutableList.Builder<String> argBuilder = ImmutableList.builder();
            argBuilder.add("-c").add(Double.toString(parameters.getC()));
            if (parameters.getWeights() != null) {
                for (int i = 0; i < parameters.getWeights().length; ++i) {
                    argBuilder.add("-w" + i).add(Float.toString(parameters.getWeights()[i]));
                }
            }
            argBuilder.add("-h").add(Integer.toString(SHRINKING));
            argBuilder.add("-b").add("1");
            if (parameters.getGamma() != null) {
                argBuilder.add("-g").add(parameters.getGamma().toString()); // libsvm only
            }
            if (parameters.getCoeff() != null) {
                argBuilder.add("-r").add(parameters.getCoeff().toString()); // libsvm only
            }
            if (parameters.getDegree() != null) {
                argBuilder.add("-d").add(parameters.getDegree().toString()); // libsvm only
            }
            argBuilder.add("-s").add("0");
            switch (parameters.getAlgorithm()) {
            case SVM_LINEAR_KERNEL:
                argBuilder.add("-t").add("0");
                break;
            case SVM_RBF_KERNEL:
                argBuilder.add("-t").add("2");
                break;
            case SVM_SIGMOID_KERNEL:
                argBuilder.add("-t").add("3");
                break;
            case SVM_POLY_KERNEL:
                argBuilder.add("-t").add("1");
                break;
            default:
                throw new Error();
            }
            argBuilder.add(trainingFile.getAbsolutePath());
            argBuilder.add(modelFile.getAbsolutePath());
            invokeNative("svm-train", argBuilder.build(), false);

            // Load the model from the generated model file
            final String modelString = com.google.common.io.Files.toString(modelFile,
                    Charset.defaultCharset());
            final String modelHash = computeHash(dictionary, modelString);
            final svm_model model = svm.svm_load_model(new BufferedReader(new StringReader(
                    modelString)));

            // Delete temporary files
            trainingFile.delete();
            modelFile.delete();

            // Build and return the SVM object
            return new LibSvmClassifier(parameters, modelHash, dictionary, model);
        }

        private static svm_parameter encodeParameters(final Parameters parameters) {

            // Initialize the svm_parameter object, using a C-SVC machine
            final svm_parameter param = new svm_parameter();
            param.svm_type = svm_parameter.C_SVC;
            param.eps = 0.001; // default value in libsvm docs
            param.shrinking = 1; // default value in libsvm docs
            param.probability = 1;
            param.cache_size = Math.min(1024.0, Runtime.getRuntime().maxMemory() / 2);

            // Set parameter C
            param.C = parameters.getC();

            // Set the kernel and its parameters
            switch (parameters.getAlgorithm()) {
            case SVM_LINEAR_KERNEL:
                param.kernel_type = svm_parameter.LINEAR;
                break;
            case SVM_RBF_KERNEL:
                param.kernel_type = svm_parameter.RBF;
                param.gamma = parameters.getGamma();
                break;
            case SVM_SIGMOID_KERNEL:
                param.kernel_type = svm_parameter.SIGMOID;
                param.gamma = parameters.getGamma();
                param.coef0 = parameters.getCoeff();
                break;
            case SVM_POLY_KERNEL:
                param.kernel_type = svm_parameter.POLY;
                param.gamma = parameters.getGamma();
                param.coef0 = parameters.getCoeff();
                param.degree = parameters.getDegree();
                break;
            default:
                throw new Error();
            }

            // Set weights, if supplied
            final float[] weights = parameters.getWeights();
            if (weights != null) {
                param.nr_weight = weights.length;
                param.weight_label = new int[weights.length];
                param.weight = new double[weights.length];
                for (int i = 0; i < weights.length; ++i) {
                    param.weight_label[i] = i;
                    param.weight[i] = weights[i];
                }
            }
            return param;
        }

        private static svm_problem encodeProblem(final Dictionary<String> dictionary,
                final Iterable<LabelledVector> vectors) {
            final int size = Iterables.size(vectors);
            final svm_problem problem = new svm_problem();
            problem.l = size;
            problem.x = new svm_node[size][];
            problem.y = new double[size];
            int index = 0;
            for (final LabelledVector vector : vectors) {
                problem.x[index] = encodeVector(dictionary, vector);
                problem.y[index] = vector.getLabel();
                ++index;
            }
            return problem;
        }

        private static svm_node[] encodeVector(final Dictionary<String> dictionary,
                final Vector vector) {
            final int size = vector.size();
            svm_node[] nodes = new svm_node[size];
            int index = 0;
            for (int i = 0; i < size; ++i) {
                final String feature = vector.getFeature(i);
                if (feature.charAt(0) != '_') {
                    final Integer featureIndex = dictionary.indexFor(vector.getFeature(i));
                    if (featureIndex != null) {
                        final svm_node node = new svm_node();
                        node.index = featureIndex;
                        node.value = vector.getValue(i);
                        nodes[index++] = node;
                    }
                }
            }
            if (index < size) {
                nodes = Arrays.copyOfRange(nodes, 0, index);
            }
            Arrays.sort(nodes, NODE_ORDERING);
            return nodes;
        }

        static {
            svm.svm_set_print_string_function(new svm_print_interface() {

                @Override
                public void print(final String s) {
                    if (!".".equals(s) && !"*".equals(s)) {
                        LOGGER.debug("LIBSVM: " + s.replace('\n', ' ').trim());
                    }
                }

            });
        }

    }

}

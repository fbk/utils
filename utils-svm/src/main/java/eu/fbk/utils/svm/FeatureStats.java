package eu.fbk.utils.svm;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public final class FeatureStats implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Ordering<FeatureStats> FSCORE_COMPARATOR = new Ordering<FeatureStats>() {

        @Override
        public int compare(final FeatureStats first, final FeatureStats second) {
            return -Float.compare(first.getFScore(), second.getFScore());
        }

    };

    private final String name;

    private final int[] counts;

    private final int[] countsNonZero;

    private final float[] mins;

    private final float[] maxs;

    private final float[] means;

    private final float[] squareMeans;

    private FeatureStats(final String name, final int[] counts, final int[] countsNonZero,
            final float[] mins, final float[] maxs, final float[] means, final float[] squareMeans) {
        this.name = name;
        this.counts = counts;
        this.countsNonZero = countsNonZero;
        this.mins = mins;
        this.maxs = maxs;
        this.means = means;
        this.squareMeans = squareMeans;
    }

    public static Map<String, FeatureStats> forVectors(final int numLabels,
            final Iterable<? extends Vector> vectors, @Nullable final Predicate<String> selector) {

        final Map<String, Builder> builders = Maps.newLinkedHashMap();
        for (final Vector vector : vectors) {
            for (final String feature : vector.getFeatures()) {
                if (feature.length() > 0 && feature.charAt(0) == '_') {
                    continue;
                }
                if ((selector == null || selector.apply(feature))
                        && !builders.containsKey(feature)) {
                    builders.put(feature, builder(numLabels));
                }
            }
        }

        final int[] counts = new int[numLabels];
        for (final Vector vector : vectors) {
            final int size = vector.size();
            final int label = vector instanceof LabelledVector ? ((LabelledVector) vector)
                    .getLabel() : 0;
            ++counts[label];
            for (int i = 0; i < size; ++i) {
                final String feature = vector.getFeature(i);
                final Builder builder = builders.get(feature);
                if (builder != null) {
                    builder.add(vector.getValue(i), label);
                }
            }
        }

        final Map<String, FeatureStats> result = Maps.newLinkedHashMap();
        for (final String feature : Ordering.natural().sortedCopy(builders.keySet())) {
            result.put(feature, builders.get(feature).build(feature, counts));
        }
        return result;
    }

    public String getName() {
        return this.name;
    }

    public int getCount() {
        int count = 0;
        for (int i = 0; i < this.counts.length; ++i) {
            count += this.counts[i];
        }
        return count;
    }

    public int getCount(final int label) {
        return this.counts[label];
    }

    public int getCountNonZero() {
        int countNonZero = 0;
        for (int i = 0; i < this.countsNonZero.length; ++i) {
            countNonZero += this.countsNonZero[i];
        }
        return countNonZero;
    }

    public int getCountNonZero(final int label) {
        return this.countsNonZero[label];
    }

    public float getMin() {
        float result = Float.MAX_VALUE;
        for (int i = 0; i < this.mins.length; ++i) {
            result = Math.min(result, this.mins[i]);
        }
        return result;
    }

    public float getMin(final int label) {
        return this.mins[label];
    }

    public float getMax() {
        float result = Float.MIN_VALUE;
        for (int i = 0; i < this.maxs.length; ++i) {
            result = Math.max(result, this.maxs[i]);
        }
        return result;
    }

    public float getMax(final int label) {
        return this.maxs[label];
    }

    public float getMean() {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < this.means.length; ++i) {
            sum += this.means[i] * this.counts[i];
            count += this.counts[i];
        }
        return (float) (sum / count);
    }

    public float getMean(final int label) {
        return this.means[label];
    }

    public float getVariance() {
        double count = 0;
        double sum = 0;
        double squareSum = 0;
        for (int i = 0; i < this.means.length; ++i) {
            sum += this.means[i] * this.counts[i];
            squareSum += this.squareMeans[i] * this.counts[i];
            count += this.counts[i];
        }
        final double mean = sum / count;
        final double squareMean = squareSum / count;
        return (float) (count / (count - 1) * (squareMean - mean * mean));
    }

    public float getVariance(final int label) {
        final double mean = this.means[label];
        final double squareMean = this.squareMeans[label];
        final double count = this.counts[label];
        return (float) (count / (count - 1) * (squareMean - mean * mean));
    }

    public float getFScore() {
        double num = 0.0;
        double den = 0.0;
        final double mean = getMean();
        for (int i = 0; i < this.counts.length; ++i) {
            num += Math.pow(getMean(i) - mean, 2.0);
            den += getVariance(i);
        }
        return den == 0.0 ? Float.NaN : (float) (num / den);
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof FeatureStats)) {
            return false;
        }
        final FeatureStats other = (FeatureStats) object;
        return this.name.equals(other.name) && Arrays.equals(this.counts, other.counts)
                && Arrays.equals(this.countsNonZero, other.countsNonZero)
                && Arrays.equals(this.mins, other.mins) && Arrays.equals(this.maxs, other.maxs)
                && Arrays.equals(this.means, other.means)
                && Arrays.equals(this.squareMeans, other.squareMeans);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name, Arrays.hashCode(this.counts),
                Arrays.hashCode(this.countsNonZero), Arrays.hashCode(this.mins),
                Arrays.hashCode(this.maxs), Arrays.hashCode(this.means),
                Arrays.hashCode(this.squareMeans));
    }

    public <T extends Appendable> T toString(final T out) throws IOException {
        out.append(String.format("%-20s", this.name));
        toStringHelper(out, "count!=0");
        out.append(", ");
        toStringHelper(out, "min");
        out.append(", ");
        toStringHelper(out, "avg");
        out.append(", ");
        toStringHelper(out, "max");
        out.append(", ");
        toStringHelper(out, "std");
        out.append(", fscore ").append(String.format("%.3f", getFScore()));
        return out;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        try {
            toString(builder);
        } catch (final IOException ex) {
            throw new Error(ex);
        }
        return builder.toString();
    }

    public static String toString(final Iterable<FeatureStats> fss, final int maxFeaturesDisplayed) {
        final StringBuilder builder = new StringBuilder();
        final List<String> uselessFeatures = Lists.newArrayList();
        int i = 0;
        for (final FeatureStats fs : FeatureStats.FSCORE_COMPARATOR.immutableSortedCopy(fss)) {
            if (++i <= maxFeaturesDisplayed) {
                builder.append(fs).append("\n");
            }
            if (fs.getMin() == fs.getMax()) {
                uselessFeatures.add(fs.getName());
            }
        }
        if (i > maxFeaturesDisplayed) {
            builder.append("... [truncated] ...\n");
        }
        if (!uselessFeatures.isEmpty()) {
            builder.append("USELESS FEATURES: ");
            Joiner.on(", ").appendTo(builder, uselessFeatures);
            builder.append("\n");
        }
        return builder.toString();
    }

    private void toStringHelper(final Appendable out, final String field) throws IOException {
        out.append(field).append(' ');
        if (field == "count") {
            out.append(Integer.toString(getCount()));
        } else if (field == "count!=0") {
            out.append(Integer.toString(getCountNonZero()));
        } else if (field == "min") {
            out.append(String.format("%.3f", getMin()));
        } else if (field == "avg") {
            out.append(String.format("%.3f", getMean()));
        } else if (field == "max") {
            out.append(String.format("%.3f", getMax()));
        } else if (field == "std") {
            out.append(String.format("%.3f", Math.sqrt(getVariance())));
        }
        if (this.counts.length > 1) {
            out.append(" (");
            for (int i = 0; i < this.counts.length; ++i) {
                if (i > 0) {
                    out.append(' ');
                }
                out.append(Integer.toString(i));
                out.append(':');
                if (field == "count") {
                    out.append(Integer.toString(this.counts[i]));
                } else if (field == "count!=0") {
                    out.append(Integer.toString(this.countsNonZero[i]));
                } else if (field == "min") {
                    out.append(String.format("%.3f", getMin(i)));
                } else if (field == "avg") {
                    out.append(String.format("%.3f", getMean(i)));
                } else if (field == "max") {
                    out.append(String.format("%.3f", getMax(i)));
                } else if (field == "std") {
                    out.append(String.format("%.3f", Math.sqrt(getVariance(i))));
                }
            }
            out.append(')');
        }
    }

    public static Builder builder(final int numLabels) {
        return new Builder(numLabels);
    }

    public static final class Builder {

        private final int[] countsNonZero;

        private final float[] mins;

        private final float[] maxs;

        private final float[] sums;

        private final float[] squareSums;

        private Builder(final int numLabels) {
            this.countsNonZero = new int[numLabels];
            this.mins = new float[numLabels];
            this.maxs = new float[numLabels];
            this.sums = new float[numLabels];
            this.squareSums = new float[numLabels];
        }

        public Builder add(final float value) {
            return add(value, 0);
        }

        public Builder add(final float value, final int label) {
            if (value != 0.0f) {
                ++this.countsNonZero[label];
            }
            this.mins[label] = Math.min(this.mins[label], value);
            this.maxs[label] = Math.max(this.maxs[label], value);
            this.sums[label] += value;
            this.squareSums[label] += value * value;
            return this;
        }

        public Builder add(final float value, final int label, final int repetitions) {
            if (value != 0.0f) {
                this.countsNonZero[label] += repetitions;
            }
            this.mins[label] = Math.min(this.mins[label], value);
            this.maxs[label] = Math.max(this.maxs[label], value);
            this.sums[label] += value * repetitions;
            this.squareSums[label] += value * value * repetitions;
            return this;
        }

        public FeatureStats build(final String name, final int... counts) {
            Preconditions.checkArgument(counts.length == this.sums.length);
            final float[] mins = this.mins.clone();
            final float[] maxs = this.maxs.clone();
            final float[] means = new float[this.sums.length];
            final float[] squareMeans = new float[this.sums.length];
            for (int i = 0; i < this.sums.length; ++i) {
                means[i] = this.sums[i] / counts[i];
                squareMeans[i] = this.squareSums[i] / counts[i];
                if (counts[i] > this.countsNonZero[i]) {
                    mins[i] = Math.min(mins[i], 0);
                    maxs[i] = Math.max(maxs[i], 0);
                }
            }
            return new FeatureStats(name, counts.clone(), this.countsNonZero.clone(), mins, maxs,
                    means, squareMeans);
        }

    }

}

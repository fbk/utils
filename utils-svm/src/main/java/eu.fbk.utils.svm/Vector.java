package eu.fbk.utils.svm;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

import eu.fbk.utils.core.Dictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Vector implements Serializable, Comparable<Vector> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Vector.class);

    private static final long serialVersionUID = 1L;

    static Vector create(@Nullable final Map<String, Float> map) {
        final int size = map == null ? 0 : map.size();
        if (size == 0) {
            return Vector0.INSTANCE;
        } else if (size == 1) {
            final Map.Entry<String, Float> entry = map.entrySet().iterator().next();
            return new Vector1(entry.getKey().intern(), entry.getValue());
        } else {
            final String[] features = map.keySet().toArray(new String[size]);
            Arrays.sort(features);
            final float[] values = new float[size];
            boolean allOnes = true;
            for (int i = 0; i < size; ++i) {
                features[i] = features[i].intern();
                values[i] = map.get(features[i]);
                allOnes &= values[i] == 1.0f;
            }
            return allOnes ? new VectorAllOnes(features) : new VectorN(features, values);
        }
    }

    abstract int doSize();

    abstract String doGetFeature(int index);

    abstract float doGetValue(int index);

    LabelledVector doLabel(final int label, final float... probabilities) {
        return LabelledVector.create(unlabel(), label, probabilities);
    }

    Vector doUnlabel() {
        return this;
    }

    void doToString(final Appendable out) throws IOException {
        final int size = doSize();
        for (int i = 0; i < size; ++i) {
            out.append(i == 0 ? "" : " ");
            out.append(doGetFeature(i)).append(":").append(Float.toString(doGetValue(i)));
        }
    }

    private void checkFeatureIndex(final int index) {
        if (index < 0 || index >= size()) {
            throw new IllegalArgumentException("Invalid feature index " + index + " (size "
                    + doSize() + ")");
        }
    }

    public final int size() {
        return doSize();
    }

    public final boolean isEmpty() {
        return doSize() == 0;
    }

    public final String getFeature(final int index) {
        checkFeatureIndex(index);
        return doGetFeature(index);
    }

    public final Set<String> getFeatures() {
        return new AbstractSet<String>() {

            @Override
            public int size() {
                return doSize();
            }

            @Override
            public boolean contains(final Object object) {
                return object instanceof String && hasFeature((String) object);
            }

            @Override
            public Iterator<String> iterator() {
                return new UnmodifiableIterator<String>() {

                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return this.index < doSize();
                    }

                    @Override
                    public String next() {
                        return getFeature(this.index++);
                    }

                };
            }

        };
    }

    public Set<String> getFeatures(final String prefix) {
        return Sets.filter(getFeatures(), new Predicate<String>() {

            @Override
            public boolean apply(final String feature) {
                return feature.startsWith(prefix);
            }

        });
    }

    public final boolean hasFeature(final String feature) {
        return getValue(feature) != 0.0;
    }

    public final float getValue(final int index) {
        checkFeatureIndex(index);
        return doGetValue(index);
    }

    public final float getValue(final String feature) {
        final String internedFeature = feature.intern();
        final int size = doSize();
        for (int i = 0; i < size; ++i) {
            if (doGetFeature(i) == internedFeature) {
                return doGetValue(i);
            }
        }
        return 0.0f;
    }

    public final LabelledVector label(final int label, final float... probabilities) {
        if (probabilities != null && probabilities.length > 0) {
            float sum = 0.0f;
            for (int i = 0; i < probabilities.length; ++i) {
                sum += probabilities[i];
            }
            if (Math.abs(sum - 1.0f) > 0.01f) {
                throw new IllegalArgumentException("Supplied probabilities sum to " + sum);
            }
        }
        return doLabel(label, probabilities);
    }

    public final Vector unlabel() {
        return doUnlabel();
    }

    @Override
    public int compareTo(final Vector other) {
        final int thisSize = doSize();
        final int otherSize = other.doSize();
        final int minSize = Math.min(thisSize, otherSize);
        for (int i = 0; i < minSize; ++i) {
            int result = doGetFeature(i).compareTo(other.doGetFeature(i));
            if (result != 0) {
                return result;
            }
            result = Float.compare(doGetValue(i), other.doGetValue(i));
            if (result != 0) {
                return result;
            }
        }
        return thisSize - otherSize;
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Vector)) {
            return false;
        }
        final Vector other = (Vector) object;
        final int size = doSize();
        if (size != other.doSize()) {
            return false;
        }
        for (int i = 0; i < size; ++i) {
            if (doGetFeature(i) != other.doGetFeature(i) || doGetValue(i) != other.doGetValue(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final int hashCode() {
        int result = 0;
        final int size = doSize();
        for (int i = 0; i < size; ++i) {
            result = result * 983 + doGetFeature(i).hashCode() * 37
                    + Float.hashCode(doGetValue(i));
        }
        return result;
    }

    public final void toString(final Appendable out) throws IOException {
        doToString(out);
    }

    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();
        try {
            doToString(builder);
        } catch (final IOException ex) {
            throw new Error(ex);
        }
        return builder.toString();
    }

    public static <T extends Appendable> T write(final Iterable<? extends Vector> vectors,
            final Dictionary<String> dictionary, final T out) throws IOException {

        for (final Vector vector : vectors) {
            out.append(vector instanceof LabelledVector ? Integer
                    .toString(((LabelledVector) vector).getLabel()) : "0");

            final int size = vector.size();
            final List<IndexValue> vs = Lists.newArrayListWithCapacity(size);
            for (int i = 0; i < size; ++i) {
                final String feature = vector.getFeature(i);
                if (feature.charAt(0) != '_') {
                    final Integer featureIndex = dictionary.indexFor(vector.getFeature(i));
                    if (featureIndex != null) {
                        vs.add(new IndexValue(featureIndex, vector.getValue(i)));
                    }
                }
            }
            Collections.sort(vs);

            for (final IndexValue v : vs) {
                out.append(' ');
                out.append(Integer.toString(v.index));
                out.append(':');
                out.append(Float.toString(v.value));
            }

            out.append('\n');
        }

        return out;
    }

    public static <T extends Vector> List<List<T>> split(final Iterable<T> vectors,
            final int numPartitions, final int maxVectors) {

        // Check parameters
        Preconditions.checkNotNull(vectors);
        Preconditions.checkArgument(numPartitions > 0);

        // Split the vectors in labelled clusters (each of them must be allocated as a whole to a
        // single partition) and - for vectors non assigned to clusters - based on their labels
        final Map<Integer, List<T>> labelIndex = Maps.newHashMap();
        final Map<String, SplitCluster<T>> clusterIndex = Maps.newHashMap();
        int[] distribution = new int[1];
        int totalVectors = 0;
        for (final T vector : Ordering.natural().immutableSortedCopy(vectors)) {

            // Increment total size and extract the label
            final int label = vector instanceof LabelledVector ? ((LabelledVector) vector)
                    .getLabel() : 0;
            distribution = label < distribution.length ? distribution : Arrays.copyOf(
                    distribution, label + 1);
            ++distribution[label];
            ++totalVectors;

            // Extract the cluster the vector belongs to (null = no cluster)
            String clusterID = null;
            for (final String feature : vector.getFeatures("_cluster.")) {
                if (clusterID == null) {
                    clusterID = feature;
                } else {
                    LOGGER.warn("Ignoring extra cluster assignment: " + feature);
                }
            }

            // Update indexes
            if (clusterID == null) {
                List<T> labelList = labelIndex.get(label);
                if (labelList == null) {
                    labelList = Lists.newArrayList();
                    labelIndex.put(label, labelList);
                }
                labelList.add(vector);
            } else {
                SplitCluster<T> cluster = clusterIndex.get(clusterID);
                if (cluster == null) {
                    cluster = new SplitCluster<T>(clusterID);
                    clusterIndex.put(clusterID, cluster);
                }
                cluster.add(label, vector);
            }
        }

        // Scale target distribution if total vectors > max output vectors
        if (totalVectors > maxVectors) {
            final float scale = (float) maxVectors / (float) totalVectors;
            for (int i = 0; i < distribution.length; ++i) {
                distribution[i] = (int) (distribution[i] * scale);
            }
        }

        // Allocate partitions
        final List<SplitCluster<T>> partitions = Lists.newArrayList();
        for (int i = 0; i < numPartitions; ++i) {
            partitions.add(new SplitCluster<T>(Integer.toString(i)));
        }

        // Add clustered vectors to partitions
        int allocated = 0;
        for (final SplitCluster<T> cluster : Ordering.natural().sortedCopy(clusterIndex.values())) {
            if (maxVectors > 0 && allocated + cluster.vectors.size() > maxVectors) {
                break;
            }
            SplitCluster<T> smallest = null;
            for (final SplitCluster<T> partition : partitions) {
                if (smallest == null || partition.vectors.size() < smallest.vectors.size()) {
                    smallest = partition;
                }
            }
            smallest.add(cluster);
            allocated += cluster.vectors.size();
        }

        // Add remaining vectors in the label index to partitions
        outer: for (final Integer label : Ordering.natural().sortedCopy(labelIndex.keySet())) {
            for (final T vector : labelIndex.get(label)) {
                SplitCluster<T> selected = null;
                int selectedDelta = 0;
                for (final SplitCluster<T> partition : partitions) {
                    final int count = label >= partition.distribution.length ? 0
                            : partition.distribution[label];
                    final int delta = distribution[label] - numPartitions * count;
                    if (selected == null || delta > selectedDelta) {
                        selected = partition;
                        selectedDelta = delta;
                    }
                }
                if (allocated >= maxVectors) {
                    break outer; // cannot add more vectors at all
                } else if (selectedDelta <= 0) {
                    break; // cannot add more vectors for current label
                }
                selected.add(label, vector);
                ++allocated;
            }
        }

        // Log result
        if (LOGGER.isDebugEnabled()) {
            final StringBuilder builder = new StringBuilder("Split results:");
            for (int i = 0; i < partitions.size(); ++i) {
                final SplitCluster<T> partition = partitions.get(i);
                builder.append("\n- partition ").append(i).append(": ")
                        .append(partition.vectors.size()).append(" vectors ")
                        .append(Arrays.toString(partition.distribution));
            }
            LOGGER.debug(builder.toString());
        }

        // Transform list of clusters to list of lists and return the result
        final List<List<T>> result = Lists.newArrayList();
        for (final SplitCluster<T> partition : partitions) {
            result.add(partition.vectors);
        }
        return result;
    }

    private static final class IndexValue implements Comparable<IndexValue> {

        private final int index;

        private final float value;

        public IndexValue(final int index, final float value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public int compareTo(final IndexValue other) {
            return this.index - other.index;
        }

    }

    private static final class SplitCluster<T extends Vector> implements
            Comparable<SplitCluster<T>> {

        private static final int[] EMPTY_DISTRIBUTION = new int[0];

        String id;

        int[] distribution;

        List<T> vectors;

        SplitCluster(final String id) {
            this.id = id;
            this.distribution = EMPTY_DISTRIBUTION;
            this.vectors = Lists.newArrayList();
        }

        void add(final SplitCluster<T> cluster) {
            this.vectors.addAll(cluster.vectors);
            if (this.distribution.length < cluster.distribution.length) {
                this.distribution = Arrays.copyOf(this.distribution, cluster.distribution.length);
            }
            for (int i = 0; i < cluster.distribution.length; ++i) {
                this.distribution[i] += cluster.distribution[i];
            }
        }

        void add(final int label, final T vector) {
            this.vectors.add(vector);
            if (label >= this.distribution.length) {
                this.distribution = Arrays.copyOf(this.distribution, label + 1);
            }
            ++this.distribution[label];
        }

        @Override
        public int compareTo(final SplitCluster<T> other) {
            int result = other.vectors.size() - this.vectors.size();
            if (result == 0) {
                result = this.id.compareTo(other.id);
            }
            return result;
        }

    }

    public static Builder builder() {
        return new Builder(null);
    }

    public static Builder builder(final Vector vector) {
        return new Builder(vector.unlabel());
    }

    public static final class Builder {

        @Nullable
        private Vector vector;

        @Nullable
        private Map<String, Float> map;

        @Nullable
        private String prefix;

        Builder(@Nullable final Vector vector) {
            this.vector = vector;
            this.map = null;
        }

        public float get(final String feature) {
            final String actualFeature = this.prefix == null ? feature : this.prefix + feature;
            if (this.map != null) {
                final Float value = this.map.get(actualFeature);
                return value == null ? 0.0f : value;
            } else if (this.vector != null) {
                return this.vector.getValue(actualFeature);
            }
            return 0.0f;
        }

        public Builder set(final String feature, final float value) {
            final String actualFeature = this.prefix == null ? feature : this.prefix + feature;
            if (this.map != null) {
                if (value == 0.0f) {
                    this.map.remove(actualFeature);
                    if (this.map.isEmpty()) {
                        this.map = null;
                    }
                } else {
                    this.map.put(actualFeature, value);
                }
            } else if (this.vector != null) {
                if (value != this.vector.getValue(actualFeature)) {
                    final Vector v = this.vector;
                    this.vector = null;
                    this.map = Maps.newHashMap();
                    set(v);
                    set(actualFeature, value);
                }
            } else if (value != 0.0f) {
                this.map = Maps.newHashMap();
                this.map.put(actualFeature, value);
            }
            return this;
        }

        public Builder set(final String feature, final boolean value) {
            return set(feature, value ? 1.0f : 0.0f);
        }

        public Builder set(final String... features) {
            for (final String feature : features) {
                set(feature, 1.0f);
            }
            return this;
        }

        public Builder set(final Iterable<String> features) {
            for (final String feature : features) {
                set(feature, 1.0f);
            }
            return this;
        }

        public Builder set(final String prefix, final Iterable<String> features) {
            for (final String feature : features) {
                set(prefix + feature, 1.0f);
            }
            return this;
        }

        public Builder set(final Vector vector) {
            final int size = vector.doSize();
            for (int i = 0; i < size; ++i) {
                set(vector.doGetFeature(i), vector.doGetValue(i));
            }
            return this;
        }

        public Builder clear(final String... features) {
            for (final String feature : features) {
                set(feature, 0.0f);
            }
            return this;
        }

        public Builder clear(final Iterable<String> features) {
            for (final String feature : features) {
                set(feature, 0.0f);
            }
            return this;
        }

        public Builder prefix(@Nullable final String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Vector build() {
            return this.vector != null ? this.vector : create(this.map);
        }

    }

    private static final class Vector0 extends Vector {

        private static final long serialVersionUID = 1L;

        private static final Vector0 INSTANCE = new Vector0();

        @Override
        int doSize() {
            return 0;
        }

        @Override
        String doGetFeature(final int index) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        float doGetValue(final int index) {
            throw new IndexOutOfBoundsException();
        }

    }

    private static final class Vector1 extends Vector {

        private static final long serialVersionUID = 1L;

        private final String feature;

        private final float value;

        Vector1(final String feature, final float value) {
            this.feature = feature;
            this.value = value;
        }

        @Override
        int doSize() {
            return 1;
        }

        @Override
        String doGetFeature(final int index) {
            if (index == 0) {
                return this.feature;
            }
            throw new IndexOutOfBoundsException();
        }

        @Override
        float doGetValue(final int index) {
            if (index == 0) {
                return this.value;
            }
            throw new IndexOutOfBoundsException();
        }

    }

    private static final class VectorN extends Vector {

        private static final long serialVersionUID = 1L;

        private final String[] features;

        private final float[] values;

        VectorN(final String[] features, final float[] values) {
            this.features = features;
            this.values = values;
        }

        @Override
        int doSize() {
            return this.features.length;
        }

        @Override
        String doGetFeature(final int index) {
            return this.features[index];
        }

        @Override
        float doGetValue(final int index) {
            return this.values[index];
        }

    }

    private static final class VectorAllOnes extends Vector {

        private static final long serialVersionUID = 1L;

        private final String[] features;

        VectorAllOnes(final String[] features) {
            this.features = features;
        }

        @Override
        int doSize() {
            return this.features.length;
        }

        @Override
        String doGetFeature(final int index) {
            return this.features[index];
        }

        @Override
        float doGetValue(final int index) {
            return 1.0f;
        }

    }

}
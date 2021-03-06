package eu.fbk.utils.eval;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

public final class SetPrecisionRecall implements Serializable {

    private static final long serialVersionUID = 1L;

    private final PrecisionRecall exactPR;

    private final PrecisionRecall overlapPR;

    private final PrecisionRecall intersectionPR;

    private final PrecisionRecall alignedPR;

    public SetPrecisionRecall(final PrecisionRecall exactPR, final PrecisionRecall overlapPR,
            final PrecisionRecall intersectionPR, final PrecisionRecall alignedPR) {

        Preconditions.checkNotNull(exactPR);
        Preconditions.checkNotNull(overlapPR);
        Preconditions.checkNotNull(intersectionPR);
        Preconditions.checkNotNull(alignedPR);

        this.exactPR = exactPR;
        this.overlapPR = overlapPR;
        this.intersectionPR = intersectionPR;
        this.alignedPR = alignedPR;
    }

    public PrecisionRecall getExactPR() {
        return this.exactPR;
    }

    public PrecisionRecall getOverlapPR() {
        return this.overlapPR;
    }

    public PrecisionRecall getIntersectionPR() {
        return this.intersectionPR;
    }

    public PrecisionRecall getAlignedPR() {
        return this.alignedPR;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(final Class<T> clazz, final Object... elements) {
        final T[] array = newArray(clazz, elements.length);
        for (int i = 0; i < elements.length; ++i) {
            array[i] = (T) elements[i];
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(final Class<T> clazz, final int length) {
        return (T[]) Array.newInstance(clazz, length);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[][] newArray(final Class<T> clazz, final int length1, final int length2) {
        final Class<T[]> elementClass = (Class<T[]>) newArray(clazz, 0).getClass();
        final T[][] array = newArray(elementClass, length1);
        if (length2 > 0) {
            for (int i = 0; i < length1; ++i) {
                array[i] = newArray(clazz, length2);
            }
        }
        return array;
    }

    private static final class AlignPair<T> implements Comparable<AlignPair<?>> {

        final T object1;

        final T object2;

        @Nullable
        Object similarity;

        public AlignPair(final T object1, final T object2, final Object similarity) {
            this.object1 = object1;
            this.object2 = object2;
            this.similarity = similarity;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public int compareTo(final AlignPair other) {
            if (this.similarity == null) {
                return other.similarity == null ? 0 : -1;
            } else if (other.similarity == null) {
                return 1;
            } else if (this.similarity instanceof Comparable<?>) {
                return ((Comparable<Object>) this.similarity).compareTo(other.similarity);
            } else if (this.similarity instanceof Iterable<?>) {
                return Ordering
                        .natural()
                        .lexicographical()
                        .compare((Iterable<Comparable>) this.similarity,
                                (Iterable<Comparable>) other.similarity);
            } else {
                throw new IllegalArgumentException("Could not compare similarities "
                        + this.similarity + ", " + other.similarity);
            }
        }

    }

    public static <T, E extends T> T[][] align(final Class<T> clazz, final Iterable<E> objects1,
            final Iterable<E> objects2, final boolean functional, final boolean invFunctional,
            final boolean emitUnaligned, final BiFunction<? super E, ? super E, ?> matcher) {

        final Table<E, E, AlignPair<E>> table = HashBasedTable.create();
        for (final E object1 : objects1) {
            for (final E object2 : objects2) {
                final Object similarity = matcher.apply(object1, object2);
                table.put(object1, object2, new AlignPair<E>(object1, object2, similarity));
            }
        }

        final Set<E> set1 = emitUnaligned ? Sets.newHashSet(objects1) : null;
        final Set<E> set2 = emitUnaligned ? Sets.newHashSet(objects2) : null;

        final List<T[]> pairs = Lists.newArrayList();

        while (!table.isEmpty()) {
            final AlignPair<E> bestPair = Ordering.natural().max(table.values());
            if (bestPair.similarity == null) {
                break; // no more matches
            }
            final T[] pair = newArray(clazz, bestPair.object1, bestPair.object2);
            pairs.add(pair);
            if (functional) {
                table.rowKeySet().remove(bestPair.object1);
            }
            if (invFunctional) {
                table.columnKeySet().remove(bestPair.object2);
            }
            if (!functional && !invFunctional) {
                table.remove(bestPair.object1, bestPair.object2);
            }
            if (emitUnaligned) {
                set1.remove(bestPair.object1);
                set2.remove(bestPair.object2);
            }
        }

        if (emitUnaligned) {
            for (final E object1 : set1) {
                pairs.add(newArray(clazz, object1, null));
            }
            for (final E object2 : set2) {
                pairs.add(newArray(clazz, null, object2));
            }
        }

        return pairs.toArray(newArray(clazz, pairs.size(), -1));
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof SetPrecisionRecall)) {
            return false;
        }
        final SetPrecisionRecall other = (SetPrecisionRecall) object;
        return this.exactPR.equals(other.exactPR)
                && this.overlapPR.equals(other.overlapPR) //
                && this.intersectionPR.equals(other.intersectionPR)
                && this.alignedPR.equals(other.alignedPR);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.exactPR, this.overlapPR, this.intersectionPR, this.alignedPR);
    }

    @Override
    public String toString() {
        return "exact:        " + this.exactPR + "\noverlap:      " + this.overlapPR
                + "\nintersection: " + this.intersectionPR + "\naligned:      " + this.alignedPR;
    }

    public static <T> BiFunction<Set<T>, Set<T>, List<Double>> matcher() {
        return (final Set<T> g, final Set<T> t) -> {
            final List<Double> scores = Lists.newArrayListWithCapacity(2);
            final Set<T> intersection = Sets.intersection(g, t);
            if (intersection.isEmpty()) {
                return null;
            }
            scores.add((double) intersection.size() / (double) g.size());
            scores.add((double) intersection.size() / (double) t.size());
            return scores;
        };
    }

    public static <T, L> BiFunction<Entry<Set<T>, L>, Entry<Set<T>, L>, List<Double>> matcherLabelled() {
        return (final Entry<Set<T>, L> ge, final Entry<Set<T>, L> te) -> {
            if (!Objects.equals(ge.getValue(), te.getValue())) {
                return null;
            }
            final Set<T> g = ge.getKey();
            final Set<T> t = te.getKey();
            final Set<T> intersection = Sets.intersection(g, t);
            if (intersection.isEmpty()) {
                return null;
            }
            final List<Double> scores = Lists.newArrayListWithCapacity(2);
            scores.add((double) intersection.size() / (double) g.size());
            scores.add((double) intersection.size() / (double) t.size());
            return scores;
        };
    }

    public static Evaluator evaluator() {
        return new Evaluator();
    }

    public static final class Evaluator {

        private static final Object DUMMY_LABEL = new Object();

        private final PrecisionRecall.Evaluator exactEvaluator;

        private double overlapP;

        private double overlapR;

        private double intersectionP;

        private double intersectionR;

        private double alignedP;

        private double alignedR;

        @Nullable
        private SetPrecisionRecall score;

        private Evaluator() {
            this.exactEvaluator = PrecisionRecall.evaluator();
            this.overlapP = 0.0;
            this.overlapR = 0.0;
            this.intersectionP = 0.0;
            this.intersectionR = 0.0;
            this.alignedP = 0.0;
            this.alignedR = 0.0;
            this.score = null;
        }

        public <T> Evaluator add(final Iterable<Set<T>> goldSets, final Iterable<Set<T>> testSets) {
            final ImmutableMap.Builder<Set<T>, Object> goldBuilder = ImmutableMap.builder();
            final ImmutableMap.Builder<Set<T>, Object> testBuilder = ImmutableMap.builder();
            for (final Set<T> set : goldSets) {
                goldBuilder.put(set, DUMMY_LABEL);
            }
            for (final Set<T> set : testSets) {
                testBuilder.put(set, DUMMY_LABEL);
            }
            return add(goldBuilder.build(), testBuilder.build());
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public <T, L> Evaluator add(final Map<Set<T>, L> goldMap, final Map<Set<T>, L> testMap) {

            final int goldSize = goldMap.size();
            final int testSize = testMap.size();

            // Compute TP for exact precision/recall
            int exactTP = 0;
            for (final Entry<Set<T>, L> goldEntry : goldMap.entrySet()) {
                final Set<?> set = goldEntry.getKey();
                final Object goldLabel = goldEntry.getValue();
                if (testMap.containsKey(set)) {
                    final Object testLabel = testMap.get(set);
                    if (Objects.equals(goldLabel, testLabel)) {
                        ++exactTP;
                    }
                }
            }

            // Compute deltas for overlap precision/recall
            double overlapP = 0.0;
            double overlapR = 0.0;
            for (final Entry<Set<T>, L> te : testMap.entrySet()) {
                for (final Entry<Set<T>, L> ge : goldMap.entrySet()) {
                    if (Objects.equals(te.getValue(), ge.getValue())
                            && !Sets.intersection(te.getKey(), ge.getKey()).isEmpty()) {
                        overlapP += 1.0;
                        break;
                    }
                }
            }
            for (final Entry<Set<T>, L> ge : goldMap.entrySet()) {
                for (final Entry<Set<T>, L> te : testMap.entrySet()) {
                    if (Objects.equals(te.getValue(), ge.getValue())
                            && !Sets.intersection(te.getKey(), ge.getKey()).isEmpty()) {
                        overlapR += 1.0;
                        break;
                    }
                }
            }

            // Compute deltas for intersection precision/recall
            double intersectionP = 0.0;
            double intersectionR = 0.0;
            for (final Entry<Set<T>, L> te : testMap.entrySet()) {
                final Set<Object> intersection = Sets.newHashSet();
                for (final Entry<Set<T>, L> ge : goldMap.entrySet()) {
                    if (Objects.equals(te.getValue(), ge.getValue())) {
                        intersection.addAll(Sets.intersection(te.getKey(), ge.getKey()));
                    }
                }
                intersectionP += (double) intersection.size() / (double) te.getKey().size();
            }
            for (final Entry<Set<T>, L> ge : goldMap.entrySet()) {
                final Set<Object> intersection = Sets.newHashSet();
                for (final Entry<Set<T>, L> te : testMap.entrySet()) {
                    if (Objects.equals(te.getValue(), ge.getValue())) {
                        intersection.addAll(Sets.intersection(te.getKey(), ge.getKey()));
                    }
                }
                intersectionR += (double) intersection.size() / (double) ge.getKey().size();
            }

            // Compute deltas for aligned precision/recall
            double alignedP = 0.0;
            double alignedR = 0.0;
            final Entry<Set<T>, L>[][] pairs = align(Entry.class, goldMap.entrySet(),
                    testMap.entrySet(), true, true, true, matcherLabelled());
            for (final Entry[] pair : pairs) {
                if (pair[0] != null && pair[1] != null) {
                    final Set g = (Set) pair[0].getKey();
                    final Set t = (Set) pair[1].getKey();
                    final double intersection = Sets.intersection(g, t).size();
                    alignedP += intersection / t.size();
                    alignedR += intersection / g.size();
                }
            }

            // Update the evaluator state atomically
            synchronized (this) {
                this.score = null;
                this.exactEvaluator.add(exactTP, testSize - exactTP, goldSize - exactTP);
                this.overlapP += overlapP;
                this.overlapR += overlapR;
                this.intersectionP += intersectionP;
                this.intersectionR += intersectionR;
                this.alignedP += alignedP;
                this.alignedR += alignedR;
            }
            return this;
        }

        public synchronized Evaluator add(final SetPrecisionRecall spr) {
            synchronized (spr) {
                final double tn = spr.getExactPR().getTP() + spr.getExactPR().getFP();
                final double gn = spr.getExactPR().getTP() + spr.getExactPR().getFN();
                this.score = null;
                this.exactEvaluator.add(spr.getExactPR());
                this.overlapP += spr.getOverlapPR().getPrecision() * tn;
                this.overlapR += spr.getOverlapPR().getRecall() * gn;
                this.intersectionP += spr.getIntersectionPR().getPrecision() * tn;
                this.intersectionR += spr.getIntersectionPR().getRecall() * gn;
                this.alignedP += spr.getAlignedPR().getPrecision() * tn;
                this.alignedR += spr.getAlignedPR().getRecall() * gn;
            }
            return this;
        }

        public synchronized Evaluator add(final Evaluator evaluator) {
            synchronized (evaluator) {
                this.score = null;
                this.exactEvaluator.add(evaluator.exactEvaluator);
                this.overlapP += evaluator.overlapP;
                this.overlapR += evaluator.overlapR;
                this.intersectionP += evaluator.intersectionP;
                this.intersectionR += evaluator.intersectionR;
                this.alignedP += evaluator.alignedP;
                this.alignedR += evaluator.alignedR;
            }
            return this;
        }

        public synchronized SetPrecisionRecall getResult() {
            if (this.score == null) {
                final PrecisionRecall exactPR = this.exactEvaluator.getResult();
                final double tn = exactPR.getTP() + exactPR.getFP();
                final double gn = exactPR.getTP() + exactPR.getFN();
                final PrecisionRecall overlapPR = PrecisionRecall.forMeasures( //
                        this.overlapP / tn, this.overlapR / gn, //
                        1 / (2 * (tn + gn) / (this.overlapP + this.overlapR) - 1));
                final PrecisionRecall intersectionPR = PrecisionRecall.forMeasures( //
                        this.intersectionP / tn, this.intersectionR / gn, //
                        1 / (2 * (tn + gn) / (this.intersectionP + this.intersectionR) - 1));
                final PrecisionRecall alignedPR = PrecisionRecall.forMeasures( //
                        this.alignedP / tn, this.alignedR / gn, //
                        1 / (2 * (tn + gn) / (this.alignedP + this.alignedR) - 1));
                this.score = new SetPrecisionRecall(exactPR, overlapPR, intersectionPR, alignedPR);
            }
            return this.score;
        }

        @Override
        public String toString() {
            return getResult().toString();
        }

    }

}

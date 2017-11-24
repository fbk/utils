package eu.fbk.utils.svm;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import eu.fbk.utils.eval.ConfusionMatrix;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Iterator;

public abstract class LabelledVector extends Vector {

    private static final long serialVersionUID = 2L;

    private final Vector vector;

    private final int label;

    static LabelledVector create(final Vector vector, final int label,
            @Nullable final float[] probabilities) {
        if (probabilities == null || probabilities.length == 0) {
            return new LabelledVector0(vector, label);
        } else if (probabilities.length == 2) {
            return new LabelledVector1(vector, label, probabilities[0]);
        } else {
            return new LabelledVectorN(vector, label, probabilities);
        }
    }

    private LabelledVector(final Vector vector, final int label) {
        super(vector.getId());
        this.vector = vector;
        this.label = label;
    }

    public final int getLabel() {
        return this.label;
    }

    public final float getProbability(final int label) {
        Preconditions.checkArgument(label >= 0);
        return doGetProbability(label);
    }

    @Override
    final int doSize() {
        return this.vector.doSize();
    }

    @Override
    final String doGetFeature(final int index) {
        return this.vector.doGetFeature(index);
    }

    @Override
    final float doGetValue(final int index) {
        return this.vector.doGetValue(index);
    }

    abstract float doGetProbability(int label);

    @Override
    final LabelledVector doLabel(final int label, final float... probabilities) {

        if (getLabel() == label) {
            if (probabilities != null && probabilities.length > 0) {
                boolean matchProbabilities = true;
                for (int i = 0; i < probabilities.length; ++i) {
                    if (getProbability(i) != probabilities[i]) {
                        matchProbabilities = false;
                        break;
                    }
                }
                if (matchProbabilities) {
                    return this;
                }
            } else if (getProbability(label) == 1.0f) {
                return this;
            }
        }

        return super.doLabel(label, probabilities);
    }

    @Override
    final Vector doUnlabel() {
        return this.vector.doUnlabel();
    }

    public static ConfusionMatrix evaluate(final Iterable<LabelledVector> goldVectors,
            final Iterable<LabelledVector> predictedVectors, final int numLabels) {

        final int goldSize = Iterables.size(goldVectors);
        final int predictedSize = Iterables.size(predictedVectors);
        if (goldSize != predictedSize) {
            throw new IllegalArgumentException("Number of gold vectors (" + goldSize
                    + ") different from number of predicted vectors (" + predictedSize + ")");
        }

        final double[][] matrix = new double[numLabels][];
        for (int i = 0; i < numLabels; ++i) {
            matrix[i] = new double[numLabels];
        }

        final Iterator<LabelledVector> goldIterator = goldVectors.iterator();
        final Iterator<LabelledVector> predictedIterator = predictedVectors.iterator();
        while (goldIterator.hasNext()) {
            final LabelledVector goldVector = goldIterator.next();
            final LabelledVector predictedVector = predictedIterator.next();
            matrix[goldVector.getLabel()][predictedVector.getLabel()] += 1.0;
        }

        return new ConfusionMatrix(matrix);
    }

    private static final class LabelledVector0 extends LabelledVector {

        private static final long serialVersionUID = 1;

        private LabelledVector0(final Vector vector, final int label) {
            super(vector, label);
        }

        @Override
        float doGetProbability(final int label) {
            Preconditions.checkArgument(label >= 0);
            return label == getLabel() ? 1.0f : 0.0f;
        }

        @Override
        void doToString(final Appendable out) throws IOException {
            out.append(Integer.toString(getLabel())).append(' ');
            super.doToString(out);
        }

    }

    private static final class LabelledVector1 extends LabelledVector {

        private static final long serialVersionUID = 1L;

        private final float probability0;

        private LabelledVector1(final Vector vector, final int label, final float probability0) {
            super(vector, label);
            this.probability0 = probability0;
        }

        @Override
        float doGetProbability(final int label) {
            return label == 0 ? this.probability0 : label == 1 ? 1.0f - this.probability0 : 0.0f;
        }

        @Override
        void doToString(final Appendable out) throws IOException {
            out.append(Integer.toString(getLabel()));
            out.append(" (0:").append(Float.toString(this.probability0)).append(" 1:")
                    .append(Float.toString(1.0f - this.probability0)).append(") ");
            super.doToString(out);
        }

    }

    private static final class LabelledVectorN extends LabelledVector {

        private static final long serialVersionUID = 1;

        private final float[] probabilities;

        private LabelledVectorN(final Vector vector, final int label, final float[] probabilities) {
            super(vector, label);
            this.probabilities = probabilities;
        }

        @Override
        float doGetProbability(final int label) {
            Preconditions.checkArgument(label >= 0);
            return label < this.probabilities.length ? this.probabilities[label] : 0.0f;
        }

        @Override
        void doToString(final Appendable out) throws IOException {
            out.append(Integer.toString(getLabel()));
            out.append(" (");
            for (int i = 0; i < this.probabilities.length; ++i) {
                out.append(i == 0 ? "" : " ").append(Integer.toString(i)).append(':')
                        .append(Float.toString(this.probabilities[i]));
            }
            out.append(") ");
            super.doToString(out);
        }

    }

}

package eu.fbk.utils.eval;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Functional tests for PrecisionRecall class
 *
 * @author Yaroslav Nechaev (remper@me.com)
 */
public class PrecisionRecallTest {
    private static final double COMP_DELTA = 0.00001;

    @Test
    public void testEvaluatorAddMethodEquivalence() {
        PrecisionRecall.Evaluator explicitMethods = PrecisionRecall.evaluator();
        PrecisionRecall.Evaluator implicitMethods = PrecisionRecall.evaluator();
        int truePositives = 5;
        int trueNegatives = 6;
        int falsePositives = 7;
        int falseNegatives = 8;
        explicitMethods.addTP(truePositives);
        explicitMethods.addTN(trueNegatives);
        explicitMethods.addFP(falsePositives);
        explicitMethods.addFN(falseNegatives);
        for (int i = 0; i < truePositives; i++) {
            implicitMethods.addTP();
        }
        for (int i = 0; i < trueNegatives; i++) {
            implicitMethods.addTN();
        }
        for (int i = 0; i < falsePositives; i++) {
            implicitMethods.addFP();
        }
        for (int i = 0; i < falseNegatives; i++) {
            implicitMethods.addFN();
        }
        assertEquals(explicitMethods.getResult().getF1(), implicitMethods.getResult().getF1(), COMP_DELTA);
        assertEquals(explicitMethods.getResult().getAccuracy(), implicitMethods.getResult().getAccuracy(), COMP_DELTA);
    }

    @Test
    public void testZeroCornerCases() {
        PrecisionRecall precRecall = PrecisionRecall.forCounts(1.0, 0.0, 1.0);
        assertEquals("Perfect precision", 1.0, precRecall.getPrecision(), COMP_DELTA);
        precRecall = PrecisionRecall.forCounts(0.0, 1.0, 1.0);
        assertEquals("Zero precision", 0.0, precRecall.getPrecision(), COMP_DELTA);
        assertEquals("Zero recall", 0.0, precRecall.getRecall(), COMP_DELTA);
        precRecall = PrecisionRecall.forCounts(1.0, 1.0, 0.0);
        assertEquals("Perfect recall", 1.0, precRecall.getRecall(), COMP_DELTA);
        precRecall = PrecisionRecall.forCounts(0.0, 0.0, 1.0);
        assertTrue("Undefined f1", Double.isNaN(precRecall.getF1()));
        precRecall = PrecisionRecall.forCounts(0.0, 1.0, 0.0);
        assertTrue("Undefined f1", Double.isNaN(precRecall.getF1()));
    }
}
package quickml.supervised.PredictiveModelsFromPreviousVersionsToBenchMarkAgainst.oldScorers;

import quickml.supervised.PredictiveModelsFromPreviousVersionsToBenchMarkAgainst.OldScorer;
import quickml.supervised.PredictiveModelsFromPreviousVersionsToBenchMarkAgainst.oldTree.OldClassificationCounter;

import java.io.Serializable;
import java.util.Map;

/**
 * A Scorer intended to estimate the impact on the Mean of the Squared Error (MSE)
 * of a branch existing versus not existing.  The value returned is the MSE
 * without the branch minus the MSE with the branch (so higher is better, as
 * is required by the scoreSplit() interface.
 */
public class MSEOldScorer implements OldScorer {
    private final double crossValidationInstanceCorrection;

    public MSEOldScorer(CrossValidationCorrection crossValidationCorrection) {
        if (crossValidationCorrection.equals(CrossValidationCorrection.TRUE)) {
            crossValidationInstanceCorrection = 1.0;
        } else {
            crossValidationInstanceCorrection = 0.0;
        }
    }

    @Override
    public double scoreSplit(final OldClassificationCounter a, final OldClassificationCounter b) {
        OldClassificationCounter parent = OldClassificationCounter.merge(a, b);
        double parentMSE = getTotalError(parent) / parent.getTotal();
        double splitMSE = (getTotalError(a) + getTotalError(b)) / (a.getTotal() + b.getTotal());
        return parentMSE - splitMSE;
    }

    private double getTotalError(OldClassificationCounter cc) {
        double totalError = 0;
        for (Map.Entry<Serializable, Double> e : cc.getCounts().entrySet()) {
            double error = (cc.getTotal()>0) ? 1.0 - e.getValue()/cc.getTotal() : 0;
            double errorSquared = error*error;
            totalError += errorSquared * e.getValue();
        }
        return totalError;
    }

    public enum CrossValidationCorrection {
        TRUE, FALSE
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MSEScorer{");
        sb.append("cvic=").append(crossValidationInstanceCorrection);
        sb.append('}');
        return sb.toString();
    }
}

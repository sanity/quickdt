package quickml.supervised.tree.decisionTree.attributeValueIgnoringStrategies;

import quickml.supervised.tree.attributeValueIgnoringStrategies.AttributeValueIgnoringStrategy;
import quickml.supervised.tree.decisionTree.valueCounters.ClassificationCounter;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by alexanderhawk on 3/18/15.
 */
public class BinaryClassAttributeValueIgnoringStrategy implements AttributeValueIgnoringStrategy<ClassificationCounter> {
    private final int minOccurancesOfAttributeValue;
    private final Serializable minorityClassification;
    private final Serializable majorityClassification;
    private final double majorityToMinorityRatio;
    public BinaryClassAttributeValueIgnoringStrategy(ClassificationCounter cc, final int minOccurancesOfAttributeValue) {
        this.minOccurancesOfAttributeValue = minOccurancesOfAttributeValue;
        this.majorityClassification = ClassificationCounter.getMostPopularClass(cc);
        this.minorityClassification = ClassificationCounter.getLeastPopularClass(cc);
        this.majorityToMinorityRatio = cc.getCount(majorityClassification)/cc.getCount(minorityClassification);
    }

    public boolean shouldWeIgnoreThisValue(final ClassificationCounter termStatistics) {
        Map<Serializable, Double> counts = termStatistics.getCounts();
        if (counts.containsKey(minorityClassification) &&
                counts.get(minorityClassification) > minOccurancesOfAttributeValue) {
            return false;
        }

        if (counts.containsKey(majorityClassification) &&
                counts.get(majorityClassification) > majorityToMinorityRatio * minOccurancesOfAttributeValue) {
            return false;
        }

        if (hasBothClassifications(counts)
                && hasSufficientStatisticsForBothClassifications(counts)) {
            return false;
        }

        return true;
    }

    private boolean hasSufficientStatisticsForBothClassifications(Map<Serializable, Double> counts) {
        return counts.get(majorityClassification) > 0.6 * majorityToMinorityRatio * minOccurancesOfAttributeValue
                && counts.get(minorityClassification) > 0.6 * minOccurancesOfAttributeValue;
    }

    private boolean hasBothClassifications(Map<Serializable, Double> counts) {
        return counts.containsKey(majorityClassification) && counts.containsKey(minorityClassification);
    }

}

package quickml.supervised.tree.decisionTree.attributeValueIgnoringStrategies;


import quickml.supervised.tree.attributeValueIgnoringStrategies.AttributeValueIgnoringStrategy;
import quickml.supervised.tree.attributeValueIgnoringStrategies.AttributeValueIgnoringStrategyBuilder;
import quickml.supervised.tree.decisionTree.valueCounters.ClassificationCounter;

public class BinaryClassAttributeValueIgnoringStrategyBuilder implements AttributeValueIgnoringStrategyBuilder<ClassificationCounter> {

    private static final long serialVersionUID = 0L;
    public BinaryClassAttributeValueIgnoringStrategyBuilder(int minOccurancesOfAttributeValue) {
        this.minOccurancesOfAttributeValue = minOccurancesOfAttributeValue;
    }

    @Override
    public AttributeValueIgnoringStrategy<ClassificationCounter> createAttributeValueIgnoringStrategy(ClassificationCounter cc) {
        return new BinaryClassAttributeValueIgnoringStrategy(cc, minOccurancesOfAttributeValue);
    }

    private int minOccurancesOfAttributeValue;


    public BinaryClassAttributeValueIgnoringStrategyBuilder setMinOccurancesOfAttributeValue(int minOccurancesOfAttributeValue) {
        this.minOccurancesOfAttributeValue = minOccurancesOfAttributeValue;
        return this;
    }

    public BinaryClassAttributeValueIgnoringStrategyBuilder copy() {
        return new BinaryClassAttributeValueIgnoringStrategyBuilder(minOccurancesOfAttributeValue).setMinOccurancesOfAttributeValue(minOccurancesOfAttributeValue);
    }



}
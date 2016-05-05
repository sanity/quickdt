package quickml.supervised.tree.decisionTree.valueCounters;

import quickml.data.instances.ClassifierInstance;
import quickml.supervised.tree.summaryStatistics.ValueCounterProducer;

import java.util.List;

/**
 * Created by alexanderhawk on 4/22/15.
 */
public class ClassificationCounterProducer<I extends ClassifierInstance> implements ValueCounterProducer<I, ClassificationCounter> {
    @Override
    public ClassificationCounter getValueCounter(List<I> instances) {
        return ClassificationCounter.countAll(instances);
    }
}

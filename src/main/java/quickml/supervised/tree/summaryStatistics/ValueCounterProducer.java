package quickml.supervised.tree.summaryStatistics;

import quickml.data.instances.InstanceWithAttributesMap;

import java.util.List;

/**
 * Created by alexanderhawk on 4/22/15.
 */
public interface ValueCounterProducer<I extends InstanceWithAttributesMap<?>, VC extends ValueCounter<VC>> {
    public abstract VC getValueCounter(List<I> instances);
}

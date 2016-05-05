package quickml.supervised.tree.decisionTree.treeBuildContexts;

import quickml.data.instances.ClassifierInstance;
import quickml.supervised.tree.branchFinders.BranchFinderAndReducerFactory;
import quickml.supervised.tree.branchingConditions.BranchingConditions;
import quickml.supervised.tree.decisionTree.valueCounters.ClassificationCounter;
import quickml.supervised.tree.nodes.LeafBuilder;
import quickml.supervised.tree.scorers.ScorerFactory;
import quickml.supervised.tree.summaryStatistics.ValueCounterProducer;
import quickml.supervised.tree.treeBuildContexts.TreeContext;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Created by alexanderhawk on 6/21/15.
 */
public class DTreeContext<I extends ClassifierInstance> extends TreeContext<I, ClassificationCounter> {
    Set<Serializable> classifications;

    public DTreeContext(Set<Serializable> classifications,
                        BranchingConditions<ClassificationCounter> branchingConditions,
                        ScorerFactory<ClassificationCounter> scorerFactory,
                        List<BranchFinderAndReducerFactory<I, ClassificationCounter>> branchFindersAndReducers,
                        LeafBuilder<ClassificationCounter> leafBuilder,
                        ValueCounterProducer<I, ClassificationCounter> valueCounterProducer
    ) {
        super(branchingConditions, scorerFactory, branchFindersAndReducers, leafBuilder, valueCounterProducer);
        this.classifications = classifications;
    }

    public Set<Serializable> getClassifications() {
        return classifications;
    }
}

package quickml.supervised.tree.decisionTree.nodes;

import quickml.supervised.tree.decisionTree.valueCounters.ClassificationCounter;
import quickml.supervised.tree.nodes.Branch;
import quickml.supervised.tree.nodes.LeafBuilder;

/**
 * Created by alexanderhawk on 4/24/15.
 */
public class DTLeafBuilder implements LeafBuilder<ClassificationCounter> {
    private static final long serialVersionUID = 0L;

    public DTLeaf buildLeaf(Branch<ClassificationCounter> parent, ClassificationCounter valueCounter){
        return new DTLeaf(parent, valueCounter, parent==null || parent.isEmpty() ? 0 : parent.getDepth()+1);
    }

    @Override
    public LeafBuilder<ClassificationCounter> copy() {
        return new DTLeafBuilder();
    }
}

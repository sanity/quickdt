package quickml.supervised.tree.attributeIgnoringStrategies;

import com.google.common.collect.Lists;
import quickml.supervised.tree.nodes.Branch;

import java.util.List;

/**
 * Created by alexanderhawk on 2/28/15.
 */
public class CompositeAttributeIgnoringStrategy implements AttributeIgnoringStrategy {
    private static final long serialVersionUID = 0L;

    private List<AttributeIgnoringStrategy> attributeIgnoringStrategies = Lists.newArrayList();

    public CompositeAttributeIgnoringStrategy(List<AttributeIgnoringStrategy> attributeIgnoringStrategies) {
        this.attributeIgnoringStrategies = attributeIgnoringStrategies;
    }

    @Override
    public CompositeAttributeIgnoringStrategy copy() {
        List<AttributeIgnoringStrategy> copies = Lists.newArrayList();
        for (AttributeIgnoringStrategy attributeIgnoringStrategy : attributeIgnoringStrategies) {
            copies.add(attributeIgnoringStrategy.copy());
        }
        return new CompositeAttributeIgnoringStrategy(copies);
    }

    @Override
    public boolean ignoreAttribute(String attribute, Branch parent) {
        for (AttributeIgnoringStrategy attributeIgnoringStrategy : attributeIgnoringStrategies) {
            if (attributeIgnoringStrategy.ignoreAttribute(attribute, parent)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "CompositeAttributeIgnoringStrategy{" +
                "oldAttributeIgnoringStrategies=" + attributeIgnoringStrategies +
                '}';
    }
}

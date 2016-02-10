package quickml.data.instances;

import quickml.data.AttributesMap;

import java.io.Serializable;

/**
 * Created by alexanderhawk on 4/14/15.
 */
public class RegressionInstance extends InstanceWithAttributesMap<Double> {
    public RegressionInstance(AttributesMap attributes, Double label) {
        super(attributes, label, 1.0);
    }
    public RegressionInstance(AttributesMap attributes, Double label, double weight) {
        super(attributes, label, weight);
    }

}

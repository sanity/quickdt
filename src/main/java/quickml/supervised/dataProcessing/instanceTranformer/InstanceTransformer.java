package quickml.supervised.dataProcessing.instanceTranformer;

import quickml.data.instances.Instance;

/**
 * Created by alexanderhawk on 10/14/15.
 */
public interface InstanceTransformer<I extends Instance, R extends Instance> {
    /**
     * particular implementations may mutate the input instance, others may not.  Be sure to see the documentation accordingly
     */
    R transformInstance(I instance);
}

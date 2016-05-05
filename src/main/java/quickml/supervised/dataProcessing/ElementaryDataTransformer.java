package quickml.supervised.dataProcessing;

import com.google.common.collect.Lists;
import quickml.data.instances.Instance;
import quickml.supervised.dataProcessing.instanceTranformer.InstanceTransformer;

import java.util.List;

/**
 * Created by alexanderhawk on 10/14/15.
 */
public class ElementaryDataTransformer<I extends Instance, R extends Instance> {

    List<InstanceTransformer<I, I>> input2InputTypeTransformers = Lists.newArrayList();
    InstanceTransformer<I, R> input2ReturnTypeTransformer;
    List<InstanceTransformer<R, R>> returnType2ReturnTypeTransformer = Lists.newArrayList();

    public ElementaryDataTransformer(List<InstanceTransformer<I, I>> input2InputTypeTransformers) {
        this.input2InputTypeTransformers = input2InputTypeTransformers;
    }

    public ElementaryDataTransformer(List<InstanceTransformer<I, I>> input2InputTypeTransformers, InstanceTransformer<I, R> input2ReturnTypeTransformer) {
        this(input2InputTypeTransformers);
        this.input2ReturnTypeTransformer = input2ReturnTypeTransformer;
    }

    public ElementaryDataTransformer(List<InstanceTransformer<I, I>> input2InputTypeTransformers, InstanceTransformer<I, R> input2ReturnTypeTransformer, List<InstanceTransformer<R, R>> returnType2ReturnTypeTransformer) {
        this(input2InputTypeTransformers, input2ReturnTypeTransformer);
        this.returnType2ReturnTypeTransformer = returnType2ReturnTypeTransformer;
    }

    public List<R> transformInstances(List<I> inputInstances) {
        I transformedInput;
        R transformedOutput;
        List<R> outputInstances = Lists.newArrayList();

        for (I instance : inputInstances) {
            transformedInput = doInput2InputTransformations(instance);
            if (input2ReturnTypeTransformer == null) {
                transformedOutput = (R) transformedInput;
            } else {
                transformedOutput = doInput2ReturnTypeTransformation(transformedInput);
                transformedOutput = doReturnType2ReturnTypeTransformations(transformedOutput);
            }
            outputInstances.add(transformedOutput);
        }
        return outputInstances;
    }

    private R doReturnType2ReturnTypeTransformations(R transformedOutput) {
        for (InstanceTransformer<R, R> output2Output : returnType2ReturnTypeTransformer) {
            transformedOutput = output2Output.transformInstance(transformedOutput);
        }
        return transformedOutput;
    }

    private R doInput2ReturnTypeTransformation(I transformedInput) {
        return input2ReturnTypeTransformer.transformInstance(transformedInput);
    }

    private I doInput2InputTransformations(I instance) {
        I transformedInput = instance;
        for (InstanceTransformer<I, I> input2Input : input2InputTypeTransformers) {
            transformedInput = input2Input.transformInstance(transformedInput);
        }
        return transformedInput;
    }
}


package quickml.supervised.neuralNetwork;

import com.google.common.collect.Lists;
import org.testng.Assert;
import org.testng.annotations.Test;
import quickml.supervised.neuralNetwork.activationFunctions.Sigmoid;

import java.util.Collections;
import java.util.List;

public class FeedForwardNeuralNetworkTest {
    @Test
    public void testNetCreation() {
        FeedForwardNeuralNetwork feedForwardNeuralNetwork = new FeedForwardNeuralNetwork(Lists.newArrayList(1, 1));
        Assert.assertEquals(feedForwardNeuralNetwork.layers.size(), 2);
        Assert.assertEquals(feedForwardNeuralNetwork.getNeuronCount(), 2);
        List<Neuron> firstLayer = feedForwardNeuralNetwork.layers.get(0);
        Assert.assertEquals(firstLayer.size(), 1);
        Neuron firstNeuron = firstLayer.get(0);
        verifyFirstNeuron(firstNeuron);
        List<Neuron> secondLayer = feedForwardNeuralNetwork.layers.get(1);
        Assert.assertEquals(secondLayer.size(), 1);
        Neuron secondNeuron = secondLayer.get(0);
        Assert.assertEquals(secondNeuron.getInputs().size(), 1);
        Assert.assertEquals(secondNeuron.getInputs().get(0), firstNeuron.getOutputs().get(0));
        Assert.assertTrue(secondNeuron.getOutputs().isEmpty());
        verifySynapse(firstNeuron, secondNeuron);
    }

    private void verifyFirstNeuron(Neuron firstNeuron) {
        Assert.assertEquals(firstNeuron.getId(), 0);
        Assert.assertEquals(firstNeuron.getOutputs().size(), 1);
    }

    private void verifySynapse(Neuron firstNeuron, Neuron secondNeuron) {
        Synapse firstSynapse = firstNeuron.getOutputs().get(0);
        Assert.assertEquals(firstSynapse.a, firstNeuron);
        Assert.assertEquals(firstSynapse.b, secondNeuron);
    }

    @Test
    public void feedForwardTest() {
        FeedForwardNeuralNetwork feedForwardNeuralNetwork = new FeedForwardNeuralNetwork(Lists.newArrayList(2, 1));
        List<Neuron> inputLayer = feedForwardNeuralNetwork.layers.get(0);
        Neuron inputNeuron1 = inputLayer.get(0);
        inputNeuron1.getOutputs().get(0).updateWeight(0.5);
        Neuron inputNeuron2 = inputLayer.get(1);
        inputNeuron2.getOutputs().get(0).updateWeight(0.4);
        Neuron outputNeuron = feedForwardNeuralNetwork.layers.get(1).get(0);
        outputNeuron.setBias(0.5);
        double inputNeuron1Output = 0.3;
        double inputNeuron2Output = 0.2;
        double outputNeuronInput = inputNeuron1Output * 0.5 + inputNeuron2Output * 0.4;
        double outputNeuronOutput = Sigmoid.SINGLETON.apply(outputNeuronInput + 0.5);
        List<Double> output = feedForwardNeuralNetwork.predict(Lists.newArrayList(0.3, 0.2));
        Assert.assertEquals(output.get(0), outputNeuronOutput);
    }

    @Test
    public void convergeTest() {
        FeedForwardNeuralNetwork feedForwardNeuralNetwork = new FeedForwardNeuralNetwork(Lists.newArrayList(2, 2, 1));
        int iterationCount = 1000;
        for (int x=0; x< iterationCount; x++) {
            feedForwardNeuralNetwork.updateWeightsAndBiases(Lists.newArrayList(0.0, 0.0), Lists.newArrayList(0.43), 0.1);
            double prediction = feedForwardNeuralNetwork.predict(Lists.newArrayList(0.0, 0.0)).get(0);
            double error = Math.abs(prediction - 0.43);
            if (error < 0.0001) return;
        }
        Assert.fail(String.format("Failed to converge after %d iterations", iterationCount));
    }

    @Test
    public void orTrainTest() {
        FeedForwardNeuralNetwork feedForwardNeuralNetwork = new FeedForwardNeuralNetwork(Lists.newArrayList(2, 2, 1));
        List<SimpleTrainingPair> trainingData = Lists.newArrayList();
        trainingData.add(new SimpleTrainingPair(0, 0, 0));
        trainingData.add(new SimpleTrainingPair(1, 0, 1));
        trainingData.add(new SimpleTrainingPair(0, 1, 1));
        trainingData.add(new SimpleTrainingPair(1, 1, 1));

        System.out.println("OR: RMSE < 0.1 after " + testNetWithTrainingData(feedForwardNeuralNetwork, trainingData) + " training cycles");
    }

    @Test
    public void andTrainTest() {
        FeedForwardNeuralNetwork feedForwardNeuralNetwork = new FeedForwardNeuralNetwork(Lists.newArrayList(2, 2, 1));
        List<SimpleTrainingPair> trainingData = Lists.newArrayList();
        trainingData.add(new SimpleTrainingPair(0, 0, 0));
        trainingData.add(new SimpleTrainingPair(1, 0, 0));
        trainingData.add(new SimpleTrainingPair(0, 1, 0));
        trainingData.add(new SimpleTrainingPair(1, 1, 1));

        System.out.println("AND: RMSE < 0.1 after " + testNetWithTrainingData(feedForwardNeuralNetwork, trainingData) + " training cycles");
    }

    @Test()
    public void xorTrainTest() {
        FeedForwardNeuralNetwork feedForwardNeuralNetwork = new FeedForwardNeuralNetwork(Lists.newArrayList(2, 2, 1));
        List<SimpleTrainingPair> trainingData = Lists.newArrayList();
        trainingData.add(new SimpleTrainingPair(0, 0, 0));
        trainingData.add(new SimpleTrainingPair(1, 0, 1));
        trainingData.add(new SimpleTrainingPair(0, 1, 1));
        trainingData.add(new SimpleTrainingPair(1, 1, 0));

        System.out.println("XOR: RMSE < 0.1 after " + testNetWithTrainingData(feedForwardNeuralNetwork, trainingData) + " training cycles");
    }

    public int testNetWithTrainingData(FeedForwardNeuralNetwork feedForwardNeuralNetwork, List<SimpleTrainingPair> trainingData) {
        int trainingCycles = 200000;
        for (int x=0; x< trainingCycles; x++) {
            for (SimpleTrainingPair simpleTrainingPair : trainingData) {
                feedForwardNeuralNetwork.updateWeightsAndBiases(simpleTrainingPair.inputs, simpleTrainingPair.outputs, 1e-1f);
            }
            double mse = 0;
            for (SimpleTrainingPair simpleTrainingPair : trainingData) {
                List<Double> prediction = feedForwardNeuralNetwork.predict(simpleTrainingPair.inputs);
                double error = prediction.get(0) - simpleTrainingPair.outputs.get(0);
                double errorSquared = error*error;
                mse += errorSquared;
            }
            double rmse = Math.sqrt(mse / trainingData.size());
            if (rmse < 0.1) {
                return x;
            }
        }
        Assert.fail(String.format("Failed to reach an RMSE < 0.1 after %d training cycles", trainingCycles));
        return 0;
    }

    private static class SimpleTrainingPair {
        public List<Double> inputs;
        public List<Double> outputs;

        public SimpleTrainingPair(double input1, double input2, double output) {
            inputs = Lists.newArrayList(input1, input2);
            outputs = Collections.singletonList(output);
        }
    }
}
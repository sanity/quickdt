package quickdt.crossValidation.crossValLossFunctions;

import quickdt.predictiveModels.Prediction;

import java.io.Serializable;

/**
 * Created by alexanderhawk on 7/30/14.
 */
public class LabelPredictionWeight<P extends Prediction> {
    double weight;
    Serializable label;
    P prediction;

    public double getWeight() {
        return weight;
    }

    public Serializable getLabel() {
        return label;
    }

    public P getPrediction() {
        return prediction;
    }

    public LabelPredictionWeight(Serializable label, P prediction, double weight) {
        this.label = label;
        this.prediction = prediction;
        this.weight = weight;
    }
}
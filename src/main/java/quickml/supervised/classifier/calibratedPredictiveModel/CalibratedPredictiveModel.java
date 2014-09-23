package quickml.supervised.classifier.calibratedPredictiveModel;


/**
 * Created by alexanderhawk on 3/10/14.
 */

/*
  This class uses the Pool-adjacent violators algorithm to calibrate the probabilities returned by a random forest of probability estimation trees.

public class CalibratedPredictiveModel extends Classifier {
    private static final long serialVersionUID = 8291739965981425742L;
    public final Calibrator calibrator;
    public final PredictiveModel<AttributesMap, Map<Serializable, Double>> predictiveModel;
    public final Serializable positiveClassification;

    public CalibratedPredictiveModel (PredictiveModel<AttributesMap, Map<Serializable, Double>> predictiveModel, Calibrator calibrator, Serializable positiveClassification) {
        Preconditions.checkArgument(!(predictiveModel instanceof CalibratedPredictiveModel));
        this.predictiveModel = predictiveModel;
        this.calibrator = calibrator;
        this.positiveClassification = positiveClassification;
    }

    @Override
    public Map<Serializable, Double> predict(AttributesMap attributes) {
        Map<Serializable, Double> predictions = predictiveModel.predict(attributes);
        Map<Serializable, Double> calibratedPredictions = AttributesMap.newHashMap() ;
        for(Map.Entry<Serializable, Double> prediction : predictions.entrySet()) {
            calibratedPredictions.put(prediction.getKey(), calibrator.correct(prediction.getValue()));
        }
        return calibratedPredictions;
    }


    @Override
    public void dump(Appendable appendable) {
        predictiveModel.dump(appendable);
    }
}
*/
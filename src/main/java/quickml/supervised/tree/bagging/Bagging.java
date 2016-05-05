package quickml.supervised.tree.bagging;

import quickml.data.instances.InstanceWithAttributesMap;

import java.io.Serializable;
import java.util.List;

/**
 * Created by alexanderhawk on 4/5/15.
 */
public interface Bagging {
    <L extends Serializable, T extends InstanceWithAttributesMap<L>> TrainingDataPair<L, T> separateTrainingDataFromOutOfBagData(List<T> trainingData);

    class TrainingDataPair<L extends Serializable, T extends InstanceWithAttributesMap<L>> {
        public List<T> trainingData;
        public List<T> outOfBagTrainingData;

        public TrainingDataPair(List<T> trainingData, List<T> outOfBagTrainingData) {
            this.trainingData = trainingData;
            this.outOfBagTrainingData = outOfBagTrainingData;
        }
    }
}

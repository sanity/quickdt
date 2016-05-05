package quickml.supervised.featureEngineering;

import com.google.common.collect.Lists;
import org.testng.annotations.Test;
import quickml.data.*;

import quickml.data.instances.ClassifierInstance;
import quickml.data.instances.InstanceWithAttributesMap;
import quickml.supervised.PredictiveModel;
import quickml.supervised.PredictiveModelBuilder;
import quickml.supervised.featureEngineering1.AttributesEnrichStrategy;
import quickml.supervised.featureEngineering1.AttributesEnricher;
import quickml.supervised.featureEngineering1.FeatureEngineeredClassifier;
import quickml.supervised.featureEngineering1.FeatureEngineeringClassifierBuilder;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatureEngineeringClassifierBuilderTest {

    private static Integer valueToTest = 1;

    @Test
    public void simpleTest() {
        List<InstanceWithAttributesMap<?>> trainingData = Lists.newArrayList();
        trainingData.add(new ClassifierInstance(AttributesMap.newHashMap(), 1));
        PredictiveModelBuilder testPMB = new TestPMBuilder();
        FeatureEngineeringClassifierBuilder feBuilder = new FeatureEngineeringClassifierBuilder(testPMB, Lists.newArrayList(new TestAEBS()));
        final FeatureEngineeredClassifier predictiveModel = feBuilder.buildPredictiveModel(trainingData);
        predictiveModel.getProbability(trainingData.get(0).getAttributes(), valueToTest);
    }

    public static class TestAEBS implements AttributesEnrichStrategy {

        @Override
        public AttributesEnricher build(final Iterable<InstanceWithAttributesMap<?>> trainingData) {
            return new AttributesEnricher() {
                private static final long serialVersionUID = -4851048617673142530L;

                public AttributesMap apply(@Nullable final AttributesMap attributes) {
                    AttributesMap er = AttributesMap.newHashMap();
                    er.putAll(attributes);
                    er.put("enriched", 1);
                    return er;
                }
            };
        }
    }

    public static class TestPMBuilder implements PredictiveModelBuilder<TestPM, InstanceWithAttributesMap<?>> {
        @Override
        public TestPM buildPredictiveModel(Iterable<InstanceWithAttributesMap<?>> trainingData) {
            for (InstanceWithAttributesMap<?> instance : trainingData) {
                if (!instance.getAttributes().containsKey("enriched")) {
                    throw new IllegalArgumentException("Predictive model training data must contain enriched instances");
                }
            }
            return new TestPM();
        }

        @Override
        public void updateBuilderConfig(Map<String, Serializable> config) {

        }
    }


    public static class TestPM implements PredictiveModel<AttributesMap, PredictionMap> {
        private static final long serialVersionUID = -3449746370937561259L;

        @Override
        public PredictionMap predict(AttributesMap attributes) {
            if (!attributes.containsKey("enriched")) {
                throw new IllegalArgumentException("Predictive model training data must contain enriched instances");
            }
            Map<Serializable, Double> map = new HashMap<>();
            map.put(valueToTest, 0.0);
            return new PredictionMap(map);
        }

        @Override
        public PredictionMap predictWithoutAttributes(AttributesMap attributes, Set<String> attributesToIgnore) {
            return predict(attributes);
        }

    }
}



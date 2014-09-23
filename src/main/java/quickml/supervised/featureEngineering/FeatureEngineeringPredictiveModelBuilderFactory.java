package quickml.supervised.featureEngineering;

import quickml.data.AttributesMap;
import quickml.data.PredictionMap;

import quickml.supervised.PredictiveModelBuilderFactory;
import quickml.supervised.predictiveModelOptimizer.FieldValueRecommender;
import quickml.supervised.PredictiveModel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by ian on 5/21/14.
 */
public class FeatureEngineeringPredictiveModelBuilderFactory implements PredictiveModelBuilderFactory<AttributesMap,FeatureEngineeredPredictiveModel, FeatureEngineeringPredictiveModelBuilder> {


    private final PredictiveModelBuilderFactory<AttributesMap, ? extends PredictiveModel<AttributesMap, PredictionMap>, ?> wrappedBuilderBuilder;

    private final List<? extends AttributesEnrichStrategy> enrichStrategies;

    public FeatureEngineeringPredictiveModelBuilderFactory(
            PredictiveModelBuilderFactory<AttributesMap, ? extends PredictiveModel<AttributesMap, PredictionMap>, ?> wrappedBuilderBuilder,
            List<? extends AttributesEnrichStrategy> enrichStrategies) {
        this.wrappedBuilderBuilder = wrappedBuilderBuilder;
        this.enrichStrategies = enrichStrategies;
    }

    @Override
    public Map<String, FieldValueRecommender> createDefaultParametersToOptimize() {
        // Currently we don't have any parameters for this builderBuilder
        return wrappedBuilderBuilder.createDefaultParametersToOptimize();
    }

    @Override
    public FeatureEngineeringPredictiveModelBuilder buildBuilder(final Map<String, Object> predictiveModelConfig) {
        return new FeatureEngineeringPredictiveModelBuilder(wrappedBuilderBuilder.buildBuilder(predictiveModelConfig), enrichStrategies);
    }
}

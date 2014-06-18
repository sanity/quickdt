package quickdt.predictiveModels.splitOnAttributePredictiveModel;

import com.google.common.collect.Maps;
import quickdt.predictiveModelOptimizer.FieldValueRecommender;
import quickdt.predictiveModelOptimizer.fieldValueRecommenders.FixedOrderRecommender;
import quickdt.predictiveModels.PredictiveModelBuilderBuilder;

import java.util.Map;
import java.util.Set;

/**
 * Created by chrisreeves on 6/10/14.
 */
public class SplitOnAttributePMBuilderBuilder implements PredictiveModelBuilderBuilder<SplitOnAttributePM, SplitOnAttributePMBuilder> {
    private static final String MIN_AMOUNT_CROSS_DATA_TOTAL = "minAmountCrossDataTotal";
    private static final String MIN_AMOUNT_CROSS_DATA_CLASSIFICATION = "minAmountCrossDataClassificaiton";
    private static final String PERCENT_CROSS_DATA = "percentCrossData";
    private final PredictiveModelBuilderBuilder<?, ?> wrappedBuilderBuilder;
    private final String attributeKey;
    private final Set<String> attributeWhiteList;

    public SplitOnAttributePMBuilderBuilder(PredictiveModelBuilderBuilder<?, ?> wrappedBuilderBuilder, String attributeKey, Set<String> attributeWhiteList) {
        this.wrappedBuilderBuilder = wrappedBuilderBuilder;
        this.attributeKey = attributeKey;
        this.attributeWhiteList = attributeWhiteList;
    }

    @Override
    public Map<String, FieldValueRecommender> createDefaultParametersToOptimize() {
        Map<String, FieldValueRecommender> parametersToOptimize = Maps.newHashMap();
        parametersToOptimize.putAll(wrappedBuilderBuilder.createDefaultParametersToOptimize());
        parametersToOptimize.put(MIN_AMOUNT_CROSS_DATA_TOTAL, new FixedOrderRecommender(0, 100, 1000));
        parametersToOptimize.put(PERCENT_CROSS_DATA, new FixedOrderRecommender(0.1, 0.2, 0.5));
        parametersToOptimize.put(MIN_AMOUNT_CROSS_DATA_CLASSIFICATION, new FixedOrderRecommender(0, 10, 100));
        return parametersToOptimize;
    }

    @Override
    public SplitOnAttributePMBuilder buildBuilder(final Map<String, Object> predictiveModelConfig) {
        final long minAmountCrossData = (Long) predictiveModelConfig.get(MIN_AMOUNT_CROSS_DATA_TOTAL);
        final double percentCrossData = (Double) predictiveModelConfig.get(PERCENT_CROSS_DATA);
        final long minAmountCrossDataClassification = (Long) predictiveModelConfig.get(MIN_AMOUNT_CROSS_DATA_CLASSIFICATION);
        return new SplitOnAttributePMBuilder(attributeKey, wrappedBuilderBuilder.buildBuilder(predictiveModelConfig),
                minAmountCrossData, percentCrossData, attributeWhiteList, minAmountCrossDataClassification);
    }
}

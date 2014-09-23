package quickml.supervised.featureEngineering;

import quickml.data.AttributesMap;
import quickml.data.PredictionMap;
import quickml.supervised.classifier.AbstractClassifier;
import quickml.supervised.PredictiveModel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A predictive model that wraps another predictive model but modifies the input
 * Attributes based on one or more "enrichers".  This objected is created by a
 * {@link FeatureEngineeringPredictiveModelBuilder}.
 */
public class FeatureEngineeredPredictiveModel extends AbstractClassifier {
    private static final long serialVersionUID = 7279329500376419142L;
    private final PredictiveModel<AttributesMap, PredictionMap> wrappedPredictiveModel;
    private final List<AttributesEnricher> attributesEnrichers;

    public FeatureEngineeredPredictiveModel(PredictiveModel<AttributesMap, PredictionMap> wrappedPredictiveModel, List<AttributesEnricher> attributesEnrichers) {
        this.wrappedPredictiveModel = wrappedPredictiveModel;
        this.attributesEnrichers = attributesEnrichers;
    }

    @Override
    public PredictionMap predict(AttributesMap attributes) {
        AttributesMap enrichedAttributes = enrichAttributes(attributes);
        return wrappedPredictiveModel.predict(enrichedAttributes);
    }

    private AttributesMap enrichAttributes(final AttributesMap attributes) {
        AttributesMap enrichedAttributes = attributes;
        for (AttributesEnricher attributesEnricher : attributesEnrichers) {
            enrichedAttributes = attributesEnricher.apply(enrichedAttributes);
        }
        return enrichedAttributes;
    }

    @Override
    public void dump(final Appendable appendable) {
        wrappedPredictiveModel.dump(appendable);
    }
}

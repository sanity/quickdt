package quickml.supervised.classifier.splitOnAttribute;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickml.data.AttributesMap;
import quickml.data.Instance;
import quickml.data.InstanceImpl;
import quickml.supervised.classifier.Classifier;
import quickml.supervised.PredictiveModelBuilder;
import quickml.supervised.UpdatablePredictiveModelBuilder;
import quickml.supervised.classifier.decisionTree.tree.ClassificationCounter;

import java.io.Serializable;
import java.util.*;

/**
 * Created by ian on 5/29/14.
 */
public class SplitOnAttributeClassifierBuilder implements UpdatablePredictiveModelBuilder<AttributesMap, SplitOnAttributeClassifier> {
    private static final  Logger logger =  LoggerFactory.getLogger(SplitOnAttributeClassifierBuilder.class);

    public static final Double NO_VALUE_PLACEHOLDER = Double.MIN_VALUE;

    private final String attributeKey;
    private final UpdatablePredictiveModelBuilder<AttributesMap, ? extends Classifier> wrappedBuilder;

    private final int minimumAmountTotalCrossData;
    private final double percentCrossData;
    private final Set<String> attributeWhiteList;
    private final int minimumAmountCrossDataPerClassification;

    public SplitOnAttributeClassifierBuilder(String attributeKey, UpdatablePredictiveModelBuilder<AttributesMap, ? extends Classifier> wrappedBuilder, int minimumAmountCrossData, double percentCrossData, Set<String> attributeWhiteList, int minimumAmountCrossDataPerClassification) {

        this.attributeKey = attributeKey;
        this.wrappedBuilder = wrappedBuilder;
        this.minimumAmountTotalCrossData = minimumAmountCrossData;
        this.percentCrossData = percentCrossData;
        this.attributeWhiteList = attributeWhiteList;
        this.minimumAmountCrossDataPerClassification = minimumAmountCrossDataPerClassification;
    }

    @Override
    public SplitOnAttributeClassifier buildPredictiveModel(final Iterable<? extends Instance<AttributesMap>> trainingData) {
        Map<Serializable, ArrayList<Instance<AttributesMap>>> splitTrainingData = splitTrainingData(trainingData);

        Map<Serializable, Classifier> splitModels = Maps.newHashMap();
        for (Map.Entry<Serializable, ArrayList<Instance<AttributesMap>>> trainingDataEntry : splitTrainingData.entrySet()) {
            logger.info("Building predictive model for "+attributeKey+"="+trainingDataEntry.getKey());
            setID(trainingDataEntry.getKey());
            splitModels.put(trainingDataEntry.getKey(), wrappedBuilder.buildPredictiveModel(trainingDataEntry.getValue()));
        }

        logger.info("Building default predictive model");
        setID(null);
        final Classifier defaultPM = wrappedBuilder.buildPredictiveModel(trainingData);
        return new SplitOnAttributeClassifier(attributeKey, splitModels, defaultPM);
    }

    @Override
    public PredictiveModelBuilder<AttributesMap, SplitOnAttributeClassifier> updatable(boolean updatable) {
        this.wrappedBuilder.updatable(updatable);
        return this;
    }

    private Map<Serializable, ArrayList<Instance<AttributesMap>>> splitTrainingData(Iterable<? extends Instance<AttributesMap>> trainingData) {
        Map<Serializable, ArrayList<Instance<AttributesMap>>> splitTrainingData = Maps.newHashMap();
        ArrayList<Instance<AttributesMap>> allData = new ArrayList<>();
        for (Instance<AttributesMap> instance : trainingData) {
            Serializable value = instance.getAttributes().get(attributeKey);
            if (value == null) value = NO_VALUE_PLACEHOLDER;
            ArrayList<Instance<AttributesMap>> splitData = splitTrainingData.get(value);
            if (splitData == null) {
                splitData = Lists.newArrayList();
                splitTrainingData.put(value, splitData);
            }
            splitData.add(instance);
            allData.add(instance);
        }

        crossPollinateData(splitTrainingData, allData);
        return splitTrainingData;
    }

    /*
    * Add data to each split data set based on the desired cross data values. Maintain the same ratio of classifications in the split set by
    * selecting that ratio from outside sets. Only keep the attributes in the supporting instances that in in the white list
    * */
    private void crossPollinateData(Map<Serializable, ArrayList<Instance<AttributesMap>>> splitTrainingData, ArrayList<Instance<AttributesMap>> allData) {
        for(Map.Entry<Serializable, ArrayList<Instance<AttributesMap>>> entry : splitTrainingData.entrySet()) {
            ClassificationCounter splitClassificationCounter = ClassificationCounter.countAll(entry.getValue());
            long amountCrossData = (long) Math.max(splitClassificationCounter.getTotal() * percentCrossData, minimumAmountTotalCrossData);
            Set<Instance<AttributesMap>> crossData = new HashSet<>();
            ClassificationCounter crossDataCount = new ClassificationCounter();
            for(int i = allData.size()-1; i >= 0; i--) {
                Instance<AttributesMap>instance = allData.get(i);
                double classificationRatio = splitClassificationCounter.getCount(instance.getLabel()) / splitClassificationCounter.getTotal();
                double targetCount = Math.max(classificationRatio * amountCrossData, minimumAmountCrossDataPerClassification);
                if(shouldAddInstance(entry.getKey(), instance, crossDataCount, targetCount)) {
                    crossData.add(cleanSupportingData(instance));
                    crossDataCount.addClassification(instance.getLabel(), instance.getWeight());
                }
                if(crossDataCount.getTotal() >= amountCrossData) {
                    break;
                }
            }
            //cross pollinate data
            entry.getValue().addAll(crossData);
        }
    }

    /*
     * Add instances such that the ratio of classifications is unchanged
    * */
    private boolean shouldAddInstance(Serializable attributeValue, Instance<AttributesMap> instance, ClassificationCounter crossDataCount, double targetCount) {
        if (!attributeValue.equals(instance.getAttributes().get(attributeKey))) {
            if (targetCount > crossDataCount.getCount(instance.getLabel())) {
                return true;
            }
        }
        return false;
    }

    private Instance<AttributesMap>cleanSupportingData(Instance<AttributesMap> instance) {
        AttributesMap attributes = AttributesMap.newHashMap();
        for (String key : instance.getAttributes().keySet()) {
            if (attributeWhiteList.isEmpty() || attributeWhiteList.contains(key)) {
                attributes.put(key, instance.getAttributes().get(key));
            }
        }
        return new InstanceImpl(attributes, instance.getLabel(), instance.getWeight());
    }

    @Override
    public void setID(Serializable id) {
        wrappedBuilder.setID(id);
    }

    @Override
    public void updatePredictiveModel(SplitOnAttributeClassifier predictiveModel, Iterable<? extends Instance<AttributesMap>> newData, boolean splitNodes) {
        if (wrappedBuilder instanceof UpdatablePredictiveModelBuilder) {
            Map<Serializable, ArrayList<Instance<AttributesMap>>> splitNewData = splitTrainingData(newData);
            for (Map.Entry<Serializable, ArrayList<Instance<AttributesMap>>> newDataEntry : splitNewData.entrySet()) {
                Classifier pm = predictiveModel.getSplitModels().get(newDataEntry.getKey());
                if(pm == null) {
                    logger.info("Building predictive model for "+attributeKey+"="+newDataEntry.getKey());
                    setID(newDataEntry.getKey());
                    pm = wrappedBuilder.buildPredictiveModel(newDataEntry.getValue());
                    predictiveModel.getSplitModels().put(newDataEntry.getKey(), pm);
                } else {
                    logger.info("Updating predictive model for "+attributeKey+"="+newDataEntry.getKey());
                    ((UpdatablePredictiveModelBuilder) wrappedBuilder).updatePredictiveModel(pm, newDataEntry.getValue(), splitNodes);
                }
            }
            logger.info("Updating default predictive model");
            setID(null);
            ((UpdatablePredictiveModelBuilder) wrappedBuilder).updatePredictiveModel(predictiveModel.getDefaultPM(), newData, splitNodes);
        } else {
            throw new RuntimeException("Cannot update predictive model without UpdatablePredictiveModelBuilder");
        }
    }

    @Override
    public void stripData(SplitOnAttributeClassifier predictiveModel) {
        if (wrappedBuilder instanceof UpdatablePredictiveModelBuilder) {
            for(Classifier pm : predictiveModel.getSplitModels().values()) {
                ((UpdatablePredictiveModelBuilder) wrappedBuilder).stripData(pm);
            }
            ((UpdatablePredictiveModelBuilder) wrappedBuilder).stripData(predictiveModel.getDefaultPM());
        } else {
            throw new RuntimeException("Cannot strip data without UpdatablePredictiveModelBuilder");
        }
    }
}

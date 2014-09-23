package quickml.supervised.classifier.temporallyWeightClassifier;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Hours;
import quickml.data.AttributesMap;
import quickml.supervised.crossValidation.dateTimeExtractors.DateTimeExtractor;
import quickml.data.Instance;
import quickml.supervised.classifier.Classifier;
import quickml.supervised.PredictiveModelBuilder;
import quickml.supervised.UpdatablePredictiveModelBuilder;
import quickml.supervised.classifier.decisionTree.tree.ClassificationCounter;

import java.io.Serializable;
import java.util.*;

/**
 * Created by ian on 5/29/14.
 */
public class TemporallyReweightedClassifierBuilder implements UpdatablePredictiveModelBuilder<AttributesMap,TemporallyReweightedClassifier> {
    private static final double DEFAULT_DECAY_CONSTANT = 173; //approximately 5 days
    private double decayConstantOfPositive = DEFAULT_DECAY_CONSTANT;
    private double decayConstantOfNegative = DEFAULT_DECAY_CONSTANT;
    private final PredictiveModelBuilder<AttributesMap, ? extends Classifier> wrappedBuilder;
    private final DateTimeExtractor dateTimeExtractor;
    private final Serializable positiveClassification;

    public TemporallyReweightedClassifierBuilder(PredictiveModelBuilder<AttributesMap, ? extends Classifier> wrappedBuilder, DateTimeExtractor dateTimeExtractor) {
        this(wrappedBuilder, dateTimeExtractor, 1.0);
    }

    public TemporallyReweightedClassifierBuilder(PredictiveModelBuilder<AttributesMap, ? extends Classifier> wrappedBuilder, DateTimeExtractor dateTimeExtractor, Serializable positiveClassification) {
        this.wrappedBuilder = wrappedBuilder;
        this.dateTimeExtractor = dateTimeExtractor;
        this.positiveClassification = positiveClassification;
    }

    public TemporallyReweightedClassifierBuilder halfLifeOfPositive(double halfLifeOfPositiveInDays) {
        this.decayConstantOfPositive = halfLifeOfPositiveInDays * DateTimeConstants.HOURS_PER_DAY / Math.log(2);
        return this;
    }

    public TemporallyReweightedClassifierBuilder halfLifeOfNegative(double halfLifeOfNegativeInDays) {
        this.decayConstantOfNegative = halfLifeOfNegativeInDays * DateTimeConstants.HOURS_PER_DAY / Math.log(2);
        return this;
    }

    @Override
    public void setID(Serializable iD) {
        wrappedBuilder.setID(iD);
    }

    @Override
    public TemporallyReweightedClassifier buildPredictiveModel(Iterable<? extends Instance<AttributesMap>> trainingData) {
        validateData(trainingData);
        DateTime mostRecent = getMostRecentInstance(trainingData);
        List<Instance<AttributesMap>> trainingDataList = reweightTrainingData(trainingData, mostRecent);
        final Classifier predictiveModel = wrappedBuilder.buildPredictiveModel(trainingDataList);
        return new TemporallyReweightedClassifier(predictiveModel);
    }

    @Override
    public PredictiveModelBuilder<AttributesMap, TemporallyReweightedClassifier> updatable(boolean updatable) {
        wrappedBuilder.updatable(updatable);
        return this;
    }

    private List<Instance<AttributesMap>> reweightTrainingData(Iterable<? extends Instance<AttributesMap>> sortedData, DateTime mostRecentInstance) {
        ArrayList<Instance<AttributesMap>> trainingDataList = Lists.newArrayList();
        for (Instance<AttributesMap> instance : sortedData) {
            double decayConstant = (instance.getLabel().equals(positiveClassification)) ? decayConstantOfPositive : decayConstantOfNegative;
            DateTime timeOfInstance = dateTimeExtractor.extractDateTime(instance);
            double hoursBack = Hours.hoursBetween(mostRecentInstance, timeOfInstance).getHours();
            double newWeight = Math.exp(-1.0 * hoursBack / decayConstant);
            trainingDataList.add(instance.reweight(newWeight));
        }
        return trainingDataList;
    }

    private void validateData(Iterable<? extends Instance<AttributesMap>> trainingData) {
        ClassificationCounter classificationCounter = ClassificationCounter.countAll(trainingData);
        Preconditions.checkArgument(classificationCounter.getCounts().keySet().size() <= 2, "trainingData must contain only 2 classifications, but it had %s", classificationCounter.getCounts().keySet().size());
    }



    @Override
    public void updatePredictiveModel(TemporallyReweightedClassifier predictiveModel, Iterable<? extends Instance<AttributesMap>> newData, boolean splitNodes) {
        if (wrappedBuilder instanceof UpdatablePredictiveModelBuilder) {
            validateData(newData);
            DateTime mostRecentInstance = getMostRecentInstance(newData);

            List<Instance<AttributesMap>> newDataList = reweightTrainingData(newData, mostRecentInstance);

            Classifier pm = predictiveModel.getWrappedClassifier();
            ((UpdatablePredictiveModelBuilder) wrappedBuilder).updatePredictiveModel(pm, newDataList, splitNodes);
        } else {
            throw new RuntimeException("Cannot update predictive model without UpdatablePredictiveModelBuilder");
        }
    }

    private DateTime getMostRecentInstance(Iterable<? extends Instance<AttributesMap>> newData) {
        DateTime mostRecent = null;
        for(Instance<AttributesMap>instance : newData) {
            DateTime instanceTime = dateTimeExtractor.extractDateTime(instance);
            if (mostRecent == null || instanceTime.isAfter(mostRecent)) {
                mostRecent = instanceTime;
            }
        }
        return mostRecent;
    }

    @Override
    public void stripData(TemporallyReweightedClassifier predictiveModel) {
        if (wrappedBuilder instanceof UpdatablePredictiveModelBuilder) {
                ((UpdatablePredictiveModelBuilder) wrappedBuilder).stripData(predictiveModel.getWrappedClassifier());
        } else {
            throw new RuntimeException("Cannot strip data without UpdatablePredictiveModelBuilder");
        }

    }

}

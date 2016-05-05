package quickml.supervised.crossValidation.attributeImportance;


import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import quickml.data.instances.ClassifierInstance;
import quickml.data.OnespotDateTimeExtractor;
import quickml.InstanceLoader;

import quickml.supervised.crossValidation.lossfunctions.classifierLossFunctions.ClassifierLogCVLossFunction;
import quickml.supervised.tree.attributeIgnoringStrategies.IgnoreAttributesWithConstantProbability;
import quickml.supervised.crossValidation.data.OutOfTimeData;
import quickml.supervised.tree.decisionTree.DecisionTreeBuilder;
import quickml.supervised.tree.decisionTree.scorers.GRPenalizedGiniImpurityScorerFactory;

import java.util.List;
import java.util.Set;

public class AttributeImportanceFinderIntegrationTest {


    private List<ClassifierInstance> instances;

    @Before
    public void setUp() throws Exception {
        instances = InstanceLoader.getAdvertisingInstances();
    }


    @Test
    public void testAttributeImportanceFinder() throws Exception {
        System.out.println("\n \n \n new  attrImportanceTest");
        DecisionTreeBuilder<ClassifierInstance> modelBuilder = new DecisionTreeBuilder<ClassifierInstance>().scorerFactory(new GRPenalizedGiniImpurityScorerFactory()).maxDepth(16).minLeafInstances(0).numNumericBins(2).minAttributeValueOccurences(1).attributeIgnoringStrategy(new IgnoreAttributesWithConstantProbability(0.7));

        AttributeImportanceFinder<ClassifierInstance> attributeImportanceFinder = new AttributeImportanceFinderBuilder<ClassifierInstance>()
                .modelBuilder(modelBuilder)
                .dataCycler(new OutOfTimeData<>(instances, .25, 12, new OnespotDateTimeExtractor()))
                .percentAttributesToRemovePerIteration(0.3)
                .numOfIterations(3)
                .attributesToKeep(attributesToKeep())
                .primaryLossFunction(new ClassifierLogCVLossFunction(.000001))//WeightedAUCCrossValLossFunction(1.0))//new ClassifierLogCVLossFunction(.000001))//ClassifierLogCVLossFunction(0.000001))
                .build();

        System.out.println(attributeImportanceFinder.determineAttributeImportance());

    }

    private Set<String> attributesToKeep() {
        Set<String> attributesToKeepRegardessOfQuality = Sets.newHashSet();
        attributesToKeepRegardessOfQuality.add("timeOfArrival-year");
        attributesToKeepRegardessOfQuality.add("timeOfArrival-monthOfYear");
        attributesToKeepRegardessOfQuality.add("timeOfArrival-dayOfMonth");
        attributesToKeepRegardessOfQuality.add("timeOfArrival-hourOfDay");
        attributesToKeepRegardessOfQuality.add("timeOfArrival-minuteOfHour");
        attributesToKeepRegardessOfQuality.add("internalCreativeId");
        attributesToKeepRegardessOfQuality.add("domain");
        return attributesToKeepRegardessOfQuality;
    }

}
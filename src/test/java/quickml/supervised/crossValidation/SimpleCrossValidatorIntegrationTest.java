package quickml.supervised.crossValidation;

import org.junit.Before;
import org.junit.Test;
import quickml.InstanceLoader;
import quickml.data.instances.ClassifierInstance;
import quickml.data.OnespotDateTimeExtractor;
import quickml.supervised.crossValidation.data.OutOfTimeData;
import quickml.supervised.crossValidation.lossfunctions.classifierLossFunctions.ClassifierLogCVLossFunction;
import quickml.supervised.tree.decisionTree.DecisionTree;
import quickml.supervised.tree.decisionTree.DecisionTreeBuilder;
import quickml.supervised.tree.decisionTree.scorers.GRPenalizedGiniImpurityScorerFactory;

import java.util.List;

/**
 * Created by alexanderhawk on 7/8/15.
 */
public class SimpleCrossValidatorIntegrationTest {

    private List<ClassifierInstance> instances;

    @Before
    public void setUp() throws Exception {
        instances = InstanceLoader.getAdvertisingInstances().subList(0,1000);
    }


    @Test
    public void testCrossValidation() throws Exception {
        System.out.println("\n \n \n new  attrImportanceTest");
        DecisionTreeBuilder<ClassifierInstance> modelBuilder = new DecisionTreeBuilder<ClassifierInstance>().scorerFactory(new GRPenalizedGiniImpurityScorerFactory()).maxDepth(16).minLeafInstances(0).minAttributeValueOccurences(11).attributeIgnoringStrategy(new quickml.supervised.tree.attributeIgnoringStrategies.IgnoreAttributesWithConstantProbability(0.7));

        SimpleCrossValidator<DecisionTree, ClassifierInstance> cv = new SimpleCrossValidator<>(modelBuilder,
                new ClassifierLossChecker<ClassifierInstance, DecisionTree>(new ClassifierLogCVLossFunction(.000001)),
                new OutOfTimeData<>(instances, .25, 12, new OnespotDateTimeExtractor() ) );
        for (int i =0; i<3; i++) {
            System.out.println("Loss: " + cv.getLossForModel());
        }

    }
}

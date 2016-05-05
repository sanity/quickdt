package quickml.supervised.tree.decisionTree;

import com.beust.jcommander.internal.Lists;
import org.junit.Assert;
import org.junit.Test;
import quickml.data.instances.ClassifierInstance;
import quickml.InstanceLoader;
import quickml.supervised.PredictiveModelsFromPreviousVersionsToBenchMarkAgainst.OldTree;
import quickml.supervised.PredictiveModelsFromPreviousVersionsToBenchMarkAgainst.OldTreeBuilder;
import quickml.supervised.PredictiveModelsFromPreviousVersionsToBenchMarkAgainst.oldScorers.GiniImpurityOldScorer;
import quickml.supervised.ensembles.randomForest.randomDecisionForest.RandomDecisionForest;
import quickml.supervised.ensembles.randomForest.randomDecisionForest.RandomDecisionForestBuilder;
import quickml.supervised.tree.attributeIgnoringStrategies.IgnoreAttributesWithConstantProbability;
import quickml.supervised.tree.decisionTree.valueCounters.ClassificationCounter;
import quickml.supervised.tree.nodes.Branch;
import quickml.supervised.tree.nodes.Leaf;
import quickml.supervised.tree.nodes.Node;
import quickml.supervised.tree.summaryStatistics.ValueCounter;

import java.util.List;

/**
 * Created by alexanderhawk on 4/26/15.
 */
public class DecisionOldOldTreeBuilderTest {
    @Test
    public void singleTreeTest() {
        int maxDepth = 8;
        double minSplitFraction = 0.1;
        int minLeafInstances = 20;
        int minAttributeOccurences = 11;
        List<ClassifierInstance> instances = Lists.newArrayList(InstanceLoader.getAdvertisingInstances());//.subList(0, 10000);

        OldTreeBuilder modelBuilder = new OldTreeBuilder().scorer(new GiniImpurityOldScorer()).
                maxDepth(maxDepth).
                minSplitFraction(minSplitFraction).
                degreeOfGainRatioPenalty(1.0).
                minCategoricalAttributeValueOccurances(minAttributeOccurences)
                .attributeIgnoringStrategy(new quickml.supervised.PredictiveModelsFromPreviousVersionsToBenchMarkAgainst.oldTree.oldAttributeIgnoringStrategies.IgnoreAttributesWithConstantProbability(0.7));
        OldTree oldTreeOld = modelBuilder.buildPredictiveModel(instances);

        DecisionTreeBuilder<ClassifierInstance> decisionTreeBuilder = new DecisionTreeBuilder<>().numSamplesPerNumericBin(18).numNumericBins(6)
                .attributeIgnoringStrategy(new IgnoreAttributesWithConstantProbability(0.7)).maxDepth(maxDepth).minSplitFraction(minSplitFraction)
                .degreeOfGainRatioPenalty(1.0).minAttributeValueOccurences(minAttributeOccurences).minLeafInstances(minLeafInstances);


        DecisionTree decisionTree = decisionTreeBuilder.buildPredictiveModel(instances);

        Conditions<ClassificationCounter> conditions = new Conditions<>(maxDepth, minAttributeOccurences, minSplitFraction, minLeafInstances);
        recurseTree(decisionTree.root, conditions);

    //    RandomDecisionForestBuilder<ClassifierInstance> randomDecisionForestBuilder = new RandomDecisionForestBuilder<>(decisionTreeBuilder).numTrees(5);
    //    RandomDecisionForest randomDecisionForest = randomDecisionForestBuilder.buildPredictiveModel(instances);

     //   for (DecisionTree forestTree : randomDecisionForest.regressionTrees) {
       //     recurseTree(forestTree.root, conditions);
        // }
        for (ClassifierInstance instance: instances) {
            oldTreeOld.getProbability(instance.getAttributes(),1.0);// Assert.assertTrue("prob: " + randomDecisionForest.getProbabilityOfPositiveClassification(instance.getAttributes(), 1.0),randomDecisionForest.getProbabilityOfPositiveClassification(instance.getAttributes(), 1.0) < 1.0);
            oldTreeOld.getProbability(instance.getAttributes(),1.0);// Assert.assertTrue("prob: " + randomDecisionForest.getProbabilityOfPositiveClassification(instance.getAttributes(), 1.0),randomDecisionForest.getProbabilityOfPositiveClassification(instance.getAttributes(), 1.0) < 1.0);

            decisionTree.getProbability(instance.getAttributes(), 1.0);//Assert.assertTrue("prob: "+ randomDecisionForest.getProbabilityOfPositiveClassification(instance.getAttributes(),0.0), randomDecisionForest.getProbabilityOfPositiveClassification(instance.getAttributes(), 0.0) < 1.0);
            decisionTree.getProbability(instance.getAttributes(), 0.0);//Assert.assertTrue("prob: "+ randomDecisionForest.getProbabilityOfPositiveClassification(instance.getAttributes(),0.0), randomDecisionForest.getProbabilityOfPositiveClassification(instance.getAttributes(), 0.0) < 1.0);
            Assert.assertEquals(decisionTree.getProbability(instance.getAttributes(), 1.0) +decisionTree.getProbability(instance.getAttributes(), 0.0), 1.0, 1E-5);
        }

    }

    @Test
    public void mockTreeTest() {
        int maxDepth = 8;
        double minSplitFraction = 0.1;
        int minLeafInstances = 20;
        int minAttributeOccurences = 11;

        DecisionTreeBuilder<ClassifierInstance> decisionTreeBuilder = new DecisionTreeBuilder<>().numSamplesPerNumericBin(25).numNumericBins(6)
                .attributeIgnoringStrategy(new IgnoreAttributesWithConstantProbability(0.7)).maxDepth(maxDepth).minSplitFraction(minSplitFraction)
                .degreeOfGainRatioPenalty(1.0).minAttributeValueOccurences(minAttributeOccurences).minLeafInstances(minLeafInstances);

        List<ClassifierInstance> instances = Lists.newArrayList(InstanceLoader.getAdvertisingInstances());//.subList(0, 10000);

        DecisionTree decisionTree = decisionTreeBuilder.buildPredictiveModel(instances);

        Conditions<ClassificationCounter> conditions = new Conditions<>(maxDepth, minAttributeOccurences, minSplitFraction, minLeafInstances);
        recurseTree(decisionTree.root, conditions);

        RandomDecisionForestBuilder<ClassifierInstance> randomDecisionForestBuilder = new RandomDecisionForestBuilder<>(decisionTreeBuilder).numTrees(5);
        RandomDecisionForest randomDecisionForest = randomDecisionForestBuilder.buildPredictiveModel(instances);

        for (DecisionTree forestTree : randomDecisionForest.decisionTrees) {
            recurseTree(forestTree.root, conditions);
        }


    }

    @Test
    public void randomForestTest(){
        int maxDepth = 8;
        double minSplitFraction = 0.1;
        int minLeafInstances = 20;
        int minAttributeOccurences = 11;

        DecisionTreeBuilder<ClassifierInstance> decisionTreeBuilder = new DecisionTreeBuilder<>().numSamplesPerNumericBin(25).numNumericBins(6)
                .attributeIgnoringStrategy(new IgnoreAttributesWithConstantProbability(0.7)).maxDepth(maxDepth).minSplitFraction(minSplitFraction)
                .degreeOfGainRatioPenalty(1.0).minAttributeValueOccurences(minAttributeOccurences).minLeafInstances(minLeafInstances);

        List<ClassifierInstance> instances = Lists.newArrayList(InstanceLoader.getAdvertisingInstances());//.subList(0, 10000);

        RandomDecisionForestBuilder<ClassifierInstance> randomDecisionForestBuilder = new RandomDecisionForestBuilder<>(decisionTreeBuilder).numTrees(5);
        RandomDecisionForest randomDecisionForest = randomDecisionForestBuilder.buildPredictiveModel(instances);
        Conditions<ClassificationCounter> conditions = new Conditions<>(maxDepth, minAttributeOccurences, minSplitFraction, minLeafInstances);


        for (DecisionTree forestTree : randomDecisionForest.decisionTrees) {
            recurseTree(forestTree.root, conditions);
        }
        for (ClassifierInstance instance: instances) {
            randomDecisionForest.getProbability(instance.getAttributes(),1.0);// Assert.assertTrue("prob: " + randomDecisionForest.getProbabilityOfPositiveClassification(instance.getAttributes(), 1.0),randomDecisionForest.getProbabilityOfPositiveClassification(instance.getAttributes(), 1.0) < 1.0);
            randomDecisionForest.getProbability(instance.getAttributes(),0.0);//Assert.assertTrue("prob: "+ randomDecisionForest.getProbabilityOfPositiveClassification(instance.getAttributes(),0.0), randomDecisionForest.getProbabilityOfPositiveClassification(instance.getAttributes(), 0.0) < 1.0);
        }

    }


    private static void recurseTree(Node<ClassificationCounter> node, Conditions<ClassificationCounter> conditions) {
        conditions.satisfiesConditions(node);
        if (node instanceof Branch) {
            recurseTree(((Branch<ClassificationCounter>)node).getTrueChild(), conditions);
            recurseTree(((Branch<ClassificationCounter>)node).getFalseChild(), conditions);
        }
    }

    public static class Conditions<VC extends ValueCounter<VC>> {

        private int maxDepth;
        private int minAttributeOccurrences;
        private double minSplitFraction;
        private int minInstancesPerLeaf;

        public Conditions(int maxDepth, int minAttributeOccurrences, double minSplitFraction, int minInstancesPerLeaf) {
            this.maxDepth = maxDepth;
            this.minAttributeOccurrences = minAttributeOccurrences;
            this.minSplitFraction = minSplitFraction;
            this.minInstancesPerLeaf = minInstancesPerLeaf;
        }

        public void satisfiesConditions(Node<VC>  node) {
            Assert.assertTrue(node!=null);

            if (node instanceof Branch) {
                 satisfiesBranchConditions((Branch<VC>)node);
            } else {
                 satisfiesLeafConditions((Leaf<VC>)node);
            }
        }

        private void satisfiesBranchConditions(Branch<VC> branch) {
            Assert.assertTrue("attribute: " + branch.attribute+ ".  branch.getProbabilityOfTrueChild(): "+ branch.getProbabilityOfTrueChild(), branch.getProbabilityOfTrueChild() >= minSplitFraction && 1.0 - branch.getProbabilityOfTrueChild() >= minSplitFraction);
            Assert.assertTrue("branch.getValueCounter().getTotal(): " + branch.getValueCounter().getTotal(), branch.getValueCounter().getTotal()>minAttributeOccurrences);
        }

        private void satisfiesLeafConditions(Leaf<VC> leaf) {
            Assert.assertTrue("instances at leaf: leaf.getValueCounter().getTotal()", leaf.getValueCounter().getTotal() > minInstancesPerLeaf && leaf.getValueCounter().getTotal() >= minInstancesPerLeaf);
            Assert.assertTrue("leafDepth: "+ leaf.getDepth(), leaf.getDepth() <= maxDepth);
        }
    }
}
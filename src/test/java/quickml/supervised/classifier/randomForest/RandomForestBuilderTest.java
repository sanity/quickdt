package quickml.supervised.classifier.randomForest;

import org.testng.Assert;
import org.testng.annotations.Test;
import quickml.collections.MapUtils;
import quickml.data.AttributesMap;
import quickml.data.Instance;
import quickml.data.PredictionMap;
import quickml.supervised.PredictiveModelWithDataBuilder;
import quickml.supervised.classifier.TreeBuilderTestUtils;
import quickml.supervised.classifier.decisionTree.Tree;
import quickml.supervised.classifier.decisionTree.TreeBuilder;
import quickml.supervised.classifier.decisionTree.scorers.SplitDiffScorer;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Chris on 5/14/2014.
 */
public class RandomForestBuilderTest {
    @Test
    public void simpleBmiTest() throws Exception {
        final List<Instance<AttributesMap>> instances = TreeBuilderTestUtils.getInstances(10000);
        final TreeBuilder tb = new TreeBuilder(new SplitDiffScorer());
        final RandomForestBuilder rfb = new RandomForestBuilder(tb);
        final long startTime = System.currentTimeMillis();
        final RandomForest randomForest = rfb.buildPredictiveModel(instances);

        TreeBuilderTestUtils.serializeDeserialize(randomForest);

        final List<Tree> trees = randomForest.trees;
        final int treeSize = trees.size();
        Assert.assertTrue(treeSize < 400, "Forest size should be less than 400");
        Assert.assertTrue((System.currentTimeMillis() - startTime) < 20000,"Building this node should take far less than 20 seconds");

        final AttributesMap testAttributes = instances.get(0).getAttributes();
        for (Map.Entry<Serializable, Double> entry : randomForest.predict(testAttributes).entrySet()) {
            Assert.assertEquals(entry.getValue(), randomForest.getProbability(testAttributes, entry.getKey()));
        }
    }

    @Test
    public void simpleBmiTestSplit() throws Exception {
        final List<Instance<AttributesMap>> instances = TreeBuilderTestUtils.getInstances(10000);
        final PredictiveModelWithDataBuilder<AttributesMap ,RandomForest> wb = getWrappedUpdatablePredictiveModelBuilder();
        wb.splitNodeThreshold(1);
        final long startTime = System.currentTimeMillis();
        final RandomForest randomForest = wb.buildPredictiveModel(instances);

        TreeBuilderTestUtils.serializeDeserialize(randomForest);

        final List<Tree> trees = randomForest.trees;
        int treeSize = trees.size();
        int firstTreeNodeSize = trees.get(0).node.size();
        Assert.assertTrue(treeSize < 400, "Forest size should be less than 400");
        Assert.assertTrue((System.currentTimeMillis() - startTime) < 20000,"Building this node should take far less than 20 seconds");

        final List<Instance<AttributesMap>> newInstances = TreeBuilderTestUtils.getInstances(10000);
        final RandomForest newRandomForest = wb.buildPredictiveModel(newInstances);
        Assert.assertTrue(randomForest == newRandomForest, "Expect same tree to be updated");
        Assert.assertEquals(treeSize, newRandomForest.trees.size(), "Expected same number of trees");
        Assert.assertNotEquals(firstTreeNodeSize, newRandomForest.trees.get(0).node.size(), "Expected new nodes");

        treeSize = newRandomForest.trees.size();
        firstTreeNodeSize = newRandomForest.trees.get(0).node.size();
        wb.stripData(newRandomForest);
        Assert.assertEquals(treeSize, newRandomForest.trees.size(), "Expected same trees");
        Assert.assertEquals(firstTreeNodeSize, newRandomForest.trees.get(0).node.size(), "Expected same nodes");
    }

    private PredictiveModelWithDataBuilder<AttributesMap ,RandomForest> getWrappedUpdatablePredictiveModelBuilder() {
        final TreeBuilder tb = new TreeBuilder(new SplitDiffScorer()).updatable(true);
        final RandomForestBuilder urfb = new RandomForestBuilder(tb);
        return new PredictiveModelWithDataBuilder<>(urfb);
    }

    @Test
    public void simpleBmiTestNoSplit() throws Exception {
        final List<Instance<AttributesMap>> instances = TreeBuilderTestUtils.getInstances(10000);
        final PredictiveModelWithDataBuilder<AttributesMap ,RandomForest> wb = getWrappedUpdatablePredictiveModelBuilder();
        final long startTime = System.currentTimeMillis();
        final RandomForest randomForest = wb.buildPredictiveModel(instances);

        TreeBuilderTestUtils.serializeDeserialize(randomForest);

        final List<Tree> trees = randomForest.trees;
        int treeSize = trees.size();
        int firstTreeNodeSize = trees.get(0).node.size();
        Assert.assertTrue(treeSize < 400, "Forest size should be less than 400");
        Assert.assertTrue((System.currentTimeMillis() - startTime) < 20000,"Building this node should take far less than 20 seconds");

        final List<Instance<AttributesMap>> newInstances = TreeBuilderTestUtils.getInstances(10000);
        final RandomForest newRandomForest = wb.buildPredictiveModel(newInstances);
        Assert.assertTrue(randomForest == newRandomForest, "Expect same tree to be updated");
        Assert.assertEquals(treeSize, newRandomForest.trees.size(), "Expected same number of trees");
        Assert.assertEquals(firstTreeNodeSize, newRandomForest.trees.get(0).node.size(), "Expected same nodes");

        treeSize = newRandomForest.trees.size();
        firstTreeNodeSize = newRandomForest.trees.get(0).node.size();
        wb.stripData(newRandomForest);
        Assert.assertEquals(treeSize, newRandomForest.trees.size(), "Expected same trees");
        Assert.assertEquals(firstTreeNodeSize, newRandomForest.trees.get(0).node.size(), "Expected same nodes");
    }

    @Test
    public void twoDeterministicRandomForestsAreEqual() throws IOException, ClassNotFoundException {
        final List<Instance<AttributesMap>> instancesTrain = TreeBuilderTestUtils.getInstances(10000);
        final RandomForestBuilder urfb = new RandomForestBuilder(new TreeBuilder(new SplitDiffScorer()).updatable(true));
        MapUtils.random.setSeed(1l);
        final RandomForest randomForest1 = urfb.executorThreadCount(1).buildPredictiveModel(instancesTrain);
        MapUtils.random.setSeed(1l);
        final RandomForest randomForest2 = urfb.executorThreadCount(1).buildPredictiveModel(instancesTrain);

        Assert.assertTrue(randomForest1.trees.size() == randomForest2.trees.size(), "Deterministic Random Forests must have same number of trees");
        for (int i = 0; i < randomForest1.trees.size(); i++) {
            Assert.assertTrue(randomForest1.trees.get(i).node.size() == randomForest2.trees.get(i).node.size(), "Deterministic Decision Trees must have same number of nodes");
        }

        final List<Instance<AttributesMap>> instancesTest = TreeBuilderTestUtils.getInstances(1000);
        for (Instance<AttributesMap> instance : instancesTest) {
           PredictionMap map1 = randomForest1.predict(instance.getAttributes());
           PredictionMap map2 = randomForest2.predict(instance.getAttributes());
            Assert.assertTrue(map1.equals(map2), "Deterministic Decision Trees must have equal classifications");
        }
    }

}

package quickml;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;
import quickml.data.AttributesMap;
import quickml.data.instances.ClassifierInstance;
import quickml.supervised.tree.decisionTree.scorers.*;
import quickml.supervised.crossValidation.ClassifierLossChecker;
import quickml.supervised.crossValidation.SimpleCrossValidator;
import quickml.supervised.crossValidation.data.FoldedData;
import quickml.supervised.crossValidation.lossfunctions.classifierLossFunctions.ClassifierLogCVLossFunction;
import quickml.supervised.ensembles.randomForest.randomDecisionForest.RandomDecisionForest;
import quickml.supervised.ensembles.randomForest.randomDecisionForest.RandomDecisionForestBuilder;
import quickml.supervised.tree.attributeIgnoringStrategies.IgnoreAttributesWithConstantProbability;
import quickml.supervised.tree.constants.ForestOptions;
import quickml.supervised.tree.decisionTree.DecisionTree;
import quickml.supervised.tree.decisionTree.DecisionTreeBuilder;
import quickml.supervised.tree.decisionTree.valueCounters.ClassificationCounter;
import quickml.supervised.tree.scorers.GRImbalancedScorerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import static com.google.common.collect.Lists.newArrayList;

public class BenchmarkTest {

    private ClassifierLossChecker<ClassifierInstance, DecisionTree> classifierLossCheckerT;
    private ClassifierLossChecker<ClassifierInstance, RandomDecisionForest> classifierLossCheckerF;
    private ArrayList<GRImbalancedScorerFactory<ClassificationCounter>> scorerFactories;
    private DecisionTreeBuilder<ClassifierInstance> treeBuilder;
    private RandomDecisionForestBuilder randomDecisionForestBuilder;

    @Before
    public void setUp() throws Exception {
        classifierLossCheckerT = new ClassifierLossChecker<>(new ClassifierLogCVLossFunction(0.000001));
        classifierLossCheckerF = new ClassifierLossChecker<>(new ClassifierLogCVLossFunction(0.000001));

        scorerFactories = newArrayList(
                new PenalizedSplitDiffScorerFactory(),
                new PenalizedMSEScorerFactory());
        treeBuilder = createTreeBuilder();
        randomDecisionForestBuilder = createRandomForestBuilder();
    }

    @Test
    public void testDiaInstances() throws Exception {
        testWithInstances("dia", loadDiabetesDataset());
    }

    @Test
    public void testMoboInstances() throws Exception {
        testWithInstances("mobo", loadMoboDataset());
    }

    @Test
    public void performanceTest() throws Exception {
        Random random = new Random();
        List<ClassifierInstance> instances = loadDiabetesDataset();
        for (int i =1; i<60000; i++) {
            instances.add(instances.size(), instances.get(random.nextInt(instances.size()-1)));
        }
        double time0 = System.currentTimeMillis();
        DecisionTreeBuilder<ClassifierInstance> treeBuilder = new DecisionTreeBuilder<>().scorerFactory(new GRPenalizedGiniImpurityScorerFactory())
                .numSamplesPerNumericBin(20)
                .numNumericBins(5)
                .attributeIgnoringStrategy(new IgnoreAttributesWithConstantProbability(0.0))
                .maxDepth(16)
                .minLeafInstances(5);

        treeBuilder.buildPredictiveModel(instances);

        double time1 = System.currentTimeMillis();
        System.out.println("run time in seconds on numeric data set: " + (time1 - time0) / 1000);

    }


    private void testWithInstances(String dsName, final List<ClassifierInstance> instances) {
        FoldedData<ClassifierInstance> data = new FoldedData<>(instances, 4, 4);

        for (final GRImbalancedScorerFactory<ClassificationCounter> scorerFactory : scorerFactories) {
            Map<String, Serializable> cfg = Maps.newHashMap();
            cfg.put(ForestOptions.SCORER_FACTORY.name(), scorerFactory);
            SimpleCrossValidator< DecisionTree, ClassifierInstance> validator = new SimpleCrossValidator< DecisionTree, ClassifierInstance>(treeBuilder, classifierLossCheckerT, data);
            System.out.println(dsName + ", single-oldTree, " + scorerFactory + ", " + validator.getLossForModel(cfg));
            SimpleCrossValidator<RandomDecisionForest, ClassifierInstance> validator2 = new SimpleCrossValidator<RandomDecisionForest, ClassifierInstance>(randomDecisionForestBuilder, classifierLossCheckerF, data);
            System.out.println(dsName + ", random-forest, " + scorerFactory + ", " + validator2.getLossForModel(cfg));
        }
    }

    public static List<ClassifierInstance> loadDiabetesDataset() {
        final InputStream br1 = BenchmarkTest.class.getResourceAsStream("diabetesDataset.txt.gz");
        byte[] byteArr = new byte[16];
        try {
            br1.read(byteArr);

            final BufferedReader br = new BufferedReader(new InputStreamReader((new GZIPInputStream(BenchmarkTest.class.getResourceAsStream("diabetesDataset.txt.gz")))));
            final List<ClassifierInstance> instances = Lists.newLinkedList();


            String line = br.readLine();
            while (line != null) {
                String[] splitLine = line.split("\\s");
                AttributesMap attributes = AttributesMap.newHashMap();
                for (int x = 0; x < 8; x++) {
                    attributes.put("attr" + x, Double.parseDouble(splitLine[x]));
                }
                String stringLabel = splitLine[8];
                double label = 0.0;
                if (stringLabel.equals("class1")) {
                    label = 1.0;
                }
                instances.add(new ClassifierInstance(attributes, label));
                line = br.readLine();
            }

            return instances;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static  List<ClassifierInstance> loadMoboDataset() {
        try {
            final BufferedReader br = new BufferedReader(new InputStreamReader((new GZIPInputStream(BenchmarkTest.class.getResourceAsStream("mobo1.json.gz")))));
            final List<ClassifierInstance> instances = Lists.newLinkedList();

            String line = br.readLine();
            while (line != null) {
                final JSONObject jo = (JSONObject) JSONValue.parse(line);
                AttributesMap a = AttributesMap.newHashMap();
                a.putAll((JSONObject) jo.get("attributes"));
                String binaryClassification = jo.get("output").equals("none") ? "none" : "notNone";
                instances.add(new ClassifierInstance(a, binaryClassification));
                line = br.readLine();
            }
            return instances;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DecisionTreeBuilder<ClassifierInstance> createTreeBuilder() {
        return new DecisionTreeBuilder<>().attributeIgnoringStrategy(new IgnoreAttributesWithConstantProbability(0.7))
                .maxDepth(12).minAttributeValueOccurences(8).minLeafInstances(10);
    }

    private RandomDecisionForestBuilder createRandomForestBuilder() {
        return new RandomDecisionForestBuilder(createTreeBuilder()).numTrees(5);
    }

}


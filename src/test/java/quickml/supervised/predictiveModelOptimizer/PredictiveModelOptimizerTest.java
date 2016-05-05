package quickml.supervised.predictiveModelOptimizer;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import quickml.supervised.crossValidation.ClassifierLossChecker;
import quickml.supervised.crossValidation.SimpleCrossValidator;
import quickml.supervised.predictiveModelOptimizer.fieldValueRecommenders.FixedOrderRecommender;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PredictiveModelOptimizerTest {

    @Mock
    SimpleCrossValidator mockSimpleCrossValidator;

    @Mock
    ClassifierLossChecker mockLossChecker;

    private PredictiveModelOptimizer modelOptimizer;
    private HashMap<String, Object> bestConfig = Maps.newHashMap();
    private HashMap<String, Object> secondBestConfig = Maps.newHashMap();
    private HashMap<String, Object> thirdBestConfig = Maps.newHashMap();


    @Before
    public void setUp() throws Exception {
        initMocks(this);

        // Use a tree map for deteminisic order
        Map<String, FixedOrderRecommender> fields = new TreeMap<>();
        fields.put("treeDepth", new FixedOrderRecommender(1, 2, 3, 4, 5));
        fields.put("penalize_splits", new FixedOrderRecommender(true, false));
        fields.put("scorerFactory", new FixedOrderRecommender("A", "B", "C"));

        modelOptimizer = new PredictiveModelOptimizer(fields, mockSimpleCrossValidator, 10);
    }

    @Test
    public void testFindSimpleBestConfig() throws Exception {
        // Fields are checked in the following order - penalize_splits, scorerFactory, treeDepth
        thirdBestConfig = createMap(1, false, "A");
        secondBestConfig = createMap(1, false, "C");
        bestConfig = createMap(5, false, "C");


        when(mockSimpleCrossValidator.getLossForModel(anyMap())).thenReturn(0.5);
        when(mockSimpleCrossValidator.getLossForModel(eq(thirdBestConfig))).thenReturn(0.4);
        when(mockSimpleCrossValidator.getLossForModel(eq(secondBestConfig))).thenReturn(0.2);
        when(mockSimpleCrossValidator.getLossForModel(eq(bestConfig))).thenReturn(0.1);

        assertEquals(bestConfig, modelOptimizer.determineOptimalConfig());
    }

    private HashMap<String, Object> createMap(int treeDepth, boolean penalizeSplits, String scorer) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("treeDepth", treeDepth);
        map.put("penalize_splits", penalizeSplits);
        map.put("scorerFactory", scorer);
        return map;
    }
}
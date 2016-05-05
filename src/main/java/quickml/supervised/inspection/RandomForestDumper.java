package quickml.supervised.inspection;

import quickml.supervised.tree.decisionTree.nodes.DTCatBranch;
import quickml.supervised.tree.decisionTree.nodes.DTNumBranch;
import quickml.supervised.tree.decisionTree.valueCounters.ClassificationCounter;
import quickml.supervised.tree.nodes.Node;
import quickml.utlities.SerializationUtility;
import quickml.supervised.tree.decisionTree.DecisionTree;

import quickml.supervised.tree.nodes.NumBranch;
import quickml.supervised.ensembles.randomForest.randomDecisionForest.RandomDecisionForest;

import java.io.*;
import java.util.*;


public class RandomForestDumper {

    public void summarizeForest(PrintStream out, RandomDecisionForest randomDecisionForest) {
        summarizeModel(out, randomDecisionForest);
    }

    public void summarizeForest(PrintStream out, String file) {
        SerializationUtility<RandomDecisionForest> serializationUtility = new SerializationUtility<>();
        RandomDecisionForest randomDecisionForest = serializationUtility.loadObjectFromGZIPFile(file);
        summarizeModel(out, randomDecisionForest);
    }

    public void summarizeModel(PrintStream out, RandomDecisionForest forest) {

        List<TreeSummary> summaries = new ArrayList<>();
        for (DecisionTree t : forest.decisionTrees) {
            TreeSummary summary = new TreeSummary();
            summary.summarizeNode(t.root, 0);
            summaries.add(summary);
        }

        TreeSummary summary = new TreeSummary();
        for (TreeSummary t : summaries) {
            summary.splits += t.splits;
            for (AttributeSummary as : t.attributes.values()) {
                AttributeSummary fas = summary.attributes.get(as.name);
                if (fas == null) {
                    fas = new AttributeSummary();
                    fas.name = as.name;
                    summary.attributes.put(as.name, fas);
                }
                fas.splitCount+= as.splitCount;
                fas.weightedSplitCount +=as.weightedSplitCount;
                fas.treeCount++;
                for (int i = 0; i < as.depths.length; i++) {
                    fas.depths[i]+= as.depths[i];
                }
            }
        }

        // Output trees, total splits, distinct attributes
        out.format("%d trees, %d total splits, %d distinct attributes\n", forest.decisionTrees.size(), summary.splits, summary.attributes.size());

        // Get attributes, sort, emit:
        // - name, # trees, # splits, depths
        List<AttributeSummary> attributes = new ArrayList<>(summary.attributes.values());
        Collections.sort(attributes);
        for (AttributeSummary s : attributes) {
            out.format("%s : %f weightedSplits, %d trees, %d splits\n", s.name, s.weightedSplitCount, s.treeCount, s.splitCount);
            out.format("    depths = %s\n", Arrays.toString(s.depths));
        }

    }

    public static class TreeSummary {
        private int splits;
        private Map<String, AttributeSummary> attributes = new HashMap<>();

        private void summarizeNode(Node<ClassificationCounter> node, int currentDepth) {
            if (node instanceof DTCatBranch) {
                summarizeCategoricalNode((DTCatBranch)node, currentDepth);
            }
            else if (node instanceof NumBranch) {
                summarizeNumericNode((DTNumBranch) node, currentDepth);
            }
        }

        private void addAttribute(String name, int depth) {
            AttributeSummary attrSummary = attributes.get(name);
            if (attrSummary == null) {
                attrSummary = new AttributeSummary();
                attrSummary.name = name;
                attributes.put(name, attrSummary);
            }
            attrSummary.splitCount++;
            attrSummary.weightedSplitCount = attrSummary.weightedSplitCount + Math.max(0.00000001, 1.0/Math.pow(2, depth));
            attrSummary.depths[depth]++;
        }

        private void summarizeCategoricalNode(DTCatBranch node, int currentDepth) {
            splits++;
            addAttribute(node.attribute, currentDepth);
            summarizeNode(node.getTrueChild(), currentDepth+1);
            summarizeNode(node.getFalseChild(), currentDepth+1);
        }

        private void summarizeNumericNode(DTNumBranch node, int currentDepth) {
            splits++;
            addAttribute(node.attribute, currentDepth);
            summarizeNode(node.getTrueChild(), currentDepth+1);
            summarizeNode(node.getFalseChild(), currentDepth + 1);
        }
    }

    private static class AttributeSummary implements Comparable<AttributeSummary> {
        private String name;

        private int treeCount;
        private int splitCount;
        private double weightedSplitCount;
        private int[] depths= new int[20];

        public int compareTo(AttributeSummary other) {

            int result = -Double.compare(weightedSplitCount, other.weightedSplitCount);
            if (result == 0) {
                result = -Integer.compare(treeCount, other.treeCount);
            }
            if (result == 0) {
                result = -Integer.compare(splitCount, other.splitCount);
            }
            if (result == 0) {
                result = name.compareTo(other.name);
            }
            return result;
        }
    }


}
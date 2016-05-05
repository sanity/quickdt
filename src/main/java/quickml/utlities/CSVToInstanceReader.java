package quickml.utlities;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import quickml.data.AttributesMap;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import quickml.data.instances.ClassifierInstance;
import quickml.data.instances.RegressionInstance;
import quickml.utlities.selectors.*;

/**
 * Created by alexanderhawk on 10/2/14.
 */


/* This class converts the contents of a csv file into quickml instances.
   Defaults:
   1. the column containing the instance label is assumed to be the first collumn in the csv file.
   2. any variable that can be parsed to a number will be treated as numeric. Add an underscore to categorical variable values if they are numeric
   3. all instances are assumed to have equal weight

   Options:
   1. the column for an instances label can be specified by its name in the header in the function: columnNameForLabel.
   2. the column for an instances weight can be specified by its name in the header in the function: columnNameForWeight.
   2. One can specify which variables are categorical by providing either an instancet of a NumericSelector to numericSelector(), or
      a CategoricalSelector to categoricalSelector.  Only one of the two needs to be provided.
 */

public class CSVToInstanceReader {
    private List<String> header;
    private String columnNameForLabel;
    private String columnNameForWeight;
    private boolean containsUnLabeledInstances = false;
    private Optional<CategoricalSelector> categoricalSelector = Optional.absent();
    private Optional<NumericSelector> numericSelector = Optional.absent();
    private char delimiter = ',';
    private boolean hasHeader = true;

    public CSVToInstanceReader() {
    }

    public CSVToInstanceReader(char delimiter, String columnNameForLabel, String columnNameForWeight, Optional<CategoricalSelector> categoricalSelector,
                               Optional<NumericSelector> numericSelector, boolean hasHeader) {
        this.delimiter = delimiter;
        this.columnNameForLabel = columnNameForLabel;
        this.columnNameForWeight = columnNameForWeight;
        this.categoricalSelector = categoricalSelector;
        this.numericSelector = numericSelector;
        this.hasHeader = hasHeader;
    }


    public List<ClassifierInstance> readClassifierInstancesFromCsv(String fileName) throws Exception {

        CSVReader reader = new CSVReader(new FileReader(fileName), delimiter, '"');
        return getInstancesFromReader(reader);
    }

    public List<RegressionInstance> readRegressionInstancesFromCsv(String fileName) throws Exception {
       List<ClassifierInstance> classifierInstances = readClassifierInstancesFromCsv(fileName);
        List<RegressionInstance> regressionInstances = Lists.newArrayList();
        for (ClassifierInstance instance : classifierInstances ) {
            if (instance.getLabel() instanceof Double) {
                regressionInstances.add(new RegressionInstance(instance.getAttributes(), (Double) instance.getLabel(), instance.getWeight()));
            }
        }
        return regressionInstances;
    }



        public List<ClassifierInstance> readCsvFromReader(Reader reader) throws Exception {

        CSVReader csvReader = new CSVReader(reader, delimiter, '"');
        return getInstancesFromReader(csvReader);
    }

    private List<ClassifierInstance> getInstancesFromReader(CSVReader reader) throws IOException {
        List<String[]> csvLines = reader.readAll();

        List<ClassifierInstance> instances = Lists.newArrayList();
        try {
            setHeader(csvLines);
            for (int i = 1; i < csvLines.size(); i++) {
                instances.add(instanceConverter(csvLines.get(i)));
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return instances;
    }

    private void setHeader(List<String[]> csvLines) {
        header = new ArrayList<>();
        if (hasHeader) {
            Collections.addAll(header, csvLines.get(0));
        }  else {
            for (int i = 0; i< csvLines.get(0).length; i++) {
                header.add("x" + i); //variables named x1 through xn
            }
        }
    }

    private ClassifierInstance instanceConverter(String[] instanceArray) {

        AttributesMap attributesMap = AttributesMap.newHashMap();
        Serializable label = null;
        double weight = 1.0;
        for (int i = 0; i < header.size(); i++) {
            if (i >= instanceArray.length) {
                throw new IndexOutOfBoundsException();
            }

            if (instanceArray[i].isEmpty()) {
                continue;
            }

            boolean haveLabelInFirstCollumn = i == 0 && columnNameForLabel == null;
            boolean matchedCollumnToLabel = columnNameForLabel != null && columnNameForLabel.equals(header.get(i));
            if (haveLabelInFirstCollumn || matchedCollumnToLabel) {
                label = convertToNumberOrCleanedString(header.get(i), instanceArray[i]);
                continue;
            }

            boolean matchedCollumnToWeight = columnNameForWeight != null && columnNameForWeight.equals(header.get(i));
            if (matchedCollumnToWeight) {
                weight = (Double) convertToNumberOrCleanedString(header.get(i), instanceArray[i]);
                continue;
            }

            attributesMap.put(header.get(i), convertToNumberOrCleanedString(header.get(i), instanceArray[i]));
        }
        if (label == null) {
            label = "missing label";
            containsUnLabeledInstances = true;
        }

        return new ClassifierInstance(attributesMap, label, weight);
    }

    private Serializable convertToNumberOrCleanedString(String varName, String varValue) {
        boolean categoricalOrNumericSelectorProvided = categoricalSelector.isPresent() || numericSelector.isPresent();
        if (!categoricalOrNumericSelectorProvided) {
            return tryToConvertToNumeric(varValue);
        } else {
            //note: quoted values will be treated as categorical unless a selector indicates otherwise
            if (categoricalSelector.isPresent() && categoricalSelector.get().isCategorical(varName)) {
                return categoricalSelector.get().cleanValue(varValue);
            } else if (!numericSelector.isPresent() || numericSelector.get().isNumeric(varName)) {
                if (numericSelector.isPresent()) {
                    varValue = numericSelector.get().cleanValue(varValue);
                }
                return tryToConvertToNumeric(varValue);
            } else {
                //now account for the case where a numeric selector is provided, but no categorical selector is.
                return varValue;
            }
        }
    }


    private Serializable tryToConvertToNumeric(String varValue) {
        if ((varValue.startsWith("\"") && varValue.endsWith("\"")) || (varValue.startsWith("\'") && varValue.endsWith("\'"))) {
            varValue = varValue.substring(1, varValue.length() - 1);
        }
        try {
            return Long.valueOf(varValue);

        } catch (NumberFormatException e) {
            try {
                return Double.valueOf(varValue);
            } catch (NumberFormatException n) {
                return varValue;
            }
        }
    }

    public static void main(String[] args) {
        Set<String> catVariables = Sets.newHashSet();
        catVariables.add("eap");
        CSVToInstanceReaderBuilder csvReaderBuilder = new CSVToInstanceReaderBuilder().collumnNameForLabel("campaignId").categoricalSelector(new ExplicitCategoricalSelector(catVariables));
        CSVToInstanceReader csvReader = csvReaderBuilder.buildCsvReader();
        try {
            List<ClassifierInstance> instances = csvReader.readClassifierInstancesFromCsv("test3");
            for (ClassifierInstance instance : instances)
                System.out.println("label: " + instance.getLabel() + "attributes: " + instance.getAttributes().toString());

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
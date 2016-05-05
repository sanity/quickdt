package quickml.supervised.crossValidation.data;

import com.google.common.collect.Lists;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class FoldedData<I> implements TrainingDataCycler<I> {


    private final int numFolds;
    private final int foldsUsed;
    private final List<I> allData;
    private int currentFold;
    private List<I> trainingSet;
    private List<I> validationSet;

    public FoldedData(List<I> allData, int numFolds, int foldsUsed) {
        checkArguments(allData, numFolds, foldsUsed);
        this.allData = allData;
        this.numFolds = numFolds;
        this.foldsUsed = foldsUsed;
        this.currentFold = 0;
        reset();
    }

    @Override
    public void reset() {
        currentFold = 0;
        setTrainingAndValidationSets();
    }

    @Override
    public List<I> getTrainingSet() {
        return trainingSet;
    }

    @Override
    public List<I> getValidationSet() {
        return validationSet;
    }

    @Override
    public List<I> getAllData() {
        return allData;
    }

    @Override
    public boolean nextCycle() {
        currentFold++;
        if (hasMore()) {
            setTrainingAndValidationSets();
            return true;
        }
        return false;
    }

    @Override
    public boolean hasMore() {
        return currentFold < foldsUsed;
    }

    private void checkArguments(List<I> allData, int numFolds, int foldsUsed) {
        checkArgument(allData.size() > 0, "Training set cannot be empty");
        checkArgument(numFolds <= allData.size(), "Num Folds must be less than or equal to the data getSize");
        checkArgument(foldsUsed <= numFolds, "Folds used must be less then or equal to the number of folds");
        checkArgument(foldsUsed > 0, "Number of folds used must be greater than 0");
    }

    private void setTrainingAndValidationSets() {
        trainingSet = Lists.newArrayList();
        validationSet = Lists.newArrayList();
        for (int i = 0; i < allData.size(); i++) {
            if (i % numFolds == currentFold)
                validationSet.add(allData.get(i));
            else
                trainingSet.add(allData.get(i));
        }
    }

}

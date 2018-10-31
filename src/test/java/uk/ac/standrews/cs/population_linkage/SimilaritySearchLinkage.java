package uk.ac.standrews.cs.population_linkage;

import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.linkage.MTreeSearchStructure;
import uk.ac.standrews.cs.population_linkage.linkage.SimilaritySearchLinker;
import uk.ac.standrews.cs.population_linkage.linkage.WeightedAverageLevenshtein;
import uk.ac.standrews.cs.population_linkage.model.InvalidWeightsException;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_linkage.model.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class SimilaritySearchLinkage extends Linkage {

    @Override
    public Linker getLinker() {

        return new DummySimilaritySearchLinker();
    }

    @Override
    protected boolean equal(RecordPair pair1, RecordPair pair2) {

        // Don't care which way round the records are in the pair.
        // The order will depend on which of the record sets was the largest and hence got put into the search structure.

        return pair1.record1.equals(pair2.record1) && pair1.record2.equals(pair2.record2) ||
                pair1.record1.equals(pair2.record2) && pair1.record2.equals(pair2.record1);
    }

    @Test
    public void numbersOfRecordPairsAreWithinUpperBounds() throws InvalidWeightsException {

        checkNumberOfPairsWithinUpperBound(1);
        checkNumberOfPairsWithinUpperBound(2);
        checkNumberOfPairsWithinUpperBound(3);
    }

    @Test
    public void noRecordPairsFoundWithinDistanceZero() throws InvalidWeightsException {

        setThreshold(0.0);

        Iterable<RecordPair> pairs = linker.getRecordPairs(birth_records, death_records);

        assertEquals(0, numberOfMatchingPairs(pairs));
    }

    @Test
    public void foundExpectedRecordsWithinDistanceNoughtPointFive() throws InvalidWeightsException {

        // "john smith" distance 0.5 from "john stith"

        setThreshold(0.5);

        Iterable<RecordPair> pairs = linker.getRecordPairs(birth_records, death_records);

        assertEquals(1, numberOfMatchingPairs(pairs));
        assertTrue(containsPair(pairs, death2, birth1));
    }

    @Test
    public void foundExpectedRecordsWithinDistanceOne() throws InvalidWeightsException {

        // "john smith" distance 0.5 from "john stith"
        // "janet smith" distance 1 from "janet smythe"
        // "jane smyth" distance 1 from "janet smythe"

        setThreshold(1.0);

        Iterable<RecordPair> pairs = linker.getRecordPairs(birth_records, death_records);

        assertEquals(3, numberOfMatchingPairs(pairs));
        assertTrue(containsPair(pairs, death2, birth1));
        assertTrue(containsPair(pairs, death1, birth2));
        assertTrue(containsPair(pairs, death1, birth3));
    }

    private void checkNumberOfPairsWithinUpperBound(int number_of_records_to_consider) throws InvalidWeightsException {

        setNumberOfRecordsToConsider(number_of_records_to_consider);

        assertTrue(getNumberOfPairs(linker, birth_records) <= number_of_records_to_consider * birth_records.size());
    }

    private int numberOfMatchingPairs(Iterable<RecordPair> pairs) throws InvalidWeightsException {

        int count = 0;
        for (RecordPair pair : pairs) {
            if (linker.match(pair)) count++;
        }
        return count;
    }

    private void setThreshold(double threshold) {

        ((SimilaritySearchLinker) linker).setThreshold(threshold);
    }

    private void setNumberOfRecordsToConsider(int number_of_records_to_consider) {

        ((SimilaritySearchLinker) linker).setNumberOfRecordsToConsider(number_of_records_to_consider);
    }

    class DummySimilaritySearchLinker extends SimilaritySearchLinker {

        DummySimilaritySearchLinker() {
            super(() -> new MTreeSearchStructure<>(new WeightedAverageLevenshtein(Arrays.asList(0, 1))), 0);
        }

        @Override
        protected String getLinkType() {
            return null;
        }

        @Override
        protected String getProvenance() {
            return null;
        }

        @Override
        protected String getRoleType1() {
            return null;
        }

        @Override
        protected String getRoleType2() {
            return null;
        }

        @Override
        protected String getIdentifier1(LXP record) {
            return null;
        }

        @Override
        protected String getIdentifier2(LXP record) {
            return null;
        }
    }
}

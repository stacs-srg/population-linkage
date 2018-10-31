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
    public void checkNotTooManyRecordPairsConsidered() throws InvalidWeightsException {

        linker.setThreshold(Double.MAX_VALUE);

        for (int number_of_records_to_consider = 1; number_of_records_to_consider <= 3; number_of_records_to_consider++) {

            ((SimilaritySearchLinker) linker).setNumberOfRecordsToConsider(number_of_records_to_consider);

            Iterable<RecordPair> pairs = linker.getMatchingRecordPairs(birth_records, birth_records);

            assertTrue(count(pairs) <= number_of_records_to_consider * birth_records.size());
        }
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

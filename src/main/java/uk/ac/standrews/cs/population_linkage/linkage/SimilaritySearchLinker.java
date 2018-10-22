package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.*;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;

import java.util.Iterator;
import java.util.List;

public abstract class SimilaritySearchLinker extends Linker {

    private final SearchStructure<LXP> search_structure;
    final double threshold;
    private final int number_of_records_to_consider;

    SimilaritySearchLinker(SearchStructure<LXP> search_structure, double threshold, int number_of_records_to_consider, int number_of_progress_updates) {

        super(number_of_progress_updates);

        this.search_structure = search_structure;
        this.threshold = threshold;
        this.number_of_records_to_consider = number_of_records_to_consider;
    }

    @Override
    protected boolean match(RecordPair pair) {

        return pair.distance <= threshold;
    }

    @Override
    protected Iterable<RecordPair> getRecordPairs(final List<LXP> records1, final List<LXP> records2) {

        final boolean datasets_same = records1 == records2;

        List<LXP> smaller_set = records1.size() < records2.size() ? records1 : records2;
        List<LXP> larger_set = records1.size() < records2.size() ? records2 : records1;

        for (LXP record : larger_set) {
            search_structure.add(record);
        }

        progress_indicator.setTotalSteps(smaller_set.size());

        return () -> new Iterator<RecordPair>() {

            int smaller_set_index = 0;

            // Start at 1 to ignore the query record itself, if the two datasets are the same.
            int neighbours_index = datasets_same ? 1 : 0;

            List<DataDistance<LXP>> nearest_records = search_structure.findNearest(smaller_set.get(smaller_set_index), number_of_records_to_consider);

            @Override
            public boolean hasNext() {
                return smaller_set_index < smaller_set.size() && neighbours_index < nearest_records.size();
            }

            @Override
            public RecordPair next() {

                RecordPair next_pair = new RecordPair(smaller_set.get(smaller_set_index), nearest_records.get(neighbours_index).value, nearest_records.get(neighbours_index).distance);

                neighbours_index++;

                if (neighbours_index >= nearest_records.size()) {

                    smaller_set_index++;
                    neighbours_index = 1;
                    nearest_records = search_structure.findNearest(smaller_set.get(smaller_set_index), number_of_records_to_consider);

                    progress_indicator.progressStep();
                }
                return next_pair;
            }
        };
    }
}

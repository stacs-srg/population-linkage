package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.*;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;

import java.util.Iterator;
import java.util.List;

public abstract class SimilaritySearchLinker extends Linker {

    double threshold;
    private int number_of_records_to_consider;
    private SearchStructureFactory search_structure_factory;

    protected SimilaritySearchLinker(SearchStructureFactory search_structure_factory, int number_of_progress_updates) {

        super(number_of_progress_updates);

        this.search_structure_factory = search_structure_factory;
        this.threshold = Double.MAX_VALUE;
        this.number_of_records_to_consider = Integer.MAX_VALUE;
    }

    @Override
    public boolean match(RecordPair pair) {

        return pair.distance <= threshold;
    }

    @Override
    public Iterable<RecordPair> getRecordPairs(final List<LXP> records1, final List<LXP> records2) throws InvalidWeightsException {

        final boolean datasets_same = records1 == records2;

        List<LXP> smaller_set = records1.size() < records2.size() ? records1 : records2;
        List<LXP> larger_set = records1.size() < records2.size() ? records2 : records1;

        SearchStructure<LXP> search_structure = search_structure_factory.newSearchStructure();

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

                DataDistance<LXP> data_distance = nearest_records.get(neighbours_index);
                RecordPair next_pair = new RecordPair(smaller_set.get(smaller_set_index), data_distance.value, data_distance.distance);

                neighbours_index++;

                if (neighbours_index >= nearest_records.size()) {

                    smaller_set_index++;

                    // Start at 1 to ignore the query record itself, if the two datasets are the same.
                    neighbours_index = datasets_same ? 1 : 0;

                    if (smaller_set_index < smaller_set.size()) {
                        nearest_records = search_structure.findNearest(smaller_set.get(smaller_set_index), number_of_records_to_consider);
                    }

                    progress_indicator.progressStep();
                }
                return next_pair;
            }
        };
    }

    public void setThreshold(double threshold) {

        this.threshold = threshold;
    }

    public void setNumberOfRecordsToConsider(int number_of_records_to_consider) {

        this.number_of_records_to_consider = number_of_records_to_consider;
    }
}

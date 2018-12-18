package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_linkage.model.RecordPair;
import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ProgressIndicator;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.Iterator;
import java.util.List;

public abstract class SimilaritySearchLinker extends Linker {

    private SearchStructureFactory<LXP> search_structure_factory;

    SimilaritySearchLinker(SearchStructureFactory<LXP> search_structure_factory, NamedMetric<LXP> distance_metric, int number_of_progress_updates) {

        super(distance_metric, number_of_progress_updates);

        this.search_structure_factory = search_structure_factory;
    }

    @Override
    public Iterable<RecordPair> getMatchingRecordPairs(final List<LXP> records1, final List<LXP> records2) {

        final List<LXP> smaller_set = records1.size() < records2.size() ? records1 : records2;
        final List<LXP> larger_set = records1.size() < records2.size() ? records2 : records1;

        SearchStructure<LXP> search_structure = search_structure_factory.newSearchStructure(larger_set);

        return new Iterable<RecordPair>() {

            class RecordPairIterator extends AbstractRecordPairIterator {

                int smaller_set_index;
                int neighbours_index;
                List<DataDistance<LXP>> nearest_records;

                RecordPairIterator(final List<LXP> records1, final List<LXP> records2, ProgressIndicator progress_indicator) {

                    super(records1, records2, progress_indicator);

                    smaller_set_index = 0;

                    // Start at 1 to ignore the query record itself, if the two datasets are the same.
                    neighbours_index = datasets_same ? 1 : 0;

                    progress_indicator.setTotalSteps(smaller_set.size());

                    getNextRecordBatch();
                    getNextPair();
                }

                 void getNextPair() {

                    while (neighbours_index >= nearest_records.size() && smaller_set_index < smaller_set.size()) {
                        getNextRecordFromSmallerSet();
                    }

                    if (!finished()) {

                        LXP target = smaller_set.get(smaller_set_index);
                        DataDistance<LXP> data_distance = nearest_records.get(neighbours_index);
                        next_pair = new RecordPair(target, data_distance.value, data_distance.distance);

                        neighbours_index++;
                        if (neighbours_index >= nearest_records.size()) getNextRecordFromSmallerSet();

                    } else {
                        next_pair = null;
                    }
                }

                boolean finished() {

                    return smaller_set_index >= smaller_set.size() || (smaller_set_index == smaller_set.size() - 1 && neighbours_index >= nearest_records.size());
                }

                @Override
                boolean match(final RecordPair pair) {
                    return true;
                }

                private void getNextRecordFromSmallerSet() {

                    smaller_set_index++;
                    progress_indicator.progressStep();

                    // Start at 1 to ignore the query record itself, if the two datasets are the same.
                    neighbours_index = datasets_same ? 1 : 0;

                    if (smaller_set_index < smaller_set.size()) getNextRecordBatch();
                }

                private void getNextRecordBatch() {

                    nearest_records = search_structure.findWithinThreshold(smaller_set.get(smaller_set_index), threshold);
                }
            }

            @Override
            public Iterator<RecordPair> iterator() {
                return new RecordPairIterator(records1, records2, progress_indicator);
            }
        };
    }
}

package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.*;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ProgressIndicator;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;

import java.util.Iterator;
import java.util.List;

public abstract class SimilaritySearchLinker extends Linker {

    private int number_of_records_to_consider;
    private SearchStructureFactory search_structure_factory;

    protected SimilaritySearchLinker(SearchStructureFactory search_structure_factory, int number_of_progress_updates) {

        super(number_of_progress_updates);

        this.search_structure_factory = search_structure_factory;
        this.number_of_records_to_consider = Integer.MAX_VALUE;
    }

    @Override
    public Iterable<RecordPair> getMatchingRecordPairs(final List<LXP> records1, final List<LXP> records2) {

        final List<LXP> smaller_set = records1.size() < records2.size() ? records1 : records2;
        final List<LXP> larger_set = records1.size() < records2.size() ? records2 : records1;

        SearchStructure<LXP> search_structure = search_structure_factory.newSearchStructure();

        for (LXP record : larger_set) {
            search_structure.add(record);
        }

        return new Iterable<RecordPair>() {

            class RecordPairIterator extends AbstractRecordPairIterator {

                int smaller_set_index;
                int neighbours_index;
                List<DataDistance<LXP>> nearest_records;

                RecordPairIterator(final List<LXP> records1, final List<LXP> records2, ProgressIndicator progress_indicator, Double threshold) {

                    super(records1, records2, progress_indicator, threshold);

                    smaller_set_index = 0;

                    // Start at 1 to ignore the query record itself, if the two datasets are the same.
                    neighbours_index = datasets_same ? 1 : 0;

                    progress_indicator.setTotalSteps(smaller_set.size());

                    getNextRecordBatch();
                    getNextMatchingPair();
                }

                boolean finished() {

                    return smaller_set_index >= smaller_set.size() || neighbours_index >= nearest_records.size();
                }

                void advanceIndices() {

                    neighbours_index++;
                    if (neighbours_index >= nearest_records.size()) getNextRecordFromSmallerSet();
                }

                void loadNextPair() {

                    LXP target = smaller_set.get(smaller_set_index);
                    DataDistance<LXP> data_distance = nearest_records.get(neighbours_index);
                    next_pair = new RecordPair(target, data_distance.value, data_distance.distance);
                }

                private void getNextRecordFromSmallerSet() {

                    smaller_set_index++;
                    progress_indicator.progressStep();

                    // Start at 1 to ignore the query record itself, if the two datasets are the same.
                    neighbours_index = datasets_same ? 1 : 0;

                    if (smaller_set_index < smaller_set.size()) getNextRecordBatch();
                }

                private void getNextRecordBatch() {

                    final LXP target = smaller_set.get(smaller_set_index);
                    nearest_records = search_structure.findNearest(target, number_of_records_to_consider);
                }
            }

            @Override
            public Iterator<RecordPair> iterator() {
                return new RecordPairIterator(records1, records2, progress_indicator, threshold);
            }
        };
    }

    /**
     * @param number_of_records_to_consider the maximum number of records that will be considered as a potential match for each record in the smaller set
     */
    public void setNumberOfRecordsToConsider(int number_of_records_to_consider) {

        this.number_of_records_to_consider = number_of_records_to_consider;
    }
}

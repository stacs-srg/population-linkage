package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ProgressIndicator;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public abstract class SimilaritySearchLinker extends Linker {

    private SearchStructureFactory<LXP> search_structure_factory;
    private SearchStructure<LXP> search_structure;
    private Iterable<LXP> smaller_set;
    private int smaller_set_size;

    private int numberOfReferenceObjects = 70;

    protected SimilaritySearchLinker(SearchStructureFactory<LXP> search_structure_factory, Metric<LXP> distance_metric, int number_of_progress_updates) {

        super(distance_metric, number_of_progress_updates);

        this.search_structure_factory = search_structure_factory;
    }

    protected SimilaritySearchLinker(SearchStructureFactory<LXP> search_structure_factory, Metric<LXP> distance_metric, int number_of_progress_updates, int numberOfReferenceObjects) {

        this(search_structure_factory, distance_metric, number_of_progress_updates);
        this.numberOfReferenceObjects = numberOfReferenceObjects;
    }


    public void addRecords(Iterable<LXP> records1, Iterable<LXP> records2) {

        super.addRecords(records1, records2);

        int records1_size = count(records1);
        int records2_size = records1 == records2 ? records1_size : count(records2);

        Iterable<LXP> larger_set;

        if (records1_size < records2_size) {
            smaller_set = records1;
            smaller_set_size = records1_size;
            larger_set = records2;
        } else {
            smaller_set = records2;
            smaller_set_size = records2_size;
            larger_set = records1;
        }

        search_structure = search_structure_factory.newSearchStructure(larger_set);
    }

    public void terminate() {
        search_structure.terminate();
    }

    @Override
    public Iterable<RecordPair> getMatchingRecordPairs(final Iterable<LXP> records1, final Iterable<LXP> records2) {

        return new Iterable<RecordPair>() {

            class RecordPairIterator extends AbstractRecordPairIterator {

                private int neighbours_index;
                private List<DataDistance<LXP>> nearest_records;
                private LXP next_record_from_smaller_set;
                private Iterator<LXP> smaller_set_iterator;

                RecordPairIterator(final Iterable<LXP> records1, final Iterable<LXP> records2, ProgressIndicator progress_indicator) {

                    super(records1, records2, progress_indicator);

                    smaller_set_iterator = smaller_set.iterator();
                    next_record_from_smaller_set = smaller_set_iterator.next();

                    neighbours_index = 0;

                    progress_indicator.setTotalSteps(smaller_set_size);

                    getNextRecordBatch();
                    getNextPair();
                }

                @Override
                boolean match(final RecordPair pair) {
                    return true;
                }

                void getNextPair() {

                    while (smaller_set_iterator.hasNext() && !moreLinksAvailableFromCurrentRecordFromSmallerSet()) {
                        getNextRecordFromSmallerSet();
                    }

                    loadPair();

                    if (pairShouldBeSkipped()) {
                        next_pair = null;
                    }
                }

                private void loadPair() {

                    do {
                        if (moreLinksAvailable()) {

                            DataDistance<LXP> data_distance = nearest_records.get(neighbours_index++);
                            next_pair = new RecordPair(next_record_from_smaller_set, data_distance.value, data_distance.distance);

                            if (!moreLinksAvailableFromCurrentRecordFromSmallerSet()) getNextRecordFromSmallerSet();

                        } else {
                            next_pair = null;
                        }
                    }
                    while (moreLinksAvailable() && pairShouldBeSkipped());
                }

                private boolean moreLinksAvailable() {

                    return smaller_set_iterator.hasNext() || moreLinksAvailableFromCurrentRecordFromSmallerSet();
                }

                private boolean moreLinksAvailableFromCurrentRecordFromSmallerSet() {

                    return neighbours_index < nearest_records.size();
                }

                private boolean pairShouldBeSkipped() {

                    return next_pair == null || (datasets_same && next_pair.record1.getId() == next_pair.record2.getId());
                }

                private void getNextRecordFromSmallerSet() {

                    if (smaller_set_iterator.hasNext()) {

                        progress_indicator.progressStep();
                        next_record_from_smaller_set = smaller_set_iterator.next();
                        neighbours_index = 0;

                        getNextRecordBatch();
                    }
                }

                private void getNextRecordBatch() {

                    nearest_records = search_structure.findWithinThreshold(next_record_from_smaller_set, threshold);
                }
            }

            @Override
            public Iterator<RecordPair> iterator() {
                return new RecordPairIterator(records1, records2, linkage_progress_indicator);
            }
        };
    }
}

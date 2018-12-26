package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_linkage.model.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ProgressIndicator;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.Iterator;

public abstract class BruteForceLinker extends Linker {

    /**
     * @param number_of_progress_updates the number of updates to be given, zero or negative to suppress updates
     */
    protected BruteForceLinker(NamedMetric<LXP> distance_metric, int number_of_progress_updates) {

        super(distance_metric, number_of_progress_updates);
    }

    @Override
    public Iterable<RecordPair> getMatchingRecordPairs(final Iterable<LXP> records1, final Iterable<LXP> records2) {

        return new Iterable<RecordPair>() {

            class RecordPairIterator extends AbstractRecordPairIterator {

                int records1_index;
                int records2_index;

                Iterator<LXP> records1_iterator;
                Iterator<LXP> records2_iterator;

                LXP record1;
                LXP record2;

                RecordPairIterator(final Iterable<LXP> records1, final Iterable<LXP> records2, ProgressIndicator progress_indicator) {

                    super(records1, records2, progress_indicator);

                    records1_iterator = records1.iterator();
                    records2_iterator = records2.iterator();

                    record1 = records1_iterator.next();
                    record2 = records2_iterator.next();

                    records1_index = 0;
                    records2_index = 0;

                    // Don't compare record with itself.
                    if (datasets_same) {
                        record2 = records2_iterator.next();
                        records2_index = 1;
                    }

                    progress_indicator.setTotalSteps(count(records1) * count(records2));
                    getNextMatchingPair();
                }

                boolean match(RecordPair pair) {

                    return pair.distance <= threshold;
                }

                void loadNextPair() {

                    next_pair = (record1 == null || record2 == null) ? null : new RecordPair(record1, record2, distance_metric.distance(record1, record2));
                }

                void advanceIndices() {

                    if (records2_iterator.hasNext()) {

                        record2 = records2_iterator.next();

                        // Don't compare record with itself.
                        if (datasets_same && record1.getId() == record2.getId() ) {
                            if (records2_iterator.hasNext()) {
                                record2 = records2_iterator.next();
                            }
                            else {
                                record2 = null;
                            }
                        }
                    }
                    else {

                        if (records1_iterator.hasNext()) {

                            record1 = records1_iterator.next();
                            records2_iterator = records2.iterator();
                            record2 = records2_iterator.next();
                        }
                        else {
                            record1 = null;
                        }
                    }

                    progress_indicator.progressStep();
                }
            }

            @Override
            public Iterator<RecordPair> iterator() {

                return new RecordPairIterator(records1, records2, linkage_progress_indicator);
            }
        };
    }
}

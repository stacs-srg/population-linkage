/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkers;

import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ProgressIndicator;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.Iterator;
import java.util.function.Function;

public abstract class BruteForceLinker extends Linker {

    /**
     * @param number_of_progress_updates the number of updates to be given, zero or negative to suppress updates
     */
    public BruteForceLinker(Metric<LXP> distance_metric, double threshold, int number_of_progress_updates,
                               String link_type, String provenace, String role_type_1, String role_type_2, Function<RecordPair, Boolean> isViableLink) {

        super(distance_metric, threshold, number_of_progress_updates, link_type, provenace, role_type_1, role_type_2, isViableLink);
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

package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_linkage.model.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ProgressIndicator;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.Iterator;
import java.util.List;

public abstract class BruteForceLinker extends Linker {

    private boolean symmetrical_links;

    /**
     * @param number_of_progress_updates the number of updates to be given, zero or negative to suppress updates
     */
    protected BruteForceLinker(NamedMetric<LXP> distance_metric, int number_of_progress_updates) {

        super(distance_metric, number_of_progress_updates);
        symmetrical_links = false;
    }

    @Override
    public Iterable<RecordPair> getMatchingRecordPairs(final List<LXP> records1, final List<LXP> records2) {

        return new Iterable<RecordPair>() {

            class RecordPairIterator extends AbstractRecordPairIterator {

                int records1_index;
                int records2_index;

                final boolean ignore_inverse_pairs;
                final int records1_size = records1.size();
                final int records2_size = records2.size();

                RecordPairIterator(final List<LXP> records1, final List<LXP> records2, boolean symmetrical_links, ProgressIndicator progress_indicator) {

                    super(records1, records2, progress_indicator);

                    records1_index = 0;

                    // Don't compare record with itself.
                    records2_index = datasets_same ? 1 : 0;

                    ignore_inverse_pairs = datasets_same && symmetrical_links;

                    // If the two datasets are the same, and links are symmetrical, then only need to check half of the possible pairs.

                    final int total_comparisons = ignore_inverse_pairs ?
                            records1.size() * (records1.size() - 1) / 2 :
                            records1.size() * records2.size();

                    progress_indicator.setTotalSteps(total_comparisons);

                    getNextMatchingPair();
                }

                boolean finished() {

                    return records1_index >= records1_size || records2_index >= records2_size;
                }

                boolean match(RecordPair pair) {

                    return pair.distance <= threshold;
                }

                void advanceIndices() {

                    records2_index++;

                    // Don't compare record with itself.
                    if (datasets_same && records2_index == records1_index) records2_index++;

                    if (records2_index >= records2.size()) {

                        records1_index++;
                        records2_index = ignore_inverse_pairs ? records1_index + 1 : 0;
                    }

                    progress_indicator.progressStep();
                }

                void loadNextPair() {

                    LXP record1 = records1.get(records1_index);
                    LXP record2 = records2.get(records2_index);
                    next_pair = new RecordPair(record1, record2, distance_metric.distance(record1, record2));
                }
            }

            @Override
            public Iterator<RecordPair> iterator() {

                return new RecordPairIterator(records1, records2, symmetrical_links, linkage_progress_indicator);
            }
        };
    }

    /**
     * @param symmetrical_links true if links are symmetrical_links, so that if A-B has been checked then B-A doesn't need to be
     */
    public void setSymmetricalLinks(boolean symmetrical_links) {

        this.symmetrical_links = symmetrical_links;
    }
}

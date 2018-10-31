package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_linkage.model.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.Iterator;
import java.util.List;

public abstract class BruteForceLinker extends Linker {

//    private final Matcher matcher;
    private NamedMetric<LXP> distance_metric;
    private boolean symmetrical_links;

    /**
     * @param number_of_progress_updates the number of updates to be given, zero or negative to suppress updates
     */
    protected BruteForceLinker(NamedMetric<LXP> distance_metric, int number_of_progress_updates) {

        super(number_of_progress_updates);

        this.distance_metric = distance_metric;
//        this.matcher = matcher;
        symmetrical_links = false;
    }

//    @Override
//    public boolean match(RecordPair pair) {
//
//        return matcher.match(pair.record1, pair.record2);
//    }

    @Override
    public Iterable<RecordPair> getMatchingRecordPairs(final List<LXP> records1, final List<LXP> records2) {

        // If the two datasets are the same, and links are symmetrical, then only need to check half of the possible pairs.
        final boolean datasets_same = records1 == records2;
        final boolean ignore_inverse_pairs = datasets_same && symmetrical_links;

        int total_comparisons = ignore_inverse_pairs ?
                records1.size() * (records1.size() - 1) / 2 :
                records1.size() * records2.size();

        progress_indicator.setTotalSteps(total_comparisons);

        return () -> new Iterator<RecordPair>() {

            int records1_index = 0;

            // Don't compare record with itself.
            int records2_index = datasets_same ? 1 : 0;

            RecordPair next_pair = getNextMatchingPair();


            @Override
            public boolean hasNext() {
//                return smaller_set_index < smaller_set.size() && neighbours_index < nearest_records.size();
                return next_pair != null;
            }

            @Override
            public RecordPair next() {

                RecordPair result = next_pair;
                next_pair = getNextMatchingPair();
                return result;
            }

            private RecordPair getNextMatchingPair() {

                RecordPair pair = getNextPair();
                while (pair != null && !match(pair)) {
                    pair = getNextPair();
                }
                return pair;
            }

            private RecordPair getNextPair() {

                if (records1_index >= records1.size() || records2_index >= records2.size()) {
                    return null;
                }

                LXP record1 = records1.get(records1_index);
                LXP record2 = records2.get(records2_index);
                RecordPair next_pair = new RecordPair(record1, record2, distance_metric.distance(record1, record2));

                records2_index++;

                // Don't compare record with itself.
                if (datasets_same && records2_index == records1_index) records2_index++;

                if (records2_index >= records2.size()) {

                    records1_index++;
                    records2_index = ignore_inverse_pairs ? records1_index + 1 : 0;
                }

                progress_indicator.progressStep();
                return next_pair;
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

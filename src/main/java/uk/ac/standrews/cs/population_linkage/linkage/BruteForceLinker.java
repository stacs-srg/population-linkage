package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.*;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.Iterator;
import java.util.List;

public abstract class BruteForceLinker extends Linker {

    private final Matcher matcher;
    private final boolean symmetrical_links;

    /**
     * @param matcher the matching rule
     * @param symmetrical_links true if links are symmetrical_links, so that if A-B has been checked then B-A doesn't need to be
     * @param number_of_progress_updates the number of updates to be given, zero or negative to suppress updates
     */
    BruteForceLinker(Matcher matcher, boolean symmetrical_links, int number_of_progress_updates) {

        super(number_of_progress_updates);

        this.matcher = matcher;
        this.symmetrical_links = symmetrical_links;
    }

    @Override
    protected boolean match(RecordPair pair) {

        return matcher.match(pair.record1, pair.record2);
    }

    @Override
    protected Iterable<RecordPair> getRecordPairs(final List<LXP> records1, final List<LXP> records2) {

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

            @Override
            public boolean hasNext() {
                return records1_index < records1.size() && records2_index < records2.size();
            }

            @Override
            public RecordPair next() {

                // Don't calculate the distance between the records.
                // The field in RecordPair is present for compatibility with similarity search, which finds the distance along with the pair.
                RecordPair next_pair = new RecordPair(records1.get(records1_index), records2.get(records2_index), -1.0);

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
}

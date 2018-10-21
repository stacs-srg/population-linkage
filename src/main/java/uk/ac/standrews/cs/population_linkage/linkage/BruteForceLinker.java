package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.*;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.Iterator;
import java.util.List;

public abstract class BruteForceLinker extends Linker {

    private final Matcher matcher;
    private final boolean symmetrical;

    BruteForceLinker(Matcher matcher, boolean symmetrical, int number_of_progress_updates) {

        super(number_of_progress_updates);
        this.matcher = matcher;
        this.symmetrical = symmetrical;
    }

    @Override
    public Links link(List<LXP> records) {

        return link(records, records);
    }

    @Override
    public Links link(List<LXP> records1, List<LXP> records2) {

        Links links = new Links();

        int total_comparisons = records1 == records2 && symmetrical ?
                records1.size() * (records1.size() - 1) / 2 :
                records1.size() * records2.size();

        progress_indicator.setTotalSteps(total_comparisons);

        for (RecordPair pair : getRecordPairs(records1, records2)) {

            if (matcher.match(pair.record1, pair.record2)) {

                Role role1 = new Role(getIdentifier1(pair.record1), getRoleType1());
                Role role2 = new Role(getIdentifier2(pair.record2), getRoleType2());
                links.add(new Link(role1, role2, 1.0f, getLinkType(), getProvenance()));
            }

            if (number_of_progress_updates > 0) progress_indicator.progressStep();
        }

        return links;
    }

    private Iterable<RecordPair> getRecordPairs(final List<LXP> records1, final List<LXP> records2) {

        return () -> new Iterator<RecordPair>() {

            boolean compare_inverse_pairs = records1 != records2 || !symmetrical;

            int i = 0;
            int j = compare_inverse_pairs ? 0 : 1;

            @Override
            public boolean hasNext() {
                return i < records1.size() && j < records2.size();
            }

            @Override
            public RecordPair next() {

                RecordPair next_pair = new RecordPair(records1.get(i), records2.get(j), -1.0);

                j++;
                if (j == i) j++;

                if (j >= records2.size()) {
                    i++;
                    j = compare_inverse_pairs ? 0 : i + 1;
                }
                return next_pair;
            }
        };
    }
}

package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.PercentageProgressIndicator;
import uk.ac.standrews.cs.utilities.ProgressIndicator;

import java.util.List;

public abstract class Linker {

    protected double threshold;

    protected final ProgressIndicator progress_indicator;

    public Linker(int number_of_progress_updates) {

        threshold = Double.MAX_VALUE;
        progress_indicator = new PercentageProgressIndicator(number_of_progress_updates);
    }

    public Links link(List<LXP> records) throws InvalidWeightsException {

        return link(records, records);
    }

    public Links link(List<LXP> records1, List<LXP> records2) throws InvalidWeightsException {

        Links links = new Links();

        for (RecordPair pair : getMatchingRecordPairs(records1, records2)) {

            if (match(pair)) {

                Role role1 = new Role(getIdentifier1(pair.record1), getRoleType1());
                Role role2 = new Role(getIdentifier2(pair.record2), getRoleType2());

                links.add(new Link(role1, role2, 1.0f, getLinkType(), getProvenance()));
            }
        }

        return links;
    }

    public boolean match(RecordPair pair) {

        return pair.distance <= threshold;
    }

    public void setThreshold(double threshold) {

        this.threshold = threshold;
    }

    public Iterable<RecordPair> getMatchingRecordPairs(final List<LXP> records) {

        return getMatchingRecordPairs(records, records);
    }

    protected abstract String getLinkType();
    protected abstract String getProvenance();
    protected abstract String getRoleType1();
    protected abstract String getRoleType2();
    protected abstract String getIdentifier1(LXP record);
    protected abstract String getIdentifier2(LXP record);

    public abstract Iterable<RecordPair> getMatchingRecordPairs(final List<LXP> records1, final List<LXP> records2);
}

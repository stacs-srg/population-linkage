package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.PercentageProgressIndicator;
import uk.ac.standrews.cs.utilities.ProgressIndicator;

import java.util.List;

public abstract class Linker {

    protected final ProgressIndicator progress_indicator;

    public Linker(int number_of_progress_updates) {

        progress_indicator = new PercentageProgressIndicator(number_of_progress_updates);
    }

    public Links link(List<LXP> records) throws InvalidWeightsException {

        return link(records, records);
    }

    public Links link(List<LXP> records1, List<LXP> records2) throws InvalidWeightsException {

        Links links = new Links();

        for (RecordPair pair : getRecordPairs(records1, records2)) {

            if (match(pair)) {

                Role role1 = new Role(getIdentifier1(pair.record1), getRoleType1());
                Role role2 = new Role(getIdentifier2(pair.record2), getRoleType2());

                links.add(new Link(role1, role2, 1.0f, getLinkType(), getProvenance()));
            }
        }

        return links;
    }

    public Iterable<RecordPair> getRecordPairs(final List<LXP> records) throws InvalidWeightsException {
        return getRecordPairs(records, records);
    }

    protected abstract String getLinkType();
    protected abstract String getProvenance();
    protected abstract String getRoleType1();
    protected abstract String getRoleType2();
    protected abstract String getIdentifier1(LXP record);
    protected abstract String getIdentifier2(LXP record);
    public abstract boolean match(RecordPair pair);
    public abstract Iterable<RecordPair> getRecordPairs(final List<LXP> records1, final List<LXP> records2) throws InvalidWeightsException;
}

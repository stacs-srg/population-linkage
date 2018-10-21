package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.PercentageProgressIndicator;
import uk.ac.standrews.cs.utilities.ProgressIndicator;

import java.util.List;

public abstract class Linker {

    public abstract Links link(List<LXP> records);

    public abstract Links link(List<LXP> records1, List<LXP> records2);

    protected abstract String getLinkType();
    protected abstract String getProvenance();
    protected abstract String getRoleType1();
    protected abstract String getRoleType2();
    protected abstract String getIdentifier1(LXP record);
    protected abstract String getIdentifier2(LXP record);

    protected final int number_of_progress_updates;
    protected final ProgressIndicator progress_indicator;

    public Linker(int number_of_progress_updates) {

        this.number_of_progress_updates = number_of_progress_updates;

        progress_indicator = new PercentageProgressIndicator(number_of_progress_updates);
    }
}

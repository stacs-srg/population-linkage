package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.storr.interfaces.IStoreReference;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class BruteForceSiblingBundlerOverBirths extends BruteForceLinker {

    private double threshold;

    public BruteForceSiblingBundlerOverBirths(Metric<LXP> distance_metric, double threshold, int number_of_progress_updates) {

        super(distance_metric, number_of_progress_updates);

        this.threshold = threshold;
    }

    @Override
    protected String getLinkType() {
        return "sibling";
    }

    @Override
    protected String getProvenance() {
        return "threshold match at " + threshold;
    }

    @Override
    protected String getRoleType1() {
        return Birth.ROLE_BABY;
    }

    @Override
    protected String getRoleType2() {
        return Birth.ROLE_BABY;
    }

    @Override
    protected IStoreReference getIdentifier1(LXP record) throws PersistentObjectException {
        return record.getThisRef();
    }

    @Override
    protected IStoreReference getIdentifier2(LXP record) throws PersistentObjectException {
        return record.getThisRef();
    }
}

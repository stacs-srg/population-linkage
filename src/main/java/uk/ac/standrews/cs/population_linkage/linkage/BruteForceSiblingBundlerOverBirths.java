package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.model.InvalidWeightsException;
import uk.ac.standrews.cs.population_linkage.model.ThresholdMatcher;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

public class BruteForceSiblingBundlerOverBirths extends BruteForceLinker {

    private double threshold;

    public BruteForceSiblingBundlerOverBirths(double threshold, int number_of_progress_updates) throws InvalidWeightsException {

        super(new ThresholdMatcher(Utilities.weightedAverageLevenshteinOverBirths(), threshold), number_of_progress_updates);

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
    protected String getIdentifier1(LXP record) {
        return record.getString(BirthLinkageSubRecord.STANDARDISED_ID);
    }

    @Override
    protected String getIdentifier2(LXP record) {
        return record.getString(BirthLinkageSubRecord.STANDARDISED_ID);
    }
}

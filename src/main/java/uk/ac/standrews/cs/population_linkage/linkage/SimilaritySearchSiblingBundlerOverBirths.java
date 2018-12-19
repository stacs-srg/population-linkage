package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

public class SimilaritySearchSiblingBundlerOverBirths extends SimilaritySearchLinker {

    public SimilaritySearchSiblingBundlerOverBirths(SearchStructureFactory<LXP> search_structure_factory, double threshold, NamedMetric<LXP> distance_metric, int number_of_progress_updates) {

        super(search_structure_factory, distance_metric, number_of_progress_updates);
        setThreshold(threshold);
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

package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

public class SimilaritySearchSiblingBundlerOverBirths extends SimilaritySearchLinker {

    public SimilaritySearchSiblingBundlerOverBirths(SearchStructureFactory search_structure_factory, double threshold, int number_of_records_to_consider, int number_of_progress_updates) {

        super(search_structure_factory, number_of_progress_updates);
        setThreshold(threshold);
        setNumberOfRecordsToConsider(number_of_records_to_consider);
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

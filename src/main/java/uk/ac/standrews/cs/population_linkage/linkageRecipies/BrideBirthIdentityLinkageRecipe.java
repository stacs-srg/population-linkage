package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.*;

public class BrideBirthIdentityLinkageRecipe extends LinkageRecipe {

    public BrideBirthIdentityLinkageRecipe(String results_repository_name, String links_persistent_name, String source_repository_name, RecordRepository record_repository) {
        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        final String bride_id = record1.getString(Marriage.BRIDE_IDENTITY);
        final String child_id = record2.getString(Birth.CHILD_IDENTITY);

        if (child_id.isEmpty() || bride_id.isEmpty()) return LinkStatus.UNKNOWN;

        return child_id.equals(bride_id) ? LinkStatus.TRUE_MATCH : LinkStatus.NOT_TRUE_MATCH;
    }

    @Override
    public String getLinkageType() {
        return "identity bundling between a bride and her birth record";
    }

    @Override
    public Class getStoredType() {
        return Marriage.class;
    }

    @Override
    public Class getSearchType() {
        return Birth.class;
    }

    @Override
    public String getStoredRole() {
        return Marriage.ROLE_BRIDE;
    }

    @Override
    public String getSearchRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return Constants.BRIDE_IDENTITY_LIKAGE_FIELDS;
    }

    @Override
    public List<Integer> getSearchMappingFields() { return Constants.BABY_IDENTITY_LINKAGE_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOn(Marriage.BRIDE_IDENTITY, Birth.CHILD_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthTrueLinksOn(Marriage.BRIDE_IDENTITY, Birth.CHILD_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinksPostFilter() {
        return getNumberOfGroundTruthTrueLinksPostFilterOn(Marriage.BRIDE_IDENTITY, Birth.CHILD_IDENTITY);
    }

}

package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.*;

public class DeathBrideOwnMarriageIdentityLinkageRecipe extends LinkageRecipe {

    public DeathBrideOwnMarriageIdentityLinkageRecipe(String results_repository_name, String links_persistent_name, String source_repository_name, RecordRepository record_repository) {
        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
    }

    @Override
    public LinkStatus isTrueMatch(LXP death, LXP marriage) {
        String deceasedID = death.getString(Death.DECEASED_IDENTITY).trim();
        String brideID = marriage.getString(Marriage.BRIDE_IDENTITY).trim();

        if(deceasedID.isEmpty() || brideID.isEmpty() ) {
            return LinkStatus.UNKNOWN;
        }

        if (deceasedID.equals(brideID) ) {
            return LinkStatus.TRUE_MATCH;
        } else {
            return LinkStatus.NOT_TRUE_MATCH;
        }
    }

    @Override
    public String getLinkageType() {
        return "identity bundling between deaths and brides own marriage";
    }

    @Override
    public Class getStoredType() {
        return Death.class;
    }

    @Override
    public Class getSearchType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public String getSearchRole() { return Marriage.ROLE_BRIDE; }

    @Override
    public List<Integer> getLinkageFields() { return Constants.DEATH_IDENTITY_LINKAGE_FIELDS; }

    @Override
    public List<Integer> getSearchMappingFields() { return Constants.BRIDE_IDENTITY_LINKAGE_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOn(Death.DECEASED_IDENTITY, Marriage.BRIDE_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthTrueLinksOn(Death.DECEASED_IDENTITY, Marriage.BRIDE_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinksPostFilter() {
        return getNumberOfGroundTruthTrueLinksPostFilterOn(Death.DECEASED_IDENTITY, Marriage.BRIDE_IDENTITY);
    }

    @Override
    public Iterable<LXP> getPreFilteredStoredRecords() {
        return filterBySex(
                super.getPreFilteredStoredRecords(),
                Birth.SEX, "f");
    }

}

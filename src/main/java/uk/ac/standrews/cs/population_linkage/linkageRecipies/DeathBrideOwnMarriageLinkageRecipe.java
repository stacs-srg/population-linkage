package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.*;

public class DeathBrideOwnMarriageLinkageRecipe extends LinkageRecipe {

    public DeathBrideOwnMarriageLinkageRecipe(String results_repository_name, String links_persistent_name, String source_repository_name, RecordRepository record_repository) {
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
    public String getSourceType1() {
        return "deaths";
    }

    @Override
    public String getSourceType2() {
        return "marriages";
    }

    @Override
    public String getRole1() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public String getRole2() { return Marriage.ROLE_BRIDE; }

    @Override
    public List<Integer> getLinkageFields1() { return Constants.DEATH_IDENTITY_LINKAGE_FIELDS; }

    @Override
    public List<Integer> getLinkageFields2() { return Constants.BRIDE_IDENTITY_LINKAGE_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOn(Death.DECEASED_IDENTITY, Marriage.BRIDE_IDENTITY);
    }

    @Override
    public int numberOfGroundTruthTrueLinks() {
        return numberOfGroundTruthTrueLinksOn(Death.DECEASED_IDENTITY, Marriage.BRIDE_IDENTITY);
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {
        return filterSourceRecords(getSourceRecords1(), new int[]{
                        Death.FATHER_FORENAME, Death.FATHER_SURNAME,
                        Death.MOTHER_FORENAME, Death.MOTHER_MAIDEN_SURNAME,
                        Death.FORENAME, Death.SURNAME},
                3);
    }


    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {
        return filterSourceRecords(getSourceRecords2(), new int[]{
                        Marriage.BRIDE_FATHER_FORENAME, Marriage.BRIDE_FATHER_SURNAME,
                        Marriage.BRIDE_MOTHER_FORENAME, Marriage.BRIDE_MOTHER_MAIDEN_SURNAME,
                        Marriage.BRIDE_FORENAME, Marriage.BRIDE_SURNAME},
                3);
    }
}

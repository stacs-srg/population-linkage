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

public class DeathGroomOwnMarriageIdentityLinkageRecipe extends LinkageRecipe {

    public DeathGroomOwnMarriageIdentityLinkageRecipe(String results_repository_name, String links_persistent_name, String source_repository_name, RecordRepository record_repository) {
        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
    }

    @Override
    public LinkStatus isTrueMatch(LXP death, LXP marriage) {
        String deceasedID = death.getString(Death.DECEASED_IDENTITY).trim();
        String groomID = marriage.getString(Marriage.GROOM_IDENTITY).trim();

        if(deceasedID.isEmpty() || groomID.isEmpty()) {
            return LinkStatus.UNKNOWN;
        }

        if (deceasedID.equals(groomID) ) {
            return LinkStatus.TRUE_MATCH;
        } else {
            return LinkStatus.NOT_TRUE_MATCH;
        }
    }

    @Override
    public String getLinkageType() {
        return "identity bundling between deaths and grooms own marriage";
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
    public String getRole2() { return Marriage.ROLE_GROOM; }

    @Override
    public List<Integer> getLinkageFields1() { return Constants.DEATH_IDENTITY_LINKAGE_FIELDS; }

    @Override
    public List<Integer> getLinkageFields2() { return Constants.GROOM_IDENTITY_LINKAGE_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOn(Death.DECEASED_IDENTITY, Marriage.GROOM_IDENTITY);
    }

    public int numberOfGroundTruthTrueLinks() {
        return numberOfGroundTruthTrueLinksOn(Death.DECEASED_IDENTITY, Marriage.GROOM_IDENTITY);
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {
        return filterBySex(
                filterSourceRecords(getSourceRecords1(), new int[]{
                                Death.FATHER_FORENAME, Death.FATHER_SURNAME,
                                Death.MOTHER_FORENAME, Death.MOTHER_MAIDEN_SURNAME,
                                Death.FORENAME, Death.SURNAME},
                        3),
                Birth.SEX, "f");
    }


    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {
        return filterSourceRecords(getSourceRecords2(), new int[]{
                Marriage.GROOM_FATHER_FORENAME, Marriage.GROOM_FATHER_SURNAME,
                Marriage.GROOM_MOTHER_FORENAME, Marriage.GROOM_MOTHER_MAIDEN_SURNAME,
                Marriage.GROOM_FORENAME, Marriage.GROOM_SURNAME
        }, 3);
    }
}

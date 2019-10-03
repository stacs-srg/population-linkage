package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.*;

public class BirthDeathIdentityLinkageRecipe extends LinkageRecipe {

    public BirthDeathIdentityLinkageRecipe(String results_repository_name, String links_persistent_name, String source_repository_name, RecordRepository record_repository) {
        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        final String b1_baby_id = record1.getString(Birth.CHILD_IDENTITY).trim();
        final String d2_deceased_id = record2.getString(Death.DECEASED_IDENTITY).trim();

        if (b1_baby_id.isEmpty() || d2_deceased_id.isEmpty() ) {
            return LinkStatus.UNKNOWN;
        } else if (b1_baby_id.equals( d2_deceased_id ) ) {
            return LinkStatus.TRUE_MATCH;
        } else {
            return LinkStatus.NOT_TRUE_MATCH;
        }
    }

    @Override
    public String getLinkageType() {
        return "identity links between babies on birth records and deceased on death records - same person in roles of baby and deceased";
    }

    @Override
    public String getSourceType1() {
        return "births";
    }

    @Override
    public String getSourceType2() {
        return "deaths";
    }

    @Override
    public String getRole1() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getRole2() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public List<Integer> getLinkageFields1() {
        return Constants.BABY_IDENTITY_LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getLinkageFields2() { return Constants.DEATH_IDENTITY_LINKAGE_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOn(Birth.CHILD_IDENTITY, Death.DECEASED_IDENTITY);
    }

    @Override
    public int numberOfGroundTruthTrueLinks() {
        return numberOfGroundTruthTrueLinksOn(Birth.CHILD_IDENTITY, Death.DECEASED_IDENTITY);
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {
        return filterSourceRecords(getSourceRecords1(), new int[]{Birth.FORENAME, Birth.SURNAME});
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {
        return filterSourceRecords(getSourceRecords2(), new int[]{Death.FORENAME, Death.SURNAME});
    }

}

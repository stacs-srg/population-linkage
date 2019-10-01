package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.*;

public class BirthMotherIdentityLinkageRecipe extends LinkageRecipe {

    public BirthMotherIdentityLinkageRecipe(String results_repository_name, String links_persistent_name, String source_repository_name, RecordRepository record_repository) {
        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        final String b1_baby_id = record1.getString(Birth.CHILD_IDENTITY);
        final String b2_father_id = record2.getString(Birth.MOTHER_IDENTITY);

        if (b1_baby_id.isEmpty() || b2_father_id.isEmpty() ) {
            return LinkStatus.UNKNOWN;
        } else if (b1_baby_id.equals( b2_father_id ) ) {
            return LinkStatus.TRUE_MATCH;
        } else {
            return LinkStatus.NOT_TRUE_MATCH;
        }
    }

    @Override
    public String getLinkageType() {
        return "identity bundling between babies on birth records and mothers on birth records - same person in roles of baby and mother";
    }

    @Override
    public String getSourceType1() {
        return "births";
    }

    @Override
    public String getSourceType2() {
        return "births";
    }

    @Override
    public String getRole1() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getRole2() {
        return Birth.ROLE_MOTHER;
    }

    @Override
    public List<Integer> getLinkageFields1() {
        return Constants.BIRTH_FATHER_BABY_LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getLinkageFields2() { return Constants.BIRTH_MOTHER_MOTHER_LINKAGE_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOn(Birth.CHILD_IDENTITY, Birth.MOTHER_IDENTITY);
    }

    @Override
    public int numberOfGroundTruthTrueLinks() {
        return numberOfGroundTruthTrueLinksOn(Birth.CHILD_IDENTITY, Birth.MOTHER_IDENTITY);
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {
        return filterSourceRecords(getSourceRecords1(), new int[]{Birth.FORENAME, Birth.SURNAME});
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {
        return filterSourceRecords(getSourceRecords2(), new int[]{Birth.MOTHER_FORENAME, Birth.MOTHER_MAIDEN_SURNAME});
    }
}

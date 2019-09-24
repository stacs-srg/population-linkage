package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BirthDeathSiblingLinkageRecipe extends LinkageRecipe {

    public BirthDeathSiblingLinkageRecipe(String results_repository_name, String links_persistent_name, String ground_truth_persistent_name, String source_repository_name, RecordRepository record_repository) {
        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {

        String childFatherID = record1.getString(Birth.FATHER_IDENTITY).trim();
        String childMotherID = record1.getString(Birth.MOTHER_IDENTITY).trim();

        String decFatherID = record2.getString(Death.FATHER_IDENTITY).trim();
        String decMotherID = record2.getString(Death.MOTHER_IDENTITY).trim();

        if(childFatherID.isEmpty() || childMotherID.isEmpty() || decFatherID.isEmpty() || decMotherID.isEmpty())
            return LinkStatus.UNKNOWN;

        if(childFatherID.equals(decFatherID) && childMotherID.equals(decMotherID))
            return LinkStatus.TRUE_MATCH;

        return LinkStatus.NOT_TRUE_MATCH;
    }

    @Override
    public String getLinkageType() {
        return "birth-death-sibling";
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
        return Constants.SIBLING_BUNDLING_BIRTH_TO_DEATH_LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getLinkageFields2() {
        return Constants.SIBLING_BUNDLING_DEATH_TO_BIRTH_LINKAGE_FIELDS;
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOnSiblingNonSymmetric(Birth.FATHER_IDENTITY, Birth.FATHER_IDENTITY, Death.FATHER_IDENTITY, Death.MOTHER_IDENTITY);
    }

    @Override
    public int numberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthLinksOnSiblingNonSymmetric(Birth.FATHER_IDENTITY, Birth.FATHER_IDENTITY, Death.FATHER_IDENTITY, Death.MOTHER_IDENTITY);
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {
        return filterSourceRecords(getSourceRecords1(),
                new int[]{Birth.FATHER_FORENAME, Birth.FATHER_SURNAME,
                        Birth.MOTHER_FORENAME, Birth.MOTHER_MAIDEN_SURNAME},
                3);
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {
        return filterSourceRecords(getSourceRecords2(),
                new int[]{Death.FATHER_FORENAME, Death.FATHER_SURNAME,
                        Death.MOTHER_FORENAME, Death.MOTHER_MAIDEN_SURNAME},
                3);
    }
}

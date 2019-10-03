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
    public String getSourceType1() {
        return "marriages";
    }

    @Override
    public String getSourceType2() {
        return "births";
    }

    @Override
    public String getRole1() {
        return Marriage.ROLE_BRIDE;
    }

    @Override
    public String getRole2() {
        return Birth.ROLE_BABY;
    }

    @Override
    public List<Integer> getLinkageFields1() {
        return Constants.BRIDE_IDENTITY_LIKAGE_FIELDS;
    }

    @Override
    public List<Integer> getLinkageFields2() { return Constants.BABY_IDENTITY_LINKAGE_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOn(Marriage.BRIDE_IDENTITY, Birth.CHILD_IDENTITY);
    }

    @Override
    public int numberOfGroundTruthTrueLinks() {
        return numberOfGroundTruthTrueLinksOn(Marriage.BRIDE_IDENTITY, Birth.CHILD_IDENTITY);
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {

        return filterSourceRecords(getSourceRecords1(), new int[]{
                        Marriage.BRIDE_FORENAME, Marriage.BRIDE_SURNAME,
                        Marriage.BRIDE_FATHER_FORENAME, Marriage.BRIDE_FATHER_SURNAME,
                        Marriage.BRIDE_MOTHER_FORENAME, Marriage.BRIDE_MOTHER_MAIDEN_SURNAME},
                        3);
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {
        return filterBySex(
                filterSourceRecords(getSourceRecords2(), new int[]{
                        Birth.FORENAME, Birth.SURNAME,
                        Birth.FATHER_FORENAME, Birth.FATHER_SURNAME,
                        Birth.MOTHER_FORENAME, Birth.MOTHER_MAIDEN_SURNAME
                }, 3)
                ,Birth.SEX, "f");
    }

}

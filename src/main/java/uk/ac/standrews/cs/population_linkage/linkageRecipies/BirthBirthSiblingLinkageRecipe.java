package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.*;

public class BirthBirthSiblingLinkageRecipe extends LinkageRecipe {

    private final Iterable<LXP> birth_records;

    public BirthBirthSiblingLinkageRecipe(String results_repository_name, String links_persistent_name, String source_repository_name, RecordRepository record_repository) {
        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
        birth_records = Utilities.getBirthRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords1() {
        return birth_records;
    }

    @Override
    public Iterable<LXP> getSourceRecords2() {
        return birth_records;
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {

        final String b1_mother_id = record1.getString(Birth.MOTHER_IDENTITY).trim();
        final String b2_mother_id = record2.getString(Birth.MOTHER_IDENTITY).trim();

        final String b1_father_id = record1.getString(Birth.FATHER_IDENTITY).trim();
        final String b2_father_id = record2.getString(Birth.FATHER_IDENTITY).trim();

        if (b1_mother_id.isEmpty() || b1_father_id.isEmpty() || b2_mother_id.isEmpty() || b2_father_id.isEmpty()) return LinkStatus.UNKNOWN;

        if (b1_mother_id.equals(b2_mother_id) && b1_father_id.equals(b2_father_id)) return LinkStatus.TRUE_MATCH;

        return LinkStatus.NOT_TRUE_MATCH;
    }

    @Override
    public String getLinkageType() {
        return "sibling bundling between babies on birth records";
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
        return Birth.ROLE_BABY;
    }

    @Override
    public List<Integer> getLinkageFields1() {
        return Constants.SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getLinkageFields2() {
        return Constants.SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS;
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOnSiblingSymmetric(Birth.FATHER_IDENTITY, Birth.MOTHER_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthLinksOnSiblingSymmetric(Birth.FATHER_IDENTITY, Birth.MOTHER_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinksPostFilter() {
        return getNumberOfGroundTruthLinksPostFilterOnSiblingSymmetric(Birth.FATHER_IDENTITY, Birth.MOTHER_IDENTITY);
    }

    // This has been left as it's called by the groun truth classes - however it seems overly complicated, the above isTrueMatch method should always return the same as this for synthetic and for umea
    public static LinkStatus trueMatch(LXP record1, LXP record2) {

        final String b1_parent_marriage_id = record1.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);
        final String b2_parent_marriage_id = record2.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);

        final String b1_mother_id = record1.getString(Birth.MOTHER_IDENTITY);
        final String b2_mother_id = record2.getString(Birth.MOTHER_IDENTITY);

        final String b1_father_id = record1.getString(Birth.FATHER_IDENTITY);
        final String b2_father_id = record2.getString(Birth.FATHER_IDENTITY);

        final String b1_mother_birth_id = record1.getString(Birth.MOTHER_BIRTH_RECORD_IDENTITY);
        final String b2_mother_birth_id = record2.getString(Birth.MOTHER_BIRTH_RECORD_IDENTITY);

        final String b1_father_birth_id = record1.getString(Birth.FATHER_BIRTH_RECORD_IDENTITY);
        final String b2_father_birth_id = record2.getString(Birth.FATHER_BIRTH_RECORD_IDENTITY);

        if (!b1_parent_marriage_id.isEmpty() && b1_parent_marriage_id.equals(b2_parent_marriage_id)) return LinkStatus.TRUE_MATCH;

        if (!b1_mother_id.isEmpty() && b1_mother_id.equals(b2_mother_id) && !b1_father_id.isEmpty() && b1_father_id.equals(b2_father_id)) return LinkStatus.TRUE_MATCH;

        if (!b1_mother_birth_id.isEmpty() && b1_mother_birth_id.equals(b2_mother_birth_id) && !b1_father_birth_id.isEmpty() && b1_father_birth_id.equals(b2_father_birth_id)) return LinkStatus.TRUE_MATCH;

        if (b1_parent_marriage_id.isEmpty() && b2_parent_marriage_id.isEmpty() &&
                b1_mother_id.isEmpty() && b2_mother_id.isEmpty() &&
                b1_father_id.isEmpty() && b2_father_id.isEmpty() &&
                b1_mother_birth_id.isEmpty() && b2_mother_birth_id.isEmpty() &&
                b1_father_birth_id.isEmpty() && b2_father_birth_id.isEmpty()) return LinkStatus.UNKNOWN;

        return LinkStatus.NOT_TRUE_MATCH;
    }
}

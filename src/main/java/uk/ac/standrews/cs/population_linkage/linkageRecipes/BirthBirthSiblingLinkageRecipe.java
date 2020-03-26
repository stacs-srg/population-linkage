package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BirthBirthSiblingLinkageRecipe extends LinkageRecipe {

    private static final boolean TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN = false;

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe linkageRecipe = new BirthBirthSiblingLinkageRecipe(sourceRepo,
                resultsRepo, linkageType + "-links");

        new BitBlasterLinkageRunner()
                .run(linkageRecipe, new JensenShannon(2048), 0.67, true, 5, false, false, true, false
                );
    }

    public static final String linkageType = "birth-birth-sibling";

    public BirthBirthSiblingLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
    }

    @Override
    public String getLinkageType() {
        return linkageType;
    }

    @Override
    public Class getStoredType() {
        return Birth.class;
    }

    @Override
    public Class getSearchType() {
        return Birth.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getSearchRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return Arrays.asList(
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME,
                Birth.PARENTS_PLACE_OF_MARRIAGE,
                Birth.PARENTS_DAY_OF_MARRIAGE,
                Birth.PARENTS_MONTH_OF_MARRIAGE,
                Birth.PARENTS_YEAR_OF_MARRIAGE
        );
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return isViable(proposedLink);
    }

    public static boolean isViable(RecordPair proposedLink) {

        if (LinkageConfig.MAX_SIBLING_AGE_DIFF == null) return true;

        try {
            int year_of_birth1 = Integer.parseInt(proposedLink.record1.getString(Birth.BIRTH_YEAR));
            int year_of_birth2 = Integer.parseInt(proposedLink.record2.getString(Birth.BIRTH_YEAR));

            return Math.abs(year_of_birth1 - year_of_birth2) <= LinkageConfig.MAX_SIBLING_AGE_DIFF;

        } catch (NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
            return true;
        }
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return getLinkageFields();
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {

        return trueMatch(record1, record2);
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

    public static LinkStatus trueMatch(LXP record1, LXP record2) {

        // Various possible relevant sources of ground truth for siblings:
        // * identities of parents
        // * identities of parents' marriage record
        // * identities of parents' birth records

        // If TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN is false, then the recipe is tuned to the Umea dataset,
        // for which it is assumed that where an identifier is not
        // present, this means that the corresponding person/record is not included in the dataset. This
        // would be because the parent was not born or married within the geographical and temporal region.

        // Therefore we interpret absence of an identifier as having a particular meaning, and thus where
        // one record in a pair has an identifier and one doesn't, we classify as a non-match.

        // For use in a more general context with dirtier data, TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN
        // should be set to true. We then have less information about what a missing
        // identifier means, so classify as unknown.

        final String b1_mother_id = record1.getString(Birth.MOTHER_IDENTITY);
        final String b2_mother_id = record2.getString(Birth.MOTHER_IDENTITY);

        final String b1_father_id = record1.getString(Birth.FATHER_IDENTITY);
        final String b2_father_id = record2.getString(Birth.FATHER_IDENTITY);

        final String b1_parent_marriage_id = record1.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);
        final String b2_parent_marriage_id = record2.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);

        final String b1_mother_birth_id = record1.getString(Birth.MOTHER_BIRTH_RECORD_IDENTITY);
        final String b2_mother_birth_id = record2.getString(Birth.MOTHER_BIRTH_RECORD_IDENTITY);

        final String b1_father_birth_id = record1.getString(Birth.FATHER_BIRTH_RECORD_IDENTITY);
        final String b2_father_birth_id = record2.getString(Birth.FATHER_BIRTH_RECORD_IDENTITY);

        if (equalsNonEmpty(b1_mother_id, b2_mother_id) && equalsNonEmpty(b1_father_id, b2_father_id)) return LinkStatus.TRUE_MATCH;

        if (equalsNonEmpty(b1_parent_marriage_id, b2_parent_marriage_id)) return LinkStatus.TRUE_MATCH;

        if (equalsNonEmpty(b1_mother_birth_id, b2_mother_birth_id) && equalsNonEmpty(b1_father_birth_id, b2_father_birth_id)) return LinkStatus.TRUE_MATCH;

        if (TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN) {

            if (anyEmpty(
                    b1_parent_marriage_id, b2_parent_marriage_id,
                    b1_mother_id, b2_mother_id, b1_father_id, b2_father_id,
                    b1_mother_birth_id, b2_mother_birth_id, b1_father_birth_id, b2_father_birth_id))
                return LinkStatus.UNKNOWN;
        }

        else {
            if (allEmpty(
                    b1_parent_marriage_id, b2_parent_marriage_id,
                    b1_mother_id, b2_mother_id, b1_father_id, b2_father_id,
                    b1_mother_birth_id, b2_mother_birth_id, b1_father_birth_id, b2_father_birth_id))
                return LinkStatus.UNKNOWN;
        }

        return LinkStatus.NOT_TRUE_MATCH;
    }

    private static boolean equalsNonEmpty(final String s1, final String s2) {
        return !s1.isEmpty() && s1.equals(s2);
    }

    private static boolean allEmpty(final String... strings) {
        for (String s : strings) {
            if (!s.isEmpty()) return false;
        }
        return true;
    }

    private static boolean anyEmpty(final String... strings) {
        for (String s : strings) {
            if (s.isEmpty()) return true;
        }
        return false;
    }
}

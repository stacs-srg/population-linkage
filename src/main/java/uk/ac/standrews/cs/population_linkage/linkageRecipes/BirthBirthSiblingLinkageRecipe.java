package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.*;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

public class BirthBirthSiblingLinkageRecipe extends LinkageRecipe {

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

        if(LinkageConfig.SIBLINGS_MAX_AGE_DIFF == null) return true;

        try {
            int yob1 = Integer.parseInt(proposedLink.record1.getString(Birth.BIRTH_YEAR));
            int yob2 = Integer.parseInt(proposedLink.record2.getString(Birth.BIRTH_YEAR));

            return Math.abs(yob1 - yob2) <= LinkageConfig.SIBLINGS_MAX_AGE_DIFF;

        } catch(NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
            return true;
        }
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return getLinkageFields();
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

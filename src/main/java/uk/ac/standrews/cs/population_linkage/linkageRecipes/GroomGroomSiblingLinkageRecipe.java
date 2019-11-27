package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import java.util.Arrays;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.Normalisation;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.List;
import java.util.Map;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

public class GroomGroomSiblingLinkageRecipe extends LinkageRecipe {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe linkageRecipe = new GroomGroomSiblingLinkageRecipe(sourceRepo, resultsRepo,
                linkageType + "-links");

        new BitBlasterLinkageRunner()
                .run(linkageRecipe, new JensenShannon(2048), 0.67, true, 5, false, false, true, false
                );
    }

    public static final String linkageType = "groom-groom-sibling";

    public GroomGroomSiblingLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        final String m1_mother_id = record1.getString(Marriage.GROOM_MOTHER_IDENTITY);
        final String m2_mother_id = record2.getString(Marriage.GROOM_MOTHER_IDENTITY);

        final String m1_father_id = record1.getString(Marriage.GROOM_FATHER_IDENTITY);
        final String m2_father_id = record2.getString(Marriage.GROOM_FATHER_IDENTITY);

        if (!m1_mother_id.isEmpty() && m1_mother_id.equals(m2_mother_id) && !m1_father_id.isEmpty() && m1_father_id.equals(m2_father_id)) return LinkStatus.TRUE_MATCH;

        return LinkStatus.NOT_TRUE_MATCH;
    }

    @Override
    public String getLinkageType() {
        return linkageType;
    }

    @Override
    public Class getStoredType() {
        return Marriage.class;
    }

    @Override
    public Class getSearchType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Marriage.ROLE_GROOM;
    }

    @Override
    public String getSearchRole() {
        return Marriage.ROLE_GROOM;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return Arrays.asList(
                Marriage.GROOM_FATHER_FORENAME,
                Marriage.GROOM_FATHER_SURNAME,
                Marriage.GROOM_MOTHER_FORENAME,
                Marriage.GROOM_MOTHER_MAIDEN_SURNAME
        );
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return isViable( proposedLink );
    }

    public static boolean isViable(RecordPair proposedLink) {
        if(LinkageConfig.SIBLINGS_MAX_AGE_DIFF == null) return true;

        try {

            int groom1birthYear = Integer.parseInt(Normalisation.extractYear(proposedLink.record1.getString(Marriage.GROOM_AGE_OR_DATE_OF_BIRTH)));  // assumes that this field is a date
            int groom2birthYear = Integer.parseInt(Normalisation.extractYear(proposedLink.record2.getString(Marriage.GROOM_AGE_OR_DATE_OF_BIRTH)));  // assumes that this field is a date

            int groom1Age = Integer.parseInt(proposedLink.record1.getString(Marriage.YEAR_OF_REGISTRATION)) - groom1birthYear;
            int groom2Age = Integer.parseInt(proposedLink.record2.getString(Marriage.YEAR_OF_REGISTRATION)) - groom2birthYear;

            boolean possibleSiblings = Math.abs(groom1Age - groom2Age) <= LinkageConfig.SIBLINGS_MAX_AGE_DIFF;

            return possibleSiblings;

        } catch(NumberFormatException e) { 
            return true;
        }
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return Arrays.asList(
                Marriage.GROOM_FATHER_FORENAME,
                Marriage.GROOM_FATHER_SURNAME,
                Marriage.GROOM_MOTHER_FORENAME,
                Marriage.GROOM_MOTHER_MAIDEN_SURNAME
        );
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOnSiblingSymmetric(Marriage.GROOM_FATHER_IDENTITY, Marriage.GROOM_MOTHER_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthLinksOnSiblingSymmetric(Marriage.GROOM_FATHER_IDENTITY, Marriage.GROOM_MOTHER_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinksPostFilter() {
        return getNumberOfGroundTruthLinksPostFilterOnSiblingSymmetric(Marriage.GROOM_FATHER_IDENTITY, Marriage.GROOM_MOTHER_IDENTITY);
    }

}

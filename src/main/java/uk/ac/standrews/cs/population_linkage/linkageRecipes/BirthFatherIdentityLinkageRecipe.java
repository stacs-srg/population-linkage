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

public class BirthFatherIdentityLinkageRecipe extends LinkageRecipe {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe linkageRecipe = new BirthFatherIdentityLinkageRecipe(sourceRepo,
                resultsRepo, linkageType + "-links");

        LinkageConfig.numberOfROs = 20;

        new BitBlasterLinkageRunner()
                .run(linkageRecipe, new JensenShannon(2048), 0.2, true,
                        2, false, false, true, false
                );
    }

    public static final String linkageType = "birth-father-identity";

    public BirthFatherIdentityLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        final String b1_baby_id = record1.getString(Birth.CHILD_IDENTITY);
        final String b2_father_id = record2.getString(Birth.FATHER_IDENTITY);

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
        return Birth.ROLE_FATHER;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return Arrays.asList(
            Birth.FORENAME,
            Birth.SURNAME
        );
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return isViable( proposedLink );
    }

    /*
     * This is a bit (a lot (Graham made me do it) of a hack) - can call the static method from the GT experiments
     */
    public static boolean isViable(RecordPair proposedLink) {
        try {
            int fathersYOB = Integer.parseInt(proposedLink.record1.getString(Birth.BIRTH_YEAR));
            int childsYOB = Integer.parseInt(proposedLink.record2.getString(Birth.BIRTH_YEAR));

            return fathersYOB + LinkageConfig.MIN_AGE_AT_BIRTH <= childsYOB && childsYOB <= fathersYOB + LinkageConfig.MALE_MAX_AGE_AT_BIRTH;
        } catch (NumberFormatException e) {
            return true; // a YOB is missing or in an unexpected format
        }
    }



    @Override
    public List<Integer> getSearchMappingFields() { return Arrays.asList(
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME
        );
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOn(Birth.CHILD_IDENTITY, Birth.FATHER_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthTrueLinksOn(Birth.CHILD_IDENTITY, Birth.FATHER_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinksPostFilter() {
        return getNumberOfGroundTruthTrueLinksPostFilterOn(Birth.CHILD_IDENTITY, Birth.FATHER_IDENTITY);
    }

    @Override
    public Iterable<LXP> getPreFilteredStoredRecords() {
        return filterBySex(
                super.getPreFilteredStoredRecords(),
                Birth.SEX, "m");
    }

}

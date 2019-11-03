package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import java.util.Arrays;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.List;
import java.util.Map;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

public class BirthDeathSiblingLinkageRecipe extends LinkageRecipe {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe linkageRecipe = new BirthDeathSiblingLinkageRecipe(sourceRepo, resultsRepo,
                linkageType + "-links");

        new BitBlasterLinkageRunner()
                .run(linkageRecipe, new JensenShannon(2048), 0.67, true, 5, false, false, true, false
                );
    }

    public static final String linkageType = "birth-death-sibling";

    public BirthDeathSiblingLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
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
        return linkageType;
    }

    @Override
    public Class getStoredType() {
        return Birth.class;
    }

    @Override
    public Class getSearchType() {
        return Death.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getSearchRole() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return Arrays.asList(
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME
        );
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        if(LinkageConfig.SIBLINGS_MAX_AGE_DIFF == null) return true;

        try {
            int yob1 = Integer.parseInt(proposedLink.record1.getString(Birth.BIRTH_YEAR));
            int approxYob2 = Integer.parseInt(proposedLink.record2.getString(Death.DEATH_YEAR)) -
                    Integer.parseInt(proposedLink.record2.getString(Death.AGE_AT_DEATH));

            return Math.abs(yob1 - approxYob2) <= LinkageConfig.SIBLINGS_MAX_AGE_DIFF;

        } catch(NumberFormatException e) { // in this case a BIRTH_YEAR or DEATH_YEAR is invalid
            return true;
        }
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return Arrays.asList(
                Death.FATHER_FORENAME,
                Death.FATHER_SURNAME,
                Death.MOTHER_FORENAME,
                Death.MOTHER_MAIDEN_SURNAME
        );
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOnSiblingNonSymmetric(Birth.FATHER_IDENTITY, Birth.FATHER_IDENTITY, Death.FATHER_IDENTITY, Death.MOTHER_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthLinksOnSiblingNonSymmetric(Birth.FATHER_IDENTITY, Birth.FATHER_IDENTITY, Death.FATHER_IDENTITY, Death.MOTHER_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinksPostFilter() {
        return getNumberOfGroundTruthLinksPostFilterOnSiblingNonSymmetric(Birth.FATHER_IDENTITY, Birth.FATHER_IDENTITY, Death.FATHER_IDENTITY, Death.MOTHER_IDENTITY);
    }

}

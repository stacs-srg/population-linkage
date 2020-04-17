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

import java.util.List;
import java.util.Map;

public class BirthSiblingLinkageRecipe extends LinkageRecipe {

    public static final String LINKAGE_TYPE = "birth-birth-sibling";

    public static final List<Integer> LINKAGE_FIELDS = list(
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.PARENTS_PLACE_OF_MARRIAGE,
            Birth.PARENTS_DAY_OF_MARRIAGE,
            Birth.PARENTS_MONTH_OF_MARRIAGE,
            Birth.PARENTS_YEAR_OF_MARRIAGE
    );

    /**
     * Various possible relevant sources of ground truth for siblings:
     * * identities of parents
     * * identities of parents' marriage record
     * * identities of parents' birth records
     */
    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.MOTHER_IDENTITY, Birth.MOTHER_IDENTITY), pair(Birth.FATHER_IDENTITY, Birth.FATHER_IDENTITY)),
            list(pair(Birth.PARENT_MARRIAGE_RECORD_IDENTITY, Birth.PARENT_MARRIAGE_RECORD_IDENTITY)),
            list(pair(Birth.MOTHER_BIRTH_RECORD_IDENTITY, Birth.MOTHER_BIRTH_RECORD_IDENTITY), pair(Birth.FATHER_BIRTH_RECORD_IDENTITY, Birth.FATHER_BIRTH_RECORD_IDENTITY))
    );

    public BirthSiblingLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
    }

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe linkageRecipe = new BirthSiblingLinkageRecipe(sourceRepo, resultsRepo, LINKAGE_TYPE + "-links");

        new BitBlasterLinkageRunner().run(linkageRecipe, new JensenShannon(2048), 0.67, true, 5, false, false, true, false);
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

    public static LinkStatus trueMatch(LXP record1, LXP record2) {
         return trueMatch(record1, record2, TRUE_MATCH_ALTERNATIVES);
    }

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public Class<? extends LXP> getStoredType() {
        return Birth.class;
    }

    @Override
    public Class<? extends LXP> getSearchType() {
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
        return LINKAGE_FIELDS;
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return isViable(proposedLink);
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
        throw new RuntimeException("ground truth implementation not consistent with trueMatch()");
//        return getGroundTruthLinksOnSiblingSymmetric(Birth.FATHER_IDENTITY, Birth.MOTHER_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        throw new RuntimeException("ground truth implementation not consistent with trueMatch()");
//        return getNumberOfGroundTruthLinksOnSiblingSymmetric(Birth.FATHER_IDENTITY, Birth.MOTHER_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinksPostFilter() {
        throw new RuntimeException("ground truth implementation not consistent with trueMatch()");
//        return getNumberOfGroundTruthLinksPostFilterOnSiblingSymmetric(Birth.FATHER_IDENTITY, Birth.MOTHER_IDENTITY);
    }
}

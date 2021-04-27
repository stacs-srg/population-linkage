/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * EvidencePair Recipe
 * In all linkage recipies the naming convention is:
 *     the stored type is the first part of the name
 *     the query type is the second part of the name
 * So for example in BirthBrideIdentityLinkageRecipe the stored type (stored in the search structure) is a birth and Marriages are used to query.
 * In all recipes if the query and the stored types are not the same the query type is converted to a stored type using getQueryMappingFields() before querying.
 *
 */
public class FatherGroomIdentityLinkageRecipe extends LinkageRecipe {

    private static final int REQUIRED_FIELDS = 8;
    private static final double DISTANCE_THESHOLD = 0;

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe linkageRecipe = new FatherGroomIdentityLinkageRecipe(sourceRepo, resultsRepo, LINKAGE_TYPE + "-links");

        new BitBlasterLinkageRunner()
                .run(linkageRecipe, new JensenShannon(2048), false, false, true, false
        );
    }

    public static final String LINKAGE_TYPE = "father-groom-identity";

    public FatherGroomIdentityLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        final String father_id = record1.getString(Birth.FATHER_IDENTITY);
        final String groom_id = record2.getString(Marriage.GROOM_IDENTITY);

        if (father_id.isEmpty() || groom_id.isEmpty() ) {
            return LinkStatus.UNKNOWN;
        } else if (father_id.equals( groom_id ) ) {
            return LinkStatus.TRUE_MATCH;
        } else {
            return LinkStatus.NOT_TRUE_MATCH;
        }
    }

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public Class getStoredType() {
        return Birth.class;
    }

    @Override
    public Class<? extends LXP> getQueryType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_FATHER;
    }

    @Override
    public String getQueryRole() {
        return Marriage.ROLE_GROOM;
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
        return true;
    }

    @Override
    public List<Integer> getQueryMappingFields() {
        return Arrays.asList(
                Marriage.GROOM_FORENAME,
                Marriage.GROOM_SURNAME,
                Marriage.BRIDE_FORENAME,
                Marriage.BRIDE_SURNAME,
                Marriage.PLACE_OF_MARRIAGE,
                Marriage.MARRIAGE_DAY,
                Marriage.MARRIAGE_MONTH,
                Marriage.MARRIAGE_YEAR
        );
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOn(Birth.FATHER_IDENTITY, Marriage.GROOM_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthTrueLinksOn(Birth.FATHER_IDENTITY, Marriage.GROOM_IDENTITY);
    }

    @Override
    public double getTheshold() {
        System.exit(1);
        System.out.println( "THESHOLD set to zero - fix me"); // TODO 666
        return DISTANCE_THESHOLD;
    }
}

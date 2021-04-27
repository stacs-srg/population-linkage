/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

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
public class BrideBrideSiblingLinkageRecipe extends LinkageRecipe {

    public static final String LINKAGE_TYPE = "bride-bride-sibling";
    private static final double DISTANCE_THESHOLD = 0.15;

    public static List<Integer> COMPARISON_FIELDS = Arrays.asList(

            Marriage.BRIDE_FATHER_FORENAME,
            Marriage.BRIDE_FATHER_SURNAME,
            Marriage.BRIDE_MOTHER_FORENAME,
            Marriage.BRIDE_MOTHER_MAIDEN_SURNAME
    );

    public static final int ID_FIELD_INDEX = Marriage.STANDARDISED_ID;

    /**
     * Various possible relevant sources of ground truth for siblings:
     * * identities of parents
     * * identities of parents' birth records
     */
    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
                    list(pair(Marriage.BRIDE_MOTHER_IDENTITY, Marriage.BRIDE_MOTHER_IDENTITY), pair(Marriage.BRIDE_FATHER_IDENTITY, Marriage.BRIDE_FATHER_IDENTITY)),
                    list(pair(Marriage.BRIDE_MOTHER_BIRTH_RECORD_IDENTITY, Marriage.BRIDE_MOTHER_BIRTH_RECORD_IDENTITY), pair(Marriage.BRIDE_FATHER_BIRTH_RECORD_IDENTITY, Marriage.BRIDE_FATHER_BIRTH_RECORD_IDENTITY))
            );

    public BrideBrideSiblingLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {

        return trueMatch(record1, record2);
    }

    public static LinkStatus trueMatch(LXP record1, LXP record2) {

        final String m1_bride_id = record1.getString(Marriage.BRIDE_IDENTITY);
        final String m2_bride_id = record2.getString(Marriage.BRIDE_IDENTITY);

        // Exclude matches for multiple marriages of the same bride.
        if (m1_bride_id.equals(m2_bride_id))  return LinkStatus.NOT_TRUE_MATCH;

        return trueMatch(record1, record2, TRUE_MATCH_ALTERNATIVES);
    }

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public Class<? extends LXP> getStoredType() {
        return Marriage.class;
    }

    @Override
    public Class<? extends LXP> getQueryType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Marriage.ROLE_BRIDE;
    }

    @Override
    public String getQueryRole() {
        return Marriage.ROLE_BRIDE;
    }

    @Override
    public List<Integer> getLinkageFields() {

        return getComparisonFields();
    }

    public static List<Integer> getComparisonFields() {
        return COMPARISON_FIELDS;
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return isViable( proposedLink );
    }

    public static boolean isViable(RecordPair proposedLink) {

        if( proposedLink.record1.getString(Marriage.STANDARDISED_ID).equals(proposedLink.record2.getString(Marriage.STANDARDISED_ID ))) { // avoid self links. TODO maybe do this elsewhere?
            return false;
        }

        if (LinkageConfig.MAX_SIBLING_AGE_DIFF == null) return true;

        try {
            int year_of_birth1 = getBirthYearOfPersonBeingMarried(proposedLink.record1, true);
            int year_of_birth2 = getBirthYearOfPersonBeingMarried(proposedLink.record2, true);

            return Math.abs(year_of_birth1 - year_of_birth2) <= LinkageConfig.MAX_SIBLING_AGE_DIFF;

        } catch(NumberFormatException e) { 
            return true;
        }
    }

    @Override
    public List<Integer> getQueryMappingFields() {
        return getLinkageFields();
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOnSiblingSymmetric(Marriage.BRIDE_FATHER_IDENTITY, Marriage.BRIDE_MOTHER_IDENTITY);
    }

    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthLinksOnSiblingSymmetric(Marriage.BRIDE_FATHER_IDENTITY, Marriage.BRIDE_MOTHER_IDENTITY);
    }

    @Override
    public double getTheshold() {
        return DISTANCE_THESHOLD;
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

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
public class BrideBrideIdentityLinkageRecipe extends LinkageRecipe {

    private static final double DISTANCE_THRESHOLD = 0.49;

    public static final String LINKAGE_TYPE = "bride-bride-identity";
    public static final int ID_FIELD_INDEX1 = Marriage.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Marriage.STANDARDISED_ID;

    public BrideBrideIdentityLinkageRecipe(String source_repository_name, String links_persistent_name) {
        super(source_repository_name, links_persistent_name);
    }

    public static final List<Integer> LINKAGE_FIELDS = list(
            Marriage.BRIDE_FORENAME,
            Marriage.BRIDE_SURNAME,
            Marriage.BRIDE_MOTHER_FORENAME,
            Marriage.BRIDE_MOTHER_MAIDEN_SURNAME,
            Marriage.BRIDE_FATHER_FORENAME,
            Marriage.BRIDE_FATHER_SURNAME,
            Marriage.BRIDE_FATHER_OCCUPATION,
            Marriage.BRIDE_OCCUPATION
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Marriage.BRIDE_FORENAME,
            Marriage.BRIDE_SURNAME,
            Marriage.BRIDE_MOTHER_FORENAME,
            Marriage.BRIDE_MOTHER_MAIDEN_SURNAME,
            Marriage.BRIDE_FATHER_FORENAME,
            Marriage.BRIDE_FATHER_SURNAME,
            Marriage.BRIDE_FATHER_OCCUPATION,
            Marriage.BRIDE_OCCUPATION
    );

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Marriage.BRIDE_IDENTITY, Marriage.BRIDE_IDENTITY))
    );

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2);
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
        return LINKAGE_FIELDS;
    }

    @Override
    public double getThreshold() {
        return DISTANCE_THRESHOLD;
    }

    @Override
    public List<Integer> getQueryMappingFields() {
        return SEARCH_FIELDS;
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return isViable( proposedLink );
    }

    private boolean isViable(RecordPair proposedLink) {

        if (LinkageConfig.MAX_SIBLING_AGE_DIFF == null) return true;

//        try {
//            int bride_age_or_dob1 = Integer.parseInt(proposedLink.record1.getString(Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH));
//            int bride_age_or_dob2 = Integer.parseInt(proposedLink.record2.getString(Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH));
//            // in Umea the BRIDE_AGE_OR_DATE_OF_BIRTH all seem to be --/--/----
//            IF YOU UNCOMMENT THIS CODE IS UNFINISHED!!!! LINE BELOW WILL NOT WORK!
//            return ...
//        } catch (NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
//            return true;
//        }
        // Although above doesn't work can still check inter marriage range
        try {
            int yom1 = Integer.parseInt(proposedLink.record1.getString(Marriage.MARRIAGE_YEAR));
            int yom2 = Integer.parseInt(proposedLink.record2.getString(Marriage.MARRIAGE_YEAR));
            return Math.abs(yom1 - yom2) <= LinkageConfig.MAX_INTER_MARRIAGE_DIFFERENCE;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksAsymmetric();
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthLinksAsymmetric();
    }

}
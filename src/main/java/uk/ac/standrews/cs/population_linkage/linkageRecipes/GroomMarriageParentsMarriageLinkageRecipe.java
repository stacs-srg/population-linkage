/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
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

public class GroomMarriageParentsMarriageLinkageRecipe extends LinkageRecipe {

    private static final double DISTANCE_THESHOLD = 0.6;

    public static final List<Integer> LINKAGE_FIELDS = list(
            Marriage.GROOM_FATHER_FORENAME,
            Marriage.GROOM_FATHER_SURNAME,
            Marriage.GROOM_MOTHER_FORENAME,
            Marriage.GROOM_MOTHER_MAIDEN_SURNAME,
            Marriage.GROOM_FATHER_OCCUPATION
    );

    public static final List<Integer> SEARCH_FIELDS= list(
            Marriage.GROOM_FORENAME,
            Marriage.GROOM_SURNAME,
            Marriage.BRIDE_FORENAME,
            Marriage.BRIDE_SURNAME,
            Marriage.GROOM_OCCUPATION
    );


    public static final int ID_FIELD_INDEX1 = Marriage.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Marriage.STANDARDISED_ID;

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Marriage.GROOM_FATHER_IDENTITY, Marriage.GROOM_IDENTITY )),
            list(pair(Marriage.GROOM_MOTHER_IDENTITY, Marriage.BRIDE_IDENTITY ))
    );

    public static final String LINKAGE_TYPE = "groom-parents-marriage-identity";

    public GroomMarriageParentsMarriageLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
    }

    @Override
    public LinkStatus isTrueMatch(LXP birth, LXP marriage) {
        return trueMatch(birth, marriage);
    }

    public static LinkStatus trueMatch(LXP birth, LXP marriage) {
        return trueMatch(birth, marriage, TRUE_MATCH_ALTERNATIVES);
    }

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public Class getStoredType() {
        return Marriage.class;
    }

    @Override
    public Class<? extends LXP> getQueryType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Marriage.ROLE_GROOM;
    }

    @Override
    public String getQueryRole() {
        return Marriage.ROLE_PARENTS;  // bride and groom
    }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) { return true; } // TODO VIABLE

    @Override
    public List<Integer> getQueryMappingFields() { return SEARCH_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOn(Marriage.GROOM_FATHER_IDENTITY, Marriage.GROOM_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthTrueLinksOn(Marriage.GROOM_FATHER_IDENTITY, Marriage.GROOM_IDENTITY);
    }

    @Override
    public double getTheshold() {
        return DISTANCE_THESHOLD;
    }
}

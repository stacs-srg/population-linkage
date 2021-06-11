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
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;

import java.util.List;
import java.util.Map;

public class BirthDeathSiblingLinkageRecipe extends LinkageRecipe {

    public static final String LINKAGE_TYPE = "birth-death-sibling";

    private static final double DISTANCE_THESHOLD = 0; // TODO 8888

    public static final List<Integer> LINKAGE_FIELDS = list(
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Death.FATHER_FORENAME,
            Death.FATHER_SURNAME,
            Death.MOTHER_FORENAME,
            Death.MOTHER_MAIDEN_SURNAME
    );

    public static final int ID_FIELD_INDEX1 = Birth.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Death.STANDARDISED_ID;


    public BirthDeathSiblingLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
    }

    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(   pair(Birth.FATHER_IDENTITY, Death.FATHER_IDENTITY),
                    pair(Birth.MOTHER_IDENTITY, Death.MOTHER_IDENTITY) ) );

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
    public Class getStoredType() {
        return Birth.class;
    }

    @Override
    public Class<? extends LXP> getQueryType() {
        return Death.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getQueryRole() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public List<Integer> getQueryMappingFields() { return SEARCH_FIELDS; }

    @Override
    public List<Integer> getLinkageFields() { return LINKAGE_FIELDS; }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {

        if (LinkageConfig.MAX_SIBLING_AGE_DIFF == null) return true;

        try {
            int year_of_birth1 = Integer.parseInt(proposedLink.record1.getString(Birth.BIRTH_YEAR));
            int year_of_birth2 = Integer.parseInt(proposedLink.record2.getString(Death.DEATH_YEAR)) - Integer.parseInt(proposedLink.record2.getString(Death.AGE_AT_DEATH));

            return Math.abs(year_of_birth1 - year_of_birth2) <= LinkageConfig.MAX_SIBLING_AGE_DIFF;

        } catch (NumberFormatException e) { // in this case a BIRTH_YEAR or DEATH_YEAR is invalid
            return true;
        }
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
    public double getTheshold() {
        return DISTANCE_THESHOLD;
    }
}

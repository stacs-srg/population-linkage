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
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.util.List;
import java.util.Map;

public class BirthBrideSiblingLinkageRecipe extends LinkageRecipe {

    public static final String LINKAGE_TYPE = "birth-bride-sibling";

    private static final double DISTANCE_THESHOLD = 0.5; // TODO THIS THRESHOLD WAS NOT MEASURED - 0.15 in table

    public static final List<Integer> LINKAGE_FIELDS = list(
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.FATHER_OCCUPATION
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Marriage.BRIDE_FATHER_FORENAME,
            Marriage.BRIDE_FATHER_SURNAME,
            Marriage.BRIDE_MOTHER_FORENAME,
            Marriage.BRIDE_MOTHER_MAIDEN_SURNAME,
            Marriage.BRIDE_FATHER_OCCUPATION
    );

    public static final int ID_FIELD_INDEX1 = Birth.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Marriage.STANDARDISED_ID;


    public BirthBrideSiblingLinkageRecipe(String source_repository_name, String links_persistent_name) {
        super(source_repository_name, links_persistent_name);
    }

    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(   pair(Birth.FATHER_IDENTITY, Marriage.BRIDE_FATHER_IDENTITY),
                    pair(Birth.MOTHER_IDENTITY, Marriage.BRIDE_MOTHER_IDENTITY) ) );

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
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getQueryRole() {
        return Marriage.ROLE_BRIDE;
    }

    @Override
    public List<Integer> getQueryMappingFields() { return SEARCH_FIELDS; }

    @Override
    public List<Integer> getLinkageFields() { return LINKAGE_FIELDS; }

    private boolean isViable(RecordPair proposedLink) {

        if (LinkageConfig.MAX_SIBLING_AGE_DIFF == null) return true;

//        try {
//            int year_of_birth1 = Integer.parseInt(proposedLink.record1.getString(Birth.BIRTH_YEAR));
//            int bride_age_or_dob = Integer.parseInt(proposedLink.record2.getString(Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH));
//            // in Umea the BRIDE_AGE_OR_DATE_OF_BIRTH all seem to be --/--/----
//            IF YOU UNCOMMENT THIS CODE IS UNFINISHED!!!! LINE BELOW WILL NOT WORK!
//            return Math.abs(year_of_birth1 - bride_yob) <= LinkageConfig.MAX_SIBLING_AGE_DIFF;
//        } catch (NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
//            return true;
//        }
        // Although above doesn't work can still check yom > yob (crude)
        try {
            int year_of_birth = Integer.parseInt(proposedLink.record1.getString(Birth.BIRTH_YEAR));
            int year_of_marriage = Integer.parseInt(proposedLink.record2.getString(Marriage.MARRIAGE_YEAR));
            return year_of_birth < year_of_marriage;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) { return isViable(proposedLink); }



    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksAsymmetric();
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthLinksAsymmetric();
    }

    @Override
    public double getThreshold() {
        return DISTANCE_THESHOLD;
    }
}
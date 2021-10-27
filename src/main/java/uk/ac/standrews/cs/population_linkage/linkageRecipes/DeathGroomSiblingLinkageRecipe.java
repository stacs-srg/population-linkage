/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.util.List;
import java.util.Map;

/**
 * Links a person appearing as the deceased on a death record with a sibling appearing as the groom on a marriage record.
 */
public class DeathGroomSiblingLinkageRecipe extends LinkageRecipe {

    private static final double DISTANCE_THRESHOLD = 0.5; // used values from UmeaGroomBirthViabilityPRFByThreshold.csv

    public static final String LINKAGE_TYPE = "death-groom-sibling";

    public static final int ID_FIELD_INDEX1 = Death.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Marriage.STANDARDISED_ID;

    public static final List<Integer> LINKAGE_FIELDS = list(
            Death.MOTHER_FORENAME,
            Death.MOTHER_MAIDEN_SURNAME,
            Death.FATHER_FORENAME,
            Death.FATHER_SURNAME,
            Death.FATHER_OCCUPATION
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Marriage.GROOM_MOTHER_FORENAME,
            Marriage.GROOM_MOTHER_MAIDEN_SURNAME,
            Marriage.GROOM_FATHER_FORENAME,
            Marriage.GROOM_FATHER_SURNAME,
            Marriage.GROOM_FATHER_OCCUPATION
    );

    public DeathGroomSiblingLinkageRecipe(String source_repository_name, String links_persistent_name) {
        super(source_repository_name, links_persistent_name);
    }

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(
                    pair(Death.MOTHER_IDENTITY, Marriage.GROOM_MOTHER_IDENTITY),
                    pair(Death.FATHER_IDENTITY, Marriage.GROOM_FATHER_IDENTITY) ) );

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
        return Death.class;
    }

    @Override
    public Class<? extends LXP> getQueryType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public String getQueryRole() {
        return Marriage.ROLE_GROOM;
    }

    @Override
    public List<Integer> getQueryMappingFields() { return SEARCH_FIELDS; }

    @Override
    public List<Integer> getLinkageFields() { return LINKAGE_FIELDS; }

    private boolean isViable(RecordPair proposedLink) {

        return true; // see *** below

//        if (LinkageConfig.MAX_SIBLING_AGE_DIFF == null) return true;
//
////        try {
////            int death = Integer.parseInt(proposedLink.record1.getString(Death.DEATH_YEAR));
////            int groom_age_or_dob = Integer.parseInt(proposedLink.record2.getString(Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH));
////            // in Umea the GROOM_AGE_OR_DATE_OF_BIRTH all seem to be --/--/----
////            IF YOU UNCOMMENT THIS CODE IS UNFINISHED!!!! LINE BELOW WILL NOT WORK!
////            return ...
////        } catch (NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
////            return true;
////        }
//        // Although above doesn't work can still check yom > yob (crude)
//        try {
//            int year_of_death = Integer.parseInt(proposedLink.record1.getString(Death.DEATH_YEAR));
//            int year_of_marriage = Integer.parseInt(proposedLink.record2.getString(Marriage.MARRIAGE_YEAR));
//            return year_of_marriage < year_of_death;
//            // This is very conservative and may field false negatives.
//            // *** Siblings could be married after the death of another sibling
//
//        } catch (NumberFormatException e) {
//            return true;
//        }
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
        return DISTANCE_THRESHOLD;
    }
}

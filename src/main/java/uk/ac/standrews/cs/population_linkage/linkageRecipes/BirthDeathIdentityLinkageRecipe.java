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
import uk.ac.standrews.cs.population_records.Normalisation;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;

import java.util.List;
import java.util.Map;

/**
 * Links a person appearing as the child on a birth record with the same person appearing as the deceased on a death record.
 */
public class BirthDeathIdentityLinkageRecipe extends LinkageRecipe {

    private static final double THRESHOLD = 0.38;  // from earlier experiments

    public static final String LINKAGE_TYPE = "birth-death-identity";

    public static final int ID_FIELD_INDEX1 = Birth.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Death.STANDARDISED_ID;

    // TODO Why not father/mother occupation? - even longer duration

    public static final List<Integer> LINKAGE_FIELDS = list(
            Birth.FORENAME,
            Birth.SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Death.FORENAME,
            Death.SURNAME,
            Death.MOTHER_FORENAME,
            Death.MOTHER_MAIDEN_SURNAME,
            Death.FATHER_FORENAME,
            Death.FATHER_SURNAME
    );

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.CHILD_IDENTITY, Death.DECEASED_IDENTITY)),
            list(pair(Birth.STANDARDISED_ID, Death.BIRTH_RECORD_IDENTITY)),
            list(pair(Birth.DEATH_RECORD_IDENTITY, Death.STANDARDISED_ID))
    );

    public BirthDeathIdentityLinkageRecipe(String source_repository_name, String links_persistent_name) {
        super(source_repository_name, links_persistent_name);
    }

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
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return isViable( proposedLink );
    }

    /**
     * Checks whether the birth occurs before the death, that the age at death is plausible, and that the ages at death
     * derived in three different ways are consistent (recorded on death record; difference between birth year on birth
     * record and death year; difference between birth year on death record and death year).
     *
     * @param proposedLink the proposed link
     * @return true if the link is viable
     */
    public static boolean isViable(final RecordPair proposedLink) {

        // TODO run separate profiling to check for internal consistency with death records - all fields populated in Umea.

        try {
            final LXP birth_record = proposedLink.record1;
            final LXP death_record = proposedLink.record2;

            final int year_of_birth_from_birth_record = Integer.parseInt(birth_record.getString(Birth.BIRTH_YEAR));
            final int year_of_birth_from_death_record = Integer.parseInt(Normalisation.extractYear(death_record.getString(Death.DATE_OF_BIRTH)));
            final int year_of_death_from_death_record = Integer.parseInt(death_record.getString(Death.DEATH_YEAR));

            final int age_at_death_recorded_on_death_record = Integer.parseInt(death_record.getString(Death.AGE_AT_DEATH));

            final int age_at_death_calculated_from_both_records = year_of_death_from_death_record - year_of_birth_from_birth_record;
            final int age_at_death_calculated_from_death_record = year_of_death_from_death_record - year_of_birth_from_death_record;

            final int age_at_death_discrepancy_1 = Math.abs(age_at_death_recorded_on_death_record - age_at_death_calculated_from_both_records);
            final int age_at_death_discrepancy_2 = Math.abs(age_at_death_recorded_on_death_record - age_at_death_calculated_from_death_record);

            return  age_at_death_calculated_from_both_records >= 0 &&
                    age_at_death_calculated_from_both_records <= LinkageConfig.MAX_AGE_AT_DEATH &&
                    age_at_death_discrepancy_1 <= LinkageConfig.MAX_ALLOWABLE_AGE_DISCREPANCY &&
                    age_at_death_discrepancy_2 <= LinkageConfig.MAX_ALLOWABLE_AGE_DISCREPANCY;

        } catch (NumberFormatException e) { // Invalid year.
            return true;
        }
    }

    @Override
    public List<Integer> getQueryMappingFields() { return SEARCH_FIELDS; }

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
        return THRESHOLD;
    }
}

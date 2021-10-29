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
 * Links a person appearing as the child on a birth record with a sibling appearing as the deceased on a death record.
 */
public class BirthDeathSiblingLinkageRecipe extends LinkageRecipe {

    // TODO Do we need to do something to avoid self-links (linker & ground truth)?

    private static final double DISTANCE_THESHOLD = 0.36;

    public static final String LINKAGE_TYPE = "birth-death-sibling";

    public static final int ID_FIELD_INDEX1 = Birth.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Death.STANDARDISED_ID;

    // TODO Why not father/mother occupation?

    public static final List<Integer> LINKAGE_FIELDS = list(
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Death.MOTHER_FORENAME,
            Death.MOTHER_MAIDEN_SURNAME,
            Death.FATHER_FORENAME,
            Death.FATHER_SURNAME
    );

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.MOTHER_IDENTITY, Death.MOTHER_IDENTITY),
                    pair(Birth.FATHER_IDENTITY, Death.FATHER_IDENTITY)));

    public BirthDeathSiblingLinkageRecipe(String source_repository_name, String links_persistent_name) {
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
    public List<Integer> getQueryMappingFields() {
        return SEARCH_FIELDS;
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
     * Checks whether the age difference between the potential siblings is plausible, and that the age at death
     * recorded on the death record is consistent with the difference between birth year on death record and death year.
     *
     * @param proposedLink the proposed link
     * @return true if the link is viable
     */
    public static boolean isViable(RecordPair proposedLink) {

        try {
            final LXP birth_record = proposedLink.record1;
            final LXP death_record = proposedLink.record2;

            final int year_of_birth_from_birth_record = Integer.parseInt(birth_record.getString(Birth.BIRTH_YEAR));
            final int year_of_birth_from_death_record = Integer.parseInt(Normalisation.extractYear(death_record.getString(Death.DATE_OF_BIRTH)));
            final int year_of_death_from_death_record = Integer.parseInt(death_record.getString(Death.DEATH_YEAR));

            final int age_difference_between_siblings = Math.abs(year_of_birth_from_birth_record - year_of_birth_from_death_record);

            final int age_at_death_recorded_on_death_record = Integer.parseInt(death_record.getString(Death.AGE_AT_DEATH));
            final int age_at_death_calculated_from_death_record = year_of_death_from_death_record - year_of_birth_from_death_record;

            final int age_at_death_discrepancy = Math.abs(age_at_death_recorded_on_death_record - age_at_death_calculated_from_death_record);

            return  age_difference_between_siblings <= LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE &&
                    age_at_death_discrepancy <= LinkageConfig.MAX_ALLOWABLE_AGE_DISCREPANCY;

        } catch (NumberFormatException e) { // Invalid year.
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

    @Override
    public double getThreshold() {
        return DISTANCE_THESHOLD;
    }
}

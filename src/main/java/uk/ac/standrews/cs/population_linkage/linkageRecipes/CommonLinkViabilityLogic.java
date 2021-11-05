/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.Normalisation;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class CommonLinkViabilityLogic {

    // TODO factor out sibling age difference logic.

    protected static int getBirthYearOfPersonBeingMarried(final LXP record, final boolean spouse_is_bride) {

        final String age_or_birth_date = record.getString(spouse_is_bride ? Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH : Marriage.GROOM_AGE_OR_DATE_OF_BIRTH);

        try {
            final int age_at_marriage_recorded = Integer.parseInt(age_or_birth_date);
            final int marriage_year = Integer.parseInt(record.getString(Marriage.MARRIAGE_YEAR));

            return marriage_year - age_at_marriage_recorded;

        } catch (NumberFormatException e) {

            // Probably date of birth recorded rather than age.
            return Integer.parseInt(Normalisation.extractYear(age_or_birth_date));
        }
    }

    private static LocalDate getMarriageDateFromMarriageRecord(LXP record) {

        int marriage_day = Integer.parseInt(record.getString(Marriage.MARRIAGE_DAY));
        int marriage_month = Integer.parseInt(record.getString(Marriage.MARRIAGE_MONTH));
        int marriage_year = Integer.parseInt(record.getString(Marriage.MARRIAGE_YEAR));

        return LocalDate.of(marriage_year, marriage_month, marriage_day);
    }

    protected static LocalDate getBirthDateFromBirthRecord(LXP record) {

        int birth_day = Integer.parseInt(record.getString(Birth.BIRTH_DAY));
        int birth_month = Integer.parseInt(record.getString(Birth.BIRTH_MONTH));
        int birth_year = Integer.parseInt(record.getString(Birth.BIRTH_YEAR));

        return LocalDate.of(birth_year, birth_month, birth_day);
    }

    protected static LocalDate getBirthDateFromDeathRecord(LXP record) {

        try {
            return Normalisation.parseDate(record.getString(Death.DATE_OF_BIRTH));
        }
        catch (DateTimeParseException e) {

            // Try approximating date from year of death and age at death.
            int year_of_birth = Integer.parseInt(record.getString(Death.DEATH_YEAR)) - Integer.parseInt(record.getString(Death.AGE_AT_DEATH));
            return LocalDate.of(year_of_birth, 1, 1);
        }
    }

    /**
     * Checks the age at death
     * recorded on the death record is consistent with the difference between birth year on death record and death year.
     *
     * @param death_record the record
     * @return true if the record is consistent
     */
    public static boolean checkInternalConsistency(final LXP death_record) {

        try {
            final int year_of_birth_from_death_record = Integer.parseInt(Normalisation.extractYear(death_record.getString(Death.DATE_OF_BIRTH)));
            final int year_of_death_from_death_record = Integer.parseInt(death_record.getString(Death.DEATH_YEAR));

            final int age_at_death_recorded_on_death_record = Integer.parseInt(death_record.getString(Death.AGE_AT_DEATH));
            final int age_at_death_calculated_from_death_record = year_of_death_from_death_record - year_of_birth_from_death_record;

            final int age_at_death_discrepancy = Math.abs(age_at_death_recorded_on_death_record - age_at_death_calculated_from_death_record);

            return age_at_death_discrepancy <= LinkageConfig.MAX_ALLOWABLE_AGE_DISCREPANCY;

        } catch (NumberFormatException e) { // Invalid year.
            return true;
        }
    }

    public static boolean siblingBirthDatesAreViable(final LocalDate date_of_birth1, final LocalDate date_of_birth2) {

        final long days_between_sibling_births = Math.abs(ChronoUnit.DAYS.between(date_of_birth1, date_of_birth2));

        return (days_between_sibling_births <= LinkageConfig.MAX_TWIN_AGE_DIFFERENCE_IN_DAYS ||
                days_between_sibling_births >= LinkageConfig.MIN_SIBLING_AGE_DIFFERENCE_IN_DAYS) &&
                days_between_sibling_births <= LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE_IN_DAYS;
    }

    protected static boolean birthMarriageSiblingLinkIsViable(RecordPair proposedLink, boolean marriage_role_is_bride) {

        try {
            final LXP birth_record = proposedLink.record1;
            final LXP marriage_record = proposedLink.record2;

            int year_of_birth1 = Integer.parseInt(birth_record.getString(Birth.BIRTH_YEAR));
            int year_of_birth2 = CommonLinkViabilityLogic.getBirthYearOfPersonBeingMarried(marriage_record, marriage_role_is_bride);

            return Math.abs(year_of_birth1 - year_of_birth2) <= LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE;

        } catch (NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
            return true;
        }
    }

    protected static boolean birthMarriageIdentityLinkIsViable(final RecordPair proposedLink, final boolean marriage_role_is_bride) {

        // Returns true if age at marriage as calculated from the date of birth on the birth record and date of marriage on the marriage
        // record is within acceptable range, and the discrepancy between that age and the age recorded on, or calculated from, the
        // marriage record is acceptably low.

        try {
            final LXP birth_record = proposedLink.record1;
            final LXP marriage_record = proposedLink.record2;

            final LocalDate birth_date_from_birth_record = getBirthDateFromBirthRecord(birth_record);
            final int birth_year_from_marriage_record = getBirthYearOfPersonBeingMarried(marriage_record, marriage_role_is_bride);
            final LocalDate marriage_date_from_marriage_record = getMarriageDateFromMarriageRecord(marriage_record);

            final int age_at_marriage_calculated = birth_date_from_birth_record.until(marriage_date_from_marriage_record).getYears();
            final int age_at_marriage_recorded = marriage_date_from_marriage_record.getYear() - birth_year_from_marriage_record;

            final int age_discrepancy = Math.abs(age_at_marriage_calculated - age_at_marriage_recorded);

            return  age_at_marriage_calculated >= LinkageConfig.MIN_AGE_AT_MARRIAGE &&
                    age_at_marriage_calculated <= LinkageConfig.MAX_AGE_AT_DEATH &&
                    age_discrepancy <= LinkageConfig.MAX_ALLOWABLE_AGE_DISCREPANCY;

        } catch (NumberFormatException | DateTimeException e ) {
            // Invalid BIRTH_YEAR or MARRIAGE_YEAR or AGE_OR_DATE_OF_BIRTH, or unparseable date.
            return true;
        }
    }

    protected static boolean deathMarriageIdentityLinkIsViable(final RecordPair proposedLink, final boolean marriage_role_is_bride) {

        // Returns true if year of death is not before year of marriage, and year of birth inferred from death record is
        // consistent with year of birth inferred from marriage record.

        try {
            LXP death_record = proposedLink.record1;
            LXP marriage_record = proposedLink.record2;

            final int year_of_death = Integer.parseInt(death_record.getString(Death.DEATH_YEAR));
            final int year_of_marriage = Integer.parseInt(marriage_record.getString(Marriage.MARRIAGE_YEAR));

            final LocalDate birth_date_from_death_record = getBirthDateFromDeathRecord(death_record);
            final int birth_year_from_marriage_record = getBirthYearOfPersonBeingMarried(marriage_record, marriage_role_is_bride);

            final int birth_year_discrepancy = Math.abs(birth_date_from_death_record.getYear() - birth_year_from_marriage_record);

            return year_of_death >= year_of_marriage && birth_year_discrepancy <= LinkageConfig.MAX_ALLOWABLE_AGE_DISCREPANCY;

        } catch (NumberFormatException e) { // DEATH_YEAR or MARRIAGE_YEAR is invalid.
            return true;
        }
    }

    protected static boolean birthParentIdentityLinkIsViable(final RecordPair proposedLink, final boolean parent_role_is_mother) {

        // Returns true if difference in birth years is within acceptable range.

        try {
            final LXP birth_of_parent = proposedLink.record1;
            final LXP birth_of_child = proposedLink.record2;

            final int parent_year_of_birth = Integer.parseInt(birth_of_parent.getString(Birth.BIRTH_YEAR));
            final int child_year_of_birth = Integer.parseInt(birth_of_child.getString(Birth.BIRTH_YEAR));

            final int parent_age_at_birth_of_child = child_year_of_birth - parent_year_of_birth;

            return parent_age_at_birth_of_child >= LinkageConfig.MIN_PARENT_AGE_AT_BIRTH &&
                    parent_age_at_birth_of_child <= (parent_role_is_mother ? LinkageConfig.MAX_MOTHER_AGE_AT_BIRTH : LinkageConfig.MAX_FATHER_AGE_AT_BIRTH);

        } catch (NumberFormatException e) { // BIRTH_YEAR is invalid.
            return true;
        }
    }
}

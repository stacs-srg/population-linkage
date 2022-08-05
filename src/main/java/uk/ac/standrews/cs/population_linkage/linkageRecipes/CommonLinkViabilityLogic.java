/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_records.Normalisation;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class CommonLinkViabilityLogic {

    // Default month to be used for dates if only year is recorded.
    public static final int DEFAULT_MONTH = 7;

    public static LocalDate getBirthDateFromBirthRecord(final LXP record) {

        try {
            final int birth_day = Integer.parseInt(record.getString(Birth.BIRTH_DAY));
            final int birth_month = Integer.parseInt(record.getString(Birth.BIRTH_MONTH));
            final int birth_year = Integer.parseInt(record.getString(Birth.BIRTH_YEAR));

            return LocalDate.of(birth_year, birth_month, birth_day);

        } catch (NumberFormatException e) {

            // Try with just the year
            final int birth_year = Integer.parseInt(record.getString(Birth.BIRTH_YEAR));

            return LocalDate.of(birth_year, DEFAULT_MONTH, 1);
        }
    }

    /**
     * Returns the date of birth of one of the spouses on a marriage record.
     * If the precise date is not available due to only the age at marriage being recorded, the date is approximated
     * as 1st July of the calculated year.
     *
     * @param record          the record
     * @param spouse_is_bride true if the bride should be considered rather than the groom
     * @return the date of birth
     */
    public static LocalDate getBirthDateFromMarriageRecord(final LXP record, final boolean spouse_is_bride) {

        // Returns a year rather than a date because may only have the age at marriage rather than date of birth.

        final String age_or_birth_date = record.getString(spouse_is_bride ? Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH : Marriage.GROOM_AGE_OR_DATE_OF_BIRTH);

        try {
            // Assume it's a date.
            return Normalisation.parseDate(age_or_birth_date);

        } catch (DateTimeParseException e1) {

            // Try with just the year
            try {
                // Assume it's a partial date.
                final int birth_year = Integer.parseInt(Normalisation.extractYear(age_or_birth_date));

                return LocalDate.of(birth_year, DEFAULT_MONTH, 1);

            } catch (NumberFormatException e2) {

                // Assume it's an age.
                // Try approximating date from year of marriage and age at marriage.

                final int age_at_marriage_recorded = Integer.parseInt(age_or_birth_date);
                final int marriage_year = Integer.parseInt(record.getString(Marriage.MARRIAGE_YEAR));

                return LocalDate.of(marriage_year - age_at_marriage_recorded, DEFAULT_MONTH, 1);
            }
        }
    }

    public static LocalDate getMarriageDateFromMarriageRecord(final LXP record) {

        try {
            final int marriage_day = Integer.parseInt(record.getString(Marriage.MARRIAGE_DAY));
            final int marriage_month = Integer.parseInt(record.getString(Marriage.MARRIAGE_MONTH));
            final int marriage_year = Integer.parseInt(record.getString(Marriage.MARRIAGE_YEAR));

            return LocalDate.of(marriage_year, marriage_month, marriage_day);

        } catch (NumberFormatException e) {

            // Try with just the year
            final int marriage_year = Integer.parseInt(record.getString(Marriage.MARRIAGE_YEAR));

            return LocalDate.of(marriage_year, DEFAULT_MONTH, 1);
        }
    }

    /**
     * Returns the date of birth of the deceased on a death record.
     * If the precise date is not available due to only the age at death being recorded, the date is approximated
     * as 1st July of the calculated year.
     *
     * @param record the record
     * @return the date of birth
     */
    public static LocalDate getBirthDateFromDeathRecord(final LXP record) {

        final String birth_date = record.getString(Death.DATE_OF_BIRTH);

        try {
            return Normalisation.parseDate(birth_date);

        } catch (DateTimeParseException e1) {

            // Try with just the year
            try {
                final int birth_year = Integer.parseInt(Normalisation.extractYear(birth_date));

                return LocalDate.of(birth_year, DEFAULT_MONTH, 1);

            } catch (NumberFormatException e2) {

                // Try approximating date from year of death and age at death.

                final int year_of_birth = Integer.parseInt(record.getString(Death.DEATH_YEAR)) - Integer.parseInt(record.getString(Death.AGE_AT_DEATH));

                return LocalDate.of(year_of_birth, DEFAULT_MONTH, 1);
            }
        }
    }

    /**
     * Returns the date of death of the deceased on a death record.
     * If the precise date is not available due to only the age at death being recorded, the date is approximated
     * as 1st July of the calculated year.
     *
     * @param record the record
     * @return the date of birth
     */
    public static LocalDate getDeathDateFromDeathRecord(final LXP record) {

        try {
            final int death_day = Integer.parseInt(record.getString(Death.DEATH_DAY));
            final int death_month = Integer.parseInt(record.getString(Death.DEATH_MONTH));
            final int death_year = Integer.parseInt(record.getString(Death.DEATH_YEAR));

            return LocalDate.of(death_year, death_month, death_day);

        } catch (NumberFormatException e1) {

            // Try with just the year
            try {
                final int death_year = Integer.parseInt(record.getString(Death.DEATH_YEAR));

                return LocalDate.of(death_year, DEFAULT_MONTH, 1);

            } catch (NumberFormatException e2) {

                // Try with date of birth and age at death.
                final LocalDate date_of_birth = getBirthDateFromDeathRecord(record);
                final int age_at_death = Integer.parseInt(record.getString(Death.AGE_AT_DEATH));

                return date_of_birth.plusYears(age_at_death);
            }
        }
    }

    public static boolean siblingBirthDatesAreViable(final LocalDate date_of_birth1, final LocalDate date_of_birth2) {

        final long years_between_sibling_births = Math.abs(date_of_birth1.until(date_of_birth2, ChronoUnit.YEARS));

        return years_between_sibling_births <= LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE;
    }

    public static boolean alternativeIdentityBirthDatesAreViable(final LocalDate date_of_birth1, final LocalDate date_of_birth2) {

        final long years_between_alternative_birth_dates = Math.abs(date_of_birth1.until(date_of_birth2, ChronoUnit.YEARS));

        return years_between_alternative_birth_dates <= LinkageConfig.MAX_ALLOWABLE_AGE_DISCREPANCY;
    }

    public static boolean birthMarriageSiblingLinkIsViable(final LXP birth_record, final LXP marriage_record, final boolean marriage_role_is_bride) {

        try {
            final LocalDate date_of_birth1 = getBirthDateFromBirthRecord(birth_record);
            final LocalDate date_of_birth2 = getBirthDateFromMarriageRecord(marriage_record, marriage_role_is_bride);

            return siblingBirthDatesAreViable(date_of_birth1, date_of_birth2);

        } catch (NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
            return true;
        }
    }

    public static boolean deathMarriageSiblingLinkIsViable(final LXP death_record, final LXP marriage_record, final boolean marriage_role_is_bride) {

        try {
            final LocalDate date_of_birth1 = getBirthDateFromDeathRecord(death_record);
            final LocalDate date_of_birth2 = getBirthDateFromMarriageRecord(marriage_record, marriage_role_is_bride);

            return siblingBirthDatesAreViable(date_of_birth1, date_of_birth2);

        } catch (NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
            return true;
        }
    }

    public static boolean spouseMarriageParentsMarriageIdentityLinkIsViable(final LXP parents_marriage, final LXP spouse_marriage) {

        try {
            final LocalDate parents_date_of_marriage = getMarriageDateFromMarriageRecord(parents_marriage);
            final LocalDate spouse_date_of_marriage = getMarriageDateFromMarriageRecord(spouse_marriage);

            final long years_between_marriages = parents_date_of_marriage.until(spouse_date_of_marriage, ChronoUnit.YEARS);

            return years_between_marriages >= LinkageConfig.MIN_CHILD_PARENTS_MARRIAGE_DIFFERENCE &&
                    years_between_marriages <= LinkageConfig.MAX_CHILD_PARENTS_MARRIAGE_DIFFERENCE;

        } catch (NumberFormatException e) {
            return true;
        }
    }

    public static boolean birthMarriageIdentityLinkIsViable(final LXP birth_record, final LXP marriage_record, final boolean marriage_role_is_bride) {

        // Returns true if age at marriage as calculated from the date of birth on the birth record and date of marriage on the marriage
        // record is within acceptable range, and the discrepancy between that age and the age recorded on, or calculated from, the
        // marriage record is acceptably low.

        try {
            final LocalDate birth_date_from_birth_record = getBirthDateFromBirthRecord(birth_record);
            final LocalDate birth_date_from_marriage_record = getBirthDateFromMarriageRecord(marriage_record, marriage_role_is_bride);
            final LocalDate marriage_date_from_marriage_record = getMarriageDateFromMarriageRecord(marriage_record);

            final long age_at_marriage_calculated = birth_date_from_birth_record.until(marriage_date_from_marriage_record, ChronoUnit.YEARS);
            final long birth_date_discrepancy = Math.abs(birth_date_from_birth_record.until(birth_date_from_marriage_record, ChronoUnit.YEARS));

            return age_at_marriage_calculated >= LinkageConfig.MIN_AGE_AT_MARRIAGE &&
                    age_at_marriage_calculated <= LinkageConfig.MAX_AGE_AT_DEATH &&
                    birth_date_discrepancy <= LinkageConfig.MAX_ALLOWABLE_AGE_DISCREPANCY;

        } catch (NumberFormatException | DateTimeException e) {
            // Invalid BIRTH_YEAR or MARRIAGE_YEAR or AGE_OR_DATE_OF_BIRTH, or unparseable date.
            return true;
        }
    }

    public static boolean deathMarriageIdentityLinkIsViable(final LXP death_record, final LXP marriage_record, final boolean marriage_role_is_bride) {

        // Returns true if year of death is not before year of marriage, and year of birth inferred from death record is
        // consistent with year of birth inferred from marriage record.

        try {
            final LocalDate death_date = getDeathDateFromDeathRecord(death_record);
            final LocalDate marriage_date = getMarriageDateFromMarriageRecord(marriage_record);

            final LocalDate birth_date_from_death_record = getBirthDateFromDeathRecord(death_record);
            final LocalDate birth_date_from_marriage_record = getBirthDateFromMarriageRecord(marriage_record, marriage_role_is_bride);

            final long birth_year_discrepancy = Math.abs(birth_date_from_death_record.until(birth_date_from_marriage_record, ChronoUnit.YEARS));

            return !death_date.isBefore(marriage_date) && birth_year_discrepancy <= LinkageConfig.MAX_ALLOWABLE_AGE_DISCREPANCY;

        } catch (NumberFormatException e) { // DEATH_YEAR or MARRIAGE_YEAR is invalid.
            return true;
        }
    }

    public static boolean birthParentIdentityLinkIsViable(final LXP birth_of_parent , final LXP birth_of_child, final boolean parent_role_is_mother) {

        // Returns true if difference in birth years is within acceptable range.

        try {
            final LocalDate parent_birth_date = getBirthDateFromBirthRecord(birth_of_parent);
            final LocalDate child_birth_date = getBirthDateFromBirthRecord(birth_of_child);

            final long parent_age_at_birth_of_child = parent_birth_date.until(child_birth_date, ChronoUnit.YEARS);

            return parent_age_at_birth_of_child >= LinkageConfig.MIN_PARENT_AGE_AT_BIRTH &&
                    parent_age_at_birth_of_child <= (parent_role_is_mother ? LinkageConfig.MAX_MOTHER_AGE_AT_BIRTH : LinkageConfig.MAX_FATHER_AGE_AT_BIRTH);

        } catch (NumberFormatException e) { // BIRTH_YEAR is invalid.
            return true;
        }
    }
}

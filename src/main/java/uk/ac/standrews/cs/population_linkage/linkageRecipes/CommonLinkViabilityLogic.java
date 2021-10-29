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

public class CommonLinkViabilityLogic {

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

    private static LocalDate getBirthDateFromBirthRecord(LXP record) {

        int birth_day = Integer.parseInt(record.getString(Birth.BIRTH_DAY));
        int birth_month = Integer.parseInt(record.getString(Birth.BIRTH_MONTH));
        int birth_year = Integer.parseInt(record.getString(Birth.BIRTH_YEAR));

        return LocalDate.of(birth_year, birth_month, birth_day);
    }

    protected static boolean birthMarriageSiblingLinkIsViable(RecordPair proposedLink, boolean marriage_role_is_bride) {

        try {
            int year_of_birth1 = Integer.parseInt(proposedLink.record1.getString(Birth.BIRTH_YEAR));
            int year_of_birth2 = CommonLinkViabilityLogic.getBirthYearOfPersonBeingMarried(proposedLink.record2, marriage_role_is_bride);

            return Math.abs(year_of_birth1 - year_of_birth2) <= LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE;

        } catch (NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
            return true;
        }
    }

    protected static boolean marriageBirthIdentityLinkIsViable(final RecordPair proposedLink, final boolean marriage_role_is_bride) {

        // Returns true if age at marriage as calculated from the date of birth on the birth record and date of marriage on the marriage
        // record is within acceptable range, and the discrepancy between that age and the age recorded on, or calculated from, the
        // marriage record is acceptably low.

        try {
            final int birth_year_from_marriage_record = getBirthYearOfPersonBeingMarried(proposedLink.record1, marriage_role_is_bride);
            final LocalDate marriage_date_from_marriage_record = getMarriageDateFromMarriageRecord(proposedLink.record1);
            final LocalDate birth_date_from_birth_record = getBirthDateFromBirthRecord(proposedLink.record2);

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

    protected static boolean deathMarriageIdentityLinkIsViable(final RecordPair proposedLink) {

        // Returns true if year of death is after year of marriage.

        try {
            final int year_of_death = Integer.parseInt(proposedLink.record1.getString(Death.DEATH_YEAR));
            final int year_of_marriage = Integer.parseInt(proposedLink.record2.getString(Marriage.MARRIAGE_YEAR));

            return year_of_death >= year_of_marriage;

        } catch (NumberFormatException e) { // DEATH_YEAR or MARRIAGE_YEAR is invalid.
            return true;
        }
    }

    protected static boolean birthParentIdentityLinkIsViable(final RecordPair proposedLink) {

        // Returns true if difference in birth years is within acceptable range.

        try {
            final int parent_year_of_birth = Integer.parseInt(proposedLink.record1.getString(Birth.BIRTH_YEAR));
            final int child_year_of_birth = Integer.parseInt(proposedLink.record2.getString(Birth.BIRTH_YEAR));

            final int parent_age_at_child_birth = child_year_of_birth - parent_year_of_birth;

            return parent_age_at_child_birth >= LinkageConfig.MIN_PARENT_AGE_AT_BIRTH && parent_age_at_child_birth <= LinkageConfig.MAX_PARENT_AGE_AT_BIRTH;

        } catch (NumberFormatException e) { // BIRTH_YEAR is invalid.
            return true;
        }
    }
}

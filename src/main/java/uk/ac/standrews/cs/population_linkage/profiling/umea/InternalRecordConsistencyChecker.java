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
package uk.ac.standrews.cs.population_linkage.profiling.umea;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.datasets.Umea;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.CommonLinkViabilityLogic;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_records.Normalisation;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class InternalRecordConsistencyChecker {

    private static final List<String> MARRIAGE_FIELDS_CHECKED = Arrays.asList("bride birth date", "groom birth date", "marriage year");
    private static final List<String> DEATH_FIELDS_CHECKED = Arrays.asList("birth date", "death date", "death age");

    private final RecordRepository record_repository;
    private final Results marriage_results;
    private final Results death_results;

    private final boolean verbose;
    private int marriage_discrepancy_count = 0;
    private int death_discrepancy_count = 0;

    public static void main(String[] args) throws IOException {

        InternalRecordConsistencyChecker checker = new InternalRecordConsistencyChecker(false);

        checker.checkMarriageRecordsConsistency();
        checker.checkDeathRecordsConsistency();
    }

    public InternalRecordConsistencyChecker(boolean verbose) {

        this.verbose = verbose;
        record_repository = new RecordRepository(Umea.REPOSITORY_NAME);
        marriage_results = new Results(MARRIAGE_FIELDS_CHECKED);
        death_results = new Results(DEATH_FIELDS_CHECKED);
    }

    private void checkMarriageRecordsConsistency() {

        for (LXP marriage_record : record_repository.getMarriages()) {
            checkMarriageRecordInternalConsistency(marriage_record);
        }

        summariseMarriagesResults();
    }

    private void checkDeathRecordsConsistency() {

        for (LXP death_record : record_repository.getDeaths()) {
            checkDeathRecordInternalConsistency(death_record);
        }

        summariseDeathResults();
    }

    private void summariseMarriagesResults() {

        System.out.println();
        marriage_results.summariseFieldPresenceCounts();

        System.out.println();
        System.out.println("discrepancy count: " + marriage_discrepancy_count);
    }

    private void summariseDeathResults() {

        System.out.println();
        death_results.summariseFieldPresenceCounts();

        System.out.println();
        System.out.println("discrepancy count: " + death_discrepancy_count);
    }

    /**
     * Checks the ages of bride and groom are plausible, either as recorded or as calculated from birth
     * date and marriage date.
     *
     * @param marriage_record the record
     * @return true if the record is consistent
     */
    public void checkMarriageRecordInternalConsistency(final LXP marriage_record) {

        final String bride_age_or_birth_date = marriage_record.getString(Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH);
        final String groom_age_or_birth_date = marriage_record.getString(Marriage.GROOM_AGE_OR_DATE_OF_BIRTH);
        final String marriage_year = marriage_record.getString(Marriage.MARRIAGE_YEAR);

        marriage_results.recordUsableFields(
                usableDate(bride_age_or_birth_date) || usableNumber(bride_age_or_birth_date),
                usableDate(groom_age_or_birth_date) || usableNumber(groom_age_or_birth_date),
                usableNumber(marriage_year));

        boolean discrepancy = false;

        if (usableNumber(bride_age_or_birth_date)) {

            final int bride_age_at_marriage = Integer.parseInt(bride_age_or_birth_date);

            if (bride_age_at_marriage < LinkageConfig.MIN_AGE_AT_MARRIAGE || bride_age_at_marriage > LinkageConfig.MAX_AGE_AT_DEATH) {

                discrepancy = true;
                if (verbose) {
                    System.out.println();
                    System.out.println("ID: " + marriage_record.getString(Marriage.STANDARDISED_ID));
                    System.out.println("bride_age_at_marriage: " + bride_age_at_marriage);
                }
            }
        }
        else {
            if (usableDate(bride_age_or_birth_date) && usableNumber(marriage_year)) {
                int bride_birth_year = CommonLinkViabilityLogic.getBirthDateFromMarriageRecord(marriage_record, true).getYear();

                final int bride_age_at_marriage = Integer.parseInt(marriage_year) - bride_birth_year;

                if (bride_age_at_marriage < LinkageConfig.MIN_AGE_AT_MARRIAGE || bride_age_at_marriage > LinkageConfig.MAX_AGE_AT_DEATH) {

                    discrepancy = true;
                    if (verbose) {
                        System.out.println();
                        System.out.println("ID: " + marriage_record.getString(Marriage.STANDARDISED_ID));
                        System.out.println("bride_birth_year: " + bride_birth_year);
                        System.out.println("marriage_year: " + marriage_year);
                    }
                }
            }
        }

        if (usableNumber(groom_age_or_birth_date)) {

            final int groom_age_at_marriage = Integer.parseInt(groom_age_or_birth_date);

            if (groom_age_at_marriage < LinkageConfig.MIN_AGE_AT_MARRIAGE || groom_age_at_marriage > LinkageConfig.MAX_AGE_AT_DEATH) {

                discrepancy = true;
                if (verbose) {
                    System.out.println();
                    System.out.println("ID: " + marriage_record.getString(Marriage.STANDARDISED_ID));
                    System.out.println("groom_age_at_marriage: " + groom_age_at_marriage);
                }
            }
        }
        else {
            if (usableDate(groom_age_or_birth_date) && usableNumber(marriage_year)) {
                int groom_birth_year = CommonLinkViabilityLogic.getBirthDateFromMarriageRecord(marriage_record, false).getYear();

                final int groom_age_at_marriage = Integer.parseInt(marriage_year) - groom_birth_year;

                if (groom_age_at_marriage < LinkageConfig.MIN_AGE_AT_MARRIAGE || groom_age_at_marriage > LinkageConfig.MAX_AGE_AT_DEATH) {

                    discrepancy = true;
                    if (verbose) {
                        System.out.println();
                        System.out.println("ID: " + marriage_record.getString(Marriage.STANDARDISED_ID));
                        System.out.println("groom_birth_year: " + groom_birth_year);
                        System.out.println("marriage_year: " + marriage_year);
                    }
                }
            }
        }

        if (discrepancy) marriage_discrepancy_count++;
    }

    /**
     * Checks the age at death recorded on the death record is plausible, and consistent with the difference between
     * birth year on death record and death year.
     *
     * @param death_record the record
     * @return true if the record is consistent
     */
    public void checkDeathRecordInternalConsistency(final LXP death_record) {

        final String birth_date = death_record.getString(Death.DATE_OF_BIRTH);
        final String death_year = death_record.getString(Death.DEATH_YEAR);
        final String death_age = death_record.getString(Death.AGE_AT_DEATH);

        death_results.recordUsableFields(usableDate(birth_date), usableNumber(death_year), usableNumber(death_age));

        boolean discrepancy = false;

        if (usableNumber(death_age)) {

            final int age_at_death_recorded_on_death_record = Integer.parseInt(death_age);

            if (age_at_death_recorded_on_death_record > LinkageConfig.MAX_AGE_AT_DEATH) {

                discrepancy = true;

                if (verbose) {
                    outputInconsistentAgeAtDeath(death_record, age_at_death_recorded_on_death_record);
                }
            }

            if (usableDate(birth_date) && usableNumber(death_year)) {

                final int year_of_birth_from_death_record = Integer.parseInt(Normalisation.extractYear(birth_date));
                final int year_of_death_from_death_record = Integer.parseInt(death_year);

                final int age_at_death_calculated_from_death_record = year_of_death_from_death_record - year_of_birth_from_death_record;

                final int age_at_death_discrepancy = Math.abs(age_at_death_recorded_on_death_record - age_at_death_calculated_from_death_record);

                if (age_at_death_discrepancy > LinkageConfig.MAX_ALLOWABLE_AGE_DISCREPANCY) {

                    discrepancy = true;

                    if (verbose) {
                        outputDeathRecordInternalInconsistency(death_record, age_at_death_recorded_on_death_record, year_of_birth_from_death_record, year_of_death_from_death_record, age_at_death_calculated_from_death_record, age_at_death_discrepancy);
                    }
                }
            }
        }

        if (discrepancy) death_discrepancy_count++;
    }

    private void outputDeathRecordInternalInconsistency(LXP death_record, int age_at_death_recorded_on_death_record, int year_of_birth_from_death_record, int year_of_death_from_death_record, int age_at_death_calculated_from_death_record, int age_at_death_discrepancy) {

        System.out.println();
        System.out.println("ID: " + death_record.getString(Death.STANDARDISED_ID));
        System.out.println("year_of_birth_from_death_record: " + year_of_birth_from_death_record);
        System.out.println("year_of_death_from_death_record: " + year_of_death_from_death_record);
        System.out.println("age_at_death_recorded_on_death_record: " + age_at_death_recorded_on_death_record);
        System.out.println("age_at_death_calculated_from_death_record: " + age_at_death_calculated_from_death_record);
        System.out.println("age_at_death_discrepancy: " + age_at_death_discrepancy);
    }

    private void outputInconsistentAgeAtDeath(LXP death_record, int age_at_death_recorded_on_death_record) {

        System.out.println();
        System.out.println("ID: " + death_record.getString(Death.STANDARDISED_ID));
        System.out.println("age_at_death_recorded_on_death_record: " + age_at_death_recorded_on_death_record);
    }

    private static boolean usableDate(String date) {

        return usableNumber(Normalisation.extractYear(date));
    }

    private static boolean usableNumber(String s) {

        if (s == null) return false;
        if (s.isEmpty()) return false;

        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static class Results {

        private final List<String> fields_checked ;
        private final Map<String, Integer> field_presence_counts = new TreeMap<>();

        private Results(List<String> fields_checked) {
            this.fields_checked = fields_checked;
        }

        public void recordUsableFields(boolean... fields_usable) {

            StringBuilder description_builder = new StringBuilder();

            for (int i = 0; i < fields_usable.length; i++) {

                boolean field_usable = fields_usable[i];
                if (!field_usable) description_builder.append("no ");
                description_builder.append(fields_checked.get(i));
                if (i < fields_usable.length - 1) description_builder.append(", "); else description_builder.append(":");
            }

            String description = description_builder.toString();
            field_presence_counts.putIfAbsent(description, 0);
            field_presence_counts.put(description, field_presence_counts.get(description) + 1);
        }

        public void summariseFieldPresenceCounts() {
            for (Map.Entry<String, Integer> entry : field_presence_counts.entrySet()) {
                System.out.println(entry.getKey() + " " + entry.getValue());
            }
        }
    }
}

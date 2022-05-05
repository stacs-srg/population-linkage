/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers;

import uk.ac.standrews.cs.neoStorr.impl.LXP;

import java.util.ArrayList;
import java.util.List;

public class RecordFiltering {

    public static List<LXP> filter(long number_of_required_fields, int number_of_records_required, Iterable<LXP> records_to_filter, List<Integer> linkageFields) {

        List<LXP> filtered_source_records = new ArrayList<>();

        for (LXP record : records_to_filter) {
            if (passesFilter(record, linkageFields, number_of_required_fields)) {
                filtered_source_records.add(record);
            }
            if (filtered_source_records.size() >= number_of_records_required) {
                break;
            }
        }

        return filtered_source_records;
    }

    public static boolean passesFilter(LXP record, List<Integer> filterOn, long reqPopulatedFields) {

        long numberOfEmptyFieldsPermitted = filterOn.size() - reqPopulatedFields;

        int numberOfEmptyFields = 0;

        for (int attribute : filterOn) {
            String value = record.getString(attribute).toLowerCase().trim();
            if (isMissing(value)) {
                numberOfEmptyFields++;
            }
        }

        return numberOfEmptyFields <= numberOfEmptyFieldsPermitted;
    }

    public static boolean isMissing(String value) {
        return value == null || value.equals("") || value.contains("missing") || value.contains("--");
    }
}

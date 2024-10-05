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
            //The following code is only to permit less than all the records to be processed (for example when debugging)
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
        return value == null || value.equals("") || value.contains("missing") || value.contains("--") || value.contains("----") || value.contains("¤");
    }
}

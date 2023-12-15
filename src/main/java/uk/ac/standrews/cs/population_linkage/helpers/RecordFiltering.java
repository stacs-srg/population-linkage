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

    public static List<LXP> filter(final int number_of_records_required, final Iterable<LXP> records_to_filter, final List<Integer> field_indices, final int number_of_populated_fields_required) {

        List<LXP> filtered_source_records = new ArrayList<>();

        for (LXP record : records_to_filter) {
            if (passesFilter(record, field_indices, number_of_populated_fields_required)) {
                filtered_source_records.add(record);
            }
            if (filtered_source_records.size() >= number_of_records_required) {
                break;
            }
        }

        return filtered_source_records;
    }

    public static boolean passesFilter(final LXP record, final List<Integer> field_indices, final int number_of_populated_fields_required) {

        final long number_of_empty_fields_permitted = field_indices.size() - number_of_populated_fields_required;

        int number_of_empty_fields = 0;

        for (final int field_index : field_indices) {

            final String value = record.getString(field_index).toLowerCase().trim();
            if (isMissing(value)) {
                number_of_empty_fields++;
            }
        }

        return number_of_empty_fields <= number_of_empty_fields_permitted;
    }

    public static boolean isMissing(final String value) {
        return value == null || value.isEmpty() || value.contains("missing") || value.contains("--");
    }
}

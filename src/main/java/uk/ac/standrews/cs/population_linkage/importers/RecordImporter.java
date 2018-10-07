/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module linkage-java.
 *
 * linkage-java is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * linkage-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with linkage-java. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.importers;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.util.List;
import java.util.Map;

/**
 * Utility classes for importing records in digitising scotland format
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public abstract class RecordImporter {

    public abstract Map<Integer, String> getRecordMap();

    public abstract int[] getUnavailableRecords();

    static void addAvailableSingleFields(final DataSet data, final List<String> record, final LXP lxp_record, Map<Integer, String> label_map) {

        for (int field : label_map.keySet()) {
            lxp_record.put(field, data.getValue(record, label_map.get(field)));
        }
    }

    static void addUnavailableFields(final LXP lxp_record, final int[] unavailable_record_labels) {

        for (int field : unavailable_record_labels) {
            lxp_record.put(field, "");
        }
    }

    protected static String combineFields(final DataSet data, final List<String> record, String... source_field_labels) {

        StringBuilder builder = new StringBuilder();

        for (String source_field_label : source_field_labels) {

            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(data.getValue(record, source_field_label));
        }

        return builder.toString();
    }
}

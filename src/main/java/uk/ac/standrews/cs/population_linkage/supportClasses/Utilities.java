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
package uk.ac.standrews.cs.population_linkage.supportClasses;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Utilities {

    private static final long SEED = 34553543456223L;

    public static Iterable<LXP> getBirthRecords(RecordRepository record_repository) {

        return () -> new Iterator<>() {

            Iterator<Birth> birth_records = record_repository.getBirths().iterator();

            @Override
            public boolean hasNext() {
                return birth_records.hasNext();
            }

            @Override
            public LXP next() {
                return birth_records.next();
            }
        };
    }

    public static Iterable<LXP> getDeathRecords(RecordRepository record_repository) {

        return () -> new Iterator<>() {

            Iterator<Death> death_records = record_repository.getDeaths().iterator();

            @Override
            public boolean hasNext() {
                return death_records.hasNext();
            }

            @Override
            public LXP next() {
                return death_records.next();
            }
        };
    }

    public static Iterable<LXP> getMarriageRecords(RecordRepository record_repository) {

        return () -> new Iterator<>() {

            Iterator<Marriage> marriage_records = record_repository.getMarriages().iterator();

            @Override
            public boolean hasNext() {
                return marriage_records.hasNext();
            }

            @Override
            public LXP next() {
                return marriage_records.next();
            }
        };
    }

    public static void printSampleRecords(DataSet data_set, String record_type, int number_to_print) {
        uk.ac.standrews.cs.population_records.record_types.Utilities.printSampleRecords(data_set, record_type, number_to_print);
    }

    public static <T> List<T> permute(final Iterable<T> records) {

        return permute(records, new Random(SEED));
    }

    public static <T> List<T> permute(final Iterable<T> records, final Random random) {

        List<T> record_list = new ArrayList<>();
        for (T record : records) {
            record_list.add(record);
        }

        int number_of_records = record_list.size();

        for (int i = 0; i < number_of_records; i++) {
            int swap_index = random.nextInt(number_of_records);
            T temp = record_list.get(i);
            record_list.set(i, record_list.get(swap_index));
            record_list.set(swap_index, temp);
        }
        return record_list;
    }

    public static String originalId(LXP record) {
        if (record instanceof Birth)
            return record.getString(Birth.ORIGINAL_ID);
        if (record instanceof Marriage)
            return record.getString(Marriage.ORIGINAL_ID);
        if (record instanceof Death)
            return record.getString(Death.ORIGINAL_ID);

        throw new Error("Record of unknown type: " + record.getClass().getName());
    }

    public static List<String> getLabels(LXP record) {
        if (record instanceof Birth)
            return Birth.getLabels();
        if (record instanceof Marriage)
            return Marriage.getLabels();
        if (record instanceof Death)
            return Death.getLabels();

        throw new Error("Record of unknown type: " + record.getClass().getName());
    }
}

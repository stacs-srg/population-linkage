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
package uk.ac.standrews.cs.population_linkage.groundTruth.umea;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.datasets.Umea;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;

import java.util.HashMap;
import java.util.Map;

public class UmeaBirthSiblingExamineRecordsAlExperiment {

    protected Iterable<LXP> records;
    protected Map<String, Integer> father_surname_map = new HashMap<>();
    protected Map<String, Integer> mother_surname_map = new HashMap<>();
    protected Map<String, Integer> father_name_map = new HashMap<>();
    protected Map<String, Integer> mother_name_map = new HashMap<>();
    protected Map<String, Integer> combined_parental_map = new HashMap<>();
    protected Map<String, Integer> combined_both_known_parental_map = new HashMap<>();
    protected int record_counter = 0;
    protected int parents_known_counter = 0;

    UmeaBirthSiblingExamineRecordsAlExperiment() {
        System.out.println("Reading records from repository: " + Umea.REPOSITORY_NAME);
        final RecordRepository record_repository = new RecordRepository(Umea.REPOSITORY_NAME);

        records = getSourceRecords(record_repository);
    }

    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        System.out.println("Umea Births");
        return Utilities.getBirthRecords(record_repository);
    }

    protected void runAll() {
        run();
        UmeaNameDistribution.printStats(father_surname_map, "father surnames");
        UmeaNameDistribution.printStats(mother_surname_map, "mother surnames");
        UmeaNameDistribution.printStats(father_name_map, "father combined names");
        UmeaNameDistribution.printStats(mother_name_map, "mother combined names");
        UmeaNameDistribution.printStats(combined_parental_map, "combined surnames");
        UmeaNameDistribution.printStats(combined_both_known_parental_map, "combined both known surnames");
    }

    protected void run() {
        for (LXP record : records) {
            String father_surname = record.getString(Birth.FATHER_SURNAME);
            String father_forename = record.getString(Birth.FATHER_FORENAME);
            String mother_surname = record.getString(Birth.MOTHER_MAIDEN_SURNAME);
            String mother_forename = record.getString(Birth.FATHER_FORENAME);

            addToCounts(father_surname, father_forename, mother_surname, mother_forename);
            if (bothParentsKnown(record)) {
                parents_known_counter++;
                addToMap(combined_both_known_parental_map, father_forename + father_surname + mother_forename + mother_surname);
            }
            record_counter++;
        }
        printAnalysis();
    }

    protected void printAnalysis() {
        System.out.println("number of records = " + record_counter);
        System.out.println("number of both parents known = " + parents_known_counter);
        System.out.println("Number of unique father surnames = " + father_surname_map.keySet().size());
        System.out.println("Number of unique mother surnames = " + mother_surname_map.keySet().size());
        System.out.println("Number of unique father names = " + father_name_map.keySet().size());
        System.out.println("Number of unique mother names = " + mother_name_map.keySet().size());
        System.out.println("Number of unique combined (Mother and Father) names = " + combined_parental_map.keySet().size());
        System.out.println("Number of unique combined (Mother and Father) names (where both known) = " + combined_both_known_parental_map.keySet().size());
    }

    protected boolean bothParentsKnown(LXP record) {

        final String b1_mother_id = record.getString(Birth.MOTHER_IDENTITY);
        final String b1_father_id = record.getString(Birth.FATHER_IDENTITY);

        return !b1_mother_id.isEmpty() && b1_father_id.isEmpty();
    }

    protected void addToCounts(String father_surname, String father_forename, String mother_surname, String mother_forename) {
        addToMap(father_surname_map, father_surname);
        addToMap(mother_surname_map, mother_surname);
        addToMap(father_name_map, father_forename + father_surname);
        addToMap(mother_name_map, mother_forename + mother_surname);
        addToMap(combined_parental_map, father_forename + father_surname + mother_forename + mother_surname);
    }

    protected void addToMap(Map<String, Integer> map, String value) {
        if (!value.equals("")) {
            Integer count = map.get(value);
            if (count == null) {
                map.put(value, 0);
            } else {
                map.put(value, count + 1);
            }
        }
    }

    public static void main(String[] args) throws Exception {

        new UmeaBirthSiblingExamineRecordsAlExperiment().runAll();
    }
}

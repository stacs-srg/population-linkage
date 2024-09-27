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

import uk.ac.standrews.cs.data.umea.UmeaBirthsDataSet;
import uk.ac.standrews.cs.data.umea.UmeaDeathsDataSet;
import uk.ac.standrews.cs.data.umea.UmeaMarriagesDataSet;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.time.LocalDateTime;

public class ImportUmeaRecordsToStore {

    public final static String REPOSITORY_NAME = "umea";

    public void run() throws Exception {

         try (RecordRepository record_repository = new RecordRepository(REPOSITORY_NAME)) {

             DataSet birth_records = new UmeaBirthsDataSet();
             record_repository.importBirthRecords(birth_records);
             System.out.println("Imported " + birth_records.getRecords().size() + " birth records");

             DataSet death_records = new UmeaDeathsDataSet();
             record_repository.importDeathRecords(death_records);
             System.out.println("Imported " + death_records.getRecords().size() + " death records");

             DataSet marriage_records = new UmeaMarriagesDataSet();
             record_repository.importMarriageRecords(marriage_records);
             System.out.println("Imported " + marriage_records.getRecords().size() + " marriage records");
         }
    }

    public static void main(String[] args) throws Exception {

        System.out.println("Importing Umea records into repository: " + REPOSITORY_NAME + " @ " + LocalDateTime.now());
        new ImportUmeaRecordsToStore().run();
        System.out.println("Complete @ " + LocalDateTime.now());
    }
}

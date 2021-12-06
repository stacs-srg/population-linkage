/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.umea;

import uk.ac.standrews.cs.data.umea.UmeaBirthsDataSet;
import uk.ac.standrews.cs.data.umea.UmeaDeathsDataSet;
import uk.ac.standrews.cs.data.umea.UmeaMarriagesDataSet;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.time.LocalDateTime;

public class ImportUmeaRecordsToStore {

    public final static String REPO_NAME = "umea";

    public void run() throws Exception {

         try (RecordRepository record_repository = new RecordRepository(REPO_NAME)) {

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

        System.out.println("Importing Umea records into repository: " + REPO_NAME + " @ " + LocalDateTime.now());
        new ImportUmeaRecordsToStore().run();
        System.out.println("Complete @ " + LocalDateTime.now());
    }
}

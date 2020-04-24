/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.umea;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.nio.file.Path;

public class PrintUmeaRecordsFromStore {

    private final Path store_path;
    private final String repo_name;

    public PrintUmeaRecordsFromStore(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        System.out.println("Reading records from repository: " + repo_name);
        System.out.println();

        DataSet births_data_set = Birth.convertToDataSet(record_repository.getBirths());
        births_data_set.print(System.out);
        System.out.println("Read " + births_data_set.getRecords().size() + " birth records");

        DataSet deaths_data_set = Death.convertToDataSet(record_repository.getDeaths());
        deaths_data_set.print(System.out);
        System.out.println("Read " + deaths_data_set.getRecords().size() + " death records");

        DataSet marriages_data_set = Marriage.convertToDataSet(record_repository.getMarriages());
        marriages_data_set.print(System.out);
        System.out.println("Read " + marriages_data_set.getRecords().size() + " marriage records");

        System.out.println();
        System.out.println("Complete");
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = ApplicationProperties.getRepositoryName();

        new PrintUmeaRecordsFromStore(store_path, repo_name).run();
    }
}

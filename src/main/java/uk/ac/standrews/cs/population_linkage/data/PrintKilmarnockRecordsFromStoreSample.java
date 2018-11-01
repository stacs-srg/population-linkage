package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.nio.file.Path;

public class PrintKilmarnockRecordsFromStoreSample {

    private final Path store_path;
    private final String repo_name;

    public PrintKilmarnockRecordsFromStoreSample(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        System.out.println("Reading records from repository: " + repo_name);
        System.out.println();

        Iterable<Birth> births = record_repository.getBirths();
        DataSet births_data_set = Utilities.toDataSet(births);
        Utilities.printSampleRecords(births_data_set, "birth");

        System.out.println();

        Iterable<Death> deaths = record_repository.getDeaths();
        DataSet deaths_data_set = Utilities.toDataSet(deaths);
        Utilities.printSampleRecords(deaths_data_set, "death");

        System.out.println();

        Iterable<Marriage> marriages = record_repository.getMarriages();
        DataSet marriages_data_set = Utilities.toDataSet(marriages);
        Utilities.printSampleRecords(marriages_data_set, "marriage");
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = ApplicationProperties.getRepositoryName();

        new PrintKilmarnockRecordsFromStoreSample(store_path, repo_name).run();
    }
}

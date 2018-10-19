package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.nio.file.Path;

public class PrintKilmarnockRecordsFromStoreSample {

    private final Path store_path;
    private final String repo_name;

    private final static int NUMBER_TO_PRINT = 5;

    public PrintKilmarnockRecordsFromStoreSample(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        System.out.println("Reading records from repository: " + repo_name);
        System.out.println();

        int births_count = 0;
        for (Birth birth : record_repository.getBirths()) {
            if (births_count < NUMBER_TO_PRINT) {
                System.out.println(birth);
            }
            births_count++;
        }
        System.out.println("Read " + births_count + " birth records");
        System.out.println();

        int deaths_count = 0;
        for (Death death : record_repository.getDeaths()) {
            if (deaths_count < NUMBER_TO_PRINT) {
                System.out.println(death);
            }
            deaths_count++;
        }
        System.out.println("Read " + deaths_count + " death records");
        System.out.println();

        int marriages_count = 0;
        for (Marriage marriage : record_repository.getMarriages()) {
            if (marriages_count < NUMBER_TO_PRINT) {
                System.out.println(marriage);
            }
            marriages_count++;
        }
        System.out.println("Read " + marriages_count + " marriage records");

        System.out.println();
        System.out.println("Complete");
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = ApplicationProperties.getRepositoryName();

        new PrintKilmarnockRecordsFromStoreSample(store_path, repo_name).run();
    }
}

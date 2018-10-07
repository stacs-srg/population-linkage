package uk.ac.standrews.cs.population_linkage.actions;

import uk.ac.standrews.cs.population_linkage.importers.RecordRepository;
import uk.ac.standrews.cs.population_linkage.importers.kilmarnock.KilmarnockDataSetImporter;
import uk.ac.standrews.cs.population_linkage.record_types.Birth;
import uk.ac.standrews.cs.population_linkage.record_types.Death;
import uk.ac.standrews.cs.population_linkage.record_types.Marriage;

import java.nio.file.Path;

public class RecordsPrinter {

    private final Path store_path;

    public RecordsPrinter(Path store_path) {

        this.store_path = store_path;
    }

    public void run() throws Exception {

        String repo_name = "kilmarnock_repository";
        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        System.out.println("Reading records from repository: " + repo_name);
        System.out.println();

        int births_count = 0;
        for (long object_id : record_repository.births.getOids()) {

            Birth birth = record_repository.births.getObjectById(object_id);
            System.out.println(birth);
            births_count++;
        }

        System.out.println("Read " + births_count + " birth records");

        int deaths_count = 0;
        for (long object_id : record_repository.deaths.getOids()) {

            Death death = record_repository.deaths.getObjectById(object_id);
            System.out.println(death);
            deaths_count++;
        }
        System.out.println("Read " + deaths_count + " death records");

        int marriages_count = 0;
        for (long object_id : record_repository.marriages.getOids()) {

            Marriage marriage = record_repository.marriages.getObjectById(object_id);
            System.out.println(marriage);
            marriages_count++;
        }
        System.out.println("Read " + marriages_count + " marriage records");

        System.out.println();
        System.out.println("Complete");
    }
}

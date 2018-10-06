package uk.ac.standrews.cs.population_linkage.actions;

import uk.ac.standrews.cs.population_linkage.importers.DataSetImporter;
import uk.ac.standrews.cs.population_linkage.importers.RecordRepository;
import uk.ac.standrews.cs.population_linkage.importers.kilmarnock.KilmarnockDataSetImporter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RecordsImporter {

    public void run() throws Exception {

        Path store_path = Paths.get("/Users/graham/Desktop/store");
        String repo_name = "kilmarnock_repository";
        Path birth_records_path = Paths.get("/uk/ac/standrews/cs/population_linkage/data/kilmarnock/kilmarnock_births.csv");
        Path death_records_path = Paths.get("/uk/ac/standrews/cs/population_linkage/data/kilmarnock/kilmarnock_deaths.csv");
        Path marriage_records_path = Paths.get("/uk/ac/standrews/cs/population_linkage/data/kilmarnock/kilmarnock_marriages.csv");

        DataSetImporter importer = new KilmarnockDataSetImporter(store_path, repo_name, true, birth_records_path, death_records_path, marriage_records_path);

        System.out.println("Importing " + importer.getDataSetName() + " records into repository: " + repo_name);
        System.out.println();

        int births_count = importer.importBirthRecords();
        System.out.println("Imported " + births_count + " birth records");

        int deaths_count = importer.importDeathRecords();
        System.out.println("Imported " + deaths_count + " death records");

        int marriages_count = importer.importMarriageRecords();
        System.out.println("Imported " + marriages_count + " marriage records");

        System.out.println();
        System.out.println("Complete");
    }
}

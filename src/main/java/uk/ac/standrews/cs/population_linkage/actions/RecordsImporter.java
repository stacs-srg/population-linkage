package uk.ac.standrews.cs.population_linkage.actions;

import uk.ac.standrews.cs.data.kilmarnock.BirthsDataSet;
import uk.ac.standrews.cs.data.kilmarnock.DeathsDataSet;
import uk.ac.standrews.cs.data.kilmarnock.MarriagesDataSet;
import uk.ac.standrews.cs.population_linkage.importers.DataSetImporter;
import uk.ac.standrews.cs.population_linkage.importers.RecordRepository;
import uk.ac.standrews.cs.population_linkage.importers.kilmarnock.KilmarnockDataSetImporter;
import uk.ac.standrews.cs.utilities.crypto.CryptoException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RecordsImporter {

    private final Path store_path;

    public RecordsImporter(Path store_path) {

        this.store_path = store_path;
    }

    public void run() throws Exception, CryptoException {

        String repo_name = "kilmarnock_repository";

        DataSetImporter importer = new KilmarnockDataSetImporter(store_path, repo_name, new BirthsDataSet(), new DeathsDataSet(), new MarriagesDataSet());

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

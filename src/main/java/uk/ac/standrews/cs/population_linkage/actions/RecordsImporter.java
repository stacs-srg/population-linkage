package uk.ac.standrews.cs.population_linkage.actions;

import uk.ac.standrews.cs.data.kilmarnock.BirthsDataSet;
import uk.ac.standrews.cs.data.kilmarnock.DeathsDataSet;
import uk.ac.standrews.cs.data.kilmarnock.MarriagesDataSet;
import uk.ac.standrews.cs.population_linkage.importers.DataSetImporter;
import uk.ac.standrews.cs.population_linkage.importers.RecordRepository;
import uk.ac.standrews.cs.population_linkage.importers.kilmarnock.KilmarnockDataSetImporter;
import uk.ac.standrews.cs.utilities.crypto.CryptoException;

import java.nio.file.Path;

public class RecordsImporter {

    private final Path store_path;
    private final String repo_name;
    private RecordRepository record_repository;

    public RecordsImporter(Path store_path, String repo_name) throws Exception {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    public void run() throws Exception, CryptoException {

        record_repository = new RecordRepository(store_path, repo_name);

        DataSetImporter importer = new KilmarnockDataSetImporter();

        System.out.println("Importing " + importer.getDataSetName() + " records into repository: " + repo_name);
        System.out.println();

        int births_count = importer.importBirthRecords(record_repository, new BirthsDataSet());
        System.out.println("Imported " + births_count + " birth records");

        int deaths_count = importer.importDeathRecords(record_repository, new DeathsDataSet());
        System.out.println("Imported " + deaths_count + " death records");

        int marriages_count = importer.importMarriageRecords(record_repository, new MarriagesDataSet());
        System.out.println("Imported " + marriages_count + " marriage records");

        System.out.println();
        System.out.println("Complete");
    }
}

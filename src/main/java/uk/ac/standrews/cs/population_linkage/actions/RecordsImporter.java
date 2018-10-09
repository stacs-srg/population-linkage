package uk.ac.standrews.cs.population_linkage.actions;

import uk.ac.standrews.cs.data.kilmarnock.data.BirthsDataSet;
import uk.ac.standrews.cs.data.kilmarnock.data.DeathsDataSet;
import uk.ac.standrews.cs.data.kilmarnock.data.MarriagesDataSet;
import uk.ac.standrews.cs.data.kilmarnock.importer.KilmarnockDataSetImporter;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.importer.DataSetImporter;
import uk.ac.standrews.cs.utilities.crypto.CryptoException;

import java.nio.file.Path;

public class RecordsImporter {

    private final Path store_path;
    private final String repo_name;

    public RecordsImporter(Path store_path, String repo_name) throws Exception {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    public void run() throws Exception, CryptoException {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        DataSetImporter importer = new KilmarnockDataSetImporter();

        System.out.println("Importing " + importer.getDataSetName() + " records into repository: " + repo_name);
        System.out.println();

        BirthsDataSet birth_records = new BirthsDataSet();
        importer.importBirthRecords(record_repository, birth_records);
        System.out.println("Imported " + birth_records.getRecords().size() + " birth records");

        DeathsDataSet death_records = new DeathsDataSet();
        importer.importDeathRecords(record_repository, death_records);
        System.out.println("Imported " + death_records.getRecords().size() + " death records");

        MarriagesDataSet marriage_records = new MarriagesDataSet();
        importer.importMarriageRecords(record_repository, marriage_records);
        System.out.println("Imported " + marriage_records.getRecords().size() + " marriage records");

        System.out.println();
        System.out.println("Complete");
    }
}

package uk.ac.standrews.cs.population_linkage.data.skye;

import uk.ac.standrews.cs.data.skye.SkyeBirthsDataSet;
import uk.ac.standrews.cs.data.skye.SkyeDeathsDataSet;
import uk.ac.standrews.cs.data.skye.SkyeMarriagesDataSet;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.nio.file.Path;

public class ImportSkyeRecordsToStore {

    private final Path store_path;
    private final String repo_name;

    public ImportSkyeRecordsToStore(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        System.out.println("Importing Skye records into repository: " + repo_name);
        System.out.println();

        DataSet birth_records = new SkyeBirthsDataSet();
        record_repository.importBirthRecords(birth_records);
        System.out.println("Imported " + birth_records.getRecords().size() + " birth records");

        DataSet death_records = new SkyeDeathsDataSet();
        record_repository.importDeathRecords(death_records);
        System.out.println("Imported " + death_records.getRecords().size() + " death records");

        DataSet marriage_records = new SkyeMarriagesDataSet();
        record_repository.importMarriageRecords(marriage_records);
        System.out.println("Imported " + marriage_records.getRecords().size() + " marriage records");

        System.out.println();
        System.out.println("Complete");
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = ApplicationProperties.getRepositoryName();

        new ImportSkyeRecordsToStore(store_path, repo_name).run();
    }
}

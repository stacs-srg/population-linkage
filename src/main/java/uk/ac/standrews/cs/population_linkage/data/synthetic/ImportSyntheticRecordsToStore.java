package uk.ac.standrews.cs.population_linkage.data.synthetic;

import uk.ac.standrews.cs.data.synthetic.scot_test._570k._1._570k_1_BirthsDataSet;
import uk.ac.standrews.cs.data.synthetic.scot_test._570k._1._570k_1_DeathsDataSet;
import uk.ac.standrews.cs.data.synthetic.scot_test._570k._1._570k_1_MarriagesDataSet;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.nio.file.Path;

public class ImportSyntheticRecordsToStore {

    private final Path store_path;
    private final String repo_name;

    public ImportSyntheticRecordsToStore(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        System.out.println("Importing Synthetic records into repository: " + repo_name);
        System.out.println();

        DataSet birth_records = new _570k_1_BirthsDataSet();
        record_repository.importBirthRecords(birth_records);
        System.out.println("Imported " + birth_records.getRecords().size() + " birth records");

        DataSet death_records = new _570k_1_DeathsDataSet();
        record_repository.importDeathRecords(death_records);
        System.out.println("Imported " + death_records.getRecords().size() + " death records");

        DataSet marriage_records = new _570k_1_MarriagesDataSet();
        record_repository.importMarriageRecords(marriage_records);
        System.out.println("Imported " + marriage_records.getRecords().size() + " marriage records");

        System.out.println();
        System.out.println("Complete");
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = ApplicationProperties.getRepositoryName();

        new ImportSyntheticRecordsToStore(store_path, repo_name).run();
    }
}

package uk.ac.standrews.cs.population_linkage.data.synthetic;


import uk.ac.standrews.cs.data.synthetic.scot_test.SyntheticScotlandBirthsDataSet;
import uk.ac.standrews.cs.data.synthetic.scot_test.SyntheticScotlandDeathsDataSet;
import uk.ac.standrews.cs.data.synthetic.scot_test.SyntheticScotlandMarriagesDataSet;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.nio.file.Path;

public class ImportSyntheticScotlandRecordsToStore {

    private final Path store_path;
    private final String repo_name;
    private final String populationSize;
    private final String populationNumber;
    private final boolean corrupted;
    private final String corruptionNumber;

    public ImportSyntheticScotlandRecordsToStore(Path store_path, String populationSize, String populationNumber, boolean corrupted, String corruptionNumber) {

        this.store_path = store_path;
        this.populationNumber = populationNumber;
        this.populationSize = populationSize;
        this.corrupted = corrupted;
        this.corruptionNumber = corruptionNumber;

        if(corrupted)
            this.repo_name = "scotland_" + populationSize + "_" + populationNumber + "_corrupted_" + corruptionNumber;
        else {
            this.repo_name = "scotland_" + populationSize + "_" + populationNumber + "_clean";
        }

        System.out.println("REPO NAME: " + this.repo_name);
    }

    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        System.out.println("Importing Synthetic records into repository: " + repo_name);
        System.out.println();

        DataSet birth_records = SyntheticScotlandBirthsDataSet.factory(populationSize, populationNumber, corrupted, corruptionNumber);
        record_repository.importBirthRecords(birth_records);
        System.out.println("Imported " + birth_records.getRecords().size() + " birth records");

        DataSet death_records = SyntheticScotlandDeathsDataSet.factory(populationSize, populationNumber, corrupted, corruptionNumber);
        record_repository.importDeathRecords(death_records);
        System.out.println("Imported " + death_records.getRecords().size() + " death records");

        DataSet marriage_records = SyntheticScotlandMarriagesDataSet.factory(populationSize, populationNumber, corrupted, corruptionNumber);
        record_repository.importMarriageRecords(marriage_records);
        System.out.println("Imported " + marriage_records.getRecords().size() + " marriage records");

        System.out.println();
        System.out.println("Complete");
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();

        new ImportSyntheticScotlandRecordsToStore(store_path, args[0], args[1], args[2].equals("true"), args[3]).run();
    }
}

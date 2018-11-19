package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.data.kilmarnock.ImportKilmarnockRecordsToStore;
import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.nio.file.Path;
import java.util.Collection;

public class CreateLinkageRecords {

    private final Path store_path;
    private final String repo_name;

    public CreateLinkageRecords(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        Collection<LXP> birth_sub_records = Utilities.getBirthLinkageSubRecords(record_repository);
        Collection<LXP> death_sub_records = Utilities.getDeathLinkageSubRecords(record_repository);
        Collection<LXP> marriage_sub_records = Utilities.getMarriageLinkageSubRecords(record_repository);

        for (LXP sub_record : birth_sub_records) {
            System.out.println(sub_record);
        }

        for (LXP sub_record : death_sub_records) {
            System.out.println(sub_record);
        }

        for (LXP sub_record : marriage_sub_records) {
            System.out.println(sub_record);
        }
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = ApplicationProperties.getRepositoryName();

        new ImportKilmarnockRecordsToStore(store_path, repository_name).run();
        new CreateLinkageRecords(store_path, repository_name).run();
    }
}

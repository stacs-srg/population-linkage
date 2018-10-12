package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.data.ImportKilmarnockRecordsToStore;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class CreateBirthLinkageRecords {

    private final Path store_path;
    private final String repo_name;

    public CreateBirthLinkageRecords(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        Collection<BirthLinkageSubRecord> sub_records = new ArrayList<>();

        for (Birth birth : record_repository.getBirths()) {

            BirthLinkageSubRecord rec = new BirthLinkageSubRecord(birth);
            sub_records.add(rec);
        }

        for (BirthLinkageSubRecord sub_record : sub_records) {
            System.out.println(sub_record);
        }
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = ApplicationProperties.getRepositoryName();

        new ImportKilmarnockRecordsToStore(store_path, repository_name).run();
        new CreateBirthLinkageRecords(store_path, repository_name).run();
    }
}

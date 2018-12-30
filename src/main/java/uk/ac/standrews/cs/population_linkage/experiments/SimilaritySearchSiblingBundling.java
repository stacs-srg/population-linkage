package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.nio.file.Path;
import java.util.List;

public abstract class SimilaritySearchSiblingBundling extends SiblingBundling {

    static final double MATCH_THRESHOLD = 4.0;
    static final int NUMBER_OF_PROGRESS_UPDATES = 10000;

    private final Path store_path;
    protected final String repo_name;

    SimilaritySearchSiblingBundling(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    protected RecordRepository getRecordRepository() throws Exception {
        return new RecordRepository(store_path, repo_name);
    }

    protected List<LXP> getRecords(RecordRepository record_repository) {
        return Utilities.getBirthLinkageSubRecords(record_repository);
    }
}

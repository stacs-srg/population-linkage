package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.model.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_linkage.model.Links;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.nio.file.Path;
import java.util.List;

public abstract class SimilaritySearchThresholdSiblingBundling extends Experiment {

    protected static final double MATCH_THRESHOLD = 2.0;
    protected static final int NUMBER_OF_PROGRESS_UPDATES = 100;

    private final Path store_path;
    protected final String repo_name;

    public SimilaritySearchThresholdSiblingBundling(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    protected RecordRepository getRecordRepository() throws Exception {
        return new RecordRepository(store_path, repo_name);
    }

    protected List<LXP> getRecords(RecordRepository record_repository) {
        return Utilities.getBirthLinkageSubRecords(record_repository);
    }

    protected abstract Linker getLinker();

    protected Links getGroundTruthLinks(RecordRepository record_repository) {
        return Utilities.getGroundTruthSiblingLinks(record_repository);
    }

    protected LinkageQuality evaluateLinkage(Links calculated_links, Links ground_truth_links) {
        return Utilities.evaluateLinkage(calculated_links, ground_truth_links);
    }
}

package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkage.BruteForceSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class BruteForceThresholdSiblingBundling extends SiblingBundling {

    private static final double MATCH_THRESHOLD = 2.0;
    private static final int NUMBER_OF_PROGRESS_UPDATES = 100;
    private static final List<Integer> SIBLING_GROUND_TRUTH_FIELDS = Collections.singletonList(Birth.FAMILY);

    private final Path store_path;
    private final String repo_name;

    private BruteForceThresholdSiblingBundling(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    protected RecordRepository getRecordRepository() throws Exception {
        return new RecordRepository(store_path, repo_name);
    }

    protected void printHeader() {
        System.out.println("Sibling bundling using brute force Levenshtein threshold " + MATCH_THRESHOLD + " from repository: " + repo_name);
    }

    protected List<LXP> getRecords(RecordRepository record_repository) {
        return Utilities.getBirthLinkageSubRecords(record_repository);
    }

    protected Linker getLinker() {
        return new BruteForceSiblingBundlerOverBirths(Utilities.weightedAverageLevenshteinOverBirths(), MATCH_THRESHOLD, NUMBER_OF_PROGRESS_UPDATES);
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = ApplicationProperties.getRepositoryName();

        new BruteForceThresholdSiblingBundling(store_path, repository_name).run();
    }

    protected List<Integer> getSiblingGroundTruthFields() {

        return SIBLING_GROUND_TRUTH_FIELDS;
    }
}

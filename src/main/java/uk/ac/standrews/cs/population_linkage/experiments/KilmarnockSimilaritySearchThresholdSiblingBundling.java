package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkage.MTreeSearchStructure;
import uk.ac.standrews.cs.population_linkage.linkage.SimilaritySearchSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.model.*;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.nio.file.Path;
import java.util.List;

public class KilmarnockSimilaritySearchThresholdSiblingBundling extends Experiment {

    private static final double MATCH_THRESHOLD = 2.0;
    private static final int NUMBER_OF_RECORDS_TO_CONSIDER = 2;
    private static final int NUMBER_OF_PROGRESS_UPDATES = 1000;

    private final Path store_path;
    private final String repo_name;

    public KilmarnockSimilaritySearchThresholdSiblingBundling(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    private SearchStructure<LXP> makeSearchStructure() throws InvalidWeightsException {
        return new MTreeSearchStructure<>(Utilities.weightedAverageLevenshteinOverBirths());
    }

    protected RecordRepository getRecordRepository() throws Exception {
        return new RecordRepository(store_path, repo_name);
    }

    protected void printHeader() {
        System.out.println("Kilmarnock sibling bundling using M-tree Levenshtein threshold 2.0");
    }

    protected List<LXP> getRecords(RecordRepository record_repository) {
        return Utilities.getBirthLinkageSubRecords(record_repository);
    }

    protected Linker getLinker() throws InvalidWeightsException {
        return new SimilaritySearchSiblingBundlerOverBirths(makeSearchStructure(), MATCH_THRESHOLD, NUMBER_OF_RECORDS_TO_CONSIDER, NUMBER_OF_PROGRESS_UPDATES);
    }

    protected Links getGroundTruthLinks(RecordRepository record_repository) {
        return Utilities.getGroundTruthSiblingLinks(record_repository);
    }

    protected LinkageQuality evaluateLinkage(Links calculated_links, Links ground_truth_links) {
        return Utilities.evaluateLinkage(calculated_links, ground_truth_links);
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = ApplicationProperties.getRepositoryName();

        new KilmarnockSimilaritySearchThresholdSiblingBundling(store_path, repository_name).run();
    }
}

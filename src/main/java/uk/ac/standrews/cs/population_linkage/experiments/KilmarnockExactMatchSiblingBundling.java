package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkage.BruteForceExactMatchSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.model.InvalidWeightsException;
import uk.ac.standrews.cs.population_linkage.model.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_linkage.model.Links;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.nio.file.Path;
import java.util.List;

public class KilmarnockExactMatchSiblingBundling extends Experiment {

    private static final int NUMBER_OF_PROGRESS_UPDATES = 0;

    private final Path store_path;
    private final String repo_name;

    public KilmarnockExactMatchSiblingBundling(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    protected RecordRepository getRecordRepository() throws Exception {
        return new RecordRepository(store_path, repo_name);
    }

    protected void printHeader() {
        System.out.println("Kilmarnock sibling bundling using brute force exact-match");
    }

    protected List<LXP> getRecords(RecordRepository record_repository) {
        return Utilities.getBirthLinkageSubRecords(record_repository);
    }

    protected Linker getLinker() throws InvalidWeightsException {
        return new BruteForceExactMatchSiblingBundlerOverBirths(Birth.ROLE_BABY, NUMBER_OF_PROGRESS_UPDATES);
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

        new KilmarnockExactMatchSiblingBundling(store_path, repository_name).run();
    }
}

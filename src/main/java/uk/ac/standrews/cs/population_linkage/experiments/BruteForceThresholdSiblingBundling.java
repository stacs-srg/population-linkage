package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkage.BruteForceSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class BruteForceThresholdSiblingBundling extends SiblingBundling {

    private static final double MATCH_THRESHOLD = 2.0;
    private static final int NUMBER_OF_PROGRESS_UPDATES = 100;
    private static final List<Integer> SIBLING_GROUND_TRUTH_FIELDS = Collections.singletonList(Birth.FAMILY);

    private BruteForceThresholdSiblingBundling(Path store_path, String repo_name) {

       super(store_path, repo_name);
    }

    @Override
    protected RecordRepository getRecordRepository() {
        return new RecordRepository(store_path, repo_name);
    }

    @Override
    protected void printHeader() {

        System.out.println("Sibling bundling using brute force, " + getBaseMetric().getMetricName() + " with threshold " + getMatchThreshold() + " from repository: " + repo_name);
    }

    @Override
    protected Iterable<LXP> getRecords(RecordRepository record_repository) {

        return Utilities.getBirthRecords(record_repository);
    }

    @Override
    protected Linker getLinker() {

        return new BruteForceSiblingBundlerOverBirths(getCompositeMetric(), getMatchThreshold(), getNumberOfProgressUpdates());
    }

    @Override
    protected NamedMetric<String> getBaseMetric() {
        return Utilities.LEVENSHTEIN;
    }

    @Override
    protected NamedMetric<LXP> getCompositeMetric() {
        return new Sigma(getBaseMetric(), getMatchFields());
    }

    @Override
    protected double getMatchThreshold() {
        return MATCH_THRESHOLD;
    }

    @Override
    protected int getNumberOfProgressUpdates() {
        return NUMBER_OF_PROGRESS_UPDATES;
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = ApplicationProperties.getRepositoryName();

        new BruteForceThresholdSiblingBundling(store_path, repository_name).run();
    }
}

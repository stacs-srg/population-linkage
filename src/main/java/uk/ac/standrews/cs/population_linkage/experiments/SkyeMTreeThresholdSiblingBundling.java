package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkage.MTreeSearchStructure;
import uk.ac.standrews.cs.population_linkage.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.linkage.SimilaritySearchSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class SkyeMTreeThresholdSiblingBundling extends MTreeThresholdSiblingBundling {

    private static final List<Integer> SIBLING_GROUND_TRUTH_FIELDS = Collections.singletonList(Birth.FAMILY);

    private SkyeMTreeThresholdSiblingBundling(Path store_path, String repo_name) {

        super(store_path, repo_name);
    }

    protected void printHeader() {
        System.out.println("Sibling bundling using M-tree Levenshtein threshold " + MATCH_THRESHOLD + " from repository: " + repo_name);
    }

    protected Linker getLinker() {

        NamedMetric<LXP> metric = Utilities.weightedAverageLevenshteinOverBirths();

        SearchStructureFactory<LXP> factory = records -> new MTreeSearchStructure<>(metric, records);

        return new SimilaritySearchSiblingBundlerOverBirths(factory, MATCH_THRESHOLD, metric, NUMBER_OF_PROGRESS_UPDATES);
    }

    protected List<Integer> getSiblingGroundTruthFields() {

        return SIBLING_GROUND_TRUTH_FIELDS;
    }

    public static void main(String[] args) throws Exception {

        final Path store_path = ApplicationProperties.getStorePath();
        final String repository_name = "skye";

        new SkyeMTreeThresholdSiblingBundling(store_path, repository_name).run();
    }
}

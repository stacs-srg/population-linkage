package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.*;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.nio.file.Path;
import java.util.List;

public abstract class BitBlasterThresholdSiblingBundling extends SimilaritySearchThresholdSiblingBundling {

     BitBlasterThresholdSiblingBundling(Path store_path, String repo_name) {

        super(store_path, repo_name);
    }

    protected void printHeader() {
        System.out.println("Sibling bundling using BitBlaster Levenshtein threshold " + MATCH_THRESHOLD + " from repository: " + repo_name);
    }

    protected Linker getLinker() {

        NamedMetric<LXP> metric = Utilities.weightedAverageLevenshteinOverBirths();

        SearchStructureFactory<LXP> factory = (List<LXP> records) -> new BitBlasterSearchStructure<>(metric, records);

        return new SimilaritySearchSiblingBundlerOverBirths(factory, MATCH_THRESHOLD, metric, NUMBER_OF_PROGRESS_UPDATES);
    }
}

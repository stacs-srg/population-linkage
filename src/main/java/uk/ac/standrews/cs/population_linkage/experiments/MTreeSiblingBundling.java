package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.MTreeSearchStructure;
import uk.ac.standrews.cs.population_linkage.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.linkage.SimilaritySearchSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.nio.file.Path;

public abstract class MTreeSiblingBundling extends SimilaritySearchSiblingBundling {

     MTreeSiblingBundling(Path store_path, String repo_name) {

        super(store_path, repo_name);
    }

    protected void printHeader() {
        System.out.println("Sibling bundling using M-tree Levenshtein threshold " + MATCH_THRESHOLD + " from repository: " + repo_name);
    }

    protected Linker getLinker() {

        NamedMetric<LXP> metric = Utilities.weightedAverageLevenshteinOverBirths();

        SearchStructureFactory factory = records -> new MTreeSearchStructure<>(metric, records);

        return new SimilaritySearchSiblingBundlerOverBirths(factory, MATCH_THRESHOLD, metric, NUMBER_OF_PROGRESS_UPDATES);
    }
}

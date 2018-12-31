package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.linkage.SimilaritySearchSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.nio.file.Path;

public abstract class BitBlasterSiblingBundling extends SimilaritySearchSiblingBundling {

    private static final NamedMetric LINKAGE_METRIC = Utilities.JENSEN_SHANNON2;

    BitBlasterSiblingBundling(Path store_path, String repo_name) {

        super(store_path, repo_name);
    }

    protected void printHeader() {

        System.out.println("Sibling bundling using BitBlaster, " + LINKAGE_METRIC.getMetricName() + " with threshold " + MATCH_THRESHOLD + " from repository: " + repo_name);
    }

    protected Linker getLinker() {

        NamedMetric<LXP> metric = new Sigma(LINKAGE_METRIC, Utilities.BIRTH_MATCH_FIELDS);

        SearchStructureFactory<LXP> factory = (Iterable<LXP> records) -> new BitBlasterSearchStructure<>(metric, records);

        return new SimilaritySearchSiblingBundlerOverBirths(factory, MATCH_THRESHOLD, metric, NUMBER_OF_PROGRESS_UPDATES);
    }
}

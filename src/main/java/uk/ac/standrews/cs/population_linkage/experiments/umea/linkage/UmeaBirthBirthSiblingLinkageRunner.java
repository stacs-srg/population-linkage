package uk.ac.standrews.cs.population_linkage.experiments.umea.linkage;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.BitBlasterSearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Linkage;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.LinkageRunner;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.SimilaritySearchSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Sigma;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Linker;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class UmeaBirthBirthSiblingLinkageRunner extends LinkageRunner {

    protected Linkage getLinkage(final String links_persistent_name, final String gt_persistent_name, final String source_repository_name, final String results_repository_name, final RecordRepository record_repository) {
        return new UmeaBirthBirthSiblingLinkage(results_repository_name, links_persistent_name, gt_persistent_name, source_repository_name, record_repository);
    }

    protected Linker getLinker(final double match_threshold, final Metric<LXP> composite_metric, final SearchStructureFactory<LXP> search_factory) {
        return new SimilaritySearchSiblingBundlerOverBirths(search_factory, match_threshold, composite_metric, getNumberOfProgressUpdates());
    }

    protected Metric<LXP> getCompositeMetric(final Linkage linkage) {
        return new Sigma(getBaseMetric(), linkage.getLinkageFields1());
    }

    protected SearchStructureFactory<LXP> getSearchFactory(final Metric<LXP> composite_metric) {
        return new BitBlasterSearchStructureFactory<>(composite_metric);
    }

    public static void main(String[] args) {

        double match_threshold = 0.67;                          // from R metric power table [FRobustness2] - original 2.03 remapped to 0.67 by normalisation.

        new UmeaBirthBirthSiblingLinkageRunner().run("BirthBirthSiblingLinks", "BirthBirthSiblingGroundTruth", "umea", "umea_results", match_threshold);
    }
}

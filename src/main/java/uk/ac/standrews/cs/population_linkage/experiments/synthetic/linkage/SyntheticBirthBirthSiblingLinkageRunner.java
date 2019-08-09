package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.BruteForceSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Linker;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.experiments.umea.linkage.UmeaBirthBirthSiblingLinkageRunner;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class SyntheticBirthBirthSiblingLinkageRunner extends UmeaBirthBirthSiblingLinkageRunner {

    protected Linker getLinker(final double match_threshold, final Metric<LXP> composite_metric, final SearchStructureFactory<LXP> search_factory) {
        return new BruteForceSiblingBundlerOverBirths(composite_metric, match_threshold, super.getNumberOfProgressUpdates());
    }

    public static void main(String[] args) {
        double match_threshold = 0.67;                          // from R metric power table [FRobustness2] - original 2.03 remapped to 0.67 by normalisation.


        new SyntheticBirthBirthSiblingLinkageRunner().run("BirthBirthSiblingLinks", "BirthBirthSiblingGroundTruth", "scotland_3k_1_clean", "scotland_3k_1_clean_results", match_threshold);
    }

}

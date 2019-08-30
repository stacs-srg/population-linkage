package uk.ac.standrews.cs.population_linkage.experiments.linkageRunners;

import uk.ac.standrews.cs.population_linkage.experiments.LinkageRecipies.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.experiments.LinkageRecipies.BrideBirthIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.experiments.SearchStructures.BitBlasterSearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.experiments.SearchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.*;
import uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.LinkagePostFilter;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class BrideBirthIdentityLinkageRunner extends LinkageRunner {

    protected LinkageRecipe getLinkage(final String links_persistent_name, final String gt_persistent_name, final String source_repository_name, final String results_repository_name, final RecordRepository record_repository) {
        return new BrideBirthIdentityLinkageRecipe( results_repository_name,  links_persistent_name,  gt_persistent_name,  source_repository_name,  record_repository);
    }

    protected Linker getLinker(final double match_threshold, final Metric<LXP> composite_metric, final SearchStructureFactory<LXP> search_factory) {
        return new SimilaritySearchLinker(search_factory, composite_metric, match_threshold, getNumberOfProgressUpdates(),
                "birth-birth-sibling", "threshold match at " + match_threshold, Birth.ROLE_BABY, Birth.ROLE_BABY, LinkagePostFilter::isViableBBSiblingLink);
    }

    protected Metric<LXP> getCompositeMetric(final LinkageRecipe linkageRecipe) {
        return new Sigma2(getBaseMetric(), linkageRecipe.getLinkageFields1(), linkageRecipe.getLinkageFields2());
    }

    protected SearchStructureFactory<LXP> getSearchFactory(final Metric<LXP> composite_metric) {
        return new BitBlasterSearchStructureFactory<>(composite_metric, 70);
    }

    public static void main(String[] args) {

        double match_threshold = 0.67;                          // from R metric power table [FRobustness2] - original 2.03 remapped to 0.67 by normalisation.

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        new BrideBirthIdentityLinkageRunner().run("BrideBirthIdentityLinks",
                "BirthBirthIdentityGroundTruth", sourceRepo,
                resultsRepo, match_threshold, Constants.JENSEN_SHANNON,
                true, true, true, false);
    }
}

package uk.ac.standrews.cs.population_linkage.linkageRunners;

import uk.ac.standrews.cs.population_linkage.linkageRecipies.DeathGroomOwnMarriageIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipies.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.linkers.SimilaritySearchLinker;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkagePostFilter;
import uk.ac.standrews.cs.population_linkage.supportClasses.Sigma;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class DeathGroomOwnMarriageIdentityLinkageRunner extends LinkageRunner {

    public static final String linkageType = "death-groom-marriage-id";

    @Override
    public String getLinkageType() {
        return linkageType;
    }

    public LinkageRecipe getLinkageRecipe(final String links_persistent_name,
                                          final String source_repository_name, final String results_repository_name,
                                          final RecordRepository record_repository) {

        return new DeathGroomOwnMarriageIdentityLinkageRecipe(results_repository_name, links_persistent_name, source_repository_name, record_repository);
    }

    public Linker getLinker(final double match_threshold, LinkageRecipe linkageRecipe) {
        Metric<LXP> compositeMetric1 = getCompositeMetric(linkageRecipe);
        return new SimilaritySearchLinker(getSearchFactory(compositeMetric1), compositeMetric1, match_threshold, getNumberOfProgressUpdates(),
                linkageType, "threshold match at " + match_threshold, Death.ROLE_DECEASED, Marriage.ROLE_GROOM,  LinkagePostFilter::isViableDeathGroomIdentityLink, linkageRecipe);
    }

    protected Metric<LXP> getCompositeMetric(final LinkageRecipe linkageRecipe) {
        return new Sigma(getBaseMetric(), linkageRecipe.getLinkageFields());
    }

    protected SearchStructureFactory<LXP> getSearchFactory(final Metric<LXP> composite_metric) {
        return new BitBlasterSearchStructureFactory<>(composite_metric, LinkageConfig.numberOfROs);
    }

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        double match_threshold = 0.45;                          // from R metric power table [FRobustness2] - original 2.03 remapped to 0.67 by normalisation.

        LinkageConfig.numberOfROs = 10;

        new DeathGroomOwnMarriageIdentityLinkageRunner()
                .run("DeathGroomOwnMarriageIdentityLinks",
                        sourceRepo, resultsRepo,
                        match_threshold, new JensenShannon(2048),
                        true, false, true, 5, false, false);

    }
}

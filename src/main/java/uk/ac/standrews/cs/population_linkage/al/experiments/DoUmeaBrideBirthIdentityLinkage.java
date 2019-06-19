package uk.ac.standrews.cs.population_linkage.al.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.linkage.SimilaritySearchSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.nio.file.Path;

public class DoUmeaBrideBirthIdentityLinkage {

    public static void main(String[] args) throws Exception {

        final Path store_path = ApplicationProperties.getStorePath();

        final int NUMBER_OF_PROGRESS_UPDATES = 100;
        final double match_threshold = 2.03;                          // from R metric power table [FRobustness2).
        final StringMetric base_metric = Utilities.JACCARD;

        final String links_persistent_name = "BrideBirthSiblingLinks";
        final String gt_persistent_name = "BrideBirthSiblingGT";

        final String source_repository_name = "umea";
        final String results_repository_name = "umea_results";

        RecordRepository record_repository = new RecordRepository(store_path, source_repository_name);

        Linkage linkage = new UmeaBrideBirthIdentityLinkage( results_repository_name,  links_persistent_name,  gt_persistent_name,  source_repository_name,  record_repository);

        Metric<LXP> composite_metric = new Sigma(base_metric, linkage.getLinkageFields1());

        SearchStructureFactory<LXP> search_factory = new BitBlasterSearchStructureFactory<>(composite_metric);

        Linker linker = new SimilaritySearchSiblingBundlerOverBirths(search_factory, match_threshold, composite_metric, NUMBER_OF_PROGRESS_UPDATES);

        new LinkageFramework( linkage,linker ).link();
    }
}

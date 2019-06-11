package uk.ac.standrews.cs.population_linkage.al.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.linkage.SimilaritySearchSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.nio.file.Path;

public class DoUmeaBBSiblingLinkage {

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();

        int NUMBER_OF_PROGRESS_UPDATES = 100;
        double match_threshold = 2.03;                          // from R metric power table [FRobustness2).
        NamedMetric<String> base_metric = Utilities.JACCARD;

        String links_persistent_name = "BirthBirthSiblingLinks";
        String gt_persistent_name = "BirthBirthSiblingGT";

        String source_repository_name = "umea";
        String results_repository_name = "umea_results";

        RecordRepository record_repository = new RecordRepository(store_path, source_repository_name);

        Linkage linkage = new  UmeaBirthBirthSiblingLinkage( results_repository_name,  links_persistent_name,  gt_persistent_name,  source_repository_name,  record_repository);

        NamedMetric<LXP> composite_metric = new Sigma(base_metric, linkage.getLinkageFields1());

        SearchStructureFactory search_factory = new BitBlasterSearchStructureFactory( composite_metric );

        Linker linker = new SimilaritySearchSiblingBundlerOverBirths(search_factory, match_threshold, composite_metric, NUMBER_OF_PROGRESS_UPDATES);

        new LinkageFramework( linkage,linker ).link();
    }

}

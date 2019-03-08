package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This class performs linkage analysis on data pulled from a single data sources, for example births.
 */
public abstract class SymmetricSingleSourceLinkageAnalysis extends SingleSourceLinkageAnalysis {

    protected SymmetricSingleSourceLinkageAnalysis(final Path store_path, final String repo_name, final String linkage_results_filename, final String distance_results_filename, int number_of_records_to_be_checked, int number_of_runs) throws IOException {

        super(store_path,repo_name, linkage_results_filename,  distance_results_filename,  number_of_records_to_be_checked, number_of_runs );
    }

    protected List<NamedMetric<LXP>> getCombinedMetrics() {

        final List<NamedMetric<LXP>> result = new ArrayList<>();

        for (final NamedMetric<String> base_metric : Utilities.BASE_METRICS) {
            result.add(new Sigma(base_metric, getComparisonFields()));
        }
        return result;
    }
}

package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.Constants;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

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

    protected List<Metric<LXP>> getCombinedMetrics() {

        final List<Metric<LXP>> result = new ArrayList<>();

        for (final StringMetric base_metric : Constants.BASE_METRICS) {
            result.add(new Sigma(base_metric, getComparisonFields()));
        }
        return result;
    }
}

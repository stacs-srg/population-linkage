/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.compositeMetrics.Sigma2;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This class performs linkage analysis on data pulled from a single data sources, for example births.
 */
public abstract class AsymmetricSingleSourceLinkageAnalysis extends SingleSourceLinkageAnalysis {

    protected AsymmetricSingleSourceLinkageAnalysis(final String repo_name, final String linkage_results_filename, final String distance_results_filename, final int number_of_records_to_be_checked, final int number_of_runs, final boolean allow_multiple_links) throws IOException {

        super(repo_name, linkage_results_filename, distance_results_filename, number_of_records_to_be_checked, number_of_runs, allow_multiple_links);
    }

    protected abstract List<Integer> getComparisonFields2();

    protected abstract int getIdFieldIndex2();

    @Override
    public List<Metric<uk.ac.standrews.cs.neoStorr.impl.LXP>> getCombinedMetrics() {

        final List<Metric<LXP>> result = new ArrayList<>();

        for (final StringMetric base_metric : Constants.BASE_METRICS) {
            result.add(new Sigma2(base_metric, getComparisonFields(), getComparisonFields2(), getIdFieldIndex(), getIdFieldIndex2()));
        }
        return result;
    }
}

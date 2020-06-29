/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruthML;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author al
 */
public abstract class SymmetricSingleSourceWeightedLinkageAnalysis extends SingleSourceWeightedLinkageAnalysis {

    private final Metric<LXP>  metric;

    protected SymmetricSingleSourceWeightedLinkageAnalysis(final Path store_path, final String repo_name,
                                                           final String linkage_results_filename, final String distance_results_filename,
                                                           final int number_of_records_to_be_checked, final int number_of_runs,
                                                           final List<Integer> fields, final List<Metric> metrics, final List<Float> weights,
                                                           final boolean allow_multiple_links) throws IOException {

        super(store_path, repo_name, linkage_results_filename, distance_results_filename, number_of_records_to_be_checked, number_of_runs, allow_multiple_links);

        this.metric = new SigmaWeighted(fields, metrics, weights, getIdFieldIndex());
    }

    @Override
    public Metric<LXP> getMetric() {

        return metric;

    }
}

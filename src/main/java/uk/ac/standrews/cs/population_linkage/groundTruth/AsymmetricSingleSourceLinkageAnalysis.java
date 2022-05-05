/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.MeanOfFieldDistancesNormalised;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class performs linkage analysis on data pulled from a single data sources, for example births.
 */
public abstract class AsymmetricSingleSourceLinkageAnalysis extends SingleSourceLinkageAnalysis {

    protected AsymmetricSingleSourceLinkageAnalysis(final String repo_name, final String[] args, final String linkage_results_filename, final String distance_results_filename, final boolean allow_multiple_links) throws IOException {

        super(repo_name, args, linkage_results_filename, distance_results_filename, allow_multiple_links);
    }

    protected abstract List<Integer> getComparisonFields2();

    @Override
    public List<LXPMeasure> getCombinedMeasures() {

        final List<LXPMeasure> result = new ArrayList<>();

        for (final StringMeasure base_measure : Constants.BASE_MEASURES) {
            result.add(new MeanOfFieldDistancesNormalised(base_measure, getComparisonFields(), getComparisonFields2(), getNormalisationCutoff()));
        }

        return result;
    }
}

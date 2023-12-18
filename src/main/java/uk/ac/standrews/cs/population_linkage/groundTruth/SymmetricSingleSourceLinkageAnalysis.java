/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
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
public abstract class SymmetricSingleSourceLinkageAnalysis extends ThresholdAnalysis {

    protected SymmetricSingleSourceLinkageAnalysis(final String repo_name, final String[] args, final String linkage_results_filename, final String distance_results_filename, final boolean allow_multiple_links) throws IOException {

        super(repo_name, args, linkage_results_filename, distance_results_filename, allow_multiple_links);
    }

//    public List<LXPMeasure> getCombinedMeasures() {
//
//        final List<LXPMeasure> result = new ArrayList<>();
//
//        for (final StringMeasure base_measure : Constants.BASE_MEASURES) {
//            result.add(new MeanOfFieldDistancesNormalised(base_measure, getComparisonFields(), getNormalisationCutoff()));
//
//        }
//        return result;
//    }

    @Override
    public boolean singleSource() {
        return true;
    }
}

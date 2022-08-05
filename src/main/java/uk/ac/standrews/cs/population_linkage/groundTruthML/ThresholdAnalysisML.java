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
package uk.ac.standrews.cs.population_linkage.groundTruthML;

import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the base class for performing linkage analysis from ground truth.
 * Classes extending this class are required to implement the following method:
 *     getComparisonFields(), returns the set of fields to be used for distance comparison from the data source
 * Known subclasses are:
 * AllPairsSameSourceLinkageAnalysisML, which compares records from a single data source
 * TwoSourcesLinkageAnalysis, which compares records from two different data sources
 */

abstract class ThresholdAnalysisML {

    static final long SEED = 87626L;
    static final int NUMBER_OF_RUNS = 10;

    static final int NUMBER_OF_THRESHOLDS_SAMPLED = 101; // 0.01 granularity including 0.0 and 1.0.
    private static final double EPSILON = 0.00001;

    final List<Map<String, Sample[]>> linkage_results; // Maps from measure name to counts of TPFP etc.

    final List<StringMeasure> measures;

    public abstract List<Integer> getComparisonFields();

    ThresholdAnalysisML() {

        measures = Constants.BASE_MEASURES;
        linkage_results = initialiseState();
    }

    private List<Map<String, Sample[]>> initialiseState() {

        final List<Map<String, Sample[]>> result = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_RUNS; i++) {

            final Map<String, Sample[]> map = new HashMap<>();

            for (final StringMeasure measure : measures) {

                final Sample[] samples = new Sample[NUMBER_OF_THRESHOLDS_SAMPLED];
                for (int j = 0; j < NUMBER_OF_THRESHOLDS_SAMPLED; j++) {
                    samples[j] = new Sample();
                }

                map.put(measure.getMeasureName(), samples);
            }

            result.add(map);
        }
        return result;
    }

    static int thresholdToIndex(final double threshold) {

        return (int) (threshold * (NUMBER_OF_THRESHOLDS_SAMPLED - 1) + EPSILON);
    }

    static double indexToThreshold(final int index) {

        return (double) index / (NUMBER_OF_THRESHOLDS_SAMPLED - 1);
    }

    class Sample {

        int fp = 0;
        int tp = 0;
        int fn = 0;
        int tn = 0;
    }
}

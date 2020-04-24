/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage;

import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructure;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.ArrayList;
import java.util.List;

public class BitBlasterTest extends SimilaritySearchTest {

    // BitBlaster implementation assumes at least two reference points.
    private static final int MIN_NUMBER_OF_REFERENCE_POINTS = 2;
    private static final int MAX_NUMBER_OF_REFERENCE_POINTS = 30;
    private static final int NUMBER_OF_REFERENCE_POINTS_INCREMENT = 5;

    @Override
    SearchStructure<Point> getSearchStructure(Metric<Point> metric, List<Point> data_points, final List<Point> reference_points) {

        return new BitBlasterSearchStructure<>(metric, reference_points, data_points);
    }

    @Override
    List<Point> getReferencePoints(final List<Point> data_points, int number_of_reference_points) {

        return BitBlasterSearchStructure.chooseRandomReferencePoints(data_points, number_of_reference_points);
    }

    @Override
    List<Integer> getReferencePointsOptions(int number_of_data_points) {

        List<Integer> result = new ArrayList<>();

        for (int i = MIN_NUMBER_OF_REFERENCE_POINTS; i <= Math.min(MAX_NUMBER_OF_REFERENCE_POINTS, number_of_data_points); i += NUMBER_OF_REFERENCE_POINTS_INCREMENT) {
            result.add(i);
        }

        return result;
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage;

import uk.ac.standrews.cs.population_linkage.searchStructures.MTreeSearchStructure;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructure;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.Collections;
import java.util.List;

public class MTreeTest extends SimilaritySearchTest {

    @Override
    SearchStructure<Point> getSearchStructure(Metric<Point> metric, List<Point> data_points, List<Point> reference_points) {

        return new MTreeSearchStructure<>(metric, data_points);
    }

    @Override
    List<Point> getReferencePoints(final List<Point> data_points, final int number_of_reference_points) {
        return null;
    }

    @Override
    List<Integer> getReferencePointsOptions(int number_of_data_points) {
        return Collections.singletonList(0);
    }
}

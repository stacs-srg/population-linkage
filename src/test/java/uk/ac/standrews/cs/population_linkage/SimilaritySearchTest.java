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
package uk.ac.standrews.cs.population_linkage;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructure;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.Measure;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.fail;

public abstract class SimilaritySearchTest {

    private static final double MAX_SIDE_OF_SQUARE = 10.0;
    private static final double MAX_THRESHOLD = 12.0;

    private Measure<Point> measure;

    @Before
    public void setup() {

        measure = new Measure<>() {

            @Override
            public String getMeasureName() {
                return "2D Euclidean";
            }

            @Override
            public boolean maxDistanceIsOne() {
                return false;
            }

            @Override
            public double calculateDistance(final Point p1, final Point p2) {

                double delta_x = p1.x - p2.x;
                double delta_y = p1.y - p2.y;

                return normaliseArbitraryPositiveDistance(Math.sqrt(delta_x * delta_x + delta_y * delta_y));
            }
        };
    }

    abstract SearchStructure<Point> getSearchStructure(Measure<Point> measure, List<Point> data_points, List<Point> reference_points);

    abstract List<Point> getReferencePoints(List<Point> data_points, int number_of_reference_points);

    abstract List<Integer> getReferencePointsOptions(int number_of_data_points);

    @Test
    public void similaritySearchGivesCorrectResults() {

        for (double side_of_square = 1.0; side_of_square <= MAX_SIDE_OF_SQUARE; side_of_square += 2) {
            for (double threshold = 0.0; threshold <= MAX_THRESHOLD; threshold ++) {

                for (Point query : getQueryPoints()) {

                    List<Point> data_points = generatePointGrid(side_of_square);

                    for (int number_of_reference_points : getReferencePointsOptions(data_points.size())) {

                        final List<Point> reference_points = getReferencePoints(data_points, number_of_reference_points);
                        check(data_points, reference_points, query, threshold);
                    }
                }
            }
        }
    }

    private List<Point> getQueryPoints() {

        List<Point> results = generatePointGrid(MAX_SIDE_OF_SQUARE);
        List<Point> extras = new ArrayList<>();

        for (Point point : results) {
            extras.add(new Point(point.x + 0.1, point.y));
        }

        results.addAll(extras);

        return results;
    }

    private void check(final List<Point> data_points, final List<Point> reference_points, final Point query, final double threshold) {

        final SearchStructure<Point> search_structure = getSearchStructure(measure, data_points, reference_points);

        final List<Point> ground_truth = bruteForceQuery(data_points, query, threshold);
        final List<Point> query_results = getPoints(search_structure.findWithinThreshold(query, threshold));

        search_structure.terminate();

        if (!checkSamePoints(ground_truth, query_results)) {

            StringBuilder builder = new StringBuilder();
            builder.append("mismatch for:\n");
            builder.append("data points: ").append(print(data_points)).append("\n");
            if (reference_points != null) System.out.println("reference points: " + print(reference_points) + "\n");
            builder.append("query: ").append(query).append("\n");
            builder.append("threshold: ").append(threshold).append("\n");
            builder.append("expected: ").append(print(ground_truth)).append("\n");
            builder.append("actual: ").append(print(query_results)).append("\n");

            fail(builder.toString());
        }
    }

    private List<Point> getPoints(final List<DataDistance<Point>> data_distances) {

        List<Point> results = new ArrayList<>();
        for (DataDistance<Point> data_distance : data_distances) results.add(data_distance.value);
        return results;
    }

    private String print(List<Point> points) {

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Point point : points) {
            if (builder.length() > 1) builder.append(", ");
            builder.append(point);
        }
        builder.append("]");

        return builder.toString();
    }

    private List<Point> bruteForceQuery(final List<Point> points, final Point query, final double threshold) {

        List<Point> results = new ArrayList<>();

        for (Point point : points) {

            if (measure.distance(point, query) <= threshold) {
                results.add(point);
            }
        }

        return results;
    }

    private boolean checkSamePoints(final List<Point> ground_truth, final List<Point> results) {

        if (ground_truth.size() != results.size()) return false;

        for (Point point : results) {
            if (!ground_truth.contains(point)) return false;
        }

        for (Point point : ground_truth) {
            if (!results.contains(point)) return false;
        }
        return true;
    }

    private List<Point> generatePointGrid(final double side_of_square) {

        List<Point> points = new ArrayList<>();

        double min_coord = -(side_of_square / 2);

        for (double x = min_coord; x <= min_coord + side_of_square; x++) {
            for (double y = min_coord; y <= min_coord + side_of_square; y++) {
                points.add(new Point(x, y));
            }
        }

        return points;
    }

    class Point {

        private static final double DELTA = 0.0000001;
        double x;
        double y;

        Point(final double x, final double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof Point && Math.abs(((Point) obj).x - x) < DELTA && Math.abs(((Point) obj).y - y) < DELTA;
        }

        @Override
        public String toString() {
            return "(" + clean(x) + ", " + clean(y) + ")";
        }

        private double clean(double d) {
            return Math.abs(d) < DELTA ? 0.0 : d;
        }
    }
}

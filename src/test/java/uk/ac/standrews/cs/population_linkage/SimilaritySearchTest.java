package uk.ac.standrews.cs.population_linkage;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.ArrayList;
import java.util.List;

public abstract class SimilaritySearchTest {

    private static final double MAX_SIDE_OF_SQUARE = 10.0;
    private static final double MAX_THRESHOLD = 12.0;

    private NamedMetric<Point> metric;
    private int check_count = 0;

    @Before
    public void setup() {

        metric = new NamedMetric<Point>() {

            @Override
            public String getMetricName() {
                return "2D Euclidean";
            }

            @Override
            public double distance(final Point p1, final Point p2) {

                double delta_x = p1.x - p2.x;
                double delta_y = p1.y - p2.y;

                return Math.sqrt(delta_x * delta_x + delta_y * delta_y);
            }
        };
    }

    abstract SearchStructure<Point> getSearchStructure(NamedMetric<Point> metric, List<Point> data_points, List<Point> reference_points);

    abstract List<Point> getReferencePoints(List<Point> data_points, int number_of_reference_points);

    abstract List<Integer> getReferencePointsOptions(int number_of_data_points);

    @Test
    public void similaritySearchGivesCorrectResults() {

        for (double side_of_square = 0.0; side_of_square <= MAX_SIDE_OF_SQUARE; side_of_square++) {
            for (double threshold = 0.0; threshold <= MAX_THRESHOLD; threshold += 0.5) {

                for (Point query : getQueryPoints()) {

                    List<Point> data_points = generatePointGrid(side_of_square);

                    for (int number_of_reference_points : getReferencePointsOptions(data_points.size())) {

                        final List<Point> reference_points = getReferencePoints(data_points, number_of_reference_points);
                        check(data_points, reference_points, query, threshold);
                    }
                }
            }
        }

        System.out.println("checks: " + check_count);
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

        final SearchStructure<Point> search_structure = getSearchStructure(metric, data_points, reference_points);

        final List<Point> ground_truth = bruteForceQuery(data_points, query, threshold);
        final List<Point> query_results = getPoints(search_structure.findWithinThreshold(query, threshold));

        if (!checkSamePoints(ground_truth, query_results)) {

            System.out.println("mismatch for:");
            System.out.println("data points: " + print(data_points));
            System.out.println("reference points: " + print(reference_points));
            System.out.println("query: " + query);
            System.out.println("threshold: " + threshold);
            System.out.println("expected: " + print(ground_truth));
            System.out.println("actual: " + print(query_results));
            System.out.println();
        }

        check_count++;
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

            if (metric.distance(point, query) <= threshold) {
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

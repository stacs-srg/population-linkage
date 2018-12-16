package uk.ac.standrews.cs.population_linkage;

import uk.ac.standrews.cs.population_linkage.linkage.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.ArrayList;
import java.util.List;

public class BitBlasterTest extends SimilaritySearchTest {

    private int MAX_NUMBER_OF_REFERENCE_POINTS = 30;

    @Override
    SearchStructure<Point> getSearchStructure(NamedMetric<Point> metric, List<Point> data_points, final List<Point> reference_points) {

        return new BitBlasterSearchStructure<>(metric, reference_points, data_points);
    }

    @Override
    List<Point> getReferencePoints(final List<Point> data_points, int number_of_reference_points) {

        List<Point> results = BitBlasterSearchStructure.chooseRandomReferencePoints(data_points, (number_of_reference_points + 1) / 2);
        List<Point> extras = new ArrayList<>();

        for (Point point : results) {
            if (results.size() + extras.size() < number_of_reference_points) {
                extras.add(new Point(point.x + 0.1, point.y));
            }
        }

        results.addAll(extras);
        return results;
    }

    @Override
    List<Integer> getReferencePointsOptions(int number_of_data_points) {

        List<Integer> result = new ArrayList<>();

        for (int i = 1; i <= Math.min(MAX_NUMBER_OF_REFERENCE_POINTS, number_of_data_points); i++) {
            result.add(i);
        }

        return result;
    }
}

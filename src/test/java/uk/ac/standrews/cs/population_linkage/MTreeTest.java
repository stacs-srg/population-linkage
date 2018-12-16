package uk.ac.standrews.cs.population_linkage;

import uk.ac.standrews.cs.population_linkage.linkage.MTreeSearchStructure;
import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.Arrays;
import java.util.List;

public class MTreeTest extends SimilaritySearchTest {

    @Override
    SearchStructure<Point> getSearchStructure(NamedMetric<Point> metric, List<Point> data_points, List<Point> reference_points) {

        return  new MTreeSearchStructure<Point>(metric, data_points);
    }

    @Override
    List<Point> getReferencePoints(final List<Point> data_points, final int number_of_reference_points) {
        return null;
    }

    @Override
    List<Integer> getReferencePointsOptions(int number_of_data_points) {
        return Arrays.asList(0);
    }
}

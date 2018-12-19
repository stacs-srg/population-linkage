package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;
import uk.al_richard.metricbitblaster.production.ParallelBitBlaster2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BitBlasterSearchStructure<T> implements SearchStructure<T> {

    private static final int DEFAULT_NUMBER_OF_REFERENCE_POINTS = 20;
    private static final long SEED = 34258723425L;
    private ParallelBitBlaster2<T> bit_blaster;

    public BitBlasterSearchStructure(NamedMetric<T> distance_metric, List<T> data) {

        init(distance_metric, chooseRandomReferencePoints(data, DEFAULT_NUMBER_OF_REFERENCE_POINTS), data);
    }

    public BitBlasterSearchStructure(NamedMetric<T> distance_metric, List<T> reference_points, List<T> data) {

        init(distance_metric, reference_points, data);
    }

    public void terminate() {
        bit_blaster.terminate();
    }

    private void init(final NamedMetric<T> distance_metric, final List<T> reference_points, final List<T> data) {

        try {
            bit_blaster = new ParallelBitBlaster2<>(distance_metric::distance, reference_points, data, 2, true);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DataDistance<T>> findWithinThreshold(final T record, final double threshold) {

        try {
            return bit_blaster.rangeSearch(record, threshold);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <X> List<X> chooseRandomReferencePoints(final List<X> data, int number_of_reference_points) {

        if (number_of_reference_points >= data.size()) {
            return data;
        }

        List<X> reference_points = new ArrayList<>();
        Random random = new Random(SEED);

        while (reference_points.size() < number_of_reference_points) {
            X item = data.get(random.nextInt(data.size()));
            if (!reference_points.contains(item)) {
                reference_points.add(item);
            }
        }

        return reference_points;
    }
}

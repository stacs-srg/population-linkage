package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.al_richard.metricbitblaster.production.ParallelBitBlaster;
import uk.al_richard.metricbitblaster.production.ParallelBitBlaster2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BitBlasterSearchStructure<T> implements SearchStructure<T> {

    // TOM - was 20
    private static final int DEFAULT_NUMBER_OF_REFERENCE_POINTS = 70;
    private static final long SEED = 34258723425L;
    private ParallelBitBlaster2<T> bit_blaster;

    public BitBlasterSearchStructure(Metric<T> distance_metric, Iterable<T> data) {
        this(distance_metric, data, DEFAULT_NUMBER_OF_REFERENCE_POINTS);
    }

    public BitBlasterSearchStructure(Metric<T> distance_metric, Iterable<T> data, int numberOfReferenceObjects) {
        List<T> copy_of_data = copyData(data);

        init(distance_metric, chooseRandomReferencePoints(copy_of_data, numberOfReferenceObjects), copy_of_data);
    }

    public BitBlasterSearchStructure(Metric<T> distance_metric, List<T> reference_points, Iterable<T> data) {

        init(distance_metric, reference_points, copyData(data));
    }

    public void terminate() {
        bit_blaster.terminate();
    }

    private void init(final Metric<T> distance_metric, final List<T> reference_points, final List<T> data) {

        try {
            bit_blaster = new ParallelBitBlaster2<>(distance_metric::distance, reference_points, data, 2, Runtime.getRuntime().availableProcessors(), true, true);
//            bit_blaster = new ParallelBitBlaster<>(distance_metric::distance, reference_points, data, 2, true);

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

    private static <X> List<X> copyData(final Iterable<X> data) {

        List<X> copy_of_data = new ArrayList<>();

        for (X x : data) {
            copy_of_data.add(x);
        }
        return copy_of_data;
    }

    public static <X> List<X> chooseRandomReferencePoints(final List<X> data, int number_of_reference_points) {

        Random random = new Random(SEED);
        List<X> reference_points = new ArrayList<>();

        if (number_of_reference_points >= data.size()) {
            return data;
        }

        while (reference_points.size() < number_of_reference_points) {
            X item = data.get(random.nextInt(data.size()));
            if (!reference_points.contains(item)) {
                reference_points.add(item);
            }
        }

        return reference_points;
    }
}

package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;
import uk.al_richard.metricbitblaster.production.ParallelBitBlaster2;

import java.util.*;

public class BitBlasterSearchStructure<T> implements SearchStructure<T> {

    private static final int DEFAULT_NUMBER_OF_REFERENCE_POINTS = 20;
    private static final long SEED = 34258723425L;
    private ParallelBitBlaster2<T> bit_blaster;

    public BitBlasterSearchStructure(NamedMetric<T> distance_metric, Iterable<T> data) {

        System.out.println("bbss1");

        List<T> copy_of_data = copyData(data);
        System.out.println("bbss2");
        init(distance_metric, chooseRandomReferencePoints(copy_of_data, DEFAULT_NUMBER_OF_REFERENCE_POINTS), copy_of_data);
        System.out.println("bbss3");
    }

    public BitBlasterSearchStructure(NamedMetric<T> distance_metric, List<T> reference_points, Iterable<T> data) {

        init(distance_metric, reference_points, copyData(data));
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

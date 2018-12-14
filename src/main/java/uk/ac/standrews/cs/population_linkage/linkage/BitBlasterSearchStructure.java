package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;
import uk.al_richard.metricbitblaster.MetricBitBlaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BitBlasterSearchStructure<T extends LXP> implements SearchStructure<T> {

    private static final int NUMBER_OF_REFERENCE_POINTS = 20;
    private MetricBitBlaster<T> bit_blaster;

    public BitBlasterSearchStructure(NamedMetric<T> distance_metric, List<T> data) {

        init(distance_metric, chooseRandomReferencePoints(data), data);
    }

    public BitBlasterSearchStructure(NamedMetric<T> distance_metric, List<T> reference_points, List<T> data) {

        init(distance_metric, reference_points, data);
    }

    private void init(final NamedMetric<T> distance_metric, final List<T> reference_points, final List<T> data) {

        bit_blaster = new MetricBitBlaster<>(distance_metric::distance, reference_points, data, false, false);
    }

    @Override
    public List<DataDistance<T>> findWithinThreshold(final T record, final double threshold) {

        return bit_blaster.rangeSearch(record, threshold);
    }

    private List<T> chooseRandomReferencePoints(final List<T> data) {

        List<T> reference_points = new ArrayList<>();
        Random random = new Random();

        while (reference_points.size() < NUMBER_OF_REFERENCE_POINTS) {
            reference_points.add(data.get(random.nextInt(data.size())));
        }

        return reference_points;
    }
}

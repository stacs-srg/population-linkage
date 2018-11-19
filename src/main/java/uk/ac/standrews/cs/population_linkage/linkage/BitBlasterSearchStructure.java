package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.storr.impl.LXP;

public abstract class BitBlasterSearchStructure<T extends LXP> implements SearchStructure<T> {

//    MetricBitBlaster<T> bit_blaster;
//
//    public BitBlasterSearchStructure(NamedMetric<T> distance_metric, List<T> reference_points, List<T> data) {
//
//        bit_blaster = new MetricBitBlaster<>(distance_metric::distance, reference_points, data, false, false);
//    }
//
//    @Override
//    public List<DataDistance<T>> findNearest(T record, int number_of_records) {
//
//        List<DataDistance<T>> results;
//        double initial_threshold = 1; // TODO These numbers are mad - need to tie to Metric?
//
//        do {
//            initial_threshold += 2;
//            results = bit_blaster.rangeSearch(record, initial_threshold);
//        }
//        while (results.size() < number_of_records);
//
//        return results.subList(0, number_of_records);
//    }
}

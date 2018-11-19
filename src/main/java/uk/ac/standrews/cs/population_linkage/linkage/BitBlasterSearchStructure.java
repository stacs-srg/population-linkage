package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;
import uk.al_richard.metricbitblaster.MetricBitBlaster;

import java.util.List;

public class BitBlasterSearchStructure<T extends LXP> implements SearchStructure<T> {

    MetricBitBlaster<T> bb;

    public BitBlasterSearchStructure(NamedMetric<T> distance_metric, List<T> refs, List<T> dat ) {

        bb = new MetricBitBlaster<>(distance_metric::distance, refs, dat, false,false );
    }

    @Override
    public void add(T record) {

       // bb.add(record);
    }

    @Override
    public List<DataDistance<T>> findNearest(T record, int number_of_records) {

       // return bb.nearestN(record, number_of_records);

        List<DataDistance<T>> results;
        double initial_threshold = 1; // TODO These numbers are mad - need to tie to Metric?

        do {
            initial_threshold += 2;
            results = bb.rangeSearch(record, initial_threshold);
        }
        while( results.size() < number_of_records ) ;

        return results.subList(0,number_of_records);
    }
}

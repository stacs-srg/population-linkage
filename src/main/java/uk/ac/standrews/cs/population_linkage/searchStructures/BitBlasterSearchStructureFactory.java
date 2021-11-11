/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.searchStructures;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.List;

public class BitBlasterSearchStructureFactory<T> implements SearchStructureFactory<T> {

    private final Metric<T> composite_metric;
    private List<T> reference_points;

    public BitBlasterSearchStructureFactory(Metric<T> composite_metric, List<T> reference_points) {
        this.composite_metric = composite_metric;
        this.reference_points = reference_points;
    }

//    @Override
//    public SearchStructure<T> newSearchStructure(final Iterable<T> records) {
//        return new BitBlasterSearchStructure<>(composite_metric, records, numberOfReferenceObjects);
//    }

    public SearchStructure<T> newSearchStructure(final Iterable<T> records, List<T> reference_points) {
        return new BitBlasterSearchStructure<>(composite_metric, reference_points, records);
    }

    @Override
    public String getSearchStructureType() {
        return "BitBlaster";
    }
}

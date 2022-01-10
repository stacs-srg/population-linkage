/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.searchStructures;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.List;

public class BitBlasterSearchStructureFactory<T> implements SearchStructureFactory<T> {

    private final Metric<T> composite_metric;

    public BitBlasterSearchStructureFactory(Metric<T> composite_metric) {

        this.composite_metric = composite_metric;
    }

    @Override
    public SearchStructure<T> newSearchStructure(final Iterable<T> records) {
        return new BitBlasterSearchStructure<>(composite_metric, records);
    }

    public SearchStructure<T> newSearchStructure(final Iterable<T> records, final List<T> reference_objects) {
        return new BitBlasterSearchStructure<>(composite_metric, records, reference_objects);
    }

    @Override
    public String getSearchStructureType() {
        return "BitBlaster";
    }
}

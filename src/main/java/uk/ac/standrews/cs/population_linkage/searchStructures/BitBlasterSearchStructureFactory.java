/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.searchStructures;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class BitBlasterSearchStructureFactory<T> implements SearchStructureFactory<T> {

    private final Metric<T> composite_metric;
    private int numberOfReferenceObjects;

    public BitBlasterSearchStructureFactory(Metric<T> composite_metric, int numberOfReferenceObjects) {
        this.composite_metric = composite_metric;
        this.numberOfReferenceObjects = numberOfReferenceObjects;
    }

    @Override
    public SearchStructure<T> newSearchStructure(final Iterable<T> records) {
        return new BitBlasterSearchStructure<>(composite_metric, records, numberOfReferenceObjects);
    }

    @Override
    public String getSearchStructureType() {
        return "BitBlaster";
    }
}

/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.searchStructures;

import uk.ac.standrews.cs.utilities.measures.coreConcepts.Measure;

import java.util.List;

public class BitBlasterSearchStructureFactory<T> implements SearchStructureFactory<T> {

    private final Measure<T> composite_measure;

    public BitBlasterSearchStructureFactory(Measure<T> composite_measure) {

        this.composite_measure = composite_measure;
    }

    @Override
    public SearchStructure<T> newSearchStructure(final Iterable<T> records) {
        return new BitBlasterSearchStructure<>(composite_measure, records);
    }

    public SearchStructure<T> newSearchStructure(final Iterable<T> records, final List<T> reference_objects) {
        return new BitBlasterSearchStructure<>(composite_measure, records, reference_objects);
    }

    @Override
    public String getSearchStructureType() {
        return "BitBlaster";
    }
}

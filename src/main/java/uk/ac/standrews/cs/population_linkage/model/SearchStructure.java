package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;

import java.util.List;

public interface SearchStructure<T> {

    List<DataDistance<T>> findWithinThreshold(T record, double threshold);
    void terminate();
}

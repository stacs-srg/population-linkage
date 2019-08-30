package uk.ac.standrews.cs.population_linkage.experiments.SearchStructures;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;

import java.util.List;

public interface SearchStructure<T> {

    List<DataDistance<T>> findWithinThreshold(T record, double threshold);
    void terminate();
}

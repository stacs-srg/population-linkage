package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;

import java.util.List;

public interface SearchStructure<T extends LXP> {

    List<DataDistance<T>> findNearest(T record, int number_of_records);
}

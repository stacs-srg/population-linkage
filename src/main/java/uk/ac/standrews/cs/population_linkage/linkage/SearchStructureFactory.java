package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.SearchStructure;

public interface SearchStructureFactory<T> {

     SearchStructure<T> newSearchStructure(Iterable<T> records);
}

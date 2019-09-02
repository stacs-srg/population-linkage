package uk.ac.standrews.cs.population_linkage.searchStructures;

public interface SearchStructureFactory<T> {

    SearchStructure<T> newSearchStructure(Iterable<T> records);

    String getSearchStructureType();
}

package uk.ac.standrews.cs.population_linkage.experiments.SearchStructures;

public interface SearchStructureFactory<T> {

    SearchStructure<T> newSearchStructure(Iterable<T> records);

    String getSearchStructureType();
}

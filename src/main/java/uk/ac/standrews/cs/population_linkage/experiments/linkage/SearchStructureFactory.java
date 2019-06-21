package uk.ac.standrews.cs.population_linkage.experiments.linkage;

public interface SearchStructureFactory<T> {

    SearchStructure<T> newSearchStructure(Iterable<T> records);

    String getSearchStructureType();
}

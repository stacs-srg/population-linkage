package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.SearchStructure;

import java.util.List;

public interface SearchStructureFactory<T> {

     SearchStructure<T> newSearchStructure(List<T> records);
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.searchStructures;

public interface SearchStructureFactory<T> {

    SearchStructure<T> newSearchStructure(Iterable<T> records);

    String getSearchStructureType();
}

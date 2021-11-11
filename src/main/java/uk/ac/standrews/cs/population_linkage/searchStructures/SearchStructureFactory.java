/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.searchStructures;

import java.util.List;

public interface SearchStructureFactory<T> {

    public String getSearchStructureType();

    SearchStructure<T> newSearchStructure(Iterable<T> storedSet, List<T> reference_objects);
}

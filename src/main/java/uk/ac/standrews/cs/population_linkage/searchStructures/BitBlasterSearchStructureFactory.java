/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.searchStructures;

import uk.ac.standrews.cs.utilities.measures.coreConcepts.Measure;

import java.util.List;

public class BitBlasterSearchStructureFactory<T> implements SearchStructureFactory<T> {

    private final Measure<T> composite_measure;

    public BitBlasterSearchStructureFactory(Measure<T> composite_measure) {

        this.composite_measure = composite_measure;
    }

    @Override
    public SearchStructure<T> newSearchStructure(final Iterable<T> records) {
        return new BitBlasterSearchStructure<>(composite_measure, records);
    }

    public SearchStructure<T> newSearchStructure(final Iterable<T> records, final List<T> reference_objects) {
        return new BitBlasterSearchStructure<>(composite_measure, records, reference_objects);
    }

    @Override
    public String getSearchStructureType() {
        return "BitBlaster";
    }
}

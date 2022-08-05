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

import uk.ac.standrews.cs.utilities.m_tree.MTree;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.Measure;

import java.util.List;

public class MTreeSearchStructure<T> implements SearchStructure<T> {

    private MTree<T> m_tree;

    public MTreeSearchStructure(Measure<T> measure, Iterable<T> records) {

        m_tree = new MTree<>(measure);
        for (T record : records) {
            m_tree.add(record);
        }
    }

    @Override
    public List<DataDistance<T>> findWithinThreshold(final T record, final double threshold) {

        return m_tree.rangeSearch(record, threshold);
    }

    public void terminate() {}
}

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
package uk.ac.standrews.cs.population_linkage;

import uk.ac.standrews.cs.population_linkage.searchStructures.MTreeSearchStructure;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructure;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.Measure;

import java.util.Collections;
import java.util.List;

public class MTreeTest extends SimilaritySearchTest {

    @Override
    SearchStructure<Point> getSearchStructure(Measure<Point> measure, List<Point> data_points, List<Point> reference_points) {

        return new MTreeSearchStructure<>(measure, data_points);
    }

    @Override
    List<Point> getReferencePoints(final List<Point> data_points, final int number_of_reference_points) {
        return null;
    }

    @Override
    List<Integer> getReferencePointsOptions(int number_of_data_points) {
        return Collections.singletonList(0);
    }
}

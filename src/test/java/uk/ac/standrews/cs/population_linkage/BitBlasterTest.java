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

import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructure;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.Measure;

import java.util.ArrayList;
import java.util.List;

public class BitBlasterTest extends SimilaritySearchTest {

    // BitBlaster implementation assumes at least two reference points.
    private static final int MIN_NUMBER_OF_REFERENCE_POINTS = 2;
    private static final int MAX_NUMBER_OF_REFERENCE_POINTS = 30;
    private static final int NUMBER_OF_REFERENCE_POINTS_INCREMENT = 5;

    @Override
    SearchStructure<Point> getSearchStructure(Measure<Point> measure, List<Point> data_points, final List<Point> reference_points) {

        return new BitBlasterSearchStructure<>(measure, data_points, reference_points);
    }

    @Override
    List<Point> getReferencePoints(final List<Point> data_points, int number_of_reference_points) {

        return BitBlasterSearchStructure.chooseRandomReferencePoints(data_points, number_of_reference_points);
    }

    @Override
    List<Integer> getReferencePointsOptions(int number_of_data_points) {

        List<Integer> result = new ArrayList<>();

        for (int i = MIN_NUMBER_OF_REFERENCE_POINTS; i <= Math.min(MAX_NUMBER_OF_REFERENCE_POINTS, number_of_data_points); i += NUMBER_OF_REFERENCE_POINTS_INCREMENT) {
            result.add(i);
        }

        return result;
    }
}

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
package uk.ac.standrews.cs.population_linkage.groundTruthML;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.List;

/**
 * Defines a distance function between LXP records as the weighted sum of the distances between values of specified fields,
 * with field distance measures specified per-field.
 */
public class SumOfFieldDistancesWeighted extends LXPMeasure {

    final double EPSILON = 1.0E-6;

    private final String name;
    private final List<StringMeasure> measures;
    private final List<Float> weights;

    public SumOfFieldDistancesWeighted(final List<Integer> field_list, final List<StringMeasure> measures, final List<Float> weights) {

        this(field_list, field_list, measures, weights);
    }

    public SumOfFieldDistancesWeighted(final List<Integer> field_list1, final List<Integer> field_list2, final List<StringMeasure> measures, final List<Float> weights) {

        super(null, field_list1, field_list2);

        if (!weightsSumToOne(weights)) throw new RuntimeException("weights must sum to one");

        name = combineNames();
        this.measures = measures;
        this.weights = weights;
    }

    @Override
    public String getMeasureName() {
        return name;
    }

    @Override
    public boolean maxDistanceIsOne() {
        return base_measure.maxDistanceIsOne();
    }

    @Override
    public double calculateDistance(final LXP x, final LXP y) {

        double total_distance = 0.0d;

        for (int i = 0; i < field_list1.size(); i++) {

            try {
                final int field_index1 = field_list1.get(i);
                final int field_index2 = field_list2.get(i);

                final String field_value1 = x.getString(field_index1);
                final String field_value2 = y.getString(field_index2);

                total_distance += measures.get(i).distance(field_value1, field_value2) * weights.get(i);

            } catch (Exception e) {
                throwExceptionWithDebug(x, y, i, e);
            }
        }

        return total_distance;
    }

    private String combineNames() {

        StringBuilder sb = new StringBuilder();
        sb.append("Weighted:");
        for (int i = 0; i < field_list1.size(); i++) {
            sb.append(measures.get(i).getMeasureName());
            sb.append(":");
            sb.append(String.format("%.2f", weights.get(i)));
            sb.append("+");
        }
        return sb.subSequence(0, sb.length() - 1).toString();
    }

    private boolean weightsSumToOne(List<Float> weights) {

        final var sum = weights.stream().mapToDouble(Float::doubleValue).sum();
        return Math.abs(sum - 1d) <= EPSILON;
    }
}

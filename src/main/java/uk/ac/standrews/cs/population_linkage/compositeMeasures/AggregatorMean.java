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
package uk.ac.standrews.cs.population_linkage.compositeMeasures;

import java.util.List;

public class AggregatorMean extends Aggregator {

    List<Double> weights;

    public AggregatorMean() {
        this(null);
    }

    public AggregatorMean(List<Double> weights) {
        this.weights = weights;
    }

    @Override
    public double aggregate(List<Double> values) {

        if (weights == null) {
            return values.stream().mapToDouble(Double::doubleValue).sum() / values.size();
        }
        else {
            if (values.size() != weights.size()) throw new RuntimeException("weighted mean: inconsistent number of weights");

            double result = 0;
            for (int i = 0; i < values.size(); i++)
                result += values.get(i) * weights.get(i);

            return result;
        }
    }
}

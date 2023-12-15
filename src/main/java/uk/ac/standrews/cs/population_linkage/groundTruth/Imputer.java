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
package uk.ac.standrews.cs.population_linkage.groundTruth;

import java.util.List;

public enum Imputer {

    RECORD_MEAN(new AggregatorMean()),

    RECORD_MEDIAN(new AggregatorMedian()),

    RECORD_MAX(new AggregatorMean()),

    ZERO(0),
    ONE(1),
    MAX_DOUBLE(Double.MAX_VALUE);

    final Aggregator aggregator;

    Imputer(Aggregator aggregator) {
        this.aggregator = aggregator;
    }

    Imputer(double fixed_value) {
        this(new Aggregator() {
            @Override
            public double aggregate(List<Double> values) {
                return fixed_value;
            }
        });
    }

    public double impute(List<Double> non_missing_values) {
        return aggregator.aggregate(non_missing_values);
    }
    //record mean/median/max, population mean/median/max
}

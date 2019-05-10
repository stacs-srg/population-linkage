/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module linkage-java.
 *
 * linkage-java is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * linkage-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with linkage-java. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.metrics;


import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.List;

/**
 * Sigma function for combining metrics  - compares different field lists over data
 * Created by al on 13/12/18
 */
public class Sigma2 implements NamedMetric<LXP> {

    final NamedMetric<String> baseDistance;
    List<Integer> fieldList1;
    List<Integer> fieldList2;

    public Sigma2(NamedMetric<String> baseDistance, List<Integer> fields1, List<Integer> fields2) {

        if (fields1.size() != fields2.size()) {
            throw new RuntimeException("Field lists must be the same length");
        }
        this.baseDistance = baseDistance;
        this.fieldList1 = fields1;
        this.fieldList2 = fields2;
    }

    @Override
    public double distance(LXP a, LXP b) {

        double total_distance = 0.0d;

        for (int i = 0; i < fieldList1.size(); i++) {
            int field1_index = fieldList1.get(i);
            int field2_index = fieldList2.get(i);

            double f_distance = baseDistance.distance(a.getString(field1_index), b.getString(field2_index));
            total_distance += f_distance;
        }

        return total_distance;
    }

    @Override
    public String getMetricName() {
        return "Sigma2" + "-" + baseDistance.getMetricName() + "-" + hyphenConcat(fieldList1) + "--" + hyphenConcat(fieldList2);
    }

    static String hyphenConcat(List<Integer> fieldList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldList.size() - 1; i++) {
            sb.append(fieldList.get(i));
            sb.append("-");
        }
        sb.append(fieldList.get(fieldList.size() - 1));
        return sb.toString();
    }
}

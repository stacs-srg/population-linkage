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
 * Sigma function for combining metrics - compares a single set of fields
 * Created by al on 13/12/18
 */
public class Sigma implements NamedMetric<LXP> {

    protected final NamedMetric<String> baseDistance;
    protected List<Integer> fields;

    public Sigma(NamedMetric<String> baseDistance, List<Integer> fields) {

        this.baseDistance = baseDistance;
        this.fields = fields;
    }

    @Override
    public double distance(LXP a, LXP b) {

        double total_distance = 0.0d;
        for (int f : fields) {
            try {
                String x = a.getString(f);
                String y = b.getString(f);

                if (x == null || x.equals("")) {
                    if (y == null || y.equals("")) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
                if (y == null || y.equals("")) {
                    return 1;
                }
                if (x.equals(y)) {
                    return 0;
                }

                double f_distance = baseDistance.distance(x, y);
                total_distance += f_distance;
            }
            catch (Exception e) {
                throw new RuntimeException("exception comparing fields " + a.getString(f) + " and " + b.getString(f) + " from field " + f + " in records \n" + a + "\n and \n" + b, e);
            }
        }

        return total_distance;
    }

    @Override
    public String getMetricName() {
        return "Sigma Over" + baseDistance.getMetricName();
    }
}


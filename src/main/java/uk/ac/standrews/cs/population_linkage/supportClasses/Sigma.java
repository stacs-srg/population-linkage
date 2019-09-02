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
package uk.ac.standrews.cs.population_linkage.supportClasses;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.List;

/**
 * Sigma function for combining metrics - compares a single set of fields
 * Created by al on 13/12/18
 */
public class Sigma extends Metric<LXP> {

    final Metric<String> base_metric;
    private List<Integer> fields;

    public Sigma(Metric<String> base_metric, List<Integer> fields) {

        this.base_metric = base_metric;
        this.fields = fields;
    }

    @Override
    public double calculateDistance(LXP a, LXP b) {

        double total_distance = 0.0d;

        for (int field : fields) {
            try {
                String x = a.getString(field);
                String y = b.getString(field);

                final double field_distance = base_metric.distance(x, y);
                total_distance += field_distance;

            } catch (NullPointerException e) {
                throw new RuntimeException("exception comparing field " + a.getMetaData().getFieldName(field) + " in records \n" + a + "\n and \n" + b, e);
            } catch (Exception e) {
                throw new RuntimeException("exception comparing fields " + a.getString(field) + " and " + b.getString(field) + " from field " + a.getMetaData().getFieldName(field) + " in records \n" + a + "\n and \n" + b, e);
            }
        }

        return normaliseArbitraryPositiveDistance(total_distance);
    }

    @Override
    public String getMetricName() {
        return base_metric.getMetricName();
        //        return "Sigma-" + base_metric.getMetricName() + "-" + Sigma2.hyphenConcat(fields);
    }
}

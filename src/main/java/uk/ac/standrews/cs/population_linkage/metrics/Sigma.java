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
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.List;

/**
 * Sigma function for combining metrics - compares a single set of fields
 * Created by al on 13/12/18
 */
public class Sigma implements NamedMetric<LXP> {

    final NamedMetric<String> baseMetric;
    private List<Integer> fields;

    public Sigma(NamedMetric<String> baseMetric, List<Integer> fields) {

        this.baseMetric = baseMetric;
        this.fields = fields;
    }

    @Override
    public double distance(LXP a, LXP b) {

        double total_distance = 0.0d;

        for (int field : fields) {
            try {
                String x = a.getString(field);
                String y = b.getString(field);

                final double field_distance = baseMetric.distance(x, y);
                total_distance += field_distance;

            } catch (NullPointerException e) {
                throw new RuntimeException("exception comparing field " + a.getMetaData().getFieldName(field) + " in records \n" + a + "\n and \n" + b, e);
            } catch (Exception e) {
                throw new RuntimeException("exception comparing fields " + a.getString(field) + " and " + b.getString(field) + " from field " + a.getMetaData().getFieldName(field) + " in records \n" + a + "\n and \n" + b, e);
            }
        }

        printDistance(a, b);
        return total_distance;
    }

    private synchronized void printDistance(LXP a, LXP b) {

        double total_distance = 0.0d;

        System.out.println("\n-----------------");
        System.out.println("number of fields: " + fields.size());

        for (int field : fields) {
            try {
                String x = a.getString(field);
                String y = b.getString(field);

                System.out.println("field values: " + x + ", " + y);

                final double field_distance = baseMetric.distance(x, y);
                System.out.println("field distance: " + field_distance);
                total_distance += field_distance;

            } catch (NullPointerException e) {
                throw new RuntimeException("exception comparing field " + a.getMetaData().getFieldName(field) + " in records \n" + a + "\n and \n" + b, e);
            } catch (Exception e) {
                throw new RuntimeException("exception comparing fields " + a.getString(field) + " and " + b.getString(field) + " from field " + a.getMetaData().getFieldName(field) + " in records \n" + a + "\n and \n" + b, e);
            }
        }

        System.out.println("total distance: " + total_distance);
        System.out.println("\n-----------------");
    }

    @Override
    public double normalisedDistance(LXP x, LXP y) {
        return Metric.normalise(distance(x, y));
    }

    @Override
    public String getMetricName() {
        return "Sigma-" + baseMetric.getMetricName() + "-" + Sigma2.hyphenConcat(fields);
    }
}

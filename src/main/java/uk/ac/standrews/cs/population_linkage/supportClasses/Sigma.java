/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;


import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.util.List;

/**
 * Sigma function for combining metrics - compares a single set of fields
 * Created by al on 13/12/18
 */
public class Sigma extends Metric<LXP> {

    final StringMetric base_distance;
    final List<Integer> field_list;
    final int id_field_index;

    public Sigma(final StringMetric base_distance, final List<Integer> field_list, final int id_field_index) {

        this.base_distance = base_distance;
        this.field_list = field_list;
        this.id_field_index = id_field_index;
    }

    @Override
    public double calculateDistance(final LXP a, final LXP b) {

        double total_distance = 0.0d;

        for (int field_index : field_list) {
            try {
                String field_value1 = a.getString(field_index);
                String field_value2 = b.getString(field_index);

                total_distance += base_distance.distance(field_value1, field_value2);

            } catch (Exception e) {
                printExceptionDebug(a, b, field_index);
                throw new RuntimeException("exception comparing field " + a.getMetaData().getFieldName(field_index) + " in records \n" + a + "\n and \n" + b, e);
            }
        }

        return normaliseArbitraryPositiveDistance(total_distance);
    }

    @Override
    public String getMetricName() {
        return base_distance.getMetricName();
    }

    private void printExceptionDebug(final LXP a, final LXP b, final int field_index) {

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("Exception in distance calculation");
        System.out.println("field index list: " + field_index);
        System.out.println("a: " + (a == null ? "null" : "not null"));
        System.out.println("b: " + (b == null ? "null" : "not null"));
        System.out.println("id of a: " + a.getString(id_field_index));
        System.out.println("id of b: " + b.getString(id_field_index));
        System.out.println("field name a: " + a.getMetaData().getFieldName(field_index));
        System.out.println("field name b: " + b.getMetaData().getFieldName(field_index));
        System.out.println("field value a: " + a.getString(field_index));
        System.out.println("field value b: " + b.getString(field_index));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
}

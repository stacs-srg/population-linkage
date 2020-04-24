/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.util.List;

/**
 * Sigma function for combining metrics  - compares different field lists over data
 * Created by al on 13/12/18
 */
public class Sigma2 extends Metric<LXP> {

    final StringMetric base_distance;
    final List<Integer> field_list1;
    final List<Integer> field_list2;
    final int id_field_index1;
    final int id_field_index2;

    public Sigma2(final StringMetric base_distance, final List<Integer> field_list1, final List<Integer> field_list2, final int id_field_index1, final int id_field_index2) {

        if (field_list1.size() != field_list2.size()) {
            throw new RuntimeException("Field lists must be the same length");
        }
        this.base_distance = base_distance;
        this.field_list1 = field_list1;
        this.field_list2 = field_list2;
        this.id_field_index1 = id_field_index1;
        this.id_field_index2 = id_field_index2;
    }

    @Override
    public double calculateDistance(final LXP a, final LXP b) {

        double total_distance = 0.0d;

        for (int i = 0; i < field_list1.size(); i++) {

            try {
                int field_index1 = field_list1.get(i);
                int field_index2 = field_list2.get(i);

                final String field_value1 = a.getString(field_index1);
                final String field_value2 = b.getString(field_index2);

                total_distance += base_distance.distance(field_value1, field_value2);

            } catch (Exception e) {
                printExceptionDebug(a, b, i);
                throw new RuntimeException("exception comparing fields " + a.getMetaData().getFieldName(field_list1.get(i)) + " and " + b.getMetaData().getFieldName(field_list2.get(i)) + " in records \n" + a + "\n and \n" + b, e);
            }
        }

        return normaliseArbitraryPositiveDistance(total_distance);
    }

    @Override
    public String getMetricName() {
        return base_distance.getMetricName();
    }

    private void printExceptionDebug(final LXP a, final LXP b, final int field_index_index) {

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("Exception in distance calculation");
        System.out.println("index of field index list: " + field_index_index);
        System.out.println("a: " + (a == null ? "null" : "not null"));
        System.out.println("b: " + (b == null ? "null" : "not null"));
        System.out.println("id of a: " + a.getString(id_field_index1));
        System.out.println("id of b: " + b.getString(id_field_index2));
        System.out.println("field name a: " + a.getMetaData().getFieldName(field_list1.get(field_index_index)));
        System.out.println("field name b: " + b.getMetaData().getFieldName(field_list2.get(field_index_index)));
        System.out.println("field value a: " + a.getString(field_list1.get(field_index_index)));
        System.out.println("field value b: " + b.getString(field_list2.get(field_index_index)));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
}

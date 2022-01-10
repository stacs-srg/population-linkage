/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruthML;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.List;

/**
 * Weighted SigmaMissingOne function for combining metrics.
 * Compares a single set field_list using multiple metrics and assigns arbitrary weights to each.
 * Created by al on 29/6/2020
 *
 */
public class SigmaWeighted extends Metric<LXP> {

    private final String name;
    private final int id_field_index;
    private final List<Integer> field_list;
    private final List<Metric<String>> metrics;
    private final List<Float> weights;

    public SigmaWeighted(final List<Integer> fields, final List<Metric<String>> metrics, final List<Float> weights, final int id_field_index) {

        this.field_list = fields;
        this.metrics = metrics;
        this.weights = weights;
        this.id_field_index = id_field_index;
        this.name = combineNames();
    }

    private String combineNames() {
        StringBuilder sb = new StringBuilder();
        sb.append( "Weighted:" );
        for( int i = 0; i < field_list.size(); i++ ) {
            sb.append( metrics.get(i).getMetricName() );
            sb.append( ":" );
            sb.append( String.format("%.2f", weights.get(i) ) );
            sb.append( "+" );
        }
        return sb.subSequence( 0,sb.length() - 1).toString();
    }

    @Override
    public double calculateDistance(final LXP a, final LXP b) {

        double total_distance = 0.0d;

        for( int i = 0; i < field_list.size(); i++ ) {

            int field_index = field_list.get(i);
            Metric<String> m = metrics.get(i);
            float weight = weights.get(i);

            try {
                String field_value1 = a.getString(field_index);
                String field_value2 = b.getString(field_index);

                double distance = m.distance(field_value1, field_value2);
                double weighted_distance = distance * weight;

                total_distance += weighted_distance;

            } catch (Exception e) {
                printExceptionDebug(a, b, field_index);
                throw new RuntimeException("exception comparing field " + a.getMetaData().getFieldName(field_index) + " in records \n" + a + "\n and \n" + b, e);
            }
        }

        return normaliseArbitraryPositiveDistance(total_distance);
    }

    @Override
    public String getMetricName() {
        return this.name;
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

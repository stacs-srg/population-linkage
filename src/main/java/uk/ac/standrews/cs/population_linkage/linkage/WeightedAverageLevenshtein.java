package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.InvalidWeightsException;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.KeyNotFoundException;
import uk.ac.standrews.cs.utilities.metrics.Levenshtein;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.util.ArrayList;
import java.util.List;

public class WeightedAverageLevenshtein<T extends LXP> implements NamedMetric<T> {

    private List<Integer> match_fields;
    private List<Double> field_weights;
    private StringMetric distance_measure;

    private static final double EPSILON = 0.0000001;

    public WeightedAverageLevenshtein() {

        distance_measure = new Levenshtein();
    }

    public WeightedAverageLevenshtein(List<Integer> match_fields) {

        this();
        this.match_fields = match_fields;
    }

    public WeightedAverageLevenshtein(List<Integer> match_fields, List<Double> field_weights) {

        this(match_fields);

        this.field_weights = field_weights;
        checkWeightsSumToOne(field_weights);
    }

    @Override
    public String getMetricName() {
        return "Weighted Average Levenshtein";
    }

    @Override
    public double distance(LXP record1, LXP record2) {

        if (match_fields == null) {

            double total_distance = 0;

            int number_of_fields = record1.getFieldCount();

            for (int field_number = 0; field_number < number_of_fields; field_number++) {

                String field1;
                String field2;
                try {

                    field1 = record1.getString(field_number);
                    field2 = record2.getString(field_number);

                    double weight = field_weights != null ? field_weights.get(field_number) : 1.0 / number_of_fields;

                    total_distance += distance_measure.distance(field1, field2) * weight;

                } catch (KeyNotFoundException e) {
                    throw e;
                }
            }

            return total_distance;

        } else {

            double total_distance = 0;
            int field_number = 0;

            for (int field : match_fields) {

                String field1;
                String field2;
                try {

                    field1 = record1.getString(field);
                    field2 = record2.getString(field);

                    double weight = field_weights != null ? field_weights.get(field_number) : 1.0 / match_fields.size();

                    total_distance += distance_measure.distance(field1, field2) * weight;

                    field_number++;
                } catch (KeyNotFoundException e) {
                    throw e;
                }
            }

            return total_distance;
        }
    }

    private void checkWeightsSumToOne(List<Double> field_weights) {

        double total = 0;
        for (Double weight : field_weights) total += weight;

        if (Math.abs(total - 1) > EPSILON) throw new InvalidWeightsException();
    }

    private static List<Double> generateEqualWeights(int number_of_fields) {

        List<Double> weights = new ArrayList<>();
        for (int i = 0; i < number_of_fields; i++) weights.add(1.0 / number_of_fields);
        return weights;
    }
}

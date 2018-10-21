package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.InvalidWeightsException;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.Levenshtein;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.util.ArrayList;
import java.util.List;

public class WeightedAverageLevenshtein<T extends LXP> implements NamedMetric<T> {

    private final List<Integer> match_fields;
    private final List<Double> field_weights;
    private final StringMetric distance_measure;

    private static final double EPSILON = 0.0000001;

    public WeightedAverageLevenshtein(List<Integer> match_fields) throws InvalidWeightsException {

        this(match_fields, generateEqualWeights(match_fields.size()));
    }

    public WeightedAverageLevenshtein(List<Integer> match_fields, List<Double> field_weights) throws InvalidWeightsException {

        this.match_fields = match_fields;
        this.field_weights = field_weights;
        distance_measure = new Levenshtein();

        checkWeightsSumToOne(field_weights);
    }

    @Override
    public String getMetricName() {
        return "Weighted Average Levenshtein";
    }

    @Override
    public double distance(LXP record1, LXP record2) {

        double total_distance = 0;
        int field_number = 0;

        for (int field : match_fields) {

            String field1 = record1.getString(field);
            String field2 = record2.getString(field);

            total_distance += distance_measure.distance(field1, field2) * field_weights.get(field_number);
            field_number++;
        }

        return total_distance;
    }

    private void checkWeightsSumToOne(List<Double> field_weights) throws InvalidWeightsException {

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

package uk.ac.standrews.cs.population_linkage.groundTruth;

import java.util.Arrays;

public class AggregatorMean extends Aggregator {

    double[] weights;

    public AggregatorMean() {
        weights = null;
    }

    public AggregatorMean(double... weights) {
        this.weights = weights;
    }

    @Override
    double aggregate(double... values) {

        if (weights == null) {
            return Arrays.stream(values).sum() / values.length;
        }
        else {
            if (values.length != weights.length) throw new RuntimeException("weighted mean: inconsistent number of weights");

            double result = 0;
            for (int i = 0; i < values.length; i++)
                result += values[i] * weights[i];

            return result;
        }
    }
}

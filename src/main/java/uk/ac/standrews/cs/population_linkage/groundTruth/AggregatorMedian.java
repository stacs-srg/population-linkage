package uk.ac.standrews.cs.population_linkage.groundTruth;

import java.util.Arrays;

public class AggregatorMedian extends Aggregator {

    @Override
    double aggregate(double... values) {

        return Arrays.stream(values).sorted().toArray()[values.length / 2];
    }
}

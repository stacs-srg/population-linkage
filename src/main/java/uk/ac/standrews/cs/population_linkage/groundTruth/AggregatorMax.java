package uk.ac.standrews.cs.population_linkage.groundTruth;

import java.util.Arrays;

public class AggregatorMax extends Aggregator {

    @Override
    double aggregate(double... values) {

        return Arrays.stream(values).max().orElseThrow();
    }
}

package uk.ac.standrews.cs.population_linkage.groundTruth;

public enum Imputer {

    RECORD_MEAN(new AggregatorMean()),

    RECORD_MEDIAN(new AggregatorMedian()),

    RECORD_MAX(new AggregatorMean());

    final Aggregator aggregator;

    Imputer(Aggregator aggregator) {
        this.aggregator = aggregator;
    }

    public double impute(double... non_missing_values) {
        return aggregator.aggregate(non_missing_values);
    }
    //record mean/median/max, population mean/median/max
}

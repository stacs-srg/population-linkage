package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

public class ThresholdMatcher implements Matcher {

    private final NamedMetric<LXP> distance_measure;
    private final double threshold;

    public ThresholdMatcher(NamedMetric<LXP> distance_measure, double threshold) {

        this.distance_measure = distance_measure;
        this.threshold = threshold;
    }

    @Override
    public boolean match(LXP record1, LXP record2) {

        return record1 != record2 && distance_measure.distance(record1, record2) <= threshold;
    }
}

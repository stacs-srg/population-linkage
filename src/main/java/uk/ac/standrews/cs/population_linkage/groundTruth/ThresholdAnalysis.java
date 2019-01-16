package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class ThresholdAnalysis {

    static final long SEED = 87626L;
    static final int NUMBER_OF_RUNS = 10;

    static final int NUMBER_OF_THRESHOLDS_SAMPLED = 101; // 0.01 granularity including 0.0 and 1.0.
    private static final double EPSILON = 0.00001;

    final List<Map<String, Sample[]>> linkage_results; // Maps from metric name to counts of TPFP etc.
    final List<NamedMetric<LXP>> combined_metrics;

    final long[] pairs_evaluated = new long[NUMBER_OF_RUNS];
    final long[] pairs_ignored = new long[NUMBER_OF_RUNS];

    /**
     *
     * @return lists of all sets of comparison fields that will be used for comparing records, can have more than one, hence List<List></list>
     */
    public abstract List<Integer> getComparisonFields();

    ThresholdAnalysis() {

        combined_metrics = getCombinedMetrics();
        linkage_results = initialiseState();
    }

    private List<Map<String, Sample[]>> initialiseState() {

        final List<Map<String, Sample[]>> result = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_RUNS; i++) {

            final Map<String, Sample[]> map = new HashMap<>();

            for (final NamedMetric<LXP> metric : combined_metrics) {

                final Sample[] samples = new Sample[NUMBER_OF_THRESHOLDS_SAMPLED];
                for (int j = 0; j < NUMBER_OF_THRESHOLDS_SAMPLED; j++) {
                    samples[j] = new Sample();
                }

                map.put(metric.getMetricName(), samples);
            }

            result.add(map);
        }
        return result;
    }

    protected List<NamedMetric<LXP>> getCombinedMetrics() {

        final List<NamedMetric<LXP>> result = new ArrayList<>();

        for (final NamedMetric<String> base_metric : Utilities.BASE_METRICS) {
                result.add(new Sigma(base_metric, getComparisonFields()));
        }
        return result;
    }

    static int thresholdToIndex(final double threshold) {

        return (int) (threshold * (NUMBER_OF_THRESHOLDS_SAMPLED - 1) + EPSILON);
    }

    static double indexToThreshold(final int index) {

        return (double) index / (NUMBER_OF_THRESHOLDS_SAMPLED - 1);
    }

    class Sample {

        int fp = 0;
        int tp = 0;
        int fn = 0;
        int tn = 0;
    }
}

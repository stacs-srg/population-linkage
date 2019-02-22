package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the base class for performing linkage analysis from ground truth.
 * Classes extending this class are required to implement the following method:
 *     getComparisonFields(), returns the set of fields to be used for distance comparison from the data source
 * Known subclasses are:
 * AllPairsSameSourceLinkageAnalysisML, which compares records from a single data source
 * AllPairs2SourcesLinkageAnalysis, which compares records from two different data sources
 */

abstract class ThresholdAnalysis {

    static final long SEED = 87626L;
    static final int NUMBER_OF_RUNS = 10;

    static final int NUMBER_OF_THRESHOLDS_SAMPLED = 101; // 0.01 granularity including 0.0 and 1.0.
    private static final double EPSILON = 0.00001;

    final List<Map<String, Sample[]>> linkage_results; // Maps from metric name to counts of TPFP etc.
    final List<NamedMetric<LXP>> combined_metrics;

    final long[] pairs_evaluated = new long[NUMBER_OF_RUNS];
    final long[] pairs_ignored = new long[NUMBER_OF_RUNS];
    final int number_of_records_to_be_checked;

    static final int DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED = 25000; // yields 0.01 error with Umea test over whole dataset for all metrics.
    static final int CHECK_ALL_RECORDS = -1;

    /**
     *
     * @return lists of all sets of comparison fields that will be used for comparing records, can have more than one, hence List<List></list>
     */
    public abstract List<Integer> getComparisonFields();

    ThresholdAnalysis(int number_of_records_to_be_checked) {

        this.number_of_records_to_be_checked = number_of_records_to_be_checked;
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

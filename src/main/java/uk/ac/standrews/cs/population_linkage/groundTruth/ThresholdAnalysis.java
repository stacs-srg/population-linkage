package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.*;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.*;

class ThresholdAnalysis {

    private static final int CHARVAL = 512;
    static final int NUMBER_OF_THRESHOLDS_SAMPLED = 101; // 0.01 granularity including 0.0 and 1.0.

    final Map<String, Sample[]> state; // Maps from metric name to counts of TPFP etc.
    final List<NamedMetric<LXP>> combined_metrics;

    int starting_counter = 0;

    private static final List<NamedMetric<String>> BASE_METRICS = Arrays.asList(
            new Levenshtein(),
            new Jaccard(),
            new Cosine(),
            new SED(CHARVAL),
            new JensenShannon2(CHARVAL));

    private static final List<Integer> SIBLING_BUNDLING_FIELDS = Arrays.asList(
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.PARENTS_PLACE_OF_MARRIAGE,
            Birth.PARENTS_DAY_OF_MARRIAGE,
            Birth.PARENTS_MONTH_OF_MARRIAGE,
            Birth.PARENTS_YEAR_OF_MARRIAGE);

    ThresholdAnalysis() {

        combined_metrics = getCombinedMetrics();
        state = initialiseState();
    }

    private Map<String, Sample[]> initialiseState() {

        final Map<String, Sample[]> result = new HashMap<>();

        for (final NamedMetric<LXP> metric : combined_metrics) {

            final Sample[] samples = new Sample[NUMBER_OF_THRESHOLDS_SAMPLED];
            for (int i = 0; i < NUMBER_OF_THRESHOLDS_SAMPLED; i++) {
                samples[i] = new Sample();
            }

            final String metricName = metric.getMetricName();
            result.put(metricName, samples);
        }
        return result;
    }

    private List<NamedMetric<LXP>> getCombinedMetrics() {

        final List<NamedMetric<LXP>> result = new ArrayList<>();

        for (final NamedMetric<String> base_metric : BASE_METRICS) {
            result.add(new Sigma(base_metric, SIBLING_BUNDLING_FIELDS));
        }
        return result;
    }

    void importStateLine(final String line) {

        try {
            setStateValue(parseStateLine(line));

        } catch (Exception e) {
            System.out.println("error parsing line: " + line);
            throw e;
        }
    }

    Sample parseStateLine(final String line) {

        final Sample result = new Sample();

        final String[] fields = line.split(",");

        result.iterations = Integer.parseInt(fields[2]);
        result.metric_name = fields[3];
        result.threshold = Double.parseDouble(fields[4]);
        result.tp = Integer.parseInt(fields[5]);
        result.fp = Integer.parseInt(fields[6]);
        result.fn = Integer.parseInt(fields[7]);
        result.tn = Integer.parseInt(fields[8]);

        return result;
    }

    private void setStateValue(final Sample line) {

        final Sample truths = state.get(line.metric_name)[thresholdToIndex(line.threshold)];

        truths.tp = line.tp;
        truths.fp = line.fp;
        truths.fn = line.fn;
        truths.tn = line.tn;

        starting_counter = line.tp + line.fp + line.fn + line.tn;
    }

    private static int thresholdToIndex(final double threshold) {

        return (int) (threshold * (NUMBER_OF_THRESHOLDS_SAMPLED - 1));
    }

     static double indexToThreshold(final int index) {

        return (double)index / (NUMBER_OF_THRESHOLDS_SAMPLED - 1);
    }

    class Sample {

        int iterations = 0;
        String metric_name = "";
        double threshold = 0.0;
        int fp = 0;
        int tp = 0;
        int fn = 0;
        int tn = 0;
    }
}

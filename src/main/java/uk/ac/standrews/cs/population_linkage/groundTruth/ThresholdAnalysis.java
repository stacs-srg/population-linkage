package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.*;

class ThresholdAnalysis {

    static final int NUMBER_OF_THRESHOLDS_SAMPLED = 101; // 0.01 granularity including 0.0 and 1.0.
    private static final double EPSILON = 0.00001;

    final Map<String, Sample[]> state; // Maps from metric name to counts of TPFP etc.
    final List<NamedMetric<LXP>> combined_metrics;

    int records_processed = 0;
    long pairs_evaluated = 0;
    long pairs_ignored = 0;

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

            result.put(metric.getMetricName(), samples);
        }
        return result;
    }

    private List<NamedMetric<LXP>> getCombinedMetrics() {

        final List<NamedMetric<LXP>> result = new ArrayList<>();

        for (final NamedMetric<String> base_metric : Utilities.BASE_METRICS) {
            result.add(new Sigma(base_metric, SIBLING_BUNDLING_FIELDS));
        }
        return result;
    }

    void importStateLine(final String line) {

        try {
            recordImportedSample(parseSampleLine(line));

        } catch (Exception e) {
            System.out.println("error parsing line: " + line);
            throw e;
        }
    }

    private void recordImportedSample(final Sample imported_sample) {

        final int i = thresholdToIndex(imported_sample.threshold);
        final Sample sample = state.get(imported_sample.metric_name)[i];

        sample.tp = imported_sample.tp;
        sample.fp = imported_sample.fp;
        sample.fn = imported_sample.fn;
        sample.tn = imported_sample.tn;

        records_processed = imported_sample.records_processed;
        pairs_evaluated = imported_sample.pairs_evaluated;
        pairs_ignored = imported_sample.pairs_ignored;
    }

    Sample parseSampleLine(final String line) {

        final Sample result = new Sample();

        final String[] fields = line.split(",");

        result.records_processed = Integer.parseInt(fields[1]);
        result.pairs_evaluated = Long.parseLong(fields[2]);
        result.pairs_ignored = Long.parseLong(fields[3]);
        result.metric_name = fields[4];
        result.threshold = Double.parseDouble(fields[5]);
        result.tp = Integer.parseInt(fields[6]);
        result.fp = Integer.parseInt(fields[7]);
        result.fn = Integer.parseInt(fields[8]);
        result.tn = Integer.parseInt(fields[9]);

        return result;
    }

    private static int thresholdToIndex(final double threshold) {

        return (int) (threshold * (NUMBER_OF_THRESHOLDS_SAMPLED - 1) + EPSILON);
    }

    static double indexToThreshold(final int index) {

        return (double) index / (NUMBER_OF_THRESHOLDS_SAMPLED - 1);
    }

    class Sample {

        int records_processed = 0;
        long pairs_evaluated = 0;
        long pairs_ignored = 0;
        String metric_name = "";
        double threshold = 0.0;
        int fp = 0;
        int tp = 0;
        int fn = 0;
        int tn = 0;
    }
}

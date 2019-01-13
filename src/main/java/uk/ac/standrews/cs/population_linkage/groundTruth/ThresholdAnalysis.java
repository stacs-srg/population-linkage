package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.*;

class ThresholdAnalysis {

    static final List<Long> SEEDS = Arrays.asList(4353L, 345345L, 94327L, 234523L, 34523L, 34524L, 56782L, 25244L, 87626L, 42921L);

    static final int NUMBER_OF_THRESHOLDS_SAMPLED = 101; // 0.01 granularity including 0.0 and 1.0.
    private static final double EPSILON = 0.00001;

    final List<Map<String, Sample[]>> linkage_results; // Maps from metric name to counts of TPFP etc.
    final List<NamedMetric<LXP>> combined_metrics;

    final int[] records_processed = new int[SEEDS.size()];
    final long[] pairs_evaluated = new long[SEEDS.size()];
    final long[] pairs_ignored = new long[SEEDS.size()];
    int run_number = 0;

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
        linkage_results = initialiseState();
    }

    private List<Map<String, Sample[]>> initialiseState() {

        final List<Map<String, Sample[]>> result = new ArrayList<>();

        for (int i = 0; i < SEEDS.size(); i++) {

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

    private List<NamedMetric<LXP>> getCombinedMetrics() {

        final List<NamedMetric<LXP>> result = new ArrayList<>();

        for (final NamedMetric<String> base_metric : Utilities.BASE_METRICS) {
            result.add(new Sigma(base_metric, SIBLING_BUNDLING_FIELDS));
        }
        return result;
    }

    void importStateLine(final String line) {

        try {
            recordImportedSample(line);

        } catch (Exception e) {
            System.out.println("error parsing line: " + line);
            throw e;
        }
    }

    private void recordImportedSample(final String line) {

        final String[] fields = extractFields(line);

        run_number = extractRunNumber(fields);
        records_processed[run_number] = extractRecordsProcessed(fields);
        pairs_evaluated[run_number] = extractPairsEvaluated(fields);
        pairs_ignored[run_number] = extractPairsIgnored(fields);

        final double threshold = extractThreshold(fields);
        final String metric_name = extractMetricName(fields);
        final Sample imported_sample = extractSample(fields);

        final int index = thresholdToIndex(threshold);
        final Sample sample = linkage_results.get(run_number).get(metric_name)[index];

        sample.tp = imported_sample.tp;
        sample.fp = imported_sample.fp;
        sample.fn = imported_sample.fn;
        sample.tn = imported_sample.tn;
    }

    protected String[] extractFields(final String line) {
        return line.split(",");
    }

    private int extractRunNumber(final String[] fields) {
        // Run number numbered from 1 in output file.
        return Integer.parseInt(fields[1]) - 1;
    }

    private int extractRecordsProcessed(final String[] fields) {
        return Integer.parseInt(fields[2]);
    }

    protected long extractPairsEvaluated(final String[] fields) {
        return Long.parseLong(fields[3]);
    }

    private long extractPairsIgnored(final String[] fields) {
        return Long.parseLong(fields[4]);
    }

    protected String extractMetricName(final String[] fields) {
        return fields[5];
    }

    protected double extractThreshold(final String[] fields) {
        return Double.parseDouble(fields[6]);
    }

    protected Sample extractSample(final String[] fields) {

        final Sample result = new Sample();

        result.tp = Integer.parseInt(fields[7]);
        result.fp = Integer.parseInt(fields[8]);
        result.fn = Integer.parseInt(fields[9]);
        result.tn = Integer.parseInt(fields[10]);

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

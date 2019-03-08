package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * This class is the base class for performing linkage analysis from ground truth.
 */
abstract class ThresholdAnalysis {

    static final long SEED = 87626L;

    static final int NUMBER_OF_THRESHOLDS_SAMPLED = 101; // 0.01 granularity including 0.0 and 1.0.
    private static final double EPSILON = 0.00001;

    final List<Map<String, Sample[]>> linkage_results; // Maps from metric name to counts of TPFP etc.
    final List<NamedMetric<LXP>> combined_metrics;

    final int number_of_runs;
    final long[] pairs_evaluated;
    final long[] pairs_ignored;
    final int number_of_records_to_be_checked;

    static final int DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED = 25000; // yields 0.01 error with Umea test over whole dataset for all metrics.
    static final int CHECK_ALL_RECORDS = -1;

    final Path store_path;
    final String repo_name;

    static final int BLOCK_SIZE = 100;
    static final String DELIMIT = ",";

    final PrintWriter linkage_results_writer;
    final PrintWriter distance_results_writer;
    final PrintWriter linkage_results_metadata_writer;
    final PrintWriter distance_results_metadata_writer;

    final List<Map<String, int[]>> non_link_distance_counts;
    final List<Map<String, int[]>> link_distance_counts;

    final Map<String, Integer> run_numbers_for_metrics;
    List<LXP> source_records;
    int number_of_records;
    int records_processed = 0;

    /**
     * @return lists of all sets of comparison fields that will be used for comparing records, can have more than one, hence List<List></list>
     */
    abstract List<Integer> getComparisonFields();
    abstract String getSourceType();
    abstract Iterable<LXP> getSourceRecords(RecordRepository record_repository);
    abstract void setupRecords();
    abstract void processRecord(int i, NamedMetric<LXP> metric, boolean evaluating_first_metric);
    abstract void printMetaData();
    abstract LinkStatus isTrueLink(final LXP record1, final LXP record2);
    abstract List<NamedMetric<LXP>> getCombinedMetrics();

    ThresholdAnalysis(final Path store_path, final String repo_name1, final String linkage_results_filename, final String distance_results_filename, int number_of_records_to_be_checked, int number_of_runs) throws IOException {

        this.number_of_runs = number_of_runs;
        pairs_evaluated = new long[number_of_runs];
        pairs_ignored = new long[number_of_runs];
        this.number_of_records_to_be_checked = number_of_records_to_be_checked;
        combined_metrics = getCombinedMetrics();
        linkage_results = initialiseState();

        this.store_path = store_path;
        this.repo_name = repo_name1;

        linkage_results_writer = new PrintWriter(new BufferedWriter(new FileWriter(linkage_results_filename + ".csv", false)));
        distance_results_writer = new PrintWriter(new BufferedWriter(new FileWriter(distance_results_filename + ".csv", false)));
        linkage_results_metadata_writer = new PrintWriter(new BufferedWriter(new FileWriter(linkage_results_filename + ".meta", false)));
        distance_results_metadata_writer = new PrintWriter(new BufferedWriter(new FileWriter(distance_results_filename + ".meta", false)));

        non_link_distance_counts = initialiseDistances();
        link_distance_counts = initialiseDistances();
        run_numbers_for_metrics = initialiseRunNumbers();

        setupRecords();
    }

    public void run() throws Exception {

        printHeaders();
        printMetaData();

        final long number_of_blocks_to_be_checked = number_of_records / BLOCK_SIZE;

        for (int block_index = 0; block_index < number_of_blocks_to_be_checked; block_index++) {

            processBlock(block_index);
            printSamples();

            System.out.println("finished block: checked " + (block_index + 1) * BLOCK_SIZE + " records");
            System.out.flush();
        }
        System.out.println("Run completed");
        System.out.flush();
    }

    private List<Map<String, Sample[]>> initialiseState() {

        final List<Map<String, Sample[]>> result = new ArrayList<>();

        for (int i = 0; i < number_of_runs; i++) {

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

    private List<Map<String, int[]>> initialiseDistances() {

        final List<Map<String, int[]>> result = new ArrayList<>();

        for (int i = 0; i < number_of_runs; i++) {

            final Map<String, int[]> map = new HashMap<>();

            for (final NamedMetric<LXP> metric : combined_metrics) {
                map.put(metric.getMetricName(), new int[NUMBER_OF_THRESHOLDS_SAMPLED]);
            }

            result.add(map);
        }
        return result;
    }

    private Map<String, Integer> initialiseRunNumbers() {

        final Map<String, Integer> map = new HashMap<>();

        for (final NamedMetric<LXP> metric : combined_metrics) {
            map.put(metric.getMetricName(), 0);
        }

        return map;
    }

    private void processBlock(final int block_index) {

        final CountDownLatch start_gate = new CountDownLatch(1);
        final CountDownLatch end_gate = new CountDownLatch(combined_metrics.size());

        for (final NamedMetric<LXP> metric : combined_metrics) {

            new Thread(() -> processBlockWithMetric(block_index, metric, start_gate, end_gate)).start();
        }

        try {
            start_gate.countDown();
            end_gate.await();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        records_processed += BLOCK_SIZE;
    }

    private void processBlockWithMetric(final int block_index, final NamedMetric<LXP> metric, final CountDownLatch start_gate, final CountDownLatch end_gate) {

        try {
            start_gate.await();

            final boolean evaluating_first_metric = metric == combined_metrics.get(0);

            final int start_index = block_index * BLOCK_SIZE;
            final int end_index = start_index + BLOCK_SIZE;

            for (int i = start_index; i < end_index; i++) {
                processRecord(i, metric, evaluating_first_metric);
            }

        } catch (InterruptedException ignored) {
        } finally {

            end_gate.countDown();
        }
    }

    void processRecord(final int record_index, final int last_record_index, final List<LXP> records1, final List<LXP> records2, final NamedMetric<LXP> metric, final boolean increment_counts) {

        final String metric_name = metric.getMetricName();
        int run_number = run_numbers_for_metrics.get(metric_name);

        for (int j = 0; j < last_record_index; j++) { //******* Process all the records in the second source for each in block

            if (j != record_index) {

                processPair(metric, increment_counts, run_number, records1.get(record_index), records2.get(j));

                run_number++;
                if (run_number == number_of_runs) run_number = 0;
            }
        }

        run_numbers_for_metrics.put(metric_name, run_number);
    }

    protected void processPair(NamedMetric<LXP> metric, boolean increment_counts, int run_number, LXP record1, LXP record2) {

        final double distance = normalise(metric.distance(record1, record2));
        final LinkStatus link_status = isTrueLink(record1, record2);

        if (link_status == LinkStatus.UNKNOWN) {
            if (increment_counts) {
                pairs_ignored[run_number]++;
            }

        } else {
            final String metric_name = metric.getMetricName();

            final int[] link_counts = link_distance_counts.get(run_number).get(metric_name);
            final int[] non_link_counts = non_link_distance_counts.get(run_number).get(metric_name);
            final Sample[] samples = linkage_results.get(run_number).get(metric_name);
            final boolean is_true_link = link_status == LinkStatus.TRUE_LINK;

            for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {
                recordSample(threshold_index, samples, is_true_link, distance);
            }

            final int index = thresholdToIndex(distance);

            if (is_true_link) {
                link_counts[index]++;
            } else {
                non_link_counts[index]++;
            }

            if (increment_counts) {
                pairs_evaluated[run_number]++;
            }
        }
    }

    void printSamples() {

        for (final NamedMetric<LXP> metric : combined_metrics) {

            final String metric_name = metric.getMetricName();

            for (int run_number = 0; run_number < number_of_runs; run_number++) {
                final Sample[] samples = linkage_results.get(run_number).get(metric_name);

                for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {
                    printSample(run_number, metric_name, indexToThreshold(threshold_index), samples[threshold_index]);
                }

                printDistances(run_number, metric_name);
            }
        }
    }

    private void printDistances(final int run_number, final String metric_name) {

        final int[] non_link_distance_counts_for_metric = non_link_distance_counts.get(run_number).get(metric_name);
        final int[] link_distance_counts_for_metric = link_distance_counts.get(run_number).get(metric_name);

        printDistances(run_number, metric_name, false, non_link_distance_counts_for_metric);
        printDistances(run_number, metric_name, true, link_distance_counts_for_metric);
    }

    static int thresholdToIndex(final double threshold) {

        return (int) (threshold * (NUMBER_OF_THRESHOLDS_SAMPLED - 1) + EPSILON);
    }

    static double indexToThreshold(final int index) {

        return (double) index / (NUMBER_OF_THRESHOLDS_SAMPLED - 1);
    }

    private void printHeaders() {

        linkage_results_writer.print("time");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("run number");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("records processed");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("pairs evaluated");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("pairs ignored");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("metric");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("threshold");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("tp");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("fp");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("fn");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("tn");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("precision");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("recall");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("f_measure");

        linkage_results_writer.println();
        linkage_results_writer.flush();

        distance_results_writer.print("time");
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print("run number");
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print("records processed");
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print("pairs evaluated");
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print("pairs ignored");
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print("metric");
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print("links_non-link");
        distance_results_writer.print(DELIMIT);

        for (int i = 0; i < NUMBER_OF_THRESHOLDS_SAMPLED; i++) {
            if (i > 0) distance_results_writer.print(DELIMIT);
            distance_results_writer.print(String.format("%.2f", indexToThreshold(i)));
        }
        distance_results_writer.println();
        distance_results_writer.flush();
    }

    private void printSample(final int run_number, final String metric_name, final double threshold, final Sample sample) {

        linkage_results_writer.print(LocalDateTime.now());
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(run_number + 1);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(records_processed);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(pairs_evaluated[run_number]);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(pairs_ignored[run_number]);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(metric_name);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(String.format("%.2f", threshold));
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(sample.tp);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(sample.fp);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(sample.fn);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(sample.tn);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(String.format("%.2f", ClassificationMetrics.precision(sample.tp, sample.fp)));
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(String.format("%.2f", ClassificationMetrics.recall(sample.tp, sample.fn)));
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(String.format("%.2f", ClassificationMetrics.F1(sample.tp, sample.fp, sample.fn)));

        linkage_results_writer.println();
        linkage_results_writer.flush();
    }

    void printDistances(final int run_number, final String metric_name, boolean links, int[] distances) {

        distance_results_writer.print(LocalDateTime.now());
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(run_number + 1);
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(records_processed);
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(pairs_evaluated[run_number]);
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(pairs_ignored[run_number]);
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(metric_name);
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(links ? "links" : "non-links");
        distance_results_writer.print(DELIMIT);

        for (int i = 0; i < NUMBER_OF_THRESHOLDS_SAMPLED; i++) {
            distance_results_writer.print(distances[i]);
            distance_results_writer.print(DELIMIT);
        }
        distance_results_writer.println();
        distance_results_writer.flush();
    }

    void recordSample(final int threshold_index, final Sample[] samples, final boolean is_true_link, final double distance) {

        final double threshold = indexToThreshold(threshold_index);

        if (distance <= threshold) {

            if (is_true_link) {
                samples[threshold_index].tp++;
            } else {
                samples[threshold_index].fp++;
            }

        } else {
            if (is_true_link) {
                samples[threshold_index].fn++;
            } else {
                samples[threshold_index].tn++;
            }
        }
    }

    /**
     * @param distance - the distance to be normalised
     * @return the distance in the range 0-1:  1 - ( 1 / d + 1 )
     */
    double normalise(double distance) {
        return 1d - (1d / (distance + 1d));
    }

    static String getCallingClassName() {
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            return e.getStackTrace()[1].getClassName();
        }
    }

    static String getLinkageResultsFilename() {

        return getCallingClassName() + "PRFByThreshold";
    }

    static String getDistanceResultsFilename() {

        return getCallingClassName() + "LinksByDistance";
    }

    class Sample {

        long fp = 0;
        long tp = 0;
        long fn = 0;
        long tn = 0;
    }

    protected enum LinkStatus {

        TRUE_LINK, NOT_TRUE_LINK, UNKNOWN
    }
}

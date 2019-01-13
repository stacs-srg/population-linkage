package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

public class AllPairsUmeaSiblingBundling extends ThresholdAnalysis {

    private final Path store_path;
    private final String repo_name;

    private static final int BLOCK_SIZE = 100;
    private static final String DELIMIT = ",";

    private final PrintWriter linkage_results_writer;
    private final PrintWriter distance_results_writer;
    private final PrintWriter linkage_results_metadata_writer;
    private final PrintWriter distance_results_metadata_writer;

    private final List<Map<String, int[]>> non_link_distance_counts;
    private final List<Map<String, int[]>> link_distance_counts;

    private List<List<Birth>> birth_records_per_run;

    private AllPairsUmeaSiblingBundling(final Path store_path, final String repo_name, final String linkage_results_filename, final String distance_results_filename) throws IOException {

        super();

        this.store_path = store_path;
        this.repo_name = repo_name;

        importPreviousState(linkage_results_filename);

        linkage_results_writer = new PrintWriter(new BufferedWriter(new FileWriter(linkage_results_filename + ".csv", true)));
        distance_results_writer = new PrintWriter(new BufferedWriter(new FileWriter(distance_results_filename + ".csv", true)));
        linkage_results_metadata_writer = new PrintWriter(new BufferedWriter(new FileWriter(linkage_results_filename + ".meta", true)));
        distance_results_metadata_writer = new PrintWriter(new BufferedWriter(new FileWriter(distance_results_filename + ".meta", true)));

        non_link_distance_counts = initialiseDistances();
        link_distance_counts = initialiseDistances();

        setupRecords();
    }

    private List<Map<String, int[]>> initialiseDistances() {

        final List<Map<String, int[]>> result = new ArrayList<>();

        for (int i = 0; i < SEEDS.size(); i++) {

            final Map<String, int[]> map = new HashMap<>();

            for (final NamedMetric<LXP> metric : combined_metrics) {
                map.put(metric.getMetricName(), new int[NUMBER_OF_THRESHOLDS_SAMPLED]);
            }

            result.add(map);
        }
        return result;
    }

    private void importPreviousState(final String filename) throws IOException {

        if (Files.exists(Paths.get(filename))) {

            System.out.println("Importing previous data");

            try (final Stream<String> lines = Files.lines(Paths.get(filename))) {
                lines.skip(1).forEachOrdered(this::importStateLine);
            }
        }
    }

    private void setupRecords() {

        System.out.println("Reading records from repository: " + repo_name);

        final RecordRepository record_repository = new RecordRepository(store_path, repo_name);
        final Iterable<Birth> births = record_repository.getBirths();

        System.out.println("Randomising record order");

        birth_records_per_run = new ArrayList<>();

        for (Long seed : SEEDS) {
            birth_records_per_run.add(Utilities.permute(births, seed));
        }
    }

    public void run() throws Exception {

        if (records_processed[0] == 0) {

            printHeaders();
            printMetaData();
        }

        for (int block_index = records_processed[0] / BLOCK_SIZE; block_index < birth_records_per_run.get(0).size() / BLOCK_SIZE; block_index++) {

            for (int i = 0; i < SEEDS.size(); i++) {

                run_number = i;
                processBlock(block_index);
                printSamples();
            }

            System.out.println("finished block: checked " + (block_index + 1) * BLOCK_SIZE + " records");
            System.out.flush();
        }
    }

    private void processBlock(final int block_index) {

        final CountDownLatch start_gate = new CountDownLatch(1);
        final CountDownLatch end_gate = new CountDownLatch(combined_metrics.size());

        for (final NamedMetric<LXP> metric : combined_metrics) {

            new Thread(() -> processBlockForMetric(block_index, metric, start_gate, end_gate)).start();
        }

        try {
            start_gate.countDown();
            end_gate.await();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        records_processed[run_number] += BLOCK_SIZE;
    }

    private void processBlockForMetric(final int block_index, final NamedMetric<LXP> metric, final CountDownLatch start_gate, final CountDownLatch end_gate) {

        try {
            start_gate.await();

            final boolean evaluating_first_metric = metric == combined_metrics.get(0);

            for (int i = block_index * BLOCK_SIZE; i < (block_index + 1) * BLOCK_SIZE; i++) {
                processRecord(i, metric, evaluating_first_metric);
            }

        } catch (InterruptedException ignored) {
        } finally {

            end_gate.countDown();
        }
    }

    private void processRecord(final int record_index, final NamedMetric<LXP> metric, final boolean increment_counts) {

        final String metric_name = metric.getMetricName();

        final List<Birth> birth_records = birth_records_per_run.get(run_number);
        final int[] link_counts = link_distance_counts.get(run_number).get(metric_name);
        final int[] non_link_counts = non_link_distance_counts.get(run_number).get(metric_name);
        final Sample[] samples = linkage_results.get(run_number).get(metric_name);

        final int number_of_records = birth_records.size();

        for (int j = record_index + 1; j < number_of_records; j++) {

            final Birth b1 = birth_records.get(record_index);
            final Birth b2 = birth_records.get(j);

            final double distance = normalise(metric.distance(b1, b2));
            final LinkStatus link_status = isTrueLink(b1, b2);

            if (link_status == LinkStatus.UNKNOWN) {
                if (increment_counts) {
                    pairs_ignored[run_number]++;
                }

            } else {
                final boolean is_true_link = link_status == LinkStatus.TRUE_LINK;

                for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {
                    recordSamples(threshold_index, samples, is_true_link, distance);
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
    }

    private LinkStatus isTrueLink(final Birth b1, final Birth b2) {

        final String b1_parent_id = b1.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);
        final String b2_parent_id = b2.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);

        if (b1_parent_id.isEmpty() || b2_parent_id.isEmpty()) return LinkStatus.UNKNOWN;

        return b1_parent_id.equals(b2_parent_id) ? LinkStatus.TRUE_LINK : LinkStatus.NOT_TRUE_LINK;
    }

    private void recordSamples(final int threshold_index, final Sample[] samples, final boolean is_true_link, final double distance) {

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

    private void printSamples() {

        for (final NamedMetric<LXP> metric : combined_metrics) {

            final String metric_name = metric.getMetricName();
            final Sample[] samples = linkage_results.get(run_number).get(metric_name);

            for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {
                printSample(metric_name, indexToThreshold(threshold_index), samples[threshold_index]);
            }

            printDistances(metric_name);
        }
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
        distance_results_writer.print("links/non-links");
        distance_results_writer.print(DELIMIT);

        for (int i = 0; i < NUMBER_OF_THRESHOLDS_SAMPLED; i++) {
            if (i > 0) distance_results_writer.print(DELIMIT);
            distance_results_writer.print(String.format("%.2f", indexToThreshold(i)));
        }
        distance_results_writer.println();
        distance_results_writer.flush();
    }

    private void printSample(String metric_name, Double threshold, Sample sample) {

        linkage_results_writer.print(LocalDateTime.now());
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(run_number + 1);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(records_processed[run_number]);
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

    private void printDistances(String metric_name) {

        final int[] non_link_distance_counts_for_metric = non_link_distance_counts.get(run_number).get(metric_name);
        final int[] link_distance_counts_for_metric = link_distance_counts.get(run_number).get(metric_name);

        printDistances(metric_name, false, non_link_distance_counts_for_metric);
        printDistances(metric_name, true, link_distance_counts_for_metric);
    }

    private void printDistances(String metric_name, boolean links, int[] distances) {

        distance_results_writer.print(LocalDateTime.now());
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(run_number + 1);
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(records_processed[run_number]);
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

    private void printMetaData() {

        linkage_results_metadata_writer.println("Output file created: " + LocalDateTime.now());
        linkage_results_metadata_writer.println("Checking quality of linkage using various string similarity metrics and thresholds");
        linkage_results_metadata_writer.println("Dataset: Umea");
        linkage_results_metadata_writer.println("Linkage type: sibling bundling");
        linkage_results_metadata_writer.println("Records: births");
        linkage_results_metadata_writer.flush();

        distance_results_metadata_writer.println("Output file created: " + LocalDateTime.now());
        distance_results_metadata_writer.println("Checking distributions of record pair distances using various string similarity metrics and thresholds");
        distance_results_metadata_writer.println("Dataset: Umea");
        distance_results_metadata_writer.println("Linkage type: sibling bundling");
        distance_results_metadata_writer.println("Records: births");
        distance_results_metadata_writer.flush();
    }

    /**
     * @param distance - the distance to be normalised
     * @return the distance in the range 0-1:  1 - ( 1 / d + 1 )
     */
    private double normalise(double distance) {
        return 1d - (1d / (distance + 1d));
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        new AllPairsUmeaSiblingBundling(store_path, repo_name, "UmeaThresholdBirthSiblingLinkage", "UmeaThresholdBirthSiblingDistances").run();
    }

    private enum LinkStatus {

        TRUE_LINK, NOT_TRUE_LINK, UNKNOWN
    }
}

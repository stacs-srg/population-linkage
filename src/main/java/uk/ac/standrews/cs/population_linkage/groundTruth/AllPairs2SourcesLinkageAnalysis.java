package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma2;
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
 * This class performs linkage analysis on data pulled from two different data sources, for example births and deaths.
 * Classes extending this class are required to implement the following methods:
 *     getSourceRecords1(RecordRepository record_repository), which provides the records from the first data source
 *     <getSourceRecords2(RecordRepository record_repository), which provides the records from the second data source
 *     getSourceType1(), which provides a textual description of the first data source, for example, "births"
 *     getSourceType2(), which provides a textual description of the first data source, for example, "deaths"
 *     LinkStatus isTrueLink(final LXP record1, final LXP record2), returns the ground truth about equivalence of datum's from source 1 and source 2
 *     getComparisonFields(), returns the set of fields to be used for distance comparison from data source 1 (note the name)
 *     getComparisonFields2(), returns the set of fields to be used for distance comparison from data source 2
 */
public abstract class AllPairs2SourcesLinkageAnalysis extends ThresholdAnalysis {

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

    private final Map<String, Integer> run_numbers_for_metrics;

    private List<LXP> source_records1;
    private List<LXP> source_records2;
    private long number_of_records1;
    private long number_of_records2;
    private long records_processed = 0;


    protected AllPairs2SourcesLinkageAnalysis(final Path store_path, final String repo_name1, final String linkage_results_filename, final String distance_results_filename, long number_of_records_to_be_checked, int number_of_runs ) throws IOException {

        super( number_of_records_to_be_checked, number_of_runs );

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

    protected abstract Iterable<LXP> getSourceRecords1(RecordRepository record_repository);

    protected abstract Iterable<LXP> getSourceRecords2(RecordRepository record_repository);

    protected abstract LinkStatus isTrueLink(final LXP record1, final LXP record2);

    protected abstract String getSourceType1();

    protected abstract String getSourceType2();

    protected abstract List<Integer> getComparisonFields2();

    @Override
    protected List<NamedMetric<LXP>> getCombinedMetrics() {

        final List<NamedMetric<LXP>> result = new ArrayList<>();

        for (final NamedMetric<String> base_metric : Utilities.BASE_METRICS) {
            result.add(new Sigma2(base_metric, getComparisonFields(), getComparisonFields2()));
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

    private void setupRecords() {

        System.out.println("Reading records from repository: " + repo_name );

        final RecordRepository record_repository = new RecordRepository(store_path, repo_name);
        final Iterable<LXP> records1 = getSourceRecords1(record_repository);

        final Iterable<LXP> records2 = getSourceRecords2(record_repository);

        System.out.println("Randomising record order");

        source_records1 = Utilities.permute(records1, SEED);
        source_records2 = Utilities.permute(records2, SEED);

        number_of_records1 = number_of_records_to_be_checked == CHECK_ALL_RECORDS ? source_records1.size() : number_of_records_to_be_checked;
        number_of_records2 = source_records2.size();
    }

    public void run() throws Exception {

        printHeaders();
        printMetaData();

        final long number_of_blocks_to_be_checked = number_of_records1 / BLOCK_SIZE;

        for (int block_index = 0; block_index < number_of_blocks_to_be_checked; block_index++) {

            processBlock(block_index);
            printSamples();

            System.out.println("finished block: checked " + (block_index + 1) * BLOCK_SIZE + " records");
            System.out.flush();
        }
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

    private void processRecord(final int record_index, final NamedMetric<LXP> metric, final boolean increment_counts) {

        final String metric_name = metric.getMetricName();
        int run_number = run_numbers_for_metrics.get(metric_name);

        for (int j = 0; j < number_of_records2; j++) { //******* Process all the records in the second source for each in block - CHECK

            // IS THIS SUPPOSED TO BE BLOCKED?
            // IN the single soutce version does the records as follows - from j
            //      for (int j = record_index + 1; j < number_of_records; j++) {

            final LXP record1 = source_records1.get(record_index);
            final LXP record2 = source_records2.get(j);

            final double distance = normalise(metric.distance(record1, record2));
            final LinkStatus link_status = isTrueLink(record1, record2);

            if (link_status == LinkStatus.UNKNOWN) {
                if (increment_counts) {
                    pairs_ignored[run_number]++;
                }

            } else {
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

            run_number++;
            if (run_number == number_of_runs) run_number = 0;
        }

        run_numbers_for_metrics.put(metric_name, run_number);
    }


    private void recordSample(final int threshold_index, final Sample[] samples, final boolean is_true_link, final double distance) {

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

            for (int run_number = 0; run_number < number_of_runs; run_number++) {
                final Sample[] samples = linkage_results.get(run_number).get(metric_name);

                for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {
                    printSample(run_number, metric_name, indexToThreshold(threshold_index), samples[threshold_index]);
                }

                printDistances(run_number, metric_name);
            }
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

    private void printDistances(final int run_number, final String metric_name) {

        final int[] non_link_distance_counts_for_metric = non_link_distance_counts.get(run_number).get(metric_name);
        final int[] link_distance_counts_for_metric = link_distance_counts.get(run_number).get(metric_name);

        printDistances(run_number, metric_name, false, non_link_distance_counts_for_metric);
        printDistances(run_number, metric_name, true, link_distance_counts_for_metric);
    }

    private void printDistances(final int run_number, final String metric_name, boolean links, int[] distances) {

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

    private void printMetaData() {

        linkage_results_metadata_writer.println("Output file created: " + LocalDateTime.now());
        linkage_results_metadata_writer.println("Checking quality of linkage using various string similarity metrics and thresholds");
        linkage_results_metadata_writer.println("Dataset: Umea");
        linkage_results_metadata_writer.println("Linkage type: sibling bundling");
        linkage_results_metadata_writer.println("Records: " + getSourceType1() );
        linkage_results_metadata_writer.flush();

        distance_results_metadata_writer.println("Output file created: " + LocalDateTime.now());
        distance_results_metadata_writer.println("Checking distributions of record pair distances using various string similarity metrics and thresholds");
        distance_results_metadata_writer.println("Dataset: Umea");
        distance_results_metadata_writer.println("Linkage type: sibling bundling");
        distance_results_metadata_writer.println("Records: " + getSourceType1());
        distance_results_metadata_writer.flush();
    }

    /**
     * @param distance - the distance to be normalised
     * @return the distance in the range 0-1:  1 - ( 1 / d + 1 )
     */
    private double normalise(double distance) {
        return 1d - (1d / (distance + 1d));
    }

    protected enum LinkStatus {

        TRUE_LINK, NOT_TRUE_LINK, UNKNOWN
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruthML;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * This class is the base class for performing linkage analysis from ground truth.
 */
public abstract class WeightedThresholdAnalysis {

    // Global flag can be used to over-ride 1:1 constaint in identity linkage.
    public static final boolean MULTIPLE_LINKS_CAN_BE_DISABLED_FOR_IDENTITY_LINKAGE = true;

    protected static final int DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED = 25000; // yields 0.01 error with Umea test over whole dataset for all metrics.
    protected static final int CHECK_ALL_RECORDS = -1;
    static final long SEED = LinkageConfig.seed;
    private static final int NUMBER_OF_DISTANCES_SAMPLED = 101; // 0.01 granularity including 0.0 and 1.0.
    private static final double EPSILON = 0.00001;
    private static final int BLOCK_SIZE = 100;
    private static final String DELIMIT = ",";
    protected final boolean allow_multiple_links;
    final int number_of_records_to_be_checked;
    final int number_of_runs;
    final Path store_path;
    final String repo_name;
    final PrintWriter linkage_results_metadata_writer;
    final PrintWriter distance_results_metadata_writer;
    private final Sample sample;                           // counts of TPFP etc.
    private final long[] pairs_evaluated;
    private final long[] pairs_ignored;
    private final PrintWriter linkage_results_writer;
    private final PrintWriter distance_results_writer;
    private int non_link_distance_counts;
    private int link_distance_counts;
    private final double threshold;
    private int run_number;
    List<LXP> source_records;
    int number_of_records;
    boolean verbose = true;
    private int records_processed = 0;

    WeightedThresholdAnalysis(final Path store_path, final String repo_name, final String linkage_results_filename, final String distance_results_filename, final int number_of_records_to_be_checked, final int number_of_runs, final boolean allow_multiple_links, double threshold) throws IOException {

        System.out.println("Running ground truth analysis for " + getLinkageType() + " on data: " + repo_name);
        System.out.printf("Max heap size: %.1fGB\n", getMaxHeapinGB());

        this.number_of_records_to_be_checked = number_of_records_to_be_checked;
        this.number_of_runs = number_of_runs;
        this.allow_multiple_links = allow_multiple_links || !MULTIPLE_LINKS_CAN_BE_DISABLED_FOR_IDENTITY_LINKAGE;
        this.threshold = threshold;

        pairs_evaluated = new long[number_of_runs];
        pairs_ignored = new long[number_of_runs];
        sample = new Sample();

        this.store_path = store_path;
        this.repo_name = repo_name;

        linkage_results_writer = new PrintWriter(new BufferedWriter(new FileWriter(linkage_results_filename + ".csv", false)));
        distance_results_writer = new PrintWriter(new BufferedWriter(new FileWriter(distance_results_filename + ".csv", false)));
        linkage_results_metadata_writer = new PrintWriter(new BufferedWriter(new FileWriter(linkage_results_filename + ".meta", false)));
        distance_results_metadata_writer = new PrintWriter(new BufferedWriter(new FileWriter(distance_results_filename + ".meta", false)));

        non_link_distance_counts = 0;
        link_distance_counts = 9;
        run_number = 0;

        setupRecords();
    }

    private static int distanceToIndex(final double distance) {

        return (int) (distance * (NUMBER_OF_DISTANCES_SAMPLED - 1) + EPSILON);
    }

    private static String getCallingClassName() {

        String full_classname = new RuntimeException().getStackTrace()[2].getClassName(); // need to jump over getCallingClassName frame and getLinkageResultsFilename frame
        return full_classname.substring(full_classname.lastIndexOf(".") + 1);
    }

    protected static String getLinkageResultsFilename() {

        return getCallingClassName() + "PRFByThreshold";
    }

    protected static String getDistanceResultsFilename() {

        return getCallingClassName() + "LinksByDistance";
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    private double getMaxHeapinGB() {

        return (double) Runtime.getRuntime().maxMemory() / 1000000000;
    }

    /**
     * @return list of comparison fields that will be used for comparing records
     */
    public abstract List<Integer> getComparisonFields();

    public abstract int getIdFieldIndex();

    public abstract String getDatasetName();

    public abstract String getSourceType();

    public abstract String getLinkageType();

    public abstract Iterable<LXP> getSourceRecords(RecordRepository record_repository);

    public abstract void setupRecords();

    public abstract void processRecord(int i, Metric<LXP> metric);

    public abstract void printMetaData();

    public abstract LinkStatus isTrueMatch(final LXP record1, final LXP record2);

    public abstract Metric<LXP> getMetric();

    public void run() throws Exception {

        printHeaders();
        printMetaData();

        final long number_of_blocks_to_be_checked = number_of_records / BLOCK_SIZE;

        for (int block_index = 0; block_index < number_of_blocks_to_be_checked; block_index++) {

            processBlock(block_index);
 //           printSamples();

            if (verbose) {
                System.out.println("finished block: checked " + (block_index + 1) * BLOCK_SIZE + " records");
                System.out.flush();
            }
        }

        printSamples();

        if (verbose) {
            System.out.println("Run completed");
            System.out.flush();
        }
    }

    private void processBlock(final int block_index) {

        final CountDownLatch start_gate = new CountDownLatch(1);
        final CountDownLatch end_gate = new CountDownLatch(1);   // only 1 metric in this version was combinedmetrics.size()

        new Thread(() -> processBlockWithMetric(block_index, getMetric(), start_gate, end_gate)).start();

        try {
            start_gate.countDown();
            end_gate.await();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        records_processed += BLOCK_SIZE;
    }

    private void processBlockWithMetric(final int block_index, final Metric<LXP> metric, final CountDownLatch start_gate, final CountDownLatch end_gate) {

        try {
            start_gate.await();

            final int start_index = block_index * BLOCK_SIZE;
            final int end_index = start_index + BLOCK_SIZE;

            for (int i = start_index; i < end_index; i++) {
                processRecord(i, metric);
            }

        } catch (InterruptedException ignored) {
        } finally {

            end_gate.countDown();
        }
    }

    void processRecord(final int record_index, final int last_record_index, final List<LXP> records1, final List<LXP> records2, final Metric<LXP> metric, final boolean increment_counts) {

        final String metric_name = metric.getMetricName();

        final LXP record1 = records1.get(record_index);

        double min_distance = 1.01;

        Sample tentative_samples = null;
        boolean tentative_link_is_true_link = false;

        for (int j = 0; j < last_record_index; j++) { //******* Process all the records in the second source for each in block

            if (j != record_index) {

                LXP record2 = records2.get(j);

                final double distance = metric.distance(record1, record2);
                final LinkStatus link_status = isTrueMatch(record1, record2);
                final RecordPair possible_link = new RecordPair(record1, record2, distance);

                final boolean distance_is_closest_encountered = distance < min_distance;
                final boolean link_is_viable = isViableLink(possible_link);

                if (link_status == LinkStatus.UNKNOWN) {
                    updatePairsIgnoredCounts(increment_counts, run_number);

                } else {

                    final boolean is_true_link = link_status == LinkStatus.TRUE_MATCH;

                    if (allow_multiple_links) {
                        recordLinkDecisions(sample, distance, link_is_viable, is_true_link);
                    } else {

                        if (distance_is_closest_encountered && link_is_viable) {

                            // Undo any previous tentative link decision.
                            undoTentativeLinkDecision(tentative_samples, min_distance, tentative_link_is_true_link);

                            recordLinkDecisions(sample, distance, link_is_viable, is_true_link);

                            // Record these as tentative in case another closer record is found.
                            tentative_samples = sample;
                            tentative_link_is_true_link = is_true_link;

                        } else {
                            recordNegativeLinkDecisions(sample, is_true_link);
                        }
                    }

                    updateTrueLinkCounts(metric_name, run_number, distance, is_true_link);
                    updatePairsEvaluatedCounts(increment_counts, run_number);
                }

                if (distance_is_closest_encountered) min_distance = distance;

                run_number++;
                if (run_number == number_of_runs) run_number = 0;
            }
        }
    }

    private void updatePairsIgnoredCounts(final boolean increment_counts, final int run_number) {

        if (increment_counts) {
            pairs_ignored[run_number]++;
        }
    }

    private void updatePairsEvaluatedCounts(final boolean increment_counts, final int run_number) {

        if (increment_counts) {
            pairs_evaluated[run_number]++;
        }
    }

    private void updateTrueLinkCounts(final String metric_name, final int run_number, final double distance, final boolean is_true_link) {

        if (is_true_link) {
            link_distance_counts++;
        } else {
            non_link_distance_counts++;
        }
    }

    private void recordNegativeLinkDecisions(final Sample samples, final boolean is_true_link) {

        if (is_true_link) {
            samples.fn++;
        } else {
            samples.tn++;
        }
    }

    private void recordLinkDecisions(final Sample samples, final double link_distance, final boolean link_is_viable, final boolean is_true_link) {

        if (link_distance <= threshold && link_is_viable) {

            if (is_true_link) {
                samples.tp++;
            } else {
                samples.fp++;
            }

        } else {
            if (is_true_link) {
                samples.fn++;
            } else {
                samples.tn++;
            }
        }
    }

    private void undoTentativeLinkDecision(final Sample tentative_samples, final double tentative_link_distance, final boolean tentative_link_is_true_link) {

        if (tentative_samples != null) {

            if (tentative_link_distance <= threshold) {

                if (tentative_link_is_true_link) {
                    tentative_samples.tp--;
                    tentative_samples.fn++;
                } else {
                    tentative_samples.fp--;
                    tentative_samples.tn++;
                }

            }
        }
    }

    private void printSamples() {

        String metric_name = getMetric().getMetricName();

        for (int run_number = 0; run_number < number_of_runs; run_number++) {
            printSample(run_number, metric_name, threshold, sample);
        }

        printDistances(run_number, metric_name);
    }


    private void printDistances(final int run_number, final String metric_name) {

        printDistances(run_number, metric_name, false, this.non_link_distance_counts);
        printDistances(run_number, metric_name, true, link_distance_counts);
    }

    private void printHeaders() {

        linkage_results_writer.print("time");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("link number");
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
        distance_results_writer.print("link number");
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

        distance_results_writer.print(String.format("%.2f", threshold));

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

    private void printDistances(final int run_number, final String metric_name, boolean links, int distances) {

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
        distance_results_writer.print(distances);
        distance_results_writer.println();
        distance_results_writer.flush();
    }

    public boolean isViableLink(RecordPair proposedLink) {
        return true;
    }

    class Sample {

        long fp = 0;
        long tp = 0;
        long fn = 0;
        long tn = 0;
    }
}

/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Base class for performing linkage analysis from ground truth.
 */
public abstract class ThresholdAnalysis {

    // Global flag can be used to over-ride 1:1 constraint in identity linkage.
    public static final boolean MULTIPLE_LINKS_CAN_BE_DISABLED_FOR_IDENTITY_LINKAGE = true;

    protected static final int CHECK_ALL_RECORDS = -1;
    static final long SEED = 87626L;
    private static final int NUMBER_OF_DISTANCES_SAMPLED = 101; // 0.01 granularity including 0.0 and 1.0.
    private static final int NUMBER_OF_THRESHOLDS_SAMPLED = 101; // 0.01 granularity including 0.0 and 1.0.
    private static final double EPSILON = 0.00001;
    private static final int BLOCK_SIZE = 100;
    private static final String DELIMIT = ",";
    protected boolean allow_multiple_links;
    int number_of_records_to_be_checked;
    int number_of_runs;
    String repo_name;
    private PrintWriter linkage_results_metadata_writer;
    private PrintWriter distance_results_metadata_writer;
    private List<Map<String, Sample[]>> linkage_results; // Maps from measure name to counts of TPFP etc.
    private List<LXPMeasure> composite_measures;
    private long[] pairs_evaluated;
    private long[] pairs_ignored;
    private PrintWriter linkage_results_writer;
    private PrintWriter distance_results_writer;
    private List<Map<String, int[]>> non_link_distance_counts;
    private List<Map<String, int[]>> link_distance_counts;
    private Map<String, Integer> run_numbers_for_measures;
    List<LXP> source_records;
    int number_of_records;
    boolean verbose = false;
    private int records_processed = 0;

    ThresholdAnalysis(final String repo_name, final String linkage_results_filename, final String distance_results_filename, final int number_of_records_to_be_checked, final int number_of_runs, final boolean allow_multiple_links) throws IOException {

        init(repo_name, linkage_results_filename, distance_results_filename, number_of_records_to_be_checked, number_of_runs, allow_multiple_links);
    }

    ThresholdAnalysis(final String repo_name, final String[] args, final String linkage_results_filename, final String distance_results_filename, final boolean allow_multiple_links) throws IOException {

        if (args.length < 2) {
            throw new RuntimeException("usage: <number of records to be checked> <number of runs>");
        }

        int number_of_records_to_be_checked = Integer.parseInt(args[0]);
        int number_of_runs = Integer.parseInt(args[1]);

        init(repo_name, linkage_results_filename, distance_results_filename, number_of_records_to_be_checked, number_of_runs, allow_multiple_links);
    }

    private void init(String repo_name, String linkage_results_filename, String distance_results_filename, int number_of_records_to_be_checked, int number_of_runs, boolean allow_multiple_links) throws IOException {

        this.repo_name = repo_name;
        this.number_of_records_to_be_checked = number_of_records_to_be_checked;
        this.number_of_runs = number_of_runs;
        this.allow_multiple_links = allow_multiple_links || !MULTIPLE_LINKS_CAN_BE_DISABLED_FOR_IDENTITY_LINKAGE;

        pairs_evaluated = new long[number_of_runs];
        pairs_ignored = new long[number_of_runs];
        composite_measures = getCombinedMeasures();
        linkage_results = initialiseState();

        linkage_results_writer = new PrintWriter(new BufferedWriter(new FileWriter(linkage_results_filename + ".csv", false)));
        distance_results_writer = new PrintWriter(new BufferedWriter(new FileWriter(distance_results_filename + ".csv", false)));
        linkage_results_metadata_writer = new PrintWriter(new BufferedWriter(new FileWriter(linkage_results_filename + ".meta", false)));
        distance_results_metadata_writer = new PrintWriter(new BufferedWriter(new FileWriter(distance_results_filename + ".meta", false)));

        non_link_distance_counts = initialiseDistances();
        link_distance_counts = initialiseDistances();
        run_numbers_for_measures = initialiseRunNumbers();

        setupRecords();
    }

    private static int distanceToIndex(final double distance) {

        if (distance > 1d) throw new RuntimeException("distance more than one: " + distance);

        return (int) (distance * (NUMBER_OF_DISTANCES_SAMPLED - 1) + EPSILON);
    }

    private static double indexToThreshold(final int index) {

        return (double) index / (NUMBER_OF_THRESHOLDS_SAMPLED - 1);
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

    public abstract String getDatasetName();

    public abstract String getLinkageType();

    public abstract Iterable<LXP> getSourceRecords(RecordRepository record_repository);

    public abstract void setupRecords();

    public abstract void processRecord(int i, LXPMeasure measure, boolean evaluating_first_measure);

    public abstract LinkStatus isTrueMatch(LXP record1, LXP record2);

    public abstract List<LXPMeasure> getCombinedMeasures();

    protected abstract double getNormalisationCutoff();

    public void run() throws Exception {

        printHeaders();
        printMetaData();

        final long number_of_blocks_to_be_checked = number_of_records / BLOCK_SIZE;

        for (int block_index = 0; block_index < number_of_blocks_to_be_checked; block_index++) {

            processBlock(block_index);
            printSamples();

            if (verbose) {
                System.out.println("finished block: checked " + (block_index + 1) * BLOCK_SIZE + " records");
                System.out.flush();
            }
        }

        if (verbose) {
            System.out.println("Run completed");
            System.out.flush();
        }
    }

    private List<Map<String, Sample[]>> initialiseState() {

        final List<Map<String, Sample[]>> result = new ArrayList<>();

        for (int i = 0; i < number_of_runs; i++) {

            final Map<String, Sample[]> map = new HashMap<>();

            for (final var measure : composite_measures) {

                final Sample[] samples = new Sample[NUMBER_OF_THRESHOLDS_SAMPLED];
                for (int j = 0; j < NUMBER_OF_THRESHOLDS_SAMPLED; j++) {
                    samples[j] = new Sample();
                }

                map.put(measure.getMeasureName(), samples);
            }

            result.add(map);
        }
        return result;
    }

    private List<Map<String, int[]>> initialiseDistances() {

        final List<Map<String, int[]>> result = new ArrayList<>();

        for (int i = 0; i < number_of_runs; i++) {

            final Map<String, int[]> map = new HashMap<>();

            for (final var measure : composite_measures) {
                map.put(measure.getMeasureName(), new int[NUMBER_OF_THRESHOLDS_SAMPLED]);
            }

            result.add(map);
        }
        return result;
    }

    private Map<String, Integer> initialiseRunNumbers() {

        final Map<String, Integer> map = new HashMap<>();

        for (final var measure : composite_measures) {
            map.put(measure.getMeasureName(), 0);
        }

        return map;
    }

    private void processBlock(final int block_index) {

        final CountDownLatch start_gate = new CountDownLatch(1);
        final CountDownLatch end_gate = new CountDownLatch(composite_measures.size());

        for (final var measure : composite_measures) {

            new Thread(() -> processBlockWithMeasure(block_index, measure, start_gate, end_gate)).start();
        }

        try {
            start_gate.countDown();
            end_gate.await();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        records_processed += BLOCK_SIZE;
    }

    private void processBlockWithMeasure(final int block_index, final LXPMeasure measure, final CountDownLatch start_gate, final CountDownLatch end_gate) {

        try {
            start_gate.await();

            final boolean evaluating_first_measure = measure == composite_measures.get(0);

            final int start_index = block_index * BLOCK_SIZE;
            final int end_index = start_index + BLOCK_SIZE;

            for (int i = start_index; i < end_index; i++) {
                processRecord(i, measure, evaluating_first_measure);
            }

        } catch (InterruptedException ignored) {
        } finally {

            end_gate.countDown();
        }
    }

    void processRecord(final int record_index, final int last_record_index, final List<LXP> records1, final List<LXP> records2, final LXPMeasure measure, final boolean increment_counts) {

        final String measure_name = measure.getMeasureName();
        int run_number = run_numbers_for_measures.get(measure_name);

        final LXP record1 = records1.get(record_index);

        double min_distance = 1.01;

        Sample[] tentative_samples = null;
        boolean tentative_link_is_true_link = false;

        for (int j = 0; j < last_record_index; j++) { //******* Process all the records in the second source for each in block

            if (j != record_index) {

                final LXP record2 = records2.get(j);

                final double distance = measure.distance(record1, record2);
//                if (distance > 1d) {
//                    System.out.println("distance > 1 for measure: " + measure_name);
//                    System.out.println("record 1: " + record1);
//                    System.out.println("record 2: " + record2);
//                }
                final LinkStatus link_status = isTrueMatch(record1, record2);
                final RecordPair possible_link = new RecordPair(record1, record2, distance);

                final boolean distance_is_closest_encountered = distance < min_distance;
                final boolean link_is_viable = isViableLink(possible_link);

                if (link_status == LinkStatus.UNKNOWN) {
                    updatePairsIgnoredCounts(increment_counts, run_number);

                } else {

                    final Sample[] samples = linkage_results.get(run_number).get(measure_name);
                    final boolean is_true_link = link_status == LinkStatus.TRUE_MATCH;

                    if (allow_multiple_links) {
                        recordLinkDecisions(samples, distance, link_is_viable, is_true_link);
                    } else {

                        if (distance_is_closest_encountered && link_is_viable) {

                            // Undo any previous tentative link decision.
                            undoTentativeLinkDecision(tentative_samples, min_distance, tentative_link_is_true_link);

                            recordLinkDecisions(samples, distance, link_is_viable, is_true_link);

                            // Record these as tentative in case another closer record is found.
                            tentative_samples = samples;
                            tentative_link_is_true_link = is_true_link;

                        } else {
                            recordNegativeLinkDecisions(samples, is_true_link);
                        }
                    }

                    updateTrueLinkCounts(measure_name, run_number, distance, is_true_link);
                    updatePairsEvaluatedCounts(increment_counts, run_number);
                }

                if (distance_is_closest_encountered) min_distance = distance;

                run_number++;
                if (run_number == number_of_runs) run_number = 0;
            }
        }

        run_numbers_for_measures.put(measure_name, run_number);
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

    private void updateTrueLinkCounts(final String measure_name, final int run_number, final double distance, final boolean is_true_link) {

        final int index = distanceToIndex(distance);

        if (is_true_link) {
            link_distance_counts.get(run_number).get(measure_name)[index]++;
        } else {
            non_link_distance_counts.get(run_number).get(measure_name)[index]++;
        }
    }

    private void recordNegativeLinkDecisions(final Sample[] samples, final boolean is_true_link) {

        for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {

            if (is_true_link) {
                samples[threshold_index].fn++;
            } else {
                samples[threshold_index].tn++;
            }
        }
    }

    private void recordLinkDecisions(final Sample[] samples, final double link_distance, final boolean link_is_viable, final boolean is_true_link) {

        for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {

            final double threshold = indexToThreshold(threshold_index);

            if (link_distance <= threshold && link_is_viable) {

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
    }

    private void undoTentativeLinkDecision(final Sample[] tentative_samples, final double tentative_link_distance, final boolean tentative_link_is_true_link) {

        if (tentative_samples != null) {

            for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {

                final double threshold = indexToThreshold(threshold_index);

                if (tentative_link_distance <= threshold) {

                    if (tentative_link_is_true_link) {
                        tentative_samples[threshold_index].tp--;
                        tentative_samples[threshold_index].fn++;
                    } else {
                        tentative_samples[threshold_index].fp--;
                        tentative_samples[threshold_index].tn++;
                    }

                }
            }
        }
    }

    private void printSamples() {

        for (final LXPMeasure measure : composite_measures) {

            final String measure_name = measure.getMeasureName();

            for (int run_number = 0; run_number < number_of_runs; run_number++) {
                final Sample[] samples = linkage_results.get(run_number).get(measure_name);

                for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {
                    printSample(run_number, measure_name, indexToThreshold(threshold_index), samples[threshold_index]);
                }

                printDistances(run_number, measure_name);
            }
        }
    }

    private void printDistances(final int run_number, final String measure_name) {

        final int[] non_link_distance_counts_for_measure = non_link_distance_counts.get(run_number).get(measure_name);
        final int[] link_distance_counts_for_measure = link_distance_counts.get(run_number).get(measure_name);

        printDistances(run_number, measure_name, false, non_link_distance_counts_for_measure);
        printDistances(run_number, measure_name, true, link_distance_counts_for_measure);
    }

    private void printMetaData() {

        printMetaData(new PrintWriter(System.out), "Running ground truth analysis");
        printMetaData(linkage_results_metadata_writer, "Checking quality of linkage using various string similarity measures and thresholds");
        printMetaData(distance_results_metadata_writer, "Checking distributions of record pair distances using various string similarity measures and thresholds");
    }

    private void printMetaData(final PrintWriter writer, final String description) {

        writer.println("Output file created: " + LocalDateTime.now());
        writer.printf("Max heap size: %.1fGB\n", getMaxHeapinGB());
        writer.println(description);
        writer.println("Dataset: " + getDatasetName());
        writer.println("Linkage type: " + getLinkageType());
        writer.println("Number of records considered from first set: " + number_of_records);
        writer.println("Number of runs: " + number_of_runs);
        writer.println();
        writer.flush();
    }

    private void printHeaders() {

        linkage_results_writer.print("time");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("link_number");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("records_processed");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("pairs_evaluated");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("pairs_ignored");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("distance_measure");
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
        distance_results_writer.print("link_number");
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print("records_processed");
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print("pairs_evaluated");
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print("pairs_ignored");
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print("measure");
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

    private void printSample(final int run_number, final String measure_name, final double threshold, final Sample sample) {

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
        linkage_results_writer.print(measure_name);
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

    private void printDistances(final int run_number, final String measure_name, boolean links, int[] distances) {

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
        distance_results_writer.print(measure_name);
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

    public boolean isViableLink(RecordPair proposed_link) {
        return true;
    }

    class Sample {

        long fp = 0;
        long tp = 0;
        long fn = 0;
        long tn = 0;
    }
}

/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Base class for performing linkage analysis from ground truth.
 */
public abstract class ThresholdAnalysis {

    // Global flag can be used to over-ride 1:1 constraint in identity linkage.
    public static final boolean MULTIPLE_LINKS_CAN_BE_DISABLED_FOR_IDENTITY_LINKAGE = true;

    protected static final int CHECK_ALL_RECORDS = -1;
    protected static final long SEED = 87626L;
    private static final int NUMBER_OF_DISTANCES_SAMPLED = 101; // 0.01 granularity including 0.0 and 1.0.
    private static final int NUMBER_OF_THRESHOLDS_SAMPLED = 101; // 0.01 granularity including 0.0 and 1.0.
    private static final double EPSILON = 0.00001;
    private static final int BLOCK_SIZE = 100;
    private static final String DELIMIT = ",";
    protected boolean allow_multiple_links;

    protected int number_of_records_to_be_checked;
    protected int number_of_runs;
    private String output_file_parent_path;           // Empty string for relative to project root.
    protected RecordRepository record_repository;

    protected List<LXPMeasure> composite_measures;
    private PrintWriter linkage_results_metadata_writer;
    private PrintWriter distance_results_metadata_writer;

    private PrintWriter linkage_results_writer;
    private PrintWriter distance_results_writer;

    protected boolean verbose = false;

    /**
     * @return list of comparison fields that will be used for comparing records
     */
    public abstract List<Integer> getComparisonFields();

    public abstract String getDatasetName();

    public abstract String getLinkageType();

    public abstract Iterable<LXP> getSourceRecords1(RecordRepository record_repository);

    public abstract Iterable<LXP> getSourceRecords2(RecordRepository record_repository);

    public abstract boolean singleSource();

    public abstract LinkStatus isTrueMatch(LXP record1, LXP record2);

    public abstract boolean isViableLink(LXP record1, LXP record2);

    public abstract List<LXPMeasure> getCombinedMeasures();

    protected abstract double getNormalisationCutoff();

    protected abstract boolean recordLinkDistances();

    ThresholdAnalysis(final String repo_name, final String linkage_results_file_root, final String distance_results_file_root, final int number_of_records_to_be_checked, final int number_of_runs, final boolean allow_multiple_links) throws IOException {

        init(repo_name, linkage_results_file_root, distance_results_file_root, number_of_records_to_be_checked, number_of_runs, "", allow_multiple_links);
    }

    ThresholdAnalysis(final String repo_name, final String[] args, final String linkage_results_file_root, final String distance_results_file_root, final boolean allow_multiple_links) throws IOException {

        if (args.length < 2) {
            throw new RuntimeException("usage: <number of records to be checked> <number of runs>");
        }

        final int number_of_records_to_be_checked = Integer.parseInt(args[0]);
        final int number_of_runs = Integer.parseInt(args[1]);
        final String output_file_parent_path = args.length > 2 ? args[2] : "";

        init(repo_name, linkage_results_file_root, distance_results_file_root, number_of_records_to_be_checked, number_of_runs, output_file_parent_path, allow_multiple_links);
    }

    private void init(final String repo_name, final String linkage_results_file_root, final String distance_results_file_root, final int number_of_records_to_be_checked, final int number_of_runs, final String output_file_parent_path, final boolean allow_multiple_links) throws IOException {

        this.number_of_records_to_be_checked = number_of_records_to_be_checked;
        this.number_of_runs = number_of_runs;
        this.output_file_parent_path = output_file_parent_path;
        this.allow_multiple_links = allow_multiple_links || !MULTIPLE_LINKS_CAN_BE_DISABLED_FOR_IDENTITY_LINKAGE;

        composite_measures = getCombinedMeasures();

        linkage_results_writer = getPrintWriter(getResultsPath(linkage_results_file_root, ".csv"));
        linkage_results_metadata_writer = getPrintWriter(getResultsPath(linkage_results_file_root, ".meta"));

        if (recordLinkDistances()) {
            distance_results_writer = getPrintWriter(getResultsPath(distance_results_file_root, ".csv"));
            distance_results_metadata_writer = getPrintWriter(getResultsPath(distance_results_file_root, ".meta"));
        }

        if (verbose) System.out.println("Reading records from repository: " + repo_name);

        record_repository = new RecordRepository(repo_name);
    }

    private Path getResultsPath(String results_file_root, String suffix) {
        return Paths.get(output_file_parent_path, results_file_root + suffix);
    }

    private static PrintWriter getPrintWriter(Path path) throws IOException {
        return new PrintWriter(Files.newBufferedWriter(path));
    }

    private static int distanceToIndex(final double distance) {

        if (distance > 1d) throw new RuntimeException("distance more than one: " + distance);

        return (int) (distance * (NUMBER_OF_DISTANCES_SAMPLED - 1) + EPSILON);
    }

    private static double indexToThreshold(final int index) {

        return (double) index / ( (NUMBER_OF_THRESHOLDS_SAMPLED - 1) * 100 );  // (0000) TODO DO NOT COMMIT HACKED BY AL *************** * 1000000 to limit range.
    }

    private static String getCallingClassName() {

        final String full_classname = new RuntimeException().getStackTrace()[2].getClassName(); // need to jump over getCallingClassName frame and getLinkageResultsFilename frame
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

    private double getMaxHeapInGB() {

        return (double) Runtime.getRuntime().maxMemory() / 1000000000;
    }

    public void run() throws Exception {

        recordHeaders();
        recordMetaData();

        final Random random = new Random(SEED);

        final int number_of_threads = number_of_runs * composite_measures.size();

        final CountDownLatch start_gate = new CountDownLatch(1);
        final CountDownLatch end_gate = new CountDownLatch(number_of_threads);

        for (int i = 0; i < number_of_runs; i++) {

            if (verbose) System.out.println("Randomising record order");

            final List<LXP> source_record_list1 = Utilities.permute(getSourceRecords1(record_repository), random);
            final List<LXP> source_record_list2 = singleSource() ? source_record_list1 : Utilities.permute(getSourceRecords2(record_repository), random);

            if (number_of_records_to_be_checked == CHECK_ALL_RECORDS) {
                number_of_records_to_be_checked = source_record_list1.size();
            }

            for (final var measure : composite_measures) {

                final int run_number = i;
                new Thread(() -> new Run(run_number, measure, source_record_list1, source_record_list2, start_gate, end_gate).run()).start();
            }
        }

        try {
            start_gate.countDown();
            end_gate.await();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        record_repository.close();
    }

    void recordDistances(final int run_number, final String measure_name, final int records_processed, final long pairs_evaluated, final long pairs_ignored, final long[] non_link_distance_counts, final long[] link_distance_counts) {

        recordDistances(run_number, measure_name, records_processed, pairs_evaluated, pairs_ignored, false, non_link_distance_counts);
        recordDistances(run_number, measure_name, records_processed, pairs_evaluated, pairs_ignored, true, link_distance_counts);
    }

    protected synchronized void recordMetaData() {

        recordMetaData(new PrintWriter(System.out), "Running ground truth analysis");
        recordMetaData(linkage_results_metadata_writer, "Checking quality of linkage using various string similarity measures and thresholds");
        if (recordLinkDistances()) recordMetaData(distance_results_metadata_writer, "Checking distributions of record pair distances using various string similarity measures and thresholds");
    }

    synchronized void recordMetaData(final PrintWriter writer, final String description) {

        writer.println("Output file created: " + LocalDateTime.now());
        writer.printf("Max heap size: %.1fGB\n", getMaxHeapInGB());
        writer.println(description);
        writer.println("Dataset: " + getDatasetName());
        writer.println("Linkage type: " + getLinkageType());
        writer.println("Number of records considered from first set: " + number_of_records_to_be_checked);
        writer.println("Number of runs: " + number_of_runs);
        writer.println();
        writer.flush();
    }

    protected synchronized void recordHeaders() {

        recordLinkageResultsHeaders();
        if (recordLinkDistances()) recordDistanceResultsHeaders();
    }

    private void recordDistanceResultsHeaders() {

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
            distance_results_writer.print(String.format("%.3f", indexToThreshold(i))); // was %.2f
        }
        distance_results_writer.println();
        distance_results_writer.flush();
    }

    private void recordLinkageResultsHeaders() {

        linkage_results_writer.print("time");
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print("run_number");
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
    }

    synchronized void recordSample(final int run_number, final String measure_name, final int records_processed, final long pairs_evaluated, final long pairs_ignored, final double threshold, final Sample sample) {

        linkage_results_writer.print(LocalDateTime.now());
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(run_number + 1);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(records_processed);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(pairs_evaluated);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(pairs_ignored);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(measure_name);
        linkage_results_writer.print(DELIMIT);
        linkage_results_writer.print(String.format("%.8f", threshold));  // TODO CHANGED BY AL FOR FS METHODS
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

    synchronized void recordDistances(final int run_number, final String measure_name, final int records_processed, final long pairs_evaluated, final long pairs_ignored, final boolean links, final long[] distance_counts) {

        distance_results_writer.print(LocalDateTime.now());
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(run_number + 1);
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(records_processed);
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(pairs_evaluated);
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(pairs_ignored);
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(measure_name);
        distance_results_writer.print(DELIMIT);
        distance_results_writer.print(links ? "links" : "non-links");
        distance_results_writer.print(DELIMIT);

        for (int i = 0; i < NUMBER_OF_THRESHOLDS_SAMPLED; i++) {
            distance_results_writer.print(distance_counts[i]);
            distance_results_writer.print(DELIMIT);
        }
        distance_results_writer.println();
        distance_results_writer.flush();
    }

    class Sample {

        long fp = 0;
        long tp = 0;
        long fn = 0;
        long tn = 0;
    }

    protected class Run {

        final int run_number;
        final LXPMeasure measure;

        final List<LXP> source_record_list1;
        final List<LXP> source_record_list2;

        final CountDownLatch start_gate;
        final CountDownLatch end_gate;

        int records_processed = 0;
        long pairs_evaluated = 0L;
        long pairs_ignored = 0L;

        long[] non_link_distance_counts;
        long[] link_distance_counts;

        Sample[] samples; // Counts of TP, FP etc at each threshold.

        Run(final int run_number, final LXPMeasure measure, final List<LXP> source_record_list1, final List<LXP> source_record_list2, final CountDownLatch start_gate, final CountDownLatch end_gate) {

            this.run_number = run_number;
            this.measure = measure;
            this.source_record_list1 = source_record_list1;
            this.source_record_list2 = source_record_list2;
            this.start_gate = start_gate;
            this.end_gate = end_gate;

            samples = new Sample[NUMBER_OF_THRESHOLDS_SAMPLED];
            for (int i = 0; i < NUMBER_OF_THRESHOLDS_SAMPLED; i++) {
                samples[i] = new Sample();
            }

            if (recordLinkDistances()) {
                non_link_distance_counts = new long[NUMBER_OF_THRESHOLDS_SAMPLED];
                link_distance_counts = new long[NUMBER_OF_THRESHOLDS_SAMPLED];
            }
        }

        public void run() {

            try {
                start_gate.await();

                final long number_of_blocks_to_be_checked = number_of_records_to_be_checked / BLOCK_SIZE;

                for (int block_index = 0; block_index < number_of_blocks_to_be_checked; block_index++) {

                    processBlock(block_index);
                    recordSamples();

                    if (verbose) {
                        System.out.println("finished block: checked " + (block_index + 1) * BLOCK_SIZE + " records");
                        System.out.flush();
                    }
                }

                if (verbose) {
                    System.out.println("Run completed");
                    System.out.flush();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                end_gate.countDown();
            }
        }

        private void processBlock(final int block_index) {

            final int start_index = block_index * BLOCK_SIZE;
            final int end_index = start_index + BLOCK_SIZE;

            for (int i = start_index; i < end_index; i++) {
                processRecordFromFirstSource(i, number_of_records_to_be_checked);
            }

            records_processed += BLOCK_SIZE;
        }

        private void processRecordFromFirstSource(final int record_index, final int last_record_index) {

            final LXP record1 = source_record_list1.get(record_index);

            double min_distance = 1.01;

            boolean made_tentative_link = false;
            boolean tentative_link_is_true_link = false;

            for (int j = 0; j < last_record_index; j++) {

                if (j != record_index) {

                    final LXP record2 = source_record_list2.get(j);

                    final double distance = measure.distance(record1, record2);
                    final LinkStatus link_status = isTrueMatch(record1, record2);
                    final boolean link_is_viable = isViableLink(record1, record2);
                    final boolean distance_is_closest_encountered = distance < min_distance;

                    if (link_status == LinkStatus.UNKNOWN) {
                        pairs_ignored++;

                    } else {

                        final boolean is_true_link = link_status == LinkStatus.TRUE_MATCH;

                        if (allow_multiple_links) {
                            recordLinkDecisions(distance, link_is_viable, is_true_link);
                        } else {

                            if (distance_is_closest_encountered && link_is_viable) {

                                // Undo any previous tentative link decision.
                                if (made_tentative_link) {
                                    undoTentativeLinkDecision(min_distance, tentative_link_is_true_link);
                                }

                                recordLinkDecisions(distance, link_is_viable, is_true_link);

                                // Record these as tentative in case another closer record is found.
                                made_tentative_link = true;
                                tentative_link_is_true_link = is_true_link;

                            } else {
                                recordNegativeLinkDecisions(is_true_link);
                            }
                        }

                        if (recordLinkDistances()) updateTrueLinkCounts(distance, is_true_link);
                        pairs_evaluated++;
                    }

                    if (distance_is_closest_encountered) min_distance = distance;
                }
            }
        }

        private void recordSamples() {

            for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {
                recordSample(run_number, measure.getMeasureName(), records_processed, pairs_evaluated, pairs_ignored, indexToThreshold(threshold_index), samples[threshold_index]);
            }

            if (recordLinkDistances()) recordDistances(run_number, measure.getMeasureName(), records_processed, pairs_evaluated, pairs_ignored, non_link_distance_counts, link_distance_counts);
        }

        private void updateTrueLinkCounts(final double distance, final boolean is_true_link) {

            final int index = distanceToIndex(distance);

            if (is_true_link) {
                link_distance_counts[index]++;
            } else {
                non_link_distance_counts[index]++;
            }
        }

        private void recordNegativeLinkDecisions(final boolean is_true_link) {

            for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {

                if (is_true_link) {
                    samples[threshold_index].fn++;
                } else {
                    samples[threshold_index].tn++;
                }
            }
        }

        private void recordLinkDecisions(final double link_distance, final boolean link_is_viable, final boolean is_true_link) {

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

        private void undoTentativeLinkDecision(final double tentative_link_distance, final boolean tentative_link_is_true_link) {

            for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {

                final double threshold = indexToThreshold(threshold_index);

                if (tentative_link_distance <= threshold) {

                    if (tentative_link_is_true_link) {
                        samples[threshold_index].tp--;
                        samples[threshold_index].fn++;
                    } else {
                        samples[threshold_index].fp--;
                        samples[threshold_index].tn++;
                    }
                }
            }
        }
    }
}

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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

public class AllPairsUmeaSiblingBundling extends ThresholdAnalysis {

    private final Path store_path;
    private final String repo_name;

    private final String DELIMIT = ",";
    private final PrintWriter outstream;

    private static final int BLOCK_SIZE = 100;

    private List<Birth> birth_records;

    private AllPairsUmeaSiblingBundling(final Path store_path, final String repo_name, final String filename) throws IOException {

        super();

        this.store_path = store_path;
        this.repo_name = repo_name;

        if (filename.equals("stdout")) {
            outstream = new PrintWriter(System.out);

        } else {
            importPreviousState(filename);
            outstream = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
        }

        setupRecords();
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

        birth_records = Utilities.randomise(births);
    }

    public void run() throws Exception {

        if (records_processed == 0) {
            outstream.println("Time" + DELIMIT + "Record counter" + DELIMIT + "Pair counter" + DELIMIT + "metric name" + DELIMIT + "threshold" + DELIMIT + "tp" + DELIMIT + "fp" + DELIMIT + "fn" + DELIMIT + "tn" + DELIMIT + "precision" + DELIMIT + "recall" + DELIMIT + "f-measure");
        }

        for (int block_index = records_processed / BLOCK_SIZE; block_index < birth_records.size() / BLOCK_SIZE; block_index++) {

            processBlock(block_index);

            records_processed += BLOCK_SIZE;
            pairs_processed += numberOfPairsProcessed(block_index);

            printSamples();

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
    }

    private void processBlockForMetric(final int block_index, final NamedMetric<LXP> metric, final CountDownLatch start_gate, final CountDownLatch end_gate) {

        try {
            start_gate.await();

            for (int i = block_index * BLOCK_SIZE; i < (block_index + 1) * BLOCK_SIZE; i++) {
                processRecord(i, metric);
            }

        } catch (InterruptedException ignored) {
        } finally {

            end_gate.countDown();
        }
    }

    private void processRecord(final int record_index, final NamedMetric<LXP> metric) {

        final int number_of_records = birth_records.size();

        for (int j = record_index + 1; j < number_of_records; j++) {

            final Birth b1 = birth_records.get(record_index);
            final Birth b2 = birth_records.get(j);

            final double distance = normalise(metric.distance(b1, b2));
            final boolean is_true_link = isTrueLink(b1, b2);

            for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {
                recordSamples(threshold_index, state.get(metric.getMetricName()), is_true_link, distance);
            }
        }
    }

    private int numberOfPairsProcessed(final int block_index) {

        return BLOCK_SIZE * (birth_records.size() - block_index * BLOCK_SIZE - (BLOCK_SIZE + 1) / 2);
    }

    private boolean isTrueLink(final Birth b1, final Birth b2) {

        final String b1_parent_id = b1.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);
        final String b2_parent_id = b2.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);

        return !b1_parent_id.isEmpty() && b1_parent_id.equals(b2_parent_id);
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
            final Sample[] samples = state.get(metric_name);

            for (int threshold_index = 0; threshold_index < NUMBER_OF_THRESHOLDS_SAMPLED; threshold_index++) {
                printSample(records_processed, pairs_processed, metric_name, indexToThreshold(threshold_index), samples[threshold_index]);
            }
        }
    }

    private void printSample(int record_count, long pair_count, String metric_name, Double threshold, Sample sample) {

        outstream.print(LocalDateTime.now());
        outstream.print(DELIMIT);
        outstream.print(record_count);
        outstream.print(DELIMIT);
        outstream.print(pair_count);
        outstream.print(DELIMIT);
        outstream.print(metric_name);
        outstream.print(DELIMIT);
        outstream.print(String.format("%.2f", threshold));
        outstream.print(DELIMIT);
        outstream.print(sample.tp);
        outstream.print(DELIMIT);
        outstream.print(sample.fp);
        outstream.print(DELIMIT);
        outstream.print(sample.fn);
        outstream.print(DELIMIT);
        outstream.print(sample.tn);
        outstream.print(DELIMIT);
        outstream.print(String.format("%.2f", ClassificationMetrics.precision(sample.tp, sample.fp)));
        outstream.print(DELIMIT);
        outstream.print(String.format("%.2f", ClassificationMetrics.recall(sample.tp, sample.fn)));
        outstream.print(DELIMIT);
        outstream.println(String.format("%.2f", ClassificationMetrics.F1(sample.tp, sample.fp, sample.fn)));

        outstream.flush();
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

        new AllPairsUmeaSiblingBundling(store_path, repo_name, "UmeaDistances.csv").run();
    }
}

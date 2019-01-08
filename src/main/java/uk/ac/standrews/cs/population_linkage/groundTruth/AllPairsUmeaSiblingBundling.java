package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

public class AllPairsUmeaSiblingBundling extends ThresholdAnalysis {

    private static final int DUMP_COUNT_INTERVAL = 10000;

    private final Path store_path;
    private final String repo_name;

    private final String DELIMIT = ",";
    private final PrintWriter outstream;

    private static final Duration OUTPUT_INTERVAL = Duration.ofHours(1);

    private AllPairsUmeaSiblingBundling(final Path store_path, final String repo_name, final String filename) throws IOException {

        super();

        this.store_path = store_path;
        this.repo_name = repo_name;

        if (filename.equals("stdout")) {
            outstream = new PrintWriter(System.out);

        } else {
//            importPreviousState(filename);
            outstream = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
        }
    }

    private void importPreviousState(final String filename) throws IOException {

        if (Files.exists(Paths.get(filename))) {

            System.out.println("Importing previous data");

            try (final Stream<String> lines = Files.lines(Paths.get(filename))) {

                lines.skip(1).forEachOrdered(this::importStateLine);
            }
        }
    }

    public void run() throws Exception {

        final RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        System.out.println("Reading records from repository: " + repo_name);

        doAllPairs(record_repository.getBirths());
    }

    private void doAllPairs(Iterable<Birth> births) {

        System.out.println("Randomising record order");

        final List<Birth> birth_records = Utilities.randomise(births);

        long counter = 0;

        if (starting_counter == 0) {
            outstream.println("Time" + DELIMIT + "Record counter" + DELIMIT + "Pair counter" + DELIMIT + "metric name" + DELIMIT + "threshold" + DELIMIT + "tp" + DELIMIT + "fp" + DELIMIT + "fn" + DELIMIT + "tn");
        }

        final int block_size = 10;
        final int number_of_blocks = birth_records.size() / block_size;

        for (int block_count = 0; block_count < number_of_blocks; block_count++) {

            final CountDownLatch start_gate = new CountDownLatch(1);
            final CountDownLatch end_gate = new CountDownLatch(combined_metrics.size());
            final int block_count_fixed = block_count;

            for (final NamedMetric<LXP> metric : combined_metrics) {

                new Thread(() -> {

                    try {
                        start_gate.await();

                    } catch (InterruptedException ignored) {
                    }

                    try {
                        for (int i = block_count_fixed * block_size; i < (block_count_fixed + 1) * block_size; i++) {

                            System.out.println(metric.getMetricName() + ": " + i);
                            System.out.flush();

                            for (int j = i + 1; j < birth_records.size(); j++) {

                                final Birth b1 = birth_records.get(i);
                                final Birth b2 = birth_records.get(j);

                                double distance = normalise(metric.distance(b1, b2));

                                final String b1_parent_id = b1.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);
                                final boolean is_true_link = !b1_parent_id.isEmpty() && b1_parent_id.equals(b2.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY));

                                for (final double thresh : thresholds) {
                                    updateTruthCounts( thresh,  state.get(metric.getMetricName()), is_true_link, distance);
                                }
                            }
                        }
                    } finally {
                        end_gate.countDown();
                    }
                }).start();
            }

            start_gate.countDown();
            try {
                end_gate.await();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            counter += block_size * birth_records.size();

            dumpState(block_count, counter);

            System.out.println("finished block");
            System.out.flush();
        }
    }

    private void dumpState(final int block_count, final long counter) {

        for (final NamedMetric<LXP> metric : combined_metrics) {

            final String metric_name = metric.getMetricName();
            final Map<Double, Line> threshold_map = state.get(metric_name);

            for (final Double threshold : thresholds) {
                printTruthCount(block_count, counter, metric_name, threshold, threshold_map.get(threshold));
            }
        }
    }

    private void updateTruthCounts(double threshold, final Map<Double, Line> map, boolean is_true_link, double distance) {

        Line truths = map.get(threshold);

        if (distance <= threshold) {

            if (is_true_link) {
                truths.tp++;
            } else {
                truths.fp++;
            }

        } else {
            if (is_true_link) {
                truths.fn++;
            } else {
                truths.tn++;
            }
        }
    }

    private void printTruthCount(int block_count, long counter, String metric_name, Double thresh, Line truth_count) {

        outstream.println(LocalDateTime.now() + DELIMIT + block_count + DELIMIT + counter + DELIMIT + metric_name + DELIMIT + String.format("%.2f", thresh) + DELIMIT + truth_count.tp + DELIMIT + truth_count.fp + DELIMIT + truth_count.fn + DELIMIT + truth_count.tn);
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

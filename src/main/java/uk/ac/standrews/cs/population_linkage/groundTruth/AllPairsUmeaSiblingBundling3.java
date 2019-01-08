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
import java.util.stream.Stream;

public class AllPairsUmeaSiblingBundling3 extends ThresholdAnalysis {

    private static final int DUMP_COUNT_INTERVAL = 10000;

    private final Path store_path;
    private final String repo_name;

    private final String DELIMIT = ",";
    private final PrintWriter outstream;

    private static final Duration OUTPUT_INTERVAL = Duration.ofHours(1);

    private AllPairsUmeaSiblingBundling3(final Path store_path, final String repo_name, final String filename) throws IOException {

        super();

        this.store_path = store_path;
        this.repo_name = repo_name;

        if (filename.equals("stdout")) {
            outstream = new PrintWriter(System.out);

        } else {
            importPreviousState(filename);
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
            outstream.println("Time" + DELIMIT + "Pair counter" + DELIMIT + "metric name" + DELIMIT + "threshold" + DELIMIT + "tp" + DELIMIT + "fp" + DELIMIT + "fn" + DELIMIT + "tn");
        }

        System.out.println("Skipping previous output");

        boolean first_iteration = true;
        LocalDateTime start_time = LocalDateTime.now();

        for (int i = 0; i < birth_records.size() - 1; i++) {
            for (int j = i + 1; j < birth_records.size(); j++) {

                if (counter >= starting_counter) {

                    if (first_iteration) {
                        System.out.println("Starting new calculations");

                    } else {
                        if (counter % DUMP_COUNT_INTERVAL == 0) {

                            final LocalDateTime now = LocalDateTime.now();

                            if (OUTPUT_INTERVAL.minus(Duration.between(start_time, now)).isNegative()) {
                                start_time = now;
                                dumpState(counter, start_time);
                            }
                        }
                    }

                    final Birth b1 = birth_records.get(i);
                    final Birth b2 = birth_records.get(j);

                    for (final NamedMetric<LXP> metric : combined_metrics) {
                        for (final double thresh : thresholds) {
                            updateTruthCounts(metric, thresh, b1, b2);
                        }
                    }

                    first_iteration = false;
                }

                counter++;
            }
        }
    }

    private void dumpState(final long counter, final LocalDateTime time) {

        for (final NamedMetric<LXP> metric : combined_metrics) {

            final String metric_name = metric.getMetricName();
            final Map<Double, Line> threshold_map = state.get(metric_name);

            for (final Double threshold : thresholds) {
                printTruthCount(time, counter, metric_name, threshold, threshold_map.get(threshold));
            }
        }
    }

    private void updateTruthCounts(NamedMetric<LXP> metric, double thresh, Birth b1, Birth b2) {

        String metricName = metric.getMetricName();

        Line truths = state.get(metricName).get(thresh);

        double distance = metric.distance(b1, b2);
        double normalised_distance = normalise(distance);

        String b1_parent_id = b1.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);
        boolean is_true_link = !b1_parent_id.isEmpty() && b1_parent_id.equals(b2.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY));

        if (normalised_distance <= thresh) {
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

    private void printTruthCount(LocalDateTime time, long counter, String metric_name, Double thresh, Line truth_count) {

        outstream.println(time + DELIMIT + counter + DELIMIT + metric_name + DELIMIT + String.format("%.2f", thresh) + DELIMIT + truth_count.tp + DELIMIT + truth_count.fp + DELIMIT + truth_count.fn + DELIMIT + truth_count.tn);
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
        String repo_name = ApplicationProperties.getRepositoryName();

        new AllPairsUmeaSiblingBundling3(store_path, repo_name, "UmeaDistances.csv").run();
    }
}

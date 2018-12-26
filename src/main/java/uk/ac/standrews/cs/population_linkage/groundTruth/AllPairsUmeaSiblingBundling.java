package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.*;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class AllPairsUmeaSiblingBundling {

    private static final int CHARVAL = 512;
    private static final int DUMP_COUNT_INTERVAL = 10000;
    public static final long SEED = 34553543456223L;
    private final Path store_path;
    private final String repo_name;

    private final String DELIMIT = ",";
    private final PrintStream outstream;

    private static final Duration OUTPUT_INTERVAL = Duration.ofHours(1);

    private static final List<NamedMetric<String>> BASE_METRICS = Arrays.asList(
            new Levenshtein(),
            new Jaccard(),
            new Cosine(),
            new SED(CHARVAL),
            new JensenShannon(),
            new JensenShannon2(CHARVAL));

    private static final List<Integer> SIBLING_BUNDLING_FIELDS = Arrays.asList(
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.PARENTS_PLACE_OF_MARRIAGE,
            Birth.PARENTS_DAY_OF_MARRIAGE,
            Birth.PARENTS_MONTH_OF_MARRIAGE,
            Birth.PARENTS_YEAR_OF_MARRIAGE);

    private final List<Double> thresholds;
    private List<NamedMetric<LXP>> combined_metrics;

    private final Map<String, Map<Double, TruthCounts>> state; // Maps from metric name to Map from threshold to counts of TPFP etc.

    private AllPairsUmeaSiblingBundling(Path store_path, String repo_name, String filename) throws Exception {

        this.store_path = store_path;
        this.repo_name = repo_name;

        if (filename.equals("stdout")) {
            outstream = System.out;
        } else {
            outstream = new PrintStream(filename);
        }

        combined_metrics = getCombinedMetrics();
        thresholds = getThresholds();
        state = initialiseState();
    }

    private Map<String, Map<Double, TruthCounts>> initialiseState() {

        Map<String, Map<Double, TruthCounts>> result = new HashMap<>();

        for (NamedMetric<LXP> metric : combined_metrics) {
            Map<Double, TruthCounts> threshold_map = new HashMap<>();
            for (Double threshold : thresholds) {
                threshold_map.put(threshold, new TruthCounts());
            }
            String metricName = metric.getMetricName();
            result.put(metricName, threshold_map);
        }
        return result;
    }

    private List<NamedMetric<LXP>> getCombinedMetrics() {

        List<NamedMetric<LXP>> result = new ArrayList<>();

        for (NamedMetric<String> base_metric : BASE_METRICS) {
            result.add(new Sigma(base_metric, SIBLING_BUNDLING_FIELDS));
        }
        return result;
    }

    private List<Double> getThresholds() {

        List<Double> result = new ArrayList<>();
        for (double threshold = 0.01; threshold < 1; threshold += 0.01) {
            result.add(threshold);
        }
        return result;
    }

    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        System.out.println("Reading records from repository: " + repo_name);
        System.out.println("Creating Sibling Bundling ground truth");
        System.out.println();

        doAllPairs(record_repository.getBirths());
    }

    private void doAllPairs(Iterable<Birth> births) {

        LocalDateTime start_time = LocalDateTime.now();

        List<Birth> birth_records = getBirthsInRandomOrder(births);

        long counter = 0;

        outstream.println("Time" + DELIMIT + "Pair counter" + DELIMIT + "metric name" + DELIMIT + "threshold" + DELIMIT + "tp" + DELIMIT + "fp" + DELIMIT + "fn" + DELIMIT + "tn");

        for (int i = 0; i < birth_records.size() - 1; i++) {
            for (int j = i + 1; j < birth_records.size(); j++) {

                Birth b1 = birth_records.get(i);
                Birth b2 = birth_records.get(j);

                counter++;

                for (NamedMetric<LXP> metric : combined_metrics) {
                    for (double thresh : thresholds) {
                        updateTruthCounts(metric, thresh, b1, b2);
                    }
                }

                if (counter % DUMP_COUNT_INTERVAL == 0) {
                    final LocalDateTime now = LocalDateTime.now();
                    if (OUTPUT_INTERVAL.minus(Duration.between(start_time, now)).isNegative()) {
                        start_time = now;
                        dumpState(counter, start_time);
                    }
                }
            }
        }
    }

    private List<Birth> getBirthsInRandomOrder(final Iterable<Birth> births) {

        Random random = new Random(SEED);

        List<Birth> birth_records = new ArrayList<>();
        for (Birth b : births) {
            birth_records.add(b);
        }

        int number_of_records = birth_records.size();

        for (int i = 0; i < number_of_records; i++) {
            int swap_index = random.nextInt(number_of_records);
            Birth temp = birth_records.get(i);
            birth_records.set(i, birth_records.get(swap_index));
            birth_records.set(swap_index, temp);
        }
        return birth_records;
    }

    private void dumpState(long counter, LocalDateTime time) {
        for (NamedMetric<LXP> metric : combined_metrics) {
            String metric_name = metric.getMetricName();
            Map<Double, TruthCounts> thresh_map = state.get(metric_name);
            for (Map.Entry<Double, TruthCounts> entry : thresh_map.entrySet()) {
                printTruthCount(time, counter, metric_name, entry.getKey(), entry.getValue());
            }
        }
    }

    private void updateTruthCounts(NamedMetric<LXP> metric, double thresh, Birth b1, Birth b2) {

        String metricName = metric.getMetricName();

        TruthCounts truths = state.get(metricName).get(thresh);

        double distance = metric.distance(b1, b2);
        double normalised_distance = normalise(distance);

        boolean is_true_link = b1.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY).equals(b2.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY));

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

    private void printTruthCount(LocalDateTime time, long counter, String metric_name, Double thresh, TruthCounts truth_count) {

        outstream.println(time.toString() + DELIMIT + counter + DELIMIT + metric_name + DELIMIT + String.format("%.2f", thresh) + DELIMIT + truth_count.tp + DELIMIT + truth_count.fp + DELIMIT + truth_count.fn + DELIMIT + truth_count.tn);
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

        new AllPairsUmeaSiblingBundling(store_path, repo_name, "UmeaDistances.csv").run();
    }

    private class TruthCounts {

        int fp = 0;
        int tp = 0;
        int fn = 0;
        int tn = 0;
    }
}

package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.*;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class AnalyseUmeaSiblingBundlingThresholds {

    private static final int CHARVAL = 512;

    private final Path store_path;
    private final String repo_name;

    private final String DELIMIT = ",";

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

    private final String filename;
    private String best_metric_name;
    private double best_threshold = 0.0;
    private double best_f_measure = 0.0;

    private AnalyseUmeaSiblingBundlingThresholds(Path store_path, String repo_name, String filename) throws IOException {

        this.store_path = store_path;
        this.repo_name = repo_name;

        combined_metrics = getCombinedMetrics();
        thresholds = getThresholds();
        state = initialiseState();

        this.filename = filename;
    }

    private void importPreviousState() throws IOException {


        try (final Stream<String> lines = Files.lines(Paths.get(filename))) {

            lines.skip(1).forEachOrdered(this::importStateLine);
        }

    }

    private void importStateLine(final String line) {

        String[] fields = line.split(",");

        String metric_name = fields[2];
        double threshold = Double.parseDouble(fields[3]);
        int tp = Integer.parseInt(fields[4]);
        int fp = Integer.parseInt(fields[5]);
        int fn = Integer.parseInt(fields[6]);
        int tn = Integer.parseInt(fields[7]);

        setStateValue(metric_name, threshold, tp, fp, fn, tn);
    }

    private void setStateValue(final String metric_name, final double threshold, final int tp, final int fp, final int fn, final int tn) {

        TruthCounts truths = state.get(metric_name).get(threshold);

        truths.tp = tp;
        truths.fp = fp;
        truths.fn = fn;
        truths.tn = tn;
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
        for (int i = 1; i < 100; i++) {
            result.add(((double) i) / 100);
        }
        return result;
    }

    public void run() throws Exception {

        importPreviousState();
        calculateMeasures();
        calculateProgressionForBestParameters();
    }

    private void calculateProgressionForBestParameters() throws IOException {

        System.out.println("F-measure progression for " + best_metric_name + " with threshold " + best_threshold);
        System.out.println("Iterations,F-measure");

        try (final Stream<String> lines = Files.lines(Paths.get(filename))) {

            lines.skip(1).forEachOrdered(this::checkStateLine);
        }
    }

    private void checkStateLine(final String line) {

        String[] fields = line.split(",");

        int iterations = Integer.parseInt(fields[1]);
        String metric_name = fields[2];
        double threshold = Double.parseDouble(fields[3]);
        int tp = Integer.parseInt(fields[4]);
        int fp = Integer.parseInt(fields[5]);
        int fn = Integer.parseInt(fields[6]);

        if (metric_name.equals(best_metric_name) && threshold == best_threshold) {

            double precision = ((double) tp) / (tp + fp);
            double recall = ((double) tp) / (tp + fn);
            double f_measure = (2 * precision * recall) / (precision + recall);

            System.out.println(iterations + DELIMIT + String.format("%.2f", f_measure));
        }
    }

    private void calculateMeasures() {

        for (NamedMetric<LXP> metric : combined_metrics) {

            String metric_name = metric.getMetricName();

            Map<Double, TruthCounts> thresh_map = state.get(metric_name);

            System.out.println(metric_name);
            System.out.println("Threshold,Precision,Recall,F-Measure");

            for (int i = 1; i < 100; i++) {

                double threshold = ((double) i) / 100;
                TruthCounts truth_counts = thresh_map.get(threshold);

                double precision = ((double) truth_counts.tp) / (truth_counts.tp + truth_counts.fp);
                double recall = ((double) truth_counts.tp) / (truth_counts.tp + truth_counts.fn);
                double f_measure = (2 * precision * recall) / (precision + recall);

                System.out.println(threshold + DELIMIT + String.format("%.2f", precision) + DELIMIT + String.format("%.2f", recall) + DELIMIT + String.format("%.2f", f_measure));

                if (f_measure > best_f_measure) {
                    best_f_measure = f_measure;
                    best_metric_name = metric_name;
                    best_threshold = threshold;
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = ApplicationProperties.getRepositoryName();

        new AnalyseUmeaSiblingBundlingThresholds(store_path, repo_name, "UmeaDistances.csv").run();
    }

    private class TruthCounts {

        int fp = 0;
        int tp = 0;
        int fn = 0;
        int tn = 0;
    }
}

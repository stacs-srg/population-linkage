package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class AnalyseUmeaSiblingBundlingThresholds extends ThresholdAnalysis {

    private final String DELIMIT = ",";

    private final String filename;
    private String best_metric_name;
    private double best_threshold = 0.0;
    private double best_f_measure = 0.0;

    final List<Double> progression_thresholds;
    final Map<String, Map<Double, XXX>> progression;

    private AnalyseUmeaSiblingBundlingThresholds(String filename) {

        super();
        this.filename = filename;

        progression_thresholds = getProgressionThresholds();
        progression = initialiseProgression();
    }

    public void run() throws Exception {

        importPreviousState();
//        calculateMeasures();
        recordProgression();
        recordProgression2();
//        outputProgression();
    }

    private List<Double> getProgressionThresholds() {

        final List<Double> result = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            result.add(((double) i) / 10);
        }
        return result;
    }

    private Map<String, Map<Double, XXX>> initialiseProgression() {

        final Map<String, Map<Double, XXX>> result = new HashMap<>();

        for (final NamedMetric<LXP> metric : combined_metrics) {
            final Map<Double, XXX> threshold_map = new HashMap<>();
            for (final Double threshold : progression_thresholds) {
                threshold_map.put(threshold, new XXX());
            }
            final String metricName = metric.getMetricName();
            result.put(metricName, threshold_map);
        }
        return result;
    }

    private void importPreviousState() throws IOException {

        try (final Stream<String> lines = Files.lines(Paths.get(filename))) {

            lines.skip(1).forEachOrdered(this::importStateLine);
        }
    }

    private void calculateMeasures() {

        for (final NamedMetric<LXP> metric : combined_metrics) {

            final String metric_name = metric.getMetricName();

            final Map<Double, Line> threshold_map = state.get(metric_name);

            System.out.println(metric_name);
            System.out.println("Threshold,Precision,Recall,F-Measure");

            for (int i = 1; i < 100; i++) {

                final double threshold = ((double) i) / 100;
                Line truth_counts = threshold_map.get(threshold);

                final double precision = precision(truth_counts.tp, truth_counts.fp);
                final double recall = recall(truth_counts.tp, truth_counts.fn);
                final double f_measure = fMeasure(precision, recall);

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

    private void recordProgression() throws IOException {

        try (final Stream<String> lines = Files.lines(Paths.get(filename))) {

            lines.skip(1).forEachOrdered(this::checkStateLine2);
        }
    }

    private void recordProgression2() throws IOException {

        System.out.println("iterations,f_measure,difference from final,difference from previous,days");

        try (final Stream<String> lines = Files.lines(Paths.get(filename))) {

            lines.skip(1).forEachOrdered(this::checkStateLine3);
        }
    }

    private void outputProgression() {

        System.out.println("metric,threshold,lowest_f_measure,highest_f_measure,first_f_measure,last_f_measure,proportional_difference_from_last,iterations_for_first,iterations_for_last,number_of_samples");

        for (final NamedMetric<LXP> metric : combined_metrics) {

            Map<Double, XXX> map = progression.get(metric.getMetricName());
            for (final double threshold : progression_thresholds) {
                XXX xxx = map.get(threshold);
                System.out.println(String.format("%s,%f,%.3f,%.3f,%.3f,%.3f,%d,%d,%d",
                        metric.getMetricName(), threshold,
                        xxx.lowest_f_measure, xxx.highest_f_measure, xxx.first_f_measure, xxx.last_f_measure,
                        xxx.iterations_for_first, xxx.iterations_for_last, xxx.number_of_samples));
            }
        }
    }

    private void checkStateLine(final String line) {

        final Line parsed_line = parseStateLine(line);

        if (parsed_line.metric_name.equals(best_metric_name) && parsed_line.threshold == best_threshold) {

            final double precision = precision(parsed_line.tp, parsed_line.fp);
            final double recall = recall(parsed_line.tp, parsed_line.fn);
            final double f_measure = fMeasure(precision, recall);

            System.out.println(parsed_line.iterations + DELIMIT + String.format("%.2f", f_measure));
        }
    }

    private void checkStateLine2(final String line) {

        final Line parsed_line = parseStateLine(line);

        Map<Double, XXX> map = progression.get(parsed_line.metric_name);
        if (map.containsKey(parsed_line.threshold)) {

            XXX xxx = map.get(parsed_line.threshold);

            final double precision = precision(parsed_line.tp, parsed_line.fp);
            final double recall = recall(parsed_line.tp, parsed_line.fn);
            final double f_measure = fMeasure(precision, recall);

            if (f_measure < xxx.lowest_f_measure) xxx.lowest_f_measure = f_measure;
            if (f_measure > xxx.highest_f_measure) xxx.highest_f_measure = f_measure;
            if (xxx.number_of_samples == 0) {
                xxx.first_f_measure = f_measure;
                xxx.iterations_for_first = parsed_line.iterations;
            }
            xxx.last_f_measure = f_measure;
            xxx.iterations_for_last = parsed_line.iterations;
            xxx.number_of_samples++;
        }
    }

    private void checkStateLine3(final String line) {

        final Line parsed_line = parseStateLine(line);

        Map<Double, XXX> map = progression.get(parsed_line.metric_name);
        if (map.containsKey(parsed_line.threshold)) {

            XXX xxx = map.get(parsed_line.threshold);

            final double precision = precision(parsed_line.tp, parsed_line.fp);
            final double recall = recall(parsed_line.tp, parsed_line.fn);
            final double f_measure = fMeasure(precision, recall);

            if (parsed_line.metric_name.equals("Sigma Over Jaccard") && parsed_line.threshold == 0.9) {
                double diff_from_final = Math.abs(f_measure - xxx.last_f_measure)/xxx.last_f_measure;
                if (previous_f_measure == 0.0) previous_f_measure = f_measure;
                double diff_from_previous = Math.abs(f_measure - previous_f_measure)/ previous_f_measure;
                int days_elapsed = (int)(8 * ((double)parsed_line.iterations) / xxx.iterations_for_last);
                System.out.println(String.format("%d,%.3f,%.4f,%.4f,%d",parsed_line.iterations, f_measure, diff_from_final,diff_from_previous,days_elapsed));
                previous_f_measure = f_measure;
            }
        }
    }

    private double previous_f_measure = 0.0;

    private double precision(final int tp, final int fp) {
        return ((double) tp) / (tp + fp);
    }

    private double recall(final int tp, final int fn) {
        return ((double) tp) / (tp + fn);
    }

    private double fMeasure(final double precision, final double recall) {
        return (2 * precision * recall) / (precision + recall);
    }

    public static void main(final String[] args) throws Exception {

        new AnalyseUmeaSiblingBundlingThresholds("UmeaDistances.csv").run();
    }

    class XXX {

        double lowest_f_measure = 1.0;
        double highest_f_measure = 0.0;
        double first_f_measure = 0.0;
        double last_f_measure = 0.0;
        int iterations_for_first = 0;
        int iterations_for_last = 0;
        int number_of_samples = 0;
    }
}

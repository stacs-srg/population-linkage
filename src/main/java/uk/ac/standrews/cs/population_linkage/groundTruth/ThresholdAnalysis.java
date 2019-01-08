package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.*;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.*;

class ThresholdAnalysis {

    private static final int CHARVAL = 512;

    final Map<String, Map<Double, Line>> state; // Maps from metric name to Map from threshold to counts of TPFP etc.
    final List<Double> thresholds;
    final List<NamedMetric<LXP>> combined_metrics;

    int starting_counter = 0;

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

    ThresholdAnalysis() {

        combined_metrics = getCombinedMetrics();
        thresholds = getThresholds();
        state = initialiseState();
    }

    private Map<String, Map<Double, Line>> initialiseState() {

        final Map<String, Map<Double, Line>> result = new HashMap<>();

        for (final NamedMetric<LXP> metric : combined_metrics) {
            final Map<Double, Line> threshold_map = new HashMap<>();
            for (final Double threshold : thresholds) {
                threshold_map.put(threshold, new Line());
            }
            final String metricName = metric.getMetricName();
            result.put(metricName, threshold_map);
        }
        return result;
    }

    private List<NamedMetric<LXP>> getCombinedMetrics() {

        final List<NamedMetric<LXP>> result = new ArrayList<>();

        for (final NamedMetric<String> base_metric : BASE_METRICS) {
            result.add(new Sigma(base_metric, SIBLING_BUNDLING_FIELDS));
        }
        return result;
    }

    private List<Double> getThresholds() {

        final List<Double> result = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            result.add(((double) i) / 100);
        }
        return result;
    }

    void importStateLine(final String line) {

        try {
            setStateValue(parseStateLine(line));

        } catch (Exception e) {
            System.out.println("error parsing line: " + line);
            throw e;
        }
    }

    Line parseStateLine(final String line) {

        final Line result = new Line();

        final String[] fields = line.split(",");

        result.iterations = Integer.parseInt(fields[1]);
        result.metric_name = fields[2];
        result.threshold = Double.parseDouble(fields[3]);
        result.tp = Integer.parseInt(fields[4]);
        result.fp = Integer.parseInt(fields[5]);
        result.fn = Integer.parseInt(fields[6]);
        result.tn = Integer.parseInt(fields[7]);

        return result;
    }

    private void setStateValue(final Line line) {

        final Line truths = state.get(line.metric_name).get(line.threshold);

        truths.tp = line.tp;
        truths.fp = line.fp;
        truths.fn = line.fn;
        truths.tn = line.tn;

        starting_counter = line.tp + line.fp + line.fn + line.tn;
    }

    class Line {

        int iterations = 0;
        String metric_name = "";
        double threshold = 0.0;
        int fp = 0;
        int tp = 0;
        int fn = 0;
        int tn = 0;
    }
}

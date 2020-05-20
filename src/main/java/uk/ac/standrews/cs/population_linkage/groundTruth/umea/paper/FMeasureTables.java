/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.umea.paper;

import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class FMeasureTables {

    public static final Map<String, String> METRIC_ABBREVIATIONS = new HashMap<>();
    public static final Path INPUT_FILES_DIRECTORY_PATH = Paths.get("/Users/graham/Desktop/data");

    public static final List<String> FILE_NAME_ROOTS = Arrays.asList(
            "UmeaBirthBrideIdentity", "UmeaBirthDeathIdentity", "UmeaBirthFatherIdentity", "UmeaBirthGroomIdentity", "UmeaBirthMotherIdentity",
            "UmeaBirthSibling", "UmeaBrideBrideSibling", "UmeaBrideGroomSibling", "UmeaGroomGroomSibling", "UmeaDeathSibling");

    static {
        METRIC_ABBREVIATIONS.put("BagDistance", "Bag");
        METRIC_ABBREVIATIONS.put("Cosine", "Cos");
        METRIC_ABBREVIATIONS.put("Damerau-Levenshtein", "DL");
        METRIC_ABBREVIATIONS.put("Dice", "Dice");
        METRIC_ABBREVIATIONS.put("Jaccard", "Jac");
        METRIC_ABBREVIATIONS.put("Jaro", "Jar");
        METRIC_ABBREVIATIONS.put("JaroWinkler", "JW");
        METRIC_ABBREVIATIONS.put("JensenShannon", "JS");
        METRIC_ABBREVIATIONS.put("Levenshtein", "Lev");
        METRIC_ABBREVIATIONS.put("LongestCommonSubstring", "LCS");
        METRIC_ABBREVIATIONS.put("Metaphone-Levenshtein", "ML");
        METRIC_ABBREVIATIONS.put("NeedlemanWunsch", "NW");
        METRIC_ABBREVIATIONS.put("NYSIIS-Levenshtein", "NL");
        METRIC_ABBREVIATIONS.put("SED", "SED");
        METRIC_ABBREVIATIONS.put("SmithWaterman", "SW");
    }

    public static void main(String[] args) throws IOException {

        final Map<String, Map<String, Double>> max_f_measures = getMaxFMeasures();
        final Map<String, Map<String, Double>> max_windowed_f_measures = getMaxWindowedFMeasures();

        outputTable(max_f_measures);
        outputTable(max_windowed_f_measures);
    }

    private static Map<String, Map<String, Double>> getMaxFMeasures() throws IOException {

        final Map<String, Map<String, Double>> max_f_measures = new TreeMap<>();

        for (String linkage : FILE_NAME_ROOTS) {

            final Map<String, Double> results_per_linkage = getMaxFMeasures(linkage);
            max_f_measures.put(linkage, results_per_linkage);
        }
        return max_f_measures;
    }

    private static Map<String, Double> getMaxFMeasures(final String linkage) throws IOException {

        Map<String, Double> results_per_linkage = new HashMap<>();

        DataSet data = loadData(linkage);

        for (String metric : getMetrics(data)) {

            double best_f = getBestF(data, metric);
            results_per_linkage.put(metric, best_f);
        }
        return results_per_linkage;
    }

    private static Map<String, Map<String, Double>> getMaxWindowedFMeasures() throws IOException {

        final Map<String, Map<String, Double>> max_windowed_f_measures = new TreeMap<>();

        for (String linkage : FILE_NAME_ROOTS) {

            final Map<String, Double> results_per_linkage = getMaxWindowedFMeasures(linkage);
            max_windowed_f_measures.put(linkage, results_per_linkage);
        }
        return max_windowed_f_measures;
    }

    private static Map<String, Double> getMaxWindowedFMeasures(final String linkage) throws IOException {

        Map<String, Double> results_per_linkage = new HashMap<>();

        DataSet data = loadData(linkage);

        for (String metric : getMetrics(data)) {

            double best_windowed_f = getBestWindowedF(data, metric);
            results_per_linkage.put(metric, best_windowed_f);
        }
        return results_per_linkage;
    }

    private static void outputTable(final Map<String, Map<String, Double>> max_f_measures) {

        printDivider();
        printHeaderRow(max_f_measures);
        printDivider();
        printRows(max_f_measures);
        printDivider();
        printMeanRow(max_f_measures);

        System.out.println();
        System.out.println();
    }

    private static void printDivider() {

        System.out.println("\\hline \\noalign{\\smallskip}");
    }

    private static void printHeaderRow(final Map<String, Map<String, Double>> values_by_linkage_and_metric) {

        final List<String> metrics_sorted_by_decreasing_values = sortMetricsByDecreasingMeanValues(values_by_linkage_and_metric);

        System.out.print(bold("Linkage") + " & ");
        for (String metric : metrics_sorted_by_decreasing_values) {
            System.out.print(METRIC_ABBREVIATIONS.get(metric) + " & ");
        }
        System.out.println(bold("Mean") + " & " + bold("Max") + " & " + bold("SD") + " \\\\");
    }

    private static void printRows(final Map<String, Map<String, Double>> values_by_linkage_and_metric) {

        final List<String> metrics_sorted_by_decreasing_values = sortMetricsByDecreasingMeanValues(values_by_linkage_and_metric);

        int linkage_number = 1;
        for (Map.Entry<String, Map<String, Double>> entry : values_by_linkage_and_metric.entrySet()) {

            System.out.print(linkage_number++ + " & ");
            final Map<String, Double> values_for_linkage = entry.getValue();

            final double max = max(values_for_linkage);
            final double mean = mean(values_for_linkage);
            final double std_dev = standardDeviation(values_for_linkage);

            for (String metric : metrics_sorted_by_decreasing_values) {

                final double value = values_for_linkage.get(metric);
                final boolean embolden = roundTo2DecimalPlaces(value) == roundTo2DecimalPlaces(max);
                System.out.printf(bold("%.2f", embolden) + " & ", value);
            }

            System.out.printf("%.2f & %.2f & %.2f \\\\", mean, max, std_dev);
            System.out.println();
        }
    }

    private static void printMeanRow(final Map<String, Map<String, Double>> values_by_linkage_and_metric) {

        final List<String> metrics_sorted_by_decreasing_values = sortMetricsByDecreasingMeanValues(values_by_linkage_and_metric);

        System.out.print(bold("Mean") + " & ");
        for (String metric : metrics_sorted_by_decreasing_values) {
            System.out.printf("%.2f & ", getMeanForMetric(values_by_linkage_and_metric, metric));
        }
        System.out.println();
    }

    private static List<String> sortMetricsByDecreasingMeanValues(final Map<String, Map<String, Double>> values_by_linkage_and_metric) {

        final Map<String, Double> mean_values = getMeanValuesPerMetric(values_by_linkage_and_metric);

        // Order by decreasing values, and then alphabetically by metric name.
        final Comparator<Map.Entry<String, Double>> comparator = (o1, o2) -> {

            double rounded_value1 = roundTo2DecimalPlaces(o1.getValue());
            double rounded_value2 = roundTo2DecimalPlaces(o2.getValue());

            if (rounded_value1 < rounded_value2) return 1;
            if (rounded_value1 > rounded_value2) return -1;
            return o1.getKey().compareTo(o2.getKey());
        };

        return mean_values.entrySet().stream().sorted(comparator).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private static double roundTo2DecimalPlaces(double d) {

        return Math.round(d * 100.0) / 100.0;
    }

    private static Map<String, Double> getMeanValuesPerMetric(final Map<String, Map<String, Double>> max_f_measures) {

        final Map<String, Double> mean_f_measures = new HashMap<>();

        for (String metric : getMetrics(max_f_measures)) {
            mean_f_measures.put(metric, getMeanForMetric(max_f_measures, metric));
        }

        return mean_f_measures;
    }

    private static String bold(String s) {
        return "\\textbf{" + s + "}";
    }

    private static String bold(String s, boolean condition) {
        return condition ? bold(s) : s;
    }

    private static double mean(final Map<String, Double> f_measures) {
        return mean(f_measures.values());
    }

    private static double mean(final Collection<Double> f_measures) {

        double sum = 0.0;
        for (double d : f_measures) sum += d;
        return sum / f_measures.size();
    }

    private static double max(final Map<String, Double> f_measures) {

        double max = 0.0;
        for (double d : f_measures.values()) if (d > max) max = d;
        return max;
    }

    private static double standardDeviation(final Map<String, Double> f_measures) {

        final double mean = mean(f_measures);

        double sum_of_squares = 0.0;
        for (double d : f_measures.values()) sum_of_squares += Math.pow(mean - d, 2);
        return Math.sqrt(sum_of_squares / f_measures.size());
    }

    private static double getMeanForMetric(final Map<String, Map<String, Double>> max_f_measures, final String metric) {

        double sum = 0.0;

        for (Map<String, Double> linkage_result : max_f_measures.values()) {
            sum += linkage_result.get(metric);
        }

        return sum / max_f_measures.keySet().size();
    }

    private static List<String> getMetrics(final DataSet data) {

        final List<String> result = new ArrayList<>();

        for (List<String> row : data.getRecords()) {
            final String metric = data.getValue(row, "metric");
            if (!result.contains(metric)) result.add(metric);
        }
        return result;
    }

    private static List<String> getMetrics(final Map<String, Map<String, Double>> max_f_measures) {

        for (Map<String, Double> row : max_f_measures.values()) {
            return new ArrayList<>(row.keySet());
        }
        return new ArrayList<>();
    }

    private static double getBestF(final DataSet data_set, String metric) {

        final int max_records_processed = getMaxRecordsProcessed(data_set);
        final DataSet filtered_data = data_set.select((list, ds) -> Integer.parseInt(ds.getValue(list, "records processed")) == max_records_processed && ds.getValue(list, "metric").equals(metric));

        double max_f = 0.0;

        for (List<String> row : filtered_data.getRecords()) {

            final double f = Double.parseDouble(data_set.getValue(row, "f_measure"));
            if (f > max_f) max_f = f;
        }
        return max_f;
    }

    private static final int WINDOW_SIZE = 10;

    private static double getBestWindowedF(final DataSet data_set, String metric) {

        final int max_records_processed = getMaxRecordsProcessed(data_set);
        final DataSet filtered_data = data_set.select((list, ds) -> Integer.parseInt(ds.getValue(list, "records processed")) == max_records_processed && ds.getValue(list, "metric").equals(metric));

        double max_windowed_f = 0.0;

        List<Double> window_values = new ArrayList<>();

        for (List<String> row : filtered_data.getRecords()) {

            final double f = Double.parseDouble(data_set.getValue(row, "f_measure"));
            window_values.add(f);

            if (window_values.size() >= WINDOW_SIZE) {
                if (window_values.size() > WINDOW_SIZE) window_values.remove(0);

                final double windowed_f = mean(window_values);
                if (windowed_f > max_windowed_f) max_windowed_f = windowed_f;
            }
        }
        return max_windowed_f;
    }

    private static int getMaxRecordsProcessed(final DataSet data_set) {

        int result = 0;

        for (List<String> row : data_set.getRecords()) {

            int records_processed = Integer.parseInt(data_set.getValue(row, "records processed"));
            if (records_processed > result) result = records_processed;
        }
        return result;
    }

    private static DataSet loadData(final String file_name_root) throws IOException {

        Path path = getInputFilePath(file_name_root);
        return new DataSet(path);
    }

    private static Path getInputFilePath(final String file_name_root) {

        return INPUT_FILES_DIRECTORY_PATH.resolve(file_name_root + "PRFByThreshold.csv");
    }
}

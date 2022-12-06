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
package uk.ac.standrews.cs.population_linkage.groundTruth.umea.paper;

import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class FMeasureTables {

    public static final Map<String, String> MEASURE_ABBREVIATIONS = new HashMap<>();
    public static final Path INPUT_FILES_DIRECTORY_PATH = Paths.get("/Users/graham/Desktop/data");

    public static final List<String> FILE_NAME_ROOTS = Arrays.asList(
            "UmeaBirthBrideIdentity", "UmeaBirthDeathIdentity", "UmeaBirthFatherIdentity", "UmeaBirthParentsMarriageIdentity", "FelligiSunterBirthParentsMarriageAnalysis",
            "UmeaBirthSibling", "UmeaBrideBrideSibling", "UmeaBrideGroomSibling", "UmeaGroomGroomSibling", "UmeaDeathSibling");

    static {
        MEASURE_ABBREVIATIONS.put("BagDistance", "Bag");
        MEASURE_ABBREVIATIONS.put("Cosine", "Cos");
        MEASURE_ABBREVIATIONS.put("Damerau-Levenshtein", "DL");
        MEASURE_ABBREVIATIONS.put("Dice", "Dice");
        MEASURE_ABBREVIATIONS.put("Jaccard", "Jac");
        MEASURE_ABBREVIATIONS.put("Jaro", "Jar");
        MEASURE_ABBREVIATIONS.put("JaroWinkler", "JW");
        MEASURE_ABBREVIATIONS.put("JensenShannon", "JS");
        MEASURE_ABBREVIATIONS.put("Levenshtein", "Lev");
        MEASURE_ABBREVIATIONS.put("LongestCommonSubstring", "LCS");
        MEASURE_ABBREVIATIONS.put("Metaphone-Levenshtein", "ML");
        MEASURE_ABBREVIATIONS.put("NeedlemanWunsch", "NW");
        MEASURE_ABBREVIATIONS.put("NYSIIS-Levenshtein", "NL");
        MEASURE_ABBREVIATIONS.put("SED", "SED");
        MEASURE_ABBREVIATIONS.put("SmithWaterman", "SW");
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

        final Map<String, Double> results_per_linkage = new HashMap<>();

        final DataSet data = loadData(linkage);

        for (final String measure : getMeasures(data)) {

            double best_f = getBestF(data, measure);
            results_per_linkage.put(measure, best_f);
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

        final Map<String, Double> results_per_linkage = new HashMap<>();

        final DataSet data = loadData(linkage);

        for (final String measure : getMeasures(data)) {

            final double best_windowed_f = getBestWindowedF(data, measure);
            results_per_linkage.put(measure, best_windowed_f);
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

    private static void printHeaderRow(final Map<String, Map<String, Double>> values_by_linkage_and_measure) {

        final List<String> measures_sorted_by_decreasing_values = sortMeasuresByDecreasingMeanValues(values_by_linkage_and_measure);

        System.out.print(bold("EvidencePair") + " & ");
        for (String measure : measures_sorted_by_decreasing_values) {
            System.out.print(MEASURE_ABBREVIATIONS.get(measure) + " & ");
        }
        System.out.println(bold("Mean") + " & " + bold("Max") + " & " + bold("SD") + " \\\\");
    }

    private static void printRows(final Map<String, Map<String, Double>> values_by_linkage_and_measure) {

        final List<String> measures_sorted_by_decreasing_values = sortMeasuresByDecreasingMeanValues(values_by_linkage_and_measure);

        int linkage_number = 1;
        for (Map.Entry<String, Map<String, Double>> entry : values_by_linkage_and_measure.entrySet()) {

            System.out.print(linkage_number++ + " & ");
            final Map<String, Double> values_for_linkage = entry.getValue();

            final double max = max(values_for_linkage);
            final double mean = mean(values_for_linkage);
            final double std_dev = standardDeviation(values_for_linkage);

            for (String measure : measures_sorted_by_decreasing_values) {

                final double value = values_for_linkage.get(measure);
                final boolean embolden = roundTo2DecimalPlaces(value) == roundTo2DecimalPlaces(max);
                System.out.printf(bold("%.2f", embolden) + " & ", value);
            }

            System.out.printf("%.2f & %.2f & %.2f \\\\", mean, max, std_dev);
            System.out.println();
        }
    }

    private static void printMeanRow(final Map<String, Map<String, Double>> values_by_linkage_and_measure) {

        final List<String> measures_sorted_by_decreasing_values = sortMeasuresByDecreasingMeanValues(values_by_linkage_and_measure);

        System.out.print(bold("Mean") + " & ");
        for (String measure : measures_sorted_by_decreasing_values) {
            System.out.printf("%.2f & ", getMeanForMeasure(values_by_linkage_and_measure, measure));
        }
        System.out.println();
    }

    private static List<String> sortMeasuresByDecreasingMeanValues(final Map<String, Map<String, Double>> values_by_linkage_and_measure) {

        final Map<String, Double> mean_values = getMeanValuesPerMeasure(values_by_linkage_and_measure);

        // Order by decreasing values, and then alphabetically by measure name.
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

    private static Map<String, Double> getMeanValuesPerMeasure(final Map<String, Map<String, Double>> max_f_measures) {

        final Map<String, Double> mean_f_measures = new HashMap<>();

        for (String measure : getMeasures(max_f_measures)) {
            mean_f_measures.put(measure, getMeanForMeasure(max_f_measures, measure));
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

    private static double getMeanForMeasure(final Map<String, Map<String, Double>> max_f_measures, final String measure) {

        double sum = 0.0;

        for (Map<String, Double> linkage_result : max_f_measures.values()) {
            sum += linkage_result.get(measure);
        }

        return sum / max_f_measures.keySet().size();
    }

    private static List<String> getMeasures(final DataSet data) {

        final List<String> result = new ArrayList<>();

        for (List<String> row : data.getRecords()) {
            final String measure = data.getValue(row, "measure");
            if (!result.contains(measure)) result.add(measure);
        }
        return result;
    }

    private static List<String> getMeasures(final Map<String, Map<String, Double>> max_f_measures) {

        for (Map<String, Double> row : max_f_measures.values()) {
            return new ArrayList<>(row.keySet());
        }
        return new ArrayList<>();
    }

    private static double getBestF(final DataSet data_set, String measure) {

        final int max_records_processed = getMaxRecordsProcessed(data_set);
        final DataSet filtered_data = data_set.select((list, ds) -> Integer.parseInt(ds.getValue(list, "records processed")) == max_records_processed && ds.getValue(list, "measure").equals(measure));

        double max_f = 0.0;

        for (List<String> row : filtered_data.getRecords()) {

            final double f = Double.parseDouble(data_set.getValue(row, "f_measure"));
            if (f > max_f) max_f = f;
        }
        return max_f;
    }

    private static final int WINDOW_SIZE = 10;

    private static double getBestWindowedF(final DataSet data_set, String measure) {

        final int max_records_processed = getMaxRecordsProcessed(data_set);
        final DataSet filtered_data = data_set.select((list, ds) -> Integer.parseInt(ds.getValue(list, "records processed")) == max_records_processed && ds.getValue(list, "measure").equals(measure));

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

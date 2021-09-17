package uk.ac.standrews.cs.population_linkage.data.england;

import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class ExtractONSDeathSummary {

    public static final String DATA_PARENT_PATH = "/Users/graham/Desktop/";
    public static final String DATA_FILE_PREFIX = "MORT";
    public static final String DATA_FILE_SUFFIX = ".TXT";
    public static final int NUMBER_OF_DATA_FILES = 2;
    public static final int DISCLOSURE_THRESHOLD = 2;

    public static void main(String[] args) throws IOException {

        Map<String, Integer> occurrences = new TreeMap<>();
        int row_count = 0;

        for (int i = 1; i <= NUMBER_OF_DATA_FILES; i++) {
            row_count += process(i, occurrences);
        }

        output(occurrences, row_count);
    }

    private static void output(Map<String, Integer> occurrences, int row_count) {

        for (String combined : occurrences.keySet()) {
            int number_of_occurrences = occurrences.get(combined);
            if (number_of_occurrences >= DISCLOSURE_THRESHOLD) {
                System.out.println(combined + ", " + number_of_occurrences);
            }
        }

        System.out.println("\nNumber of input rows: " + row_count);
    }

    private static int process(int file_number, Map<String, Integer> occurrences) throws IOException {

        String data_path = getPath(file_number);
        DataSet dataset = new DataSet(Paths.get(data_path));
        List<List<String>> records = dataset.getRecords();

        for (List<String> record : records) {
            processRecord(occurrences, dataset, record);
        }

        return records.size();
    }

    private static void processRecord(Map<String, Integer> occurrences, DataSet dataset, List<String> record) {

        List<String> ICDs = getICDs(dataset, record);
        List<String> descriptions = getDescriptions(dataset, record);

        alignEntries(ICDs, descriptions);
        recordICDDescriptionPairs(occurrences, ICDs, descriptions);
    }

    private static void recordICDDescriptionPairs(Map<String, Integer> occurrences, List<String> ICDs, List<String> descriptions) {

        for (int i = 0; i < ICDs.size(); i++) {
            if (i < descriptions.size() && !descriptions.get(i).isEmpty()) {

                recordICDDescriptionPair(occurrences, ICDs, descriptions, i);
            }
        }
    }

    private static void recordICDDescriptionPair(Map<String, Integer> occurrences, List<String> ICDs, List<String> descriptions, int pair_number) {

        String combined = ICDs.get(pair_number) + ", " + descriptions.get(pair_number).toLowerCase();

        if (occurrences.containsKey(combined)) {
            occurrences.put(combined, occurrences.get(combined) + 1);
        } else {
            occurrences.put(combined, 1);
        }
    }

    private static void alignEntries(List<String> ICDs, List<String> descriptions) {

        // If there are a different number of ICD and description strings, but the same number of
        // non-empty strings, then remove empty strings to align them.

        if (ICDs.size() != descriptions.size()) {

            if (numberOfNonEmptyEntries(ICDs) == numberOfNonEmptyEntries(descriptions)) {

                removeEmptyEntries(ICDs);
                removeEmptyEntries(descriptions);
            }
        }
    }

    private static String getPath(int i) {

        return DATA_PARENT_PATH + DATA_FILE_PREFIX + padAsString(i) + DATA_FILE_SUFFIX;
    }

    private static String padAsString(int i) {

        String s = String.valueOf(i);
        if (s.length() == 1) s = "0" + s;
        return s;
    }

    private static int numberOfNonEmptyEntries(List<String> entries) {

        int count = 0;
        for (String entry : entries) {
            if (!entry.isEmpty()) count++;
        }
        return count;
    }

    private static void removeEmptyEntries(List<String> entries) {

        int count = 0;
        while (count < entries.size()) {
            if (entries.get(count).isEmpty()) {
                entries.remove(count);
            } else {
                count++;
            }
        }
    }

    private static List<String> getICDs(DataSet dataset, List<String> record) {

        return getRowValues(dataset, record, 15, "ICD");

    }

    private static List<String> getDescriptions(DataSet dataset, List<String> record) {

        return getRowValues(dataset, record, 8, "CTEXT");
    }

    private static List<String> getRowValues(DataSet dataset, List<String> record, int number_of_columns, String column_name_prefix) {

        List<String> values = new ArrayList<>();

        for (int i = 1; i <= number_of_columns; i++) {
            try {
                values.add(dataset.getValue(record, column_name_prefix + i));
            } catch (IndexOutOfBoundsException e) {
                // Ignore: some rows in the data file have missing entries at the end rather than empty strings.
            }
        }

        removeEmptyValuesAtEnd(values);
        return values;
    }

    private static void removeEmptyValuesAtEnd(List<String> values) {
        while (values.size() > 0 && values.get(values.size() - 1).isEmpty()) {
            values.remove(values.size() - 1);
        }
    }
}

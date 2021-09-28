/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.england;

import org.neo4j.internal.batchimport.cache.idmapping.string.Radix;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.regex.Pattern;

public class ExtractONSDeathSummary {

    public static final String DATA_PARENT_PATH = "/Users/graham/Desktop/ONS/";
    public static final String DATA_FILE_PREFIX = "MORT";
    public static final String DATA_FILE_SUFFIX = ".TXT";

    public static final String ACRONYMS_PATH = "/Users/graham/Desktop/ONS/acronyms.csv";
    public static final String ALTERNATES_PATH = "/Users/graham/Desktop/ONS/alternates.csv";

    public static final String RAW_ALL_PATH = "/Users/graham/Desktop/extract-raw-all-causes.csv";
    public static final String RAW_MAIN_PATH = "/Users/graham/Desktop/extract-raw-main-causes.csv";
    public static final String CLEANED_ALL_PATH = "/Users/graham/Desktop/extract-cleaned-all-causes.csv";
    public static final String CLEANED_MAIN_PATH = "/Users/graham/Desktop/extract-cleaned-main-causes.csv";

    public static final int NUMBER_OF_DATA_FILES = 13;
    public static final int DISCLOSURE_THRESHOLD = 10;

    private static final List<Map.Entry<Pattern, String>> TIDY_PATTERNS = new ArrayList<>();

    private static final Map<String, String> ALTERNATE_STRINGS = new HashMap<>();
    private static final Set<String> ACRONYMS = new HashSet<>();

    private final boolean do_cleaning;
    private final boolean main_cause_only;
    private final String output_path;

    static {
        // A list to preserve the order, as we want to remove apostrophe altogether rather than substituting space.
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("'"), ""));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("[^a-zA-Z0-9 ]"), " "));

        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^a "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^b "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^c "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^i "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^ii "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^iii "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^I "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^II "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^III "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^1 "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^2 "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^3 "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^1a"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^1b"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^1c"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^ia"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^ib"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^ic"), " "));

        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^\\(a\\)"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^\\(b\\)"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^\\(c\\)"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^a\\)"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^b\\)"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^c\\)"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^\\(1\\)"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^\\(2\\)"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^1 a "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^1\\(a\\)"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^1 \\(a\\)"), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile("^11 "), " "));

        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile(" of "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile(" the "), " "));
        TIDY_PATTERNS.add(new SimpleEntry<>(Pattern.compile(" and "), " "));

        try {
            DataSet alternates = new DataSet(Paths.get(ALTERNATES_PATH));

            for (List<String> row : alternates.getRecords()) {
                String standardised = row.get(0);
                for (String alternate : row) {
                    ALTERNATE_STRINGS.put(" " + alternate + " ", " " + standardised + " ");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            DataSet acronyms = new DataSet(Paths.get(ACRONYMS_PATH));

            for (List<String> row : acronyms.getRecords()) {
                String acronym = row.get(0);
                ACRONYMS.add(acronym);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {

        new ExtractONSDeathSummary(false, false, RAW_ALL_PATH).extractSummary();
        new ExtractONSDeathSummary(false, true, RAW_MAIN_PATH).extractSummary();
        new ExtractONSDeathSummary(true, false, CLEANED_ALL_PATH).extractSummary();
        new ExtractONSDeathSummary(true, true, CLEANED_MAIN_PATH).extractSummary();
    }

    public ExtractONSDeathSummary(boolean do_cleaning, boolean main_cause_only, String output_path) throws IOException {

        this.do_cleaning = do_cleaning;
        this.main_cause_only = main_cause_only;
        this.output_path = output_path;
    }

    private void extractSummary() throws IOException {

        Map<String, Integer> occurrences = new TreeMap<>();
        int row_count = 0;

        for (int i = 1; i <= NUMBER_OF_DATA_FILES; i++) {
            System.out.println("started file " + i);
            row_count += process(i, occurrences);
        }

        try (PrintStream print_stream = new PrintStream(Files.newOutputStream(Paths.get(output_path)))) {
            output(occurrences, row_count, print_stream);
        }
    }

    private void output(Map<String, Integer> occurrences, int row_count, PrintStream print_stream) {

        for (String combined : occurrences.keySet()) {
            int number_of_occurrences = occurrences.get(combined);
            if (number_of_occurrences >= DISCLOSURE_THRESHOLD) {
                String s = restoreAcronyms(combined);
                int first_space_index = s.indexOf(" ");
                print_stream.println(s.substring(0, first_space_index ) + ",\"" + s.substring(first_space_index + 1) + "\"," + number_of_occurrences);
            }
        }

        print_stream.println("\nNumber of input rows: " + row_count);
    }

    private String restoreAcronyms(String s) {

        StringBuilder b = new StringBuilder();
        String[] words = s.split(" ");

        for (String word : words) {

            String upper_case_version = word.toUpperCase();
            b.append(ACRONYMS.contains(upper_case_version) ? upper_case_version : word);
            b.append(" ");
        }

        return b.toString().trim();
    }

    private int process(int file_number, Map<String, Integer> occurrences) throws IOException {

        String data_path = getPath(file_number);
        DataSet dataset = new DataSet(Paths.get(data_path));
        List<List<String>> records = dataset.getRecords();

        int count = 0;

        for (List<String> record : records) {
            if (count % 10000 == 0) System.out.println(count);
            processRecord(occurrences, dataset, record);
            count++;
        }

        return records.size();
    }

    private void processRecord(Map<String, Integer> occurrences, DataSet dataset, List<String> record) {

        String main_cause_ICD = getMainCauseICD(dataset, record);
        List<String> ICDs = getICDs(dataset, record);
        List<String> descriptions = getDescriptions(dataset, record);

        alignEntries(ICDs, descriptions);
        if (main_cause_only) stripSecondaryCauses(main_cause_ICD, ICDs, descriptions);
        recordICDDescriptionPairs(occurrences, ICDs, descriptions);
    }

    private void stripSecondaryCauses(String main_cause_icd, List<String> ICDs, List<String> descriptions) {

        try {
            int main_index = ICDs.indexOf(main_cause_icd);
            String main_cause_description = descriptions.get(main_index);

            ICDs.clear();
            ICDs.add(main_cause_icd);

            descriptions.clear();
            descriptions.add(main_cause_description);
        }
        catch (IndexOutOfBoundsException e) {
            ICDs.clear();
            descriptions.clear();
        }
    }

    private void recordICDDescriptionPairs(Map<String, Integer> occurrences, List<String> ICDs, List<String> descriptions) {

        for (int i = 0; i < ICDs.size(); i++) {
            if (i < descriptions.size() && !descriptions.get(i).isEmpty()) {

                recordICDDescriptionPair(occurrences, ICDs, descriptions, i);
            }
        }
    }

    private void recordICDDescriptionPair(Map<String, Integer> occurrences, List<String> ICDs, List<String> descriptions, int pair_number) {

        String cleaned_description = clean(descriptions.get(pair_number));

        if (!cleaned_description.isEmpty()) {
            String combined = ICDs.get(pair_number) + " " + cleaned_description + " ";

            if (occurrences.containsKey(combined)) {
                occurrences.put(combined, occurrences.get(combined) + 1);
            } else {
                occurrences.put(combined, 1);
            }
        }
    }

    private String clean(final String raw) {

        if (!do_cleaning) return raw;

        String result = removeRepeatedSpaces(raw);

        result = convertCase(result);
        result = replacePatterns(result);
        result = removeRepeatedSpaces(result);
        result = " " + result + " ";
        result = replaceAlternates(result);
        result = removeRepeatedSpaces(result);
        result = replacePatterns(result);
        result = replacePatterns(result);
        result = result.trim();
        if (result.equals(" ")) result = "";

        return result;
    }

    private String replaceAlternates(String s) {

        for (Map.Entry<String,String> entry : ALTERNATE_STRINGS.entrySet()) {
            s = s.replaceAll(entry.getKey(), entry.getValue());
        }
        return s;
    }

    private String replacePatterns(String s) {

        for (Map.Entry<Pattern, String> entry : TIDY_PATTERNS) {
            s = entry.getKey().matcher(s).replaceAll(entry.getValue());
        }
        return s;
    }

    private String convertCase(String s) {

        return s.toLowerCase();
    }

    private String removeRepeatedSpaces(String raw) {
        return raw.replaceAll("\\s\\s+", " ").trim();
    }

    private static boolean allCaps(String s) {
        return s.equals(s.toUpperCase());
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

    private static String getMainCauseICD(DataSet dataset, List<String> record) {

        return dataset.getValue(record, "ICD10U");
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

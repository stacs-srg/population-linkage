/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.profiling.umea;

import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class GroundTruthResultsFilter {

    public static final String INPUT_FILE_PATH = "/Users/al/Documents/Current/Results/2019-03-08-Umea-full-nXn/UmeaBirthSiblingLPRFByThreshold-full.csv";
    public static final String OUTPUT_FILE_PATH = "/Users/al/Desktop/filtered.csv";

    public static void main(String[] args) throws IOException {

        List<String> thresholds = Arrays.asList("0.40", "0.60", "0.80");

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader( Files.newInputStream(Paths.get(INPUT_FILE_PATH))));
             final PrintWriter writer = new PrintWriter(FileManipulation.getOutputStreamWriter(Paths.get(OUTPUT_FILE_PATH)))) {

            String line = reader.readLine();
            writer.println(line);

            while((line = reader.readLine()) != null) {

                String[] values = line.split(",");
                String metric = values[5];
                String threshold = values[6];

                if (metric.startsWith("Sigma-Levenshtein") && thresholds.contains(threshold)) {
                    writer.println(line);
                }
            }
        }
    }
}

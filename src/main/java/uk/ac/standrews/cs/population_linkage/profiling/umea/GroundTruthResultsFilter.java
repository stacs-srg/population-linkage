package uk.ac.standrews.cs.population_linkage.profiling.umea;

import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GroundTruthResultsFilter {

    public static final String INPUT_FILE_PATH = "/Users/graham/Desktop/UmeaBirthSiblingPRFByThreshold-full.csv";
    public static final String OUTPUT_FILE_PATH = "/Users/graham/Desktop/filtered.csv";

    public static void main(String[] args) throws IOException {

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader( Files.newInputStream(Paths.get(INPUT_FILE_PATH))));
             final PrintWriter writer = new PrintWriter(FileManipulation.getOutputStreamWriter(Paths.get(OUTPUT_FILE_PATH)))) {

            String line = reader.readLine();
            writer.println(line);

            while((line = reader.readLine()) != null) {

                String[] values = line.split(",");
                String metric = values[5];
                String threshold = values[6];

                if (metric.startsWith("Sigma-Levenshtein") && threshold.equals("0.80")) {
                    writer.println(line);
                }
            }
        }
    }
}

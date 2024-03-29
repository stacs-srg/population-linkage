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

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(INPUT_FILE_PATH))));
             final PrintWriter writer = new PrintWriter(FileManipulation.getOutputStreamWriter(Paths.get(OUTPUT_FILE_PATH)))) {

            String line = reader.readLine();
            writer.println(line);

            while ((line = reader.readLine()) != null) {

                String[] values = line.split(",");
                String measure = values[5];
                String threshold = values[6];

                if (measure.startsWith("SigmaMissingOne-Levenshtein") && thresholds.contains(threshold)) {
                    writer.println(line);
                }
            }
        }
    }
}

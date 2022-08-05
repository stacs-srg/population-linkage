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
package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Random;

public class DummyConvergenceData {

    public static final Random RANDOM = new Random(234523424L);
    public static final double FINAL_F_MEASURE = 0.4;
    private static final double MAX_ERROR = 0.5;

    public static void main(String[] args) throws IOException {

        try (final PrintWriter writer = new PrintWriter(FileManipulation.getOutputStreamWriter(Paths.get("/Users/graham/Desktop/dummy.csv")))) {

            for (int run_number = 1; run_number <= 10; run_number++) {

                for (int records_processed = 1000; records_processed <= 10000; records_processed += 1000) {

                    for (int threshold = 0; threshold <= 100; threshold ++) {

                        double f_measure = FINAL_F_MEASURE * (1 + error(run_number));

                        writer.printf("xxx,%d,%d,2491374,8526,dummy,%.2f,0,0,0,0,0.5,0.5,%.2f\n", run_number, records_processed, threshold/100.0, f_measure);
                    }
                }
            }
         }
    }

    private static double error(final int run_number) {

        return (MAX_ERROR / run_number) * RANDOM.nextDouble();
    }
}

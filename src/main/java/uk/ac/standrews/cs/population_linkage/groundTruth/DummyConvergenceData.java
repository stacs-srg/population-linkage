/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
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

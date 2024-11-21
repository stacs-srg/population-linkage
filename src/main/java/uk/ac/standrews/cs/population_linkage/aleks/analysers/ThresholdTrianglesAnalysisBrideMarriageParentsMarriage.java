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
package uk.ac.standrews.cs.population_linkage.aleks.analysers;

import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.aleks.resolvers.PatternsCounter;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThresholdTrianglesAnalysisBrideMarriageParentsMarriage extends ThresholdTrianglesAnalysis {
    private static final String BRIDE_PARENTS_MARRIAGE_TPC = "MATCH (m1:Marriage)-[r:ID {actors: \"Bride-Couple\"}]->(m2:Marriage) WHERE (m1)-[:GT_ID {actors: \"Bride-Couple\"}]-(m2) AND r.distance <= $threshold AND r.fields_populated >= $field return count(r)";
    private static final String BRIDE_PARENTS_MARRIAGE_FPC = "MATCH (m1:Marriage)-[r:ID {actors: \"Bride-Couple\"}]->(m2:Marriage) WHERE NOT (m1)-[:GT_ID {actors: \"Bride-Couple\"}]-(m2) AND r.distance <= $threshold AND r.fields_populated >= $field return count(r)";
    private static final String BRIDE_PARENTS_MARRIAGE_FNC = "MATCH (m1:Marriage)-[r:GT_ID {actors: \"Bride-Couple\"}]->(m2:Marriage) WHERE NOT (m1)-[:ID {actors: \"Bride-Couple\"}]-(m2) return count(r)";
    private static final String BRIDE_PARENTS_MARRIAGE_FNC_T = "MATCH (m1:Marriage)-[r:GT_ID {actors: \"Bride-Couple\"}]->(m2:Marriage), (m)-[s:ID {actors: \"Bride-Couple\"}]-(m1) WHERE s.distance > $threshold OR s.fields_populated < $field return count(r)";
    
    public static void main(String[] args) throws InterruptedException {
        NeoDbCypherBridge bridge = new NeoDbCypherBridge();
        final int MAX_FIELD = 4;
        final int MIN_FIELD = 1; //1 below target
        final double MAX_THRESHOLD = 1.01; //0.01 above target
        final double MIN_THRESHOLD = 0.00;

        ExecutorService executorService = Executors.newFixedThreadPool(MAX_FIELD - MIN_FIELD);

        System.out.println("Analysing thresholds...");
        //loop through each linkage field options
        for (int fields = MAX_FIELD; fields > MIN_FIELD; fields--) {
            final int currentField = fields;

            executorService.submit(() -> {
                try (FileWriter fileWriter = new FileWriter("brideMarriageParents" + currentField + ".csv");
                     PrintWriter printWriter = new PrintWriter(fileWriter)) {

                    //write headers
                    printWriter.println("threshold,precision,recall,fmeasure,total,fnots");

                    try (NeoDbCypherBridge localBridge = new NeoDbCypherBridge()) {
                        for (double i = MIN_THRESHOLD; i < MAX_THRESHOLD; i += 0.01) {
                            double threshold = Math.round(i * 100.0) / 100.0;

                            //get quality measurements
                            long fpc = doQuery(BRIDE_PARENTS_MARRIAGE_FPC, threshold, currentField, localBridge);
                            long tpc = doQuery(BRIDE_PARENTS_MARRIAGE_TPC, threshold, currentField, localBridge);
                            long fnc = doQuery(BRIDE_PARENTS_MARRIAGE_FNC, threshold, currentField, localBridge)
                                    + doQuery(BRIDE_PARENTS_MARRIAGE_FNC_T, i, currentField, localBridge);

                            //print to csv
                            printWriter.printf("%.2f,%.5f,%.5f,%.5f,%d,%d%n",
                                    threshold,
                                    ClassificationMetrics.precision(tpc, fpc),
                                    ClassificationMetrics.recall(tpc, fnc),
                                    ClassificationMetrics.F1(tpc, fpc, fnc),
                                    PatternsCounter.countOpenSquaresCumulativeID(bridge, "Marriage", "Marriage", i, currentField, true, "Bride"),
                                    PatternsCounter.countOpenSquaresCumulativeID(bridge, "Marriage", "Marriage", i, currentField, false, "Bride"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(12, TimeUnit.HOURS);
    }

}

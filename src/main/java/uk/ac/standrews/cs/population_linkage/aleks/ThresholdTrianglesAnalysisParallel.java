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
package uk.ac.standrews.cs.population_linkage.aleks;

import org.neo4j.driver.Result;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThresholdTrianglesAnalysisParallel {

    private static final String BIRTH_BIRTH_SIBLING_TPC = "MATCH (b1:Birth)-[r:SIBLING {actors: \"Child-Child\"}]->(b2:Birth) WHERE (b1)-[:GT_SIBLING {actors: \"Child-Child\"}]-(b2) AND r.distance <= $threshold AND r.fields_populated >= $field return count(r)";
    private static final String BIRTH_BIRTH_SIBLING_FPC = "MATCH (b1:Birth)-[r:SIBLING {actors: \"Child-Child\"}]->(b2:Birth) WHERE NOT (b1)-[:GT_SIBLING {actors: \"Child-Child\"}]-(b2) AND r.distance <= $threshold AND r.fields_populated >= $field return count(r)";
    private static final String BIRTH_BIRTH_SIBLING_FNC = "MATCH (b1:Birth)-[r:GT_SIBLING { actors: \"Child-Child\"}]->(b2:Birth) WHERE NOT (b1)-[:SIBLING {actors: \"Child-Child\"}]-(b2) return count(r)";
    private static final String BIRTH_BIRTH_SIBLING_FNC_T = "MATCH (b1:Birth)-[r:GT_SIBLING { actors: \"Child-Child\"}]->(b2:Birth), (b1)-[s:SIBLING]-(b2) WHERE s.distance > $threshold OR s.fields_populated < $field return count(r)";

    public static void main(String[] args) throws InterruptedException {
        NeoDbCypherBridge bridge = new NeoDbCypherBridge();
        final int MAX_FIELD = 8;
        final int MIN_FIELD = 4; //1 below target
        final double MAX_THRESHOLD = 2.01; //0.01 above target
        final double MIN_THRESHOLD = 0.0;


        ExecutorService executorService = Executors.newFixedThreadPool(MAX_FIELD - MIN_FIELD); // You can adjust the number of threads

        System.out.println("Analysing thresholds...");

        // For each field, create a separate CSV file and run the analysis in parallel
        for (int fields = MAX_FIELD; fields > MIN_FIELD; fields--) {
            System.out.println("Field: " + fields);
            final int currentField = fields;

            // Submit the task for each field
            executorService.submit(() -> {
                try (FileWriter fileWriter = new FileWriter("birthbirth" + currentField + ".csv");
                     PrintWriter printWriter = new PrintWriter(fileWriter)) {

                    printWriter.println("threshold,precision,recall,fmeasure,triangles");

                    // For each threshold, run the queries in parallel
                    for (double i = MIN_THRESHOLD; i < MAX_THRESHOLD; i += 0.01) {
                        double threshold = Math.round(i * 100.0) / 100.0;
                        System.out.println(threshold);

                        // Execute queries and write the results
                        long fpc = doQuery(BIRTH_BIRTH_SIBLING_FPC, threshold, currentField, bridge);
                        long tpc = doQuery(BIRTH_BIRTH_SIBLING_TPC, threshold, currentField, bridge);
                        long fnc = doQuery(BIRTH_BIRTH_SIBLING_FNC, threshold, currentField, bridge)
                                + doQuery(BIRTH_BIRTH_SIBLING_FNC_T, i, currentField, bridge);

                        printWriter.printf("%.2f,%.5f,%.5f,%.5f,%d%n",
                                threshold,
                                ClassificationMetrics.precision(tpc, fpc),
                                ClassificationMetrics.recall(tpc, fnc),
                                ClassificationMetrics.F1(tpc, fpc, fnc),
                                PatternsCounter.countOpenTrianglesCumulative(bridge, "Birth", "Birth", i, currentField));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.HOURS);

        System.out.println("Done!");
    }

    protected static long doQuery(String query_string, double threshold, int fields, NeoDbCypherBridge bridge) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("threshold", threshold);
        parameters.put("field", fields);
        Result result = bridge.getNewSession().run(query_string, parameters);
        return (long) result.list(r -> r.get("count(r)").asInt()).get(0);
    }

}

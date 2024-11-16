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

import org.neo4j.driver.Result;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.aleks.resolvers.PatternsCounter;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThresholdTrianglesAnalysisBirthParentsMarriage {
    private static final String BIRTH_MARRIAGE_ID_TPC = "MATCH (b:Birth)-[r:ID {actors: \"Child-Mother\"}]-(m:Marriage), (b)-[s:ID {actors: \"Child-Father\"}]-(m) WHERE (b)-[:GT_ID {actors: \"Child-Couple\"}]-(m) AND r.distance <= $threshold AND r.fields_populated >= $field AND s.distance <= $threshold AND s.fields_populated >= $field return count(r)";
    private static final String BIRTH_MARRIAGE_ID_FPC = "MATCH (b:Birth)-[r:ID {actors: \"Child-Mother\"}]-(m:Marriage), (b)-[s:ID {actors: \"Child-Father\"}]-(m) WHERE NOT (b)-[:GT_ID {actors: \"Child-Couple\"}]-(m) AND r.distance <= $threshold AND r.fields_populated >= $field AND s.distance <= $threshold AND s.fields_populated >= $field return count(r)";
    private static final String BIRTH_MARRIAGE_ID_FNC = "MATCH (b:Birth)-[r:GT_ID {actors: \"Child-Couple\"}]-(m:Marriage) WHERE NOT (b)-[:ID {actors: \"Child-Mother\"}]-(m) and NOT (b)-[:ID {actors: \"Child-Father\"}]-(m) return count(r)";
    private static final String BIRTH_MARRIAGE_ID_FNC_T = "MATCH (b:Birth)-[r:GT_ID {actors: \"Child-Couple\"}]-(m:Marriage), (b)-[s:ID {actors: \"Child-Mother\"}]-(m), (b)-[t:ID {actors: \"Child-Father\"}]-(m) WHERE (s.distance > $threshold OR s.fields_populated < $field) AND (t.distance > $threshold OR t.fields_populated < $field) return count(r)";

    public static void main(String[] args) throws InterruptedException {
        NeoDbCypherBridge bridge = new NeoDbCypherBridge();
        final int MAX_FIELD = 8;
        final int MIN_FIELD = 4; //1 below target
        final double MAX_THRESHOLD = 2.01; //0.01 above target
        final double MIN_THRESHOLD = 0.00;

        ExecutorService executorService = Executors.newFixedThreadPool(MAX_FIELD - MIN_FIELD);

        System.out.println("Analysing thresholds...");
        //loop through each linkage field options
        for (int fields = MAX_FIELD; fields > MIN_FIELD; fields--) {
            final int currentField = fields;

            executorService.submit(() -> {
                try (FileWriter fileWriter = new FileWriter("birthparentsID" + currentField + ".csv");
                     PrintWriter printWriter = new PrintWriter(fileWriter)) {

                    //write headers
                    printWriter.println("threshold,precision,recall,fmeasure,total,fnots");

                    try (NeoDbCypherBridge localBridge = new NeoDbCypherBridge()) {
                        for (double i = MIN_THRESHOLD; i < MAX_THRESHOLD; i += 0.01) {
                            double threshold = Math.round(i * 100.0) / 100.0;

                            //get quality measurements
                            long fpc = doQuery(BIRTH_MARRIAGE_ID_FPC, threshold, currentField, localBridge);
                            long tpc = doQuery(BIRTH_MARRIAGE_ID_TPC, threshold, currentField, localBridge);
                            long fnc = doQuery(BIRTH_MARRIAGE_ID_FNC, threshold, currentField, localBridge)
                                    + doQuery(BIRTH_MARRIAGE_ID_FNC_T, i, currentField, localBridge);

                            //print to csv
                            printWriter.printf("%.2f,%.5f,%.5f,%.5f,%d,%d%n",
                                    threshold,
                                    ClassificationMetrics.precision(tpc, fpc),
                                    ClassificationMetrics.recall(tpc, fnc),
                                    ClassificationMetrics.F1(tpc, fpc, fnc),
                                    PatternsCounter.countOpenTrianglesParentsMarriage(bridge, i, currentField, true),
                                    PatternsCounter.countOpenTrianglesParentsMarriage(bridge, i, currentField, false));
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

    /**
     * Method to query the database for quality measurements
     *
     * @param query_string Cypher query
     * @param threshold current threshold being analysed
     * @param fields current field being analysed
     * @param bridge Neo4j bridge
     * @return results of query
     */
    private static long doQuery(String query_string, double threshold, int fields, NeoDbCypherBridge bridge) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("threshold", threshold);
        parameters.put("field", fields);
        Result result = bridge.getNewSession().run(query_string, parameters);
        return (long) result.list(r -> r.get("count(r)").asInt()).get(0);
    }
}

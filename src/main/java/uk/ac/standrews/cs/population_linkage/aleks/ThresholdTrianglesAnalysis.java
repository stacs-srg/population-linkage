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

public class ThresholdTrianglesAnalysis {

    private static final String BIRTH_BIRTH_SIBLING_TPC = "MATCH (b1:Birth)-[r:SIBLING {actors: \"Child-Child\"}]->(b2:Birth) WHERE (b1)-[:GT_SIBLING {actors: \"Child-Child\"}]-(b2) AND r.distance <= $threshold return count(r)";
    private static final String BIRTH_BIRTH_SIBLING_FPC = "MATCH (b1:Birth)-[r:SIBLING {actors: \"Child-Child\"}]->(b2:Birth) WHERE NOT (b1)-[:GT_SIBLING {actors: \"Child-Child\"}]-(b2) AND r.distance <= $threshold return count(r)";
    private static final String BIRTH_BIRTH_SIBLING_FNC = "MATCH (b1:Birth)-[r:GT_SIBLING { actors: \"Child-Child\"}]->(b2:Birth) WHERE NOT (b1)-[:SIBLING {actors: \"Child-Child\"}]-(b2) return count(r)";
    private static final String BIRTH_BIRTH_SIBLING_FNC_T = "MATCH (b1:Birth)-[r:GT_SIBLING { actors: \"Child-Child\"}]->(b2:Birth), (b1)-[s:SIBLING]-(b2) WHERE s.distance > $threshold return count(r)";

    double[] recall = new double[101];
    double[] precision = new double[101];
    double[] fmeasure = new double[101];
    int[] triangles = new int[101];

    public static void main(String[] args) {
        NeoDbCypherBridge bridge = new NeoDbCypherBridge();

        try (FileWriter fileWriter = new FileWriter("birthbirthhalfplusv2.csv");
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println("threshold,precision,recall,fmeasure,triangles");

            System.out.println("Analysing thresholds...");
            for (double i = 0.0; i <= 1.0; i+=0.01) {
                double threshold = Math.round(i * 100.0) / 100.0;
                System.out.println(threshold);
//                long fpc = doQuery(BIRTH_BIRTH_SIBLING_FPC, threshold, bridge);
//                long tpc = doQuery(BIRTH_BIRTH_SIBLING_TPC, threshold, bridge);
//                long fnc = doQuery(BIRTH_BIRTH_SIBLING_FNC, threshold, bridge) + doQuery(BIRTH_BIRTH_SIBLING_FNC_T, i, bridge);
                long fpc = 1L;
                long tpc = 1L;
                long fnc = 1L;

//                recall[(int) (i*100)] = ClassificationMetrics.recall(tpc, fnc);
//                precision[(int) (i*100)] = ClassificationMetrics.precision(tpc, fpc);
//                fmeasure[(int) (i*100)] = ClassificationMetrics.F1(tpc, fpc, fnc);
//                triangles[(int) (i*100)] = PatternsCounter.countOpenTriangles(bridge, "Birth", "Birth", i);

                printWriter.printf("%.2f,%.5f,%.5f,%.5f,%d%n", threshold, ClassificationMetrics.precision(tpc, fpc), ClassificationMetrics.recall(tpc, fnc), ClassificationMetrics.F1(tpc, fpc, fnc), PatternsCounter.countOpenTriangles(bridge, "Birth", "Birth", i));
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        System.out.println("Done!");
    }

    protected static long doQuery(String query_string, double threshold, NeoDbCypherBridge bridge) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("threshold", threshold);
        Result result = bridge.getNewSession().run(query_string, parameters);
        return (long) result.list(r -> r.get("count(r)").asInt()).get(0);
    }

}

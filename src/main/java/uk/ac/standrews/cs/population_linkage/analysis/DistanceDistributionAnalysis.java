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
package uk.ac.standrews.cs.population_linkage.analysis;

import org.neo4j.driver.Result;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistanceDistributionAnalysis {

    private static final String BIRTH_SIBLING_TRIANGLE = "MATCH (x:Birth)-[r:SIBLING]-(y:Birth)-[s:SIBLING]-(z:Birth)\n" +
            "WHERE NOT (x)-[:SIBLING]-(z) AND id(x) < id(z)\n" +
            "RETURN r.distance + s.distance as cluster_sum, \n" +
            "EXISTS((x)-[:GT_SIBLING]-(z)) as has_GT_SIBLING";

    private static final String BIRTH_SIBLING_TRIANGLE_2 = "MATCH (x:Birth)-[r:SIBLING]-(y:Birth)-[s:SIBLING]-(z:Birth)\n" +
            "WHERE NOT (x)-[:SIBLING]-(z)\n" +
            "RETURN x, collect([r.distance, s.distance, EXISTS((x)-[:GT_SIBLING]-(z))]) AS openTriangles";

    public static void main(String[] args) {
        NeoDbCypherBridge bridge = new NeoDbCypherBridge();
        getSumFreq(bridge);
        getMaxAndAvgFreq(bridge);
    }

    /**
     * Method to get average and max distances for a cluster
     *
     * @param bridge
     */
    private static void getMaxAndAvgFreq(NeoDbCypherBridge bridge) {
        try (FileWriter fileWriter = new FileWriter("birthbirthtri2.csv");
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println("average_distance,max_distance,has_GT_SIBLING,link_num");

            Result result = bridge.getNewSession().run(BIRTH_SIBLING_TRIANGLE_2);
            result.stream().forEach(r -> {
                List<List<Object>> collection = (List<List<Object>>) r.asMap().get("openTriangles");
                double maxDis = 0;
                boolean maxLink = false;
                double avgDistance = 0;
                int triCount = 0;

                //loop through each cluster
                for (List<Object> record : collection) {
                    double sumDistances = 0;
                    try{
                        double rDistance = (double) record.get(0);
                        double sDistance = (double) record.get(1);
                        boolean hasLink = (boolean) record.get(2); //check if FPOT or FNOT

                        sumDistances = rDistance + sDistance;
                        avgDistance += sumDistances;
                        triCount++;

                        //update max if sum above current number
                        if(sumDistances > maxDis) {
                            maxDis = sumDistances;
                            maxLink = hasLink;
                        }
                    } catch (Exception e) {

                    }
                }

                if(triCount > 0){
                    avgDistance = avgDistance / triCount;
                    printWriter.printf("%.2f,%.2f,%b,%d%n", avgDistance, maxDis, maxLink, triCount);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to get the sum of distances for each cluster
     *
     * @param bridge Neo4j bridge
     */
    private static void getSumFreq(NeoDbCypherBridge bridge) {
        try (FileWriter fileWriter = new FileWriter("birthbirthtri.csv");
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println("distance_sum,is_sibling");

            Result result = bridge.getNewSession().run(BIRTH_SIBLING_TRIANGLE);
            result.list(r -> {
                if (!r.get("cluster_sum").isNull()) {
                    printWriter.printf("%.2f,%b%n", r.get("cluster_sum").asDouble(), r.get("has_GT_SIBLING").asBoolean());
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    protected static long doQuery(String query_string, double threshold, int fields, NeoDbCypherBridge bridge) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("threshold", threshold);
        parameters.put("field", fields);
        Result result = bridge.getNewSession().run(query_string, parameters);
        return (long) result.list(r -> r.get("count(r)").asInt()).get(0);
    }

}

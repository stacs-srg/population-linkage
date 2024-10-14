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
import org.neo4j.driver.types.Node;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DistanceDistributionAnalysis {

    private static final String BIRTH_SIBLING_TRIANGLE = "MATCH (x:Birth)-[r:SIBLING]-(y:Birth)-[s:SIBLING]-(z:Birth)\n" +
            "WHERE NOT (x)-[:SIBLING]-(z) AND id(x) < id(z)\n" +
            "RETURN r.distance + s.distance as cluster_sum, \n" +
            "EXISTS((x)-[:GT_SIBLING]-(z)) as has_GT_SIBLING";

    public static void main(String[] args) {
        NeoDbCypherBridge bridge = new NeoDbCypherBridge();

        try (FileWriter fileWriter = new FileWriter("birthbirthtri.csv");
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println("distance_sum,is_sibling");

            Result result = bridge.getNewSession().run(BIRTH_SIBLING_TRIANGLE);
            result.list(r -> printWriter.printf("%.2f,%b%n", r.get("cluster_sum").asDouble(), r.get("has_GT_SIBLING").asBoolean()));
        } catch (Exception e) {
            e.printStackTrace();
        }

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

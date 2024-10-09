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
import uk.ac.standrews.cs.neoStorr.impl.Store;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.util.List;

public class PatternsCounter {

    final static String[] RECORD_TYPES = {"Birth", "Marriage", "Death"};

    public static void main(String[] args) {
        NeoDbCypherBridge bridge = Store.getInstance().getBridge();

        for(String type1: RECORD_TYPES){
            for(String type2: RECORD_TYPES) {
                countOpenTrianglesToString(bridge, type1, type2);
            }
        }
    }

    public static void countOpenTrianglesToString(NeoDbCypherBridge bridge, String type1, String type2) {
        int count = countOpenTriangles(bridge, type1, type2);
        System.out.println(type1 + "-" + type2 + "-" + type1 + " triangle: " + count);
    }

    public static int countOpenTriangles(NeoDbCypherBridge bridge, String type1, String type2) {
        String openTriangleQuery = String.format(
                "MATCH (x:%s)-[:SIBLING]-(y:%s)-[:SIBLING]-(z:%s) " +
                        "WHERE NOT (x)-[:SIBLING]-(z) AND id(x) < id(z) AND NOT (x)-[:DELETED]-(y) AND NOT (z)-[:DELETED]-(y)" +
                        "RETURN count(DISTINCT [x, z]) as cluster_count",
                type1, type2, type1
        );

        Result result = bridge.getNewSession().run(openTriangleQuery);
        List<Long> clusters = result.list(r -> r.get("cluster_count").asLong());

        long count = 0;
        if (!clusters.isEmpty()) {
            count = clusters.get(0);
        }

        return (int) count;
    }

    //wont work for resolver cuz no deleted
    public static int countOpenTriangles(NeoDbCypherBridge bridge, String type1, String type2, double threshold) {
        int count = countOpenTriangles(bridge, type1, type2);
        String openTriangleQuery = String.format(
                "MATCH (x:%1$s)-[s:SIBLING]-(y:%2$s)-[r:SIBLING]-(z:%1$s), (x)-[t:SIBLING]-(z)  " +
                        "WHERE id(x) < id(z) AND r.distance <= %3$s AND s.distance <= %3$s AND t.distance > %3$s " +
                        "RETURN count(DISTINCT [x, z]) as cluster_count",
                type1, type2, threshold
        );

        Result result = bridge.getNewSession().run(openTriangleQuery);
        List<Long> clusters = result.list(r -> r.get("cluster_count").asLong());

        long tCount = 0;
        if (!clusters.isEmpty()) {
            tCount = clusters.get(0);
        }

        return count + (int) tCount;
    }
}

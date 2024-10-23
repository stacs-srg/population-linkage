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

    //Won't work for resolver as no deleted check
    public static int countOpenTrianglesCumulative(NeoDbCypherBridge bridge, String type1, String type2, double threshold, int fields) {
        long count = 0;
        String openTriangleQuery = String.format(
                "MATCH (x:%1$s)-[s:SIBLING]-(y:%2$s)-[r:SIBLING]-(z:%1$s) " +
                        "WHERE NOT (x)-[:SIBLING]-(z) AND id(x) < id(z) AND r.distance <= %3$s AND s.distance <= %3$s AND r.fields_populated >= %4$s AND s.fields_populated >= %4$s " +
                        "RETURN count(DISTINCT [x, z]) as cluster_count",
                type1, type2, threshold, fields
        );

        Result result = bridge.getNewSession().run(openTriangleQuery);
        List<Long> clusters = result.list(r -> r.get("cluster_count").asLong());

        if (!clusters.isEmpty()) {
            count = clusters.get(0);
        }

        String openTriangleQuery2 = String.format(
                "MATCH (x:%1$s)-[s:SIBLING]-(y:%2$s)-[r:SIBLING]-(z:%1$s), (x)-[t:SIBLING]-(z)  " +
                        "WHERE id(x) < id(z) AND r.distance <= %3$s AND s.distance <= %3$s " +
                        "AND (t.fields_populated < %4$s OR t.distance > %3$s) " +
                        "RETURN count(DISTINCT [x, z]) as cluster_count",
                type1, type2, threshold, fields
        );

        result = bridge.getNewSession().run(openTriangleQuery2);
        clusters = result.list(r -> r.get("cluster_count").asLong());

        long tCount = 0;
        if (!clusters.isEmpty()) {
            tCount = clusters.get(0);
        }

        return (int) count + (int) tCount;
    }

    public static int countOpenTrianglesCumulativeDouble(NeoDbCypherBridge bridge, String type1, String type2, double threshold, int fields) {
        long count = 0;
        String openTriangleQuery = String.format(
                "MATCH (x:%1$s)-[s:SIBLING]-(y:%1$s)-[r:SIBLING]-(z:%2$s) " +
                        "WHERE NOT (x)-[:SIBLING]-(z) AND id(x) < id(z) AND x.FORENAME <> z.FORENAME AND x.BIRTH_YEAR <> right(z.DATE_OF_BIRTH, 4) AND r.distance <= %3$s AND r.fields_populated >= %4$s\n" +
                        "RETURN count(DISTINCT [x, z]) as cluster_count",
                type1, type2, threshold, fields
        );

        Result result = bridge.getNewSession().run(openTriangleQuery);
        List<Long> clusters = result.list(r -> r.get("cluster_count").asLong());

        if (!clusters.isEmpty()) {
            count = clusters.get(0);
        }

        String openTriangleQuery2 = String.format(
                "MATCH (x:%1$s)-[s:SIBLING]-(y:%1$s)-[r:SIBLING]-(z:%2$s), (x)-[t:SIBLING]-(z) " +
                        "WHERE id(x) < id(z) AND x.FORENAME <> z.FORENAME AND x.BIRTH_YEAR <> right(z.DATE_OF_BIRTH, 4) AND r.distance <= %3$s AND r.fields_populated >= %4$s\n" +
                        "AND (t.fields_populated < %4$s OR t.distance > %3$s ) " +
                        "RETURN count(DISTINCT [x, z]) as cluster_count",
                type1, type2, threshold, fields
        );

        result = bridge.getNewSession().run(openTriangleQuery2);
        clusters = result.list(r -> r.get("cluster_count").asLong());

        long tCount = 0;
        if (!clusters.isEmpty()) {
            tCount = clusters.get(0);
        }

        return (int) count + (int) tCount;
    }

    public static int countOpenSquaresCumulative(NeoDbCypherBridge bridge, String type1, String type2, double threshold, int fields) {
        long count = 0;
        String openSquaresQuery = String.format("MATCH (b1:%1$s)-[r:ID]-(d:%2$s), " +
                "(b2:%1$s)-[s:ID]-(d) " +
                "WHERE r.distance <= %3$s AND s.distance <= %3$s AND r.fields_populated >= %4$s AND s.fields_populated >= %4$s AND id(b1) < id(b2) " +
                "RETURN count(*) as cluster_count", type1, type2, threshold, fields);

        Result result = bridge.getNewSession().run(openSquaresQuery);
        List<Long> clusters = result.list(r -> r.get("cluster_count").asLong());

        if (!clusters.isEmpty()) {
            count = clusters.get(0);
        }

        String openSquaresQuery2 = String.format("MATCH (b1:%1$s)-[:SIBLING]-(b2:%1$s), " +
                "(d1:%2$s)-[:SIBLING]-(d2:%2$s), " +
                "(b1)-[r:ID]-(d1:%2$s), " +
                "(b2)-[t:ID]-(d2:%2$s), " +
                "(b1)-[:SIBLING]-(d2), " +
                "(b2)-[:SIBLING]-(d1) " +
                "WHERE r.distance <= %3$s AND r.fields_populated >= %4$s " +
                "AND (t.fields_populated < %4$s OR t.distance > %3$s) " +
                "RETURN count(*) as cluster_count", type1, type2, threshold, fields);

//        result = bridge.getNewSession().run(openSquaresQuery2);
//        clusters = result.list(r -> r.get("cluster_count").asLong());

        long tCount = 0;
//        if (!clusters.isEmpty()) {
//            tCount = clusters.get(0);
//        }

        return (int) count + (int) tCount;
    }
}

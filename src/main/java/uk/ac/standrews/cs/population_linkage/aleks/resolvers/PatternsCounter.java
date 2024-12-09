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
package uk.ac.standrews.cs.population_linkage.aleks.resolvers;

import org.neo4j.driver.Result;
import uk.ac.standrews.cs.neoStorr.impl.Store;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.util.List;
import java.util.Objects;

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

    /**
     * Method to count all sibling triangles and print
     *
     * @param bridge Neo4j bridge
     * @param type1 records x,z
     * @param type2 record y
     */
    public static void countOpenTrianglesToString(NeoDbCypherBridge bridge, String type1, String type2) {
        int count = countOpenTriangles(bridge, type1, type2);
        System.out.println(type1 + "-" + type2 + "-" + type1 + " triangle: " + count);
    }

    /**
     * Method to count all ID triangles and print
     *
     * @param bridge Neo4j bridge
     * @param type1 records x,z
     * @param type2 record y
     */
    public static void countOpenTrianglesToStringID(NeoDbCypherBridge bridge, String type1, String type2) {
        int count = countOpenTrianglesID(bridge, type1, type2);
        System.out.println(type1 + "-" + type2 + "-" + type1 + " triangle: " + count);
    }

    /**
     * Method to count all sibling triangles
     *
     * @param bridge Neo4j bridge
     * @param type1 records x,z
     * @param type2 record y
     */
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

    /**
     * Method to count all ID triangles and print
     *
     * @param bridge Neo4j bridge
     * @param type1 records x,z
     * @param type2 record y
     */
    public static int countOpenTrianglesID(NeoDbCypherBridge bridge, String type1, String type2) {
        String openTriangleQuery = String.format(
                "MATCH (x:%s)-[:SIBLING]-(y:%s)-[:ID]-(z:%s) " +
                        "WHERE NOT (x)-[:ID]-(z) AND id(x) < id(z) AND NOT (x)-[:DELETED]-(y) AND NOT (z)-[:DELETED]-(y)" +
                        "RETURN count(DISTINCT [x, z]) as cluster_count",
                type1, type1, type2
        );

        Result result = bridge.getNewSession().run(openTriangleQuery);
        List<Long> clusters = result.list(r -> r.get("cluster_count").asLong());

        long count = 0;
        if (!clusters.isEmpty()) {
            count = clusters.get(0);
        }

        return (int) count;
    }

    /**
     * Method to count all sibling triangles for particular field and threshold
     *
     * @param bridge Neo4j bridge
     * @param type1 x,z records
     * @param type2 y record
     * @param threshold threshold to count at
     * @param fields number of fields to count at
     * @return total number of open triangles
     */
    public static int countOpenTrianglesCumulative(NeoDbCypherBridge bridge, String type1, String type2, double threshold, int fields) {
        long count = 0;
        //query to count all open triangles at the threshold and filed number
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

        //query to count all open triangles that would be there if the threshold was not maximised
        String openTriangleQuery2 = String.format(
                "MATCH (x:%1$s)-[s:SIBLING]-(y:%2$s)-[r:SIBLING]-(z:%1$s), (x)-[t:SIBLING]-(z)  " +
                        "WHERE id(x) < id(z) AND r.distance <= %3$s AND s.distance <= %3$s AND r.fields_populated >= %4$s AND s.fields_populated >= %4$s " +
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

    /**
     * Method to count FNOTs for sibling open triangles
     *
     * @param bridge Neo4j bridge
     * @param type1 records x,z
     * @param type2 record y
     * @param threshold threshold to count at
     * @param fields number of fields to count at
     * @return total number of open triangles
     */
    public static int countOpenTrianglesSibFNOT(NeoDbCypherBridge bridge, String type1, String type2, double threshold, int fields) {
        long count = 0;
        String openTriangleQuery = "";
        String openTriangleQuery2 = "";

        //if Birth-Birth-Birth open triangle
        if(Objects.equals(type1, "Birth") && Objects.equals(type2, "Birth")){
            //count number of FNOTs
            openTriangleQuery = String.format(
                    "MATCH (x:%1$s)-[s:SIBLING]-(y:%2$s)-[r:SIBLING]-(z:%1$s) " +
                            "WHERE NOT (x)-[:SIBLING]-(z) AND id(x) < id(z) AND x.PARENTS_DAY_OF_MARRIAGE = z.PARENTS_DAY_OF_MARRIAGE AND x.PARENTS_MONTH_OF_MARRIAGE = z.PARENTS_MONTH_OF_MARRIAGE AND x.PARENTS_YEAR_OF_MARRIAGE = z.PARENTS_YEAR_OF_MARRIAGE and x.PARENTS_YEAR_OF_MARRIAGE <> \"----\" AND z.PARENTS_YEAR_OF_MARRIAGE <> \"----\"\n" +
                            "AND r.distance <= %3$s AND s.distance <= %3$s AND r.fields_populated >= %4$s AND s.fields_populated >= %4$s\n" +
                            "RETURN count(DISTINCT [x, z]) as cluster_count",
                    type1, type2, threshold, fields
            );

            //count number of FNOTs that would be there if the threshold was not maximised
            openTriangleQuery2 = String.format(
                    "MATCH (x:%1$s)-[s:SIBLING]-(y:%2$s)-[r:SIBLING]-(z:%1$s), (x)-[t:SIBLING]-(z)  " +
                            "WHERE id(x) < id(z) AND x.PARENTS_DAY_OF_MARRIAGE = z.PARENTS_DAY_OF_MARRIAGE AND x.PARENTS_MONTH_OF_MARRIAGE = z.PARENTS_MONTH_OF_MARRIAGE AND x.PARENTS_YEAR_OF_MARRIAGE = z.PARENTS_YEAR_OF_MARRIAGE and x.PARENTS_YEAR_OF_MARRIAGE <> \"----\" AND z.PARENTS_YEAR_OF_MARRIAGE <> \"----\"\n" +
                            "AND r.distance <= %3$s AND s.distance <= %3$s AND r.fields_populated >= %4$s AND s.fields_populated >= %4$s\n" +
                            "AND (t.fields_populated < %4$s OR t.distance > %3$s)\n" +
                            "RETURN count(DISTINCT [x, z]) as cluster_count",
                    type1, type2, threshold, fields
            );
        }

        Result result = bridge.getNewSession().run(openTriangleQuery);
        List<Long> clusters = result.list(r -> r.get("cluster_count").asLong());

        if (!clusters.isEmpty()) {
            count = clusters.get(0);
        }

        result = bridge.getNewSession().run(openTriangleQuery2);
        clusters = result.list(r -> r.get("cluster_count").asLong());

        long tCount = 0;
        if (!clusters.isEmpty()) {
            tCount = clusters.get(0);
        }

        return (int) count + (int) tCount;
    }

    /**
     * Method to count number of open triangles for Birth-Marriage Parents ID
     *
     * @param bridge Neo4j bridge
     * @param threshold threshold to count at
     * @param fields number of fields to count at
     * @param isTotal check if total number of triangles or FNOTs is required
     * @return number of open triangles
     */
    public static int countOpenTrianglesParentsMarriage(NeoDbCypherBridge bridge, double threshold, int fields, boolean isTotal){
        long count = 0;
        String openSquaresQuery = "";
        String openSquaresQuery2 = "";

        if(isTotal){
            openSquaresQuery = String.format(
                    "MATCH (x:Birth)-[:SIBLING]-(y:Birth)-[r:ID]-(z:Marriage) " +
                            "WHERE NOT (x)-[:ID]-(z) AND id(x) < id(z) AND (r.actors = \"Child-Father\" OR r.actors = \"Child-Mother\") AND r.distance <= %1$s AND r.fields_populated >= %2$s " +
                            "AND x.PARENTS_YEAR_OF_MARRIAGE = y.PARENTS_YEAR_OF_MARRIAGE " +
                            "RETURN count(DISTINCT [x, z]) as cluster_count",
                    threshold, fields
            );

            openSquaresQuery2 = String.format(
                    "MATCH (x:Birth)-[:SIBLING]-(y:Birth)-[r:ID]-(z:Marriage), (x)-[t:ID]-(z) " +
                            "WHERE id(x) < id(z) AND (r.actors = \"Child-Father\" OR r.actors = \"Child-Mother\") AND (t.actors = \"Child-Father\" OR t.actors = \"Child-Mother\") " +
                            "AND r.distance <= %1$s AND r.fields_populated >= %2$s " +
                            "AND (t.fields_populated < %2$s OR t.distance > %1$s) " +
                            "AND x.PARENTS_YEAR_OF_MARRIAGE = y.PARENTS_YEAR_OF_MARRIAGE " +
                            "RETURN count(DISTINCT [x, z]) as cluster_count",
                    threshold, fields
            );
        }else{
            openSquaresQuery = String.format(
                    "MATCH (x:Birth)-[:SIBLING]-(y:Birth)-[r:ID]-(z:Marriage) " +
                            "WHERE NOT (x)-[:ID]-(z) AND id(x) < id(z) AND (r.actors = \"Child-Father\" OR r.actors = \"Child-Mother\") AND r.distance <= %1$s AND r.fields_populated >= %2$s " +
                            "AND x.PARENTS_YEAR_OF_MARRIAGE = z.MARRIAGE_YEAR " +
                            "AND y.PARENTS_YEAR_OF_MARRIAGE = z.MARRIAGE_YEAR " +
                            "RETURN count(DISTINCT [x, z]) as cluster_count",
                    threshold, fields
            );

            openSquaresQuery2 = String.format(
                    "MATCH (x:Birth)-[:SIBLING]-(y:Birth)-[r:ID]-(z:Marriage), (x)-[t:ID]-(z) " +
                            "WHERE id(x) < id(z) AND (r.actors = \"Child-Father\" OR r.actors = \"Child-Mother\") AND (t.actors = \"Child-Father\" OR t.actors = \"Child-Mother\") " +
                            "AND r.distance <= %1$s AND r.fields_populated >= %2$s " +
                            "AND (t.fields_populated < %2$s OR t.distance > %1$s) " +
                            "AND x.PARENTS_YEAR_OF_MARRIAGE = z.MARRIAGE_YEAR " +
                            "AND y.PARENTS_YEAR_OF_MARRIAGE = z.MARRIAGE_YEAR " +
                            "RETURN count(DISTINCT [x, z]) as cluster_count",
                    threshold, fields
            );
        }

        Result result = bridge.getNewSession().run(openSquaresQuery);
        List<Long> clusters = result.list(r -> r.get("cluster_count").asLong());

        if (!clusters.isEmpty()) {
            count = clusters.get(0);
        }

        result = bridge.getNewSession().run(openSquaresQuery2);
        clusters = result.list(r -> r.get("cluster_count").asLong());

        long tCount = 0;
        if (!clusters.isEmpty()) {
            tCount = clusters.get(0);
        }

        return (int) count + (int) tCount;
    }

    /**
     * Method to count number of open triangles for Birth-Death-Birth Siblings
     *
     * @param bridge Neo4j bridge
     * @param type1 records x,z
     * @param type2 record y
     * @param threshold threshold to count at
     * @param fields number of fields to count at
     * @param isTotal boolean to check if return total or FNOTs
     * @return total number of open triangles
     */
    public static int countOpenTrianglesBirthDeathSib(NeoDbCypherBridge bridge, String type1, String type2, double threshold, int fields, boolean isTotal) {
        long count = 0;
        String openSquaresQuery = "";
        String openSquaresQuery2 = "";

        if(isTotal){
            openSquaresQuery = String.format(
                    "MATCH (x:%1$s)-[:SIBLING]-(y:%1$s)-[r:SIBLING]-(z:%2$s) " +
                            "WHERE NOT (x)-[:SIBLING]-(z) AND id(x) < id(z) AND r.distance <= %3$s AND r.fields_populated >= %4$s " +
                            "RETURN count(DISTINCT [x, z]) as cluster_count",
                    type1, type2, threshold, fields
            );

            openSquaresQuery2 = String.format(
                    "MATCH (x:%1$s)-[:SIBLING]-(y:%1$s)-[r:SIBLING]-(z:%2$s), (x)-[t:SIBLING]-(z)  " +
                            "WHERE id(x) < id(z) AND r.distance <= %3$s AND r.fields_populated >= %4$s " +
                            "AND (t.fields_populated < %4$s OR t.distance > %3$s) " +
                            "RETURN count(DISTINCT [x, z]) as cluster_count",
                    type1, type2, threshold, fields
            );
        }else{
            openSquaresQuery = String.format("MATCH (b1:Birth)-[:SIBLING]-(b2:Birth), (b1)-[:SIBLING]-(b:Birth), (b2)-[:SIBLING]-(b), (b2)-[r:SIBLING]-(d:Death), (d)-[:ID]-(b), (b)-[:ID]-(m:Marriage), (b1)-[:ID]-(m), (b2)-[:ID]-(m) \n" +
                    "WHERE id(b1) < id(b2) AND NOT (b1)-[:SIBLING]-(d) AND NOT (d)-[:SIBLING]-(b) AND NOT (b1)-[:ID]-(d) AND b1.BIRTH_YEAR <> right(d.DATE_OF_BIRTH, 4) AND b1.BIRTH_YEAR <> \"----\" \n" +
                    "AND r.distance <= %1$s AND r.fields_populated >= %2$s\n" +
                    "return count(DISTINCT [b1, b2]) as cluster_count", threshold, fields);

            openSquaresQuery2 = String.format("MATCH (b1:Birth)-[:SIBLING]-(b2:Birth), (b1)-[:SIBLING]-(b:Birth), (b2)-[:SIBLING]-(b), (b2)-[r:SIBLING]-(d:Death), (d)-[:ID]-(b), (b)-[:ID]-(m:Marriage), (b1)-[:ID]-(m), (b2)-[:ID]-(m), (b1)-[t:SIBLING]-(d)\n" +
                    "WHERE id(b1) < id(b2) AND NOT (d)-[:SIBLING]-(b) AND b1.BIRTH_YEAR <> right(d.DATE_OF_BIRTH, 4) AND b1.BIRTH_YEAR <> \"----\" \n" +
                    "AND r.distance <= %1$s AND r.fields_populated >= %2$s\n" +
                    "AND (t.fields_populated < %2$s OR t.distance > %1$s)\n" +
                    "return count(DISTINCT [b1, b2]) as cluster_count", threshold, fields);
        }

        Result result = bridge.getNewSession().run(openSquaresQuery);
        List<Long> clusters = result.list(r -> r.get("cluster_count").asLong());

        if (!clusters.isEmpty()) {
            count = clusters.get(0);
        }

        result = bridge.getNewSession().run(openSquaresQuery2);
        clusters = result.list(r -> r.get("cluster_count").asLong());

        long tCount = 0;
        if (!clusters.isEmpty()) {
            tCount = clusters.get(0);
        }

        return (int) count + (int) tCount;
    }

    /**
     * Method to count number of open triangles using an isomorphic pattern
     *
     * @param bridge Neo4j bridge
     * @param type1 x,z records
     * @param type2 y record
     * @param threshold threshold to count at
     * @param fields number of fields to count at
     * @return total number of open triangles
     */
    public static int countOpenTrianglesIsomorphicSiblings(NeoDbCypherBridge bridge, String type1, String type2, double threshold, int fields) {
        long count = 0;
        String openTriangleQuery = String.format(
                "MATCH (b1:%1$s)-[:SIBLING]-(b2:%1$s)-[:SIBLING]-(b3:%1$s),\n" +
                        "(d1:%2$s)-[r:SIBLING]-(d2:%2$s)-[s:SIBLING]-(d3:%2$s),\n" +
                        "(b1)-[:ID]-(d1),\n" +
                        "(b2)-[:ID]-(d2),\n" +
                        "(b3)-[:ID]-(d3),\n" +
                        "(b1)-[:SIBLING]-(b3)\n" +
                        "WHERE NOT (d1)-[:SIBLING]-(d3) AND b1.BIRTH_YEAR = right(d1.DATE_OF_BIRTH, 4) AND b2.BIRTH_YEAR = right(d2.DATE_OF_BIRTH, 4) AND b3.BIRTH_YEAR = right(d3.DATE_OF_BIRTH, 4)\n" +
                        "AND id(d1) < id(d3) AND r.distance <= %3$s AND r.fields_populated >= %4$s AND s.distance <= %3$s AND s.fields_populated >= %4$s\n" +
                        "and not (b1)-[:ID]-(d2) and not (b1)-[:ID]-(d3) and not (b2)-[:ID]-(d1) and not (b2)-[:ID]-(d3) and not (b3)-[:ID]-(d2) and not (b3)-[:ID]-(d1)\n" +
                        "RETURN count(DISTINCT [d1, d2]) as cluster_count",
                type1, type2, threshold, fields
        );

        Result result = bridge.getNewSession().run(openTriangleQuery);
        List<Long> clusters = result.list(r -> r.get("cluster_count").asLong());

        if (!clusters.isEmpty()) {
            count = clusters.get(0);
        }

        String openTriangleQuery2 = String.format(
                "MATCH (b1:%1$s)-[:SIBLING]-(b2:%1$s)-[:SIBLING]-(b3:%1$s),\n" +
                        "(d1:%2$s)-[r:SIBLING]-(d2:%2$s)-[s:SIBLING]-(d3:%2$s),\n" +
                        "(b1)-[:ID]-(d1),\n" +
                        "(b2)-[:ID]-(d2),\n" +
                        "(b3)-[:ID]-(d3),\n" +
                        "(b1)-[:SIBLING]-(b3),\n" +
                        "(d1)-[t:SIBLING]-(d3)\n" +
                        "WHERE b1.BIRTH_YEAR = right(d1.DATE_OF_BIRTH, 4) AND b2.BIRTH_YEAR = right(d2.DATE_OF_BIRTH, 4) AND b3.BIRTH_YEAR = right(d3.DATE_OF_BIRTH, 4)\n" +
                        "AND id(d1) < id(d3) AND r.distance <= %3$s AND r.fields_populated >= %4$s AND s.distance <= %3$s AND s.fields_populated >= %4$s\n" +
                        "AND (t.fields_populated < %4$s OR t.distance > %3$s)\n" +
                        "and not (b1)-[:ID]-(d2) and not (b1)-[:ID]-(d3) and not (b2)-[:ID]-(d1) and not (b2)-[:ID]-(d3) and not (b3)-[:ID]-(d2) and not (b3)-[:ID]-(d1)\n" +
                        "RETURN count(DISTINCT [d1, d3]) as cluster_count",
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

    /**
     * Method to count number of open triangles for ID linkage.
     *
     * @param bridge Neo4j bridge
     * @param type1 x,z records
     * @param type2 y record
     * @param threshold threshold to count at
     * @param fields number of fields to count at
     * @param isTotal boolean check if need to return total or FNOTs
     * @param partner check for marriage linkage what partner needs analysing, null if none
     * @return total number of open triangles
     */
    public static int countOpenSquaresCumulativeID(NeoDbCypherBridge bridge, String type1, String type2, double threshold, int fields, boolean isTotal, String partner) {
        long count = 0;
        String openSquaresQuery = "";
        String openSquaresQuery2 = "";

        if (Objects.equals(type1, "Birth") && Objects.equals(type2, "Death")) {
            if(isTotal){
                openSquaresQuery = String.format("MATCH (b1:%1$s)-[:SIBLING]-(b2:%1$s),\n" +
                        "(d1:%2$s)-[:SIBLING]-(d2:%2$s),\n" +
                        "(b1)-[r:ID]-(d1)\n" +
                        "WHERE id(b1) < id(b2) AND NOT (b2)-[:ID]-(d2) AND r.distance <= %3$s AND r.fields_populated >= %4$s " +
                        "RETURN count(DISTINCT [b1, b2]) as cluster_count", type1, type2, threshold, fields);
                openSquaresQuery2 = String.format("MATCH (b1:%1$s)-[:SIBLING]-(b2:%1$s),\n" +
                        "(d1:%2$s)-[:SIBLING]-(d2:%2$s),\n" +
                        "(b1)-[r:ID]-(d1),\n" +
                        "(b2)-[s:ID]-(d2)\n" +
                        "WHERE id(b1) < id(b2) AND r.distance <= %3$s AND r.fields_populated >= %4$s\n" +
                        "AND (s.fields_populated < %4$s OR s.distance > %3$s) " +
                        "RETURN count(DISTINCT [b1, b2]) as cluster_count", type1, type2, threshold, fields);
            }else{
                openSquaresQuery = String.format("MATCH (b1:%1$s)-[:SIBLING]-(b2:%1$s),\n" +
                        "(d1:%2$s)-[:SIBLING]-(d2:%2$s),\n" +
                        "(b1)-[r:ID]-(d1)\n" +
                        "WHERE NOT (b2)-[:ID]-(d2) AND NOT (b2)-[:SIBLING]-(d2) AND b2.FORENAME = d2.FORENAME AND b2.SURNAME = d2.SURNAME AND b2.BIRTH_YEAR = right(d2.DATE_OF_BIRTH, 4) " +
                        "AND b1.FORENAME = d1.FORENAME AND b1.SURNAME = d1.SURNAME AND b1.BIRTH_YEAR = right(d1.DATE_OF_BIRTH, 4) AND r.distance <= %3$s AND r.fields_populated >= %4$s\n" +
                        "RETURN count(DISTINCT [b1, b2]) as cluster_count", type1, type2, threshold, fields);

                openSquaresQuery2 = String.format("MATCH (b1:%1$s)-[:SIBLING]-(b2:%1$s),\n" +
                        "(d1:%2$s)-[:SIBLING]-(d2:%2$s),\n" +
                        "(b1)-[r:ID]-(d1),\n" +
                        "(b2)-[s:ID]-(d2)\n" +
                        "WHERE NOT (b2)-[:SIBLING]-(d2) AND r.distance <= %3$s AND r.fields_populated >= %4$s\n" +
                        "AND b2.FORENAME = d2.FORENAME AND b2.SURNAME = d2.SURNAME AND b2.BIRTH_YEAR = right(d2.DATE_OF_BIRTH, 4) AND b1.FORENAME = d1.FORENAME AND b1.SURNAME = d1.SURNAME AND b1.BIRTH_YEAR = right(d1.DATE_OF_BIRTH, 4) " +
                        "AND (s.fields_populated < %4$s OR s.distance > %3$s) " +
                        "RETURN count(DISTINCT [b1, b2]) as cluster_count", type1, type2, threshold, fields);
            }
        }else if(Objects.equals(type1, "Marriage") && Objects.equals(type2, "Death")){
            openSquaresQuery = String.format("MATCH (b1:%1$s)-[:SIBLING]-(b2:%1$s),\n" +
                    "(d1:%2$s)-[:SIBLING]-(d2:%2$s),\n" +
                    "(b1)-[r:ID]-(d1)\n" +
                    "WHERE NOT (b2)-[:ID]-(d2) AND NOT (b2)-[:SIBLING]-(d2) AND b2.GROOM_FORENAME = d2.FORENAME AND right(b2.GROOM_AGE_OR_DATE_OF_BIRTH, 4) = right(d2.DATE_OF_BIRTH, 4) " +
                    "AND b1.GROOM_FORENAME = d1.FORENAME AND right(b1.GROOM_AGE_OR_DATE_OF_BIRTH, 4) = right(d1.DATE_OF_BIRTH, 4) AND r.distance <= %3$s AND r.fields_populated >= %4$s\n" +
                    "RETURN count(DISTINCT [b1, b2]) as cluster_count", type1, type2, threshold, fields);

            openSquaresQuery2 = String.format("MATCH (b1:%1$s)-[:SIBLING]-(b2:%1$s),\n" +
                    "(d1:%2$s)-[:SIBLING]-(d2:%2$s),\n" +
                    "(b1)-[r:ID]-(d1),\n" +
                    "(b2)-[s:ID]-(d2)\n" +
                    "WHERE NOT (b2)-[:SIBLING]-(d2) AND r.distance <= %3$s AND r.fields_populated >= %4$s\n" +
                    "AND b2.GROOM_FORENAME = d2.FORENAME AND right(b2.GROOM_AGE_OR_DATE_OF_BIRTH, 4)= right(d2.DATE_OF_BIRTH, 4) AND b1.GROOM_FORENAME = d1.FORENAME AND right(b1.GROOM_AGE_OR_DATE_OF_BIRTH, 4) = right(d1.DATE_OF_BIRTH, 4) " +
                    "AND (s.fields_populated < %4$s OR s.distance > %3$s) " +
                    "RETURN count(DISTINCT [b1, b2]) as cluster_count", type1, type2, threshold, fields);
        }else if(Objects.equals(type1, "Birth") && Objects.equals(type2, "Marriage")){
            if(isTotal){
                openSquaresQuery = String.format("MATCH (b1:%1$s)-[:SIBLING]-(b2:%1$s),\n" +
                        "(d1:%2$s)-[:SIBLING]-(d2:%2$s),\n" +
                        "(b1)-[r:ID {actors: \"Child-%5$s\"}]-(d1)\n" +
                        "WHERE id(b1) < id(b2) AND NOT (b2)-[:ID]-(d2) AND r.distance <= %3$s AND r.fields_populated >= %4$s\n" +
                        "RETURN count(DISTINCT [b1, b2]) as cluster_count", type1, type2, threshold, fields, partner);

                openSquaresQuery2 = String.format("MATCH (b1:%1$s)-[:SIBLING]-(b2:%1$s),\n" +
                        "(d1:%2$s)-[:SIBLING]-(d2:%2$s),\n" +
                        "(b1)-[r:ID {actors: \"Child-%5$s\"}]-(d1),\n" +
                        "(b2)-[s:ID {actors: \"Child-%5$s\"}]-(d2)\n" +
                        "WHERE id(b1) < id(b2) AND r.distance <= %3$s AND r.fields_populated >= %4$s\n" +
                        "AND (s.fields_populated < %4$s OR s.distance > %3$s) " +
                        "RETURN count(DISTINCT [b1, b2]) as cluster_count", type1, type2, threshold, fields, partner);
            }else{
                String partnerCapitalised = partner.toUpperCase();
                openSquaresQuery = String.format("MATCH (b1:%1$s)-[:SIBLING]-(b2:%1$s),\n" +
                        "(d1:%2$s)-[:SIBLING]-(d2:%2$s),\n" +
                        "(b1)-[r:ID {actors: \"Child-%5$s\"}]-(d1)\n" +
                        "WHERE id(b1) < id(b2) AND NOT (b2)-[:ID]-(d2) AND NOT (b2)-[:SIBLING]-(d2) AND b2.FORENAME = d2.%6$s_FORENAME AND b2.SURNAME = d2.%6$s_SURNAME AND b2.BIRTH_YEAR = right(d2.%6$s_AGE_OR_DATE_OF_BIRTH, 4)\n" +
                        "AND b1.FORENAME = d1.%6$s_FORENAME AND b1.SURNAME = d1.%6$s_SURNAME AND b1.BIRTH_YEAR = right(d1.%6$s_AGE_OR_DATE_OF_BIRTH, 4)\n" +
                        "AND r.distance <= %3$s AND r.fields_populated >= %4$s\n" +
                        "RETURN count(DISTINCT [b1, b2]) as cluster_count", type1, type2, threshold, fields, partner, partnerCapitalised);

                openSquaresQuery2 = String.format("MATCH (b1:%1$s)-[:SIBLING]-(b2:%1$s),\n" +
                        "(d1:%2$s)-[:SIBLING]-(d2:%2$s),\n" +
                        "(b1)-[r:ID {actors: \"Child-%5$s\"}]-(d1),\n" +
                        "(b2)-[s:ID {actors: \"Child-%5$s\"}]-(d2)\n" +
                        "WHERE id(b1) < id(b2) AND b2.FORENAME = d2.%6$s_FORENAME AND b2.SURNAME = d2.%6$s_SURNAME AND b2.BIRTH_YEAR = right(d2.%6$s_AGE_OR_DATE_OF_BIRTH, 4)\n" +
                        "AND b1.FORENAME = d1.%6$s_FORENAME AND b1.SURNAME = d1.%6$s_SURNAME AND b1.BIRTH_YEAR = right(d1.%6$s_AGE_OR_DATE_OF_BIRTH, 4)\n" +
                        "AND r.distance <= %3$s AND r.fields_populated >= %4$s\n" +
                        "AND (s.fields_populated < %4$s OR s.distance > %3$s)\n" +
                        "RETURN count(DISTINCT [b1, b2]) as cluster_count", type1, type2, threshold, fields, partner, partnerCapitalised);
            }
        }else if(Objects.equals(type1, "Marriage") && Objects.equals(type2, "Marriage")){
            if(isTotal){
                openSquaresQuery = String.format("MATCH (m:Marriage)-[r:ID {actors: \"%3$s-Couple\"}]->(m2:Marriage)-[:SIBLING]-(m1:Marriage)\n" +
                        "WHERE NOT (m1)-[:ID]-(m) AND id(m) < id(m1) " +
                        "AND r.distance <= %1$s AND r.fields_populated >= %2$s " +
                        "RETURN count(DISTINCT [m, m2]) as cluster_count", threshold, fields, partner);

                openSquaresQuery2 = String.format("MATCH (m:Marriage)-[r:ID {actors: \"%3$s-Couple\"}]->(m2:Marriage)-[:SIBLING]-(m1:Marriage),\n" +
                        "(m1)-[s:ID]-(m) " +
                        "WHERE id(m) < id(m1) " +
                        "AND r.distance <= %1$s AND r.fields_populated >= %2$s " +
                        "AND (s.fields_populated < %2$s OR s.distance > %1$s)\n" +
                        "RETURN count(DISTINCT [m, m2]) as cluster_count", threshold, fields, partner);
            }else{
                openSquaresQuery = String.format("MATCH (m:Marriage)-[r:ID {actors: \"%3$s-Couple\"}]->(m1:Marriage)-[:ID]-(b1:Birth)-[:SIBLING]-(b2:Birth)-[:ID]-(m2:Marriage)\n" +
                        "WHERE (m1)-[:SIBLING]-(m2)\n" +
                        "AND NOT (b1)-[:SIBLING]-(m1) AND NOT (b2)-[:SIBLING]-(m2) AND NOT (b1)-[:ID]-(m2) AND NOT (b2)-[:ID]-(m1)\n" +
                        "AND (b1)-[:ID]-(m) AND (b2)-[:ID]-(m)\n" +
                        "AND NOT (m2)-[:ID]-(m)\n" +
                        "AND r.distance <= %1$s AND r.fields_populated >= %2$s " +
                        "AND id(m1) < id(m2)\n" +
                        "AND b1.PARENTS_YEAR_OF_MARRIAGE = m.MARRIAGE_YEAR AND b2.PARENTS_YEAR_OF_MARRIAGE = m.MARRIAGE_YEAR \n" +
                        "RETURN count(DISTINCT [m, m2]) as cluster_count", threshold, fields, partner);

                openSquaresQuery2 = String.format("MATCH (m:Marriage)-[r:ID {actors: \"%3$s-Couple\"}]->(m1:Marriage)-[:ID]-(b1:Birth)-[:SIBLING]-(b2:Birth)-[:ID]-(m2:Marriage),\n" +
                        "(m2)-[t:ID]-(m)\n" +
                        "WHERE (m1)-[:SIBLING]-(m2)\n" +
                        "AND NOT (b1)-[:SIBLING]-(m1) AND NOT (b2)-[:SIBLING]-(m2) AND NOT (b1)-[:ID]-(m2) AND NOT (b2)-[:ID]-(m1)\n" +
                        "AND (b1)-[:ID]-(m) AND (b2)-[:ID]-(m)\n" +
                        "AND r.distance <= %1$s AND r.fields_populated >= %2$s " +
                        "AND (t.fields_populated < %2$s OR t.distance > %1$s)\n" +
                        "AND id(m1) < id(m2)\n" +
                        "AND b1.PARENTS_YEAR_OF_MARRIAGE = m.MARRIAGE_YEAR AND b2.PARENTS_YEAR_OF_MARRIAGE = m.MARRIAGE_YEAR " +
                        "RETURN count(DISTINCT [m, m2]) as cluster_count", threshold, fields, partner);
            }
        }


        Result result = bridge.getNewSession().run(openSquaresQuery);
        List<Long> clusters = result.list(r -> r.get("cluster_count").asLong());

        if (!clusters.isEmpty()) {
            count = clusters.get(0);
        }

        result = bridge.getNewSession().run(openSquaresQuery2);
        clusters = result.list(r -> r.get("cluster_count").asLong());

        long tCount = 0;
        if (!clusters.isEmpty()) {
            tCount = clusters.get(0);
        }

        return (int) count + (int) tCount;
    }
}

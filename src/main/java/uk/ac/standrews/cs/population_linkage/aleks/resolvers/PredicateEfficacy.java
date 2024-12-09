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

public class PredicateEfficacy {
    //sibling efficacy queries
    private String CREATED_LINKS_QUERY_TRUE = "MATCH (x:%1$s)-[r:SIBLING]-(y:%2$s) WHERE r.provenance = \"%3$s\" AND (x:%1$s)-[:GT_SIBLING]-(y:%2$s) AND NOT (x)-[:DELETED]-(y) RETURN count(r) as cluster_count";
    private String DELETED_LINKS_QUERY_TRUE = "MATCH (x:%1$s)-[r:DELETED]-(y:%2$s) WHERE r.provenance = \"%3$s\" AND NOT (x:%1$s)-[:GT_SIBLING]-(y:%2$s) RETURN count(r) as cluster_count";
    private String LINKS_QUERY_TOTAL_CREATED = "MATCH (x:%s)-[r:SIBLING]-(y:%s) WHERE r.provenance = \"%s\" AND NOT (x)-[:DELETED]-(y) RETURN count(r) as cluster_count";
    private String LINKS_QUERY_TOTAL_DELETED = "MATCH (x:%s)-[r:DELETED]-(y:%s) WHERE r.provenance = \"%s\" RETURN count(r) as cluster_count";

    //id efficacy queries
    private String DELETED_LINKS_QUERY_TRUE_ID = "MATCH (x:%1$s)-[r:DELETED]-(y:%2$s) WHERE r.provenance = \"%3$s\" and r.actors = \"%4$s\" AND NOT (x:%1$s)-[:GT_ID {actors: \"%5$s\"}]-(y:%2$s) RETURN count(r) as cluster_count";
    private String LINKS_QUERY_TOTAL_DELETED_ID = "MATCH (x:%s)-[r:DELETED]-(y:%s) WHERE r.provenance = \"%s\" and r.actors = \"%s\" RETURN count(r) as cluster_count";
    private String CREATED_LINKS_QUERY_TRUE_ID = "MATCH (x:%1$s)-[r:ID]-(y:%2$s) WHERE r.provenance = \"%3$s\" and r.actors = \"%4$s\" AND (x:%1$s)-[:GT_ID]-(y:%2$s) AND NOT (x)-[:DELETED]-(y) RETURN count(r) as cluster_count";
    private String LINKS_QUERY_TOTAL_CREATED_ID = "MATCH (x:%s)-[r:ID]-(y:%s) WHERE r.provenance = \"%s\" and r.actors = \"%s\" AND NOT (x)-[:DELETED]-(y) RETURN count(r) as cluster_count";

    NeoDbCypherBridge bridge;

    public PredicateEfficacy(){
        this.bridge = Store.getInstance().getBridge();
    }

    /**
     * Method to calculate efficacy for sibling resolvers
     *
     * @param toCreate array of predicate provenances which create links
     * @param toDelete array of predicate provenances which delete links
     * @param recordType1 type of record from
     * @param recordType2 type of record to
     */
    public void countSiblingEfficacy(String[] toCreate, String[] toDelete, String recordType1, String recordType2){
        //loop through created links
        System.out.println("Created");
        for (String s : toCreate) {
            Result result = bridge.getNewSession().run(String.format(CREATED_LINKS_QUERY_TRUE, recordType1, recordType2, s));
            List<Long> trueMatches = result.list(r -> r.get("cluster_count").asLong()); //get links that are correct
            result = bridge.getNewSession().run(String.format(LINKS_QUERY_TOTAL_CREATED, recordType1, recordType2, s));
            List<Long> total = result.list(r -> r.get("cluster_count").asLong()); //get all links made

            //print if both arent empty
            if (!trueMatches.isEmpty() && !total.isEmpty()) {
                long trueCount = trueMatches.get(0);
                long totalCount = total.get(0);

                System.out.println(s + " " + trueCount + "/" + totalCount + " " + ((double) trueCount / totalCount));
            }
        }

        //loop through deleted links
        System.out.println("\nDeleted");
        for (String s : toDelete) {
            Result result = bridge.getNewSession().run(String.format(DELETED_LINKS_QUERY_TRUE, recordType1, recordType2, s));
            List<Long> trueMatches = result.list(r -> r.get("cluster_count").asLong()); //get links that are correct
            result = bridge.getNewSession().run(String.format(LINKS_QUERY_TOTAL_DELETED, recordType1, recordType2, s));
            List<Long> total = result.list(r -> r.get("cluster_count").asLong()); //get all links made

            //print if both arent empty
            if (!trueMatches.isEmpty() && !total.isEmpty()) {
                long trueCount = trueMatches.get(0);
                long totalCount = total.get(0);

                System.out.println(s + " " + trueCount + "/" + totalCount + " " + ((double) trueCount / totalCount));
            }
        }
    }

    /**
     * Method to calculate efficacy for ID resolvers (only deleted)
     *
     * @param toDelete array of predicate provenances which delete links
     * @param recordType1 type of record from
     * @param recordType2 type of record to
     * @param actors actors used in link
     */
    public void countIDEfficacyDel(String[] toDelete, String recordType1, String recordType2, String actors){
        System.out.println("Created");
        for (String s : toDelete) {
            List<Long> trueMatches;
            List<Long> total;

            //get number of correct and total links
            if(actors.equals("Father-Groom")){ //check if actors are Father-Groom (GT uses different actors so must account for it)
                Result result = bridge.getNewSession().run(String.format(DELETED_LINKS_QUERY_TRUE_ID, recordType1, recordType2, s, "Child-Father", actors));
                trueMatches = result.list(r -> r.get("cluster_count").asLong());
                result = bridge.getNewSession().run(String.format(LINKS_QUERY_TOTAL_DELETED_ID, recordType1, recordType2, s, "Child-Father"));
                total = result.list(r -> r.get("cluster_count").asLong());
            } else if (actors.equals("Mother-Bride")) { //check if actors are Mother-Groom (GT uses different actors so must account for it)
                Result result = bridge.getNewSession().run(String.format(DELETED_LINKS_QUERY_TRUE_ID, recordType1, recordType2, s, "Child-Mother", actors));
                trueMatches = result.list(r -> r.get("cluster_count").asLong());
                result = bridge.getNewSession().run(String.format(LINKS_QUERY_TOTAL_DELETED_ID, recordType1, recordType2, s, "Child-Mother"));
                total = result.list(r -> r.get("cluster_count").asLong());
            }else{ //all other actors are treated as normal
                Result result = bridge.getNewSession().run(String.format(DELETED_LINKS_QUERY_TRUE_ID, recordType1, recordType2, s, actors, actors));
                trueMatches = result.list(r -> r.get("cluster_count").asLong());
                result = bridge.getNewSession().run(String.format(LINKS_QUERY_TOTAL_DELETED_ID, recordType1, recordType2, s, actors));
                total = result.list(r -> r.get("cluster_count").asLong());
            }

            //print if both arent empty
            if (!trueMatches.isEmpty() && !total.isEmpty()) {
                long trueCount = trueMatches.get(0);
                long totalCount = total.get(0);

                System.out.println(s + " " + trueCount + "/" + totalCount + " " + ((double) trueCount / totalCount));
            }
        }
    }

    /**
     * Method to calculate efficacy for ID resolvers (only created)
     *
     * @param toCreate array of predicate provenances which create links
     * @param recordType1 type of record from
     * @param recordType2 type of record to
     * @param actors actors used in link
     */
    public void countIDEfficacyCreate(String[] toCreate, String recordType1, String recordType2, String actors){
        System.out.println("Deleted");
        for (String s : toCreate) {
            Result result = bridge.getNewSession().run(String.format(CREATED_LINKS_QUERY_TRUE_ID, recordType1, recordType2, s, actors));
            List<Long> trueMatches = result.list(r -> r.get("cluster_count").asLong()); //get links that are correct
            result = bridge.getNewSession().run(String.format(LINKS_QUERY_TOTAL_CREATED_ID, recordType1, recordType2, s, actors));
            List<Long> total = result.list(r -> r.get("cluster_count").asLong()); //get all links made

            //print if both arent empty
            if (!trueMatches.isEmpty() && !total.isEmpty()) {
                long trueCount = trueMatches.get(0);
                long totalCount = total.get(0);

                System.out.println(s + " " + trueCount + "/" + totalCount + " " + ((double) trueCount / totalCount));
            }
        }
    }
}

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
    private String CREATED_LINKS_QUERY_TRUE = "MATCH (x:%1$s)-[r:SIBLING]-(y:%2$s) WHERE r.provenance = \"%3$s\" AND (x:%1$s)-[:GT_SIBLING]-(y:%2$s) AND NOT (x)-[:DELETED]-(y) RETURN count(r) as cluster_count";
    private String DELETED_LINKS_QUERY_TRUE = "MATCH (x:%1$s)-[r:DELETED]-(y:%2$s) WHERE r.provenance = \"%3$s\" AND NOT (x:%1$s)-[:GT_SIBLING]-(y:%2$s) RETURN count(r) as cluster_count";
    private String LINKS_QUERY_TOTAL_CREATED = "MATCH (x:%s)-[r:SIBLING]-(y:%s) WHERE r.provenance = \"%s\" AND NOT (x)-[:DELETED]-(y) RETURN count(r) as cluster_count";
    private String LINKS_QUERY_TOTAL_DELETED = "MATCH (x:%s)-[r:DELETED]-(y:%s) WHERE r.provenance = \"%s\" RETURN count(r) as cluster_count";

    private String DELETED_LINKS_QUERY_TRUE_ID = "MATCH (x:%1$s)-[r:DELETED]-(y:%2$s) WHERE r.provenance = \"%3$s\" AND NOT (x:%1$s)-[:GT_ID {actors: \"%4$s\"}]-(y:%2$s) RETURN count(r) as cluster_count";
    private String LINKS_QUERY_TOTAL_DELETED_ID = "MATCH (x:%s)-[r:DELETED]-(y:%s) WHERE r.provenance = \"%s\" RETURN count(r) as cluster_count";

    NeoDbCypherBridge bridge;

    public PredicateEfficacy(){
        this.bridge = Store.getInstance().getBridge();
    }

    public void countSiblingEfficacy(String[] toCreate, String[] toDelete, String recordType1, String recordType2){
        System.out.println("Created");
        for (String s : toCreate) {
            Result result = bridge.getNewSession().run(String.format(CREATED_LINKS_QUERY_TRUE, recordType1, recordType2, s));
            List<Long> trueMatches = result.list(r -> r.get("cluster_count").asLong());
            result = bridge.getNewSession().run(String.format(LINKS_QUERY_TOTAL_CREATED, recordType1, recordType2, s));
            List<Long> total = result.list(r -> r.get("cluster_count").asLong());

            if (!trueMatches.isEmpty() && !total.isEmpty()) {
                long trueCount = trueMatches.get(0);
                long totalCount = total.get(0);

                System.out.println(s + " " + trueCount + "/" + totalCount + " " + ((double) trueCount / totalCount));
            }
        }

        System.out.println("\nDeleted");
        for (String s : toDelete) {
            Result result = bridge.getNewSession().run(String.format(DELETED_LINKS_QUERY_TRUE, recordType1, recordType2, s));
            List<Long> trueMatches = result.list(r -> r.get("cluster_count").asLong());
            result = bridge.getNewSession().run(String.format(LINKS_QUERY_TOTAL_DELETED, recordType1, recordType2, s));
            List<Long> total = result.list(r -> r.get("cluster_count").asLong());

            if (!trueMatches.isEmpty() && !total.isEmpty()) {
                long trueCount = trueMatches.get(0);
                long totalCount = total.get(0);

                System.out.println(s + " " + trueCount + "/" + totalCount + " " + ((double) trueCount / totalCount));
            }
        }
    }

    public void countIDEfficacy(String[] toDelete, String recordType1, String recordType2, String actors){
        System.out.println("Deleted");
        for (String s : toDelete) {
            Result result = bridge.getNewSession().run(String.format(DELETED_LINKS_QUERY_TRUE_ID, recordType1, recordType2, s, actors));
            List<Long> trueMatches = result.list(r -> r.get("cluster_count").asLong());
            result = bridge.getNewSession().run(String.format(LINKS_QUERY_TOTAL_DELETED_ID, recordType1, recordType2, s));
            List<Long> total = result.list(r -> r.get("cluster_count").asLong());

            if (!trueMatches.isEmpty() && !total.isEmpty()) {
                long trueCount = trueMatches.get(0);
                long totalCount = total.get(0);

                System.out.println(s + " " + trueCount + "/" + totalCount + " " + ((double) trueCount / totalCount));
            }
        }
    }
}

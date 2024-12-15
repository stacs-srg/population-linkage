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
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;

import java.util.HashMap;
import java.util.Map;

public abstract class ThresholdTrianglesAnalysis {

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

    /**
     * Method to delete links created when maximising threshold
     *
     * @param query_string Cypher query
     * @param threshold current threshold being analysed
     * @param fields current field being analysed
     * @param bridge Neo4j bridge
     */
    protected static void doQueryDel(String query_string, double threshold, int fields, NeoDbCypherBridge bridge) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("threshold", threshold);
        parameters.put("field", fields);

        Session session = bridge.getNewSession();
        Transaction tx = session.beginTransaction();
        tx.run(query_string, parameters);
        tx.commit();
    }
}

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

import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import uk.ac.standrews.cs.neoStorr.impl.Store;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.aleks.PatternsCounter;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.DeathDeathSiblingAccuracy;

public class BirthDeathPatternResolver {
    public static void main(String[] args) {
        NeoDbCypherBridge bridge = Store.getInstance().getBridge();
        String query = "MATCH (b1:Death)-[:SIBLING]-(d:Birth),\n" +
                "(b2:Death)-[:SIBLING]-(d),\n" +
                "(b1:Death)-[:ID]-(d1:Birth),\n" +
                "(b2:Death)-[:ID]-(d2:Birth),\n" +
                "(d)-[:SIBLING]-(d1)-[:SIBLING]-(d2)\n" +
                "WHERE NOT (b1)-[:SIBLING]-(b2) and not (b1)-[:SIBLING]-(d1) and not (b2)-[:SIBLING]-(d2)\n" +
                "MERGE (b1)-[r:SIBLING { provenance: \"bd_sol\", actors: \"Deceased-Deceased\" } ]-(b2)";

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToString(bridge, "Death", "Birth");
        PatternsCounter.countOpenTrianglesToString(bridge, "Death", "Death");
        new DeathDeathSiblingAccuracy(bridge);

        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            tx.run(query);
            tx.commit();
        }

        System.out.println("After");
        PatternsCounter.countOpenTrianglesToString(bridge, "Death", "Birth");
        PatternsCounter.countOpenTrianglesToString(bridge, "Death", "Death");
        new DeathDeathSiblingAccuracy(bridge);
    }
}


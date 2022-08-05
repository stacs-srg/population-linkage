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
package uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks;

import org.neo4j.driver.Session;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.time.LocalDateTime;

/*
 * Deletes ground truth links in Neo4J for Umea data set
 *
 * @author al
 */
public class DeleteGTLinks {

    public static String[] queries = new String[] {
        "MATCH (a)-[r:GROUND_TRUTH_BIRTH_FATHER_IDENTITY]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_BIRTH_MOTHER_IDENTITY]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_BIRTH_SIBLING]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_BIRTH_HALF_SIBLING]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_BIRTH_DEATH_IDENTITY]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_BIRTH_DEATH_SIBLING]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_DEATH_SIBLING]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_BIRTH_DEATH_HALF_SIBLING]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_BIRTH_GROOM_IDENTITY]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_BIRTH_BRIDE_IDENTITY]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_FATHER_GROOM_IDENTITY]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_MOTHER_BRIDE_IDENTITY]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_BIRTH_PARENTS_MARRIAGE]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_DEATH_PARENTS_MARRIAGE]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_BIRTH_GROOM_SIBLING]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_BIRTH_BRIDE_SIBLING]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_DEATH_GROOM_IDENTITY]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_DEATH_BRIDE_IDENTITY]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_DEATH_GROOM_SIBLING]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_DEATH_BRIDE_SIBLING]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_GROOM_GROOM_IDENTITY]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_BRIDE_BRIDE_IDENTITY]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_GROOM_PARENTS_MARRIAGE]-(b) DELETE r",
                "MATCH (a)-[r:GROUND_TRUTH_BRIDE_PARENTS_MARRIAGE]-(b) DELETE r"
    };

    private static void doQueries(String... queries) {

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge(); Session session = bridge.getNewSession()) {

            System.out.println("Deleting GT links @ " + LocalDateTime.now());

            for (String query : queries) {
                session.run(query);
            }

            System.out.println("Complete @ " + LocalDateTime.now());
        }
    }

    public static void main(String[] args) {

        doQueries( queries );
    }
}
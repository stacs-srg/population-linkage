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
package uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthBirthSibling;

import org.neo4j.driver.Session;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.time.LocalDateTime;

/*
 * Establishes ground truth links in Neo4J for Umea data set
 *
 * @author al
 */
public class CreateBirthSiblingGTLinks {

    private static final String BIRTH_BIRTH_SIBLING = "MATCH (b1:Birth),(b2:Birth) WHERE " +
            "NOT (b1)-[:GT_SIBLING { actors: \"Child-Child\" } ]-(b2) AND " +
            "b1.MOTHER_IDENTITY <> \"\" AND " +
            "b1.FATHER_IDENTITY <> \"\" AND " +
            "b2.MOTHER_IDENTITY <> \"\" AND " +
            "b2.FATHER_IDENTITY <> \"\" AND " +
            "b1 <> b2 AND " +
            "b1.MOTHER_IDENTITY = b2.MOTHER_IDENTITY AND " +
            "b1.FATHER_IDENTITY = b2.FATHER_IDENTITY " +
            "MERGE (b1)-[:GT_SIBLING { actors: \"Child-Child\" } ]-(b2) ";

    private static void doQueries(String... queries) {

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge(); Session session = bridge.getNewSession()) {

            System.out.println("Creating GT links @ " + LocalDateTime.now());

            for (String query : queries) {
                System.out.println( "Running: " + query );
                session.run(query);
                System.out.println("Finished query @ " + LocalDateTime.now());
            }

            System.out.println("Complete @ " + LocalDateTime.now());
        }
    }

    public static void main(String[] args) {
        doQueries(BIRTH_BIRTH_SIBLING);
    }
}
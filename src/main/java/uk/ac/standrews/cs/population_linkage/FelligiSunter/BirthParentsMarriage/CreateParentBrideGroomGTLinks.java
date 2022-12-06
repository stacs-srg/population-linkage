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
package uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthParentsMarriage;

import org.neo4j.driver.Session;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.time.LocalDateTime;

/**
 * Copied from groundTruthNeoLinks.CreateParentBrideGroomGTLinks
 */

/*
 * Establishes ground truth links in Neo4J for Umea data set
 *
 * @author al
 *
 * All the Ground truth links are labelled as:
 * GT_ID for identity linkage
 * GT_SIBLING for sibling linkage
 * GT_HALF_SIBLING for 1/2 siblink linkage
 * Each relationship has an attribute 'actors' which indicates the actors on the linked certificates.
 * The actors are hyphen separated e.g. [:GT_ID { actors: "Child-Father" } ]
 * The strings used to identify the actors are: Child, Deceased, Father, Mother, Couple, Bride, Groom
 */
public class CreateParentBrideGroomGTLinks {

    private static final String FATHER_GROOM_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.FATHER_IDENTITY <> \"\" AND " +
            "m.GROOM_IDENTITY <> \"\" AND " +
            "b.FATHER_IDENTITY = m.GROOM_IDENTITY " +
            "MERGE (b)-[:GT_ID { actors: \"Father-Groom\" } ]-(m)";

    private static final String MOTHER_BRIDE_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.MOTHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_IDENTITY <> \"\" AND " +
            "b.MOTHER_IDENTITY = m.BRIDE_IDENTITY " +
            "MERGE (b)-[:GT_ID { actors: \"Mother-Bride\" } ]-(m)";

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
        doQueries(
                FATHER_GROOM_IDENTITY, MOTHER_BRIDE_IDENTITY
        );
    }
}
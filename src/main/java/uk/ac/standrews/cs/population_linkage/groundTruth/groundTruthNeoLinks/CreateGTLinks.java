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
public class CreateGTLinks {

    private static final String FATHER_OWN_BIRTH_IDENTITY = "MATCH (b1:Birth),(b2:Birth) WHERE " +
            "b1.CHILD_IDENTITY <> \"\" AND " +
            "b2.FATHER_IDENTITY <> \"\" AND " +
            "b1.CHILD_IDENTITY = b2.FATHER_IDENTITY " +
            "MERGE (b1)-[:GT_ID { actors: \"Child-Father\" } ]->(b2)";

    private static final String MOTHER_OWN_BIRTH_IDENTITY = "MATCH (b1:Birth),(b2:Birth) WHERE " +
            "b1.CHILD_IDENTITY <> \"\" AND " +
            "b2.MOTHER_IDENTITY <> \"\" AND " +
            "b1.CHILD_IDENTITY = b2.MOTHER_IDENTITY " +
            "MERGE (b1)-[:GT_ID { actors: \"Child-Mother\" } ]->(b2)";

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

    private static final String BIRTH_BIRTH_HALF_SIBLING = "MATCH (b1:Birth),(b2:Birth) WHERE " +
            "NOT (b1)-[:GT_HALF_SIBLING { actors: \"Child-Child\" } ]-(b2) AND " +
            "b1.MOTHER_IDENTITY <> \"\" AND " +
            "b2.MOTHER_IDENTITY <> \"\" AND " +
            "b1.FATHER_IDENTITY <> \"\" AND " +
            "b2.FATHER_IDENTITY <> \"\" AND " +
            "b1 <> b2 AND " +
            "(b1.MOTHER_IDENTITY = b2.MOTHER_IDENTITY OR b1.FATHER_IDENTITY = b2.FATHER_IDENTITY) AND " +
            "NOT (b1.MOTHER_IDENTITY = b2.MOTHER_IDENTITY AND b1.FATHER_IDENTITY = b2.FATHER_IDENTITY) " +
            "MERGE (b1)-[:GT_HALF_SIBLING { actors: \"Child-Child\" } ]-(b2)";

    private static final String BIRTH_DEATH_IDENTITY = "MATCH (b:Birth),(d:Death) WHERE " + "" +
            "b.CHILD_IDENTITY <> \"\" AND " +
            "d.DECEASED_IDENTITY <> \"\" AND " +
            "b.CHILD_IDENTITY = d.DECEASED_IDENTITY " +
            "MERGE (b)-[:GT_ID { actors: \"Child-Deceased\" } ]-(d)";

    private static final String BIRTH_DEATH_SIBLING = "MATCH (b:Birth),(d:Death) WHERE " +
            "b.MOTHER_IDENTITY <> \"\" AND " +
            "b.FATHER_IDENTITY <> \"\" AND " +
            "d.MOTHER_IDENTITY <> \"\" AND " +
            "d.FATHER_IDENTITY <> \"\" AND " +
            "b.MOTHER_IDENTITY = d.MOTHER_IDENTITY AND " +
            "b.FATHER_IDENTITY = d.FATHER_IDENTITY AND " +
            "b.CHILD_IDENTITY <> d.DECEASED_IDENTITY " +
            "MERGE (b)-[:GT_SIBLING { actors: \"Child-Deceased\" } ]-(d)";

    private static final String BIRTH_DEATH_HALF_SIBLING = "MATCH (b:Birth),(d:Death) WHERE " +
            "b.MOTHER_IDENTITY <> \"\" AND " +
            "d.MOTHER_IDENTITY <> \"\" AND " +
            "b.FATHER_IDENTITY <> \"\" AND " +
            "d.FATHER_IDENTITY <> \"\" AND " +
            "(b.MOTHER_IDENTITY = d.MOTHER_IDENTITY OR b.FATHER_IDENTITY = d.FATHER_IDENTITY) AND " +
            "NOT (b.MOTHER_IDENTITY = d.MOTHER_IDENTITY AND b.FATHER_IDENTITY = d.FATHER_IDENTITY) " +
            "MERGE (b)-[:GT_HALF_SIBLING { actors: \"Child-Deceased\" } ]-(d)";

    private static final String BIRTH_GROOM_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.CHILD_IDENTITY <> \"\" AND " +
            "m.GROOM_IDENTITY <> \"\" AND " +
            "b.CHILD_IDENTITY = m.GROOM_IDENTITY " +
            "MERGE (b)-[:GT_ID { actors: \"Child-Groom\" } ]-(m)";

    private static final String BIRTH_BRIDE_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.CHILD_IDENTITY <> \"\" AND " +
            "m.BRIDE_IDENTITY <> \"\" AND " +
            "b.CHILD_IDENTITY = m.BRIDE_IDENTITY " +
            "MERGE (b)-[:GT_ID { actors: \"Child-Bride\" } ]-(m)";

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

    private static final String BIRTH_PARENTS_MARRIAGE_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.MOTHER_IDENTITY <> \"\" AND " +
            "b.FATHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_IDENTITY <> \"\" AND " +
            "m.GROOM_IDENTITY <> \"\" AND " +
            "b.MOTHER_IDENTITY = m.BRIDE_IDENTITY AND " +
            "b.FATHER_IDENTITY = m.GROOM_IDENTITY " +
            "MERGE (b)-[:GT_ID { actors: \"Child-Couple\" } ]-(m)";

    private static final String DEATH_PARENTS_MARRIAGE_IDENTITY = "MATCH (d:Death),(m:Marriage) WHERE " +
            "d.MOTHER_IDENTITY <> \"\" AND " +
            "d.FATHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_IDENTITY <> \"\" AND " +
            "m.GROOM_IDENTITY <> \"\" AND " +
            "d.MOTHER_IDENTITY = m.BRIDE_IDENTITY AND " +
            "d.FATHER_IDENTITY = m.GROOM_IDENTITY " +
            "MERGE (b)-[:GT_ID { actors: \"Deceased-Couple\" } ]-(m)";

    private static final String BIRTH_GROOM_SIBLING = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.MOTHER_IDENTITY <> \"\" AND " +
            "b.FATHER_IDENTITY <> \"\" AND " +
            "m.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "b.MOTHER_IDENTITY = m.GROOM_MOTHER_IDENTITY AND " +
            "b.FATHER_IDENTITY = m.GROOM_FATHER_IDENTITY AND " +
            "b.CHILD_IDENTITY <> m.GROOM_IDENTITY " +
            "MERGE (b)-[:GT_SIBLING { actors: \"Child-Groom\" } ]-(m)";

    private static final String BIRTH_BRIDE_SIBLING = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.MOTHER_IDENTITY <> \"\" AND " +
            "b.FATHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "b.MOTHER_IDENTITY = m.BRIDE_MOTHER_IDENTITY AND " +
            "b.FATHER_IDENTITY = m.BRIDE_FATHER_IDENTITY AND " +
            "b.CHILD_IDENTITY <> m.BRIDE_IDENTITY " +
            "MERGE (b)-[:GT_SIBLING { actors: \"Child-Bride\" } ]-(m)";

    private static final String DEATH_GROOM_IDENTITY = "MATCH (d:Death),(m:Marriage) WHERE " +
            "d.DECEASED_IDENTITY <> \"\" AND " +
            "m.GROOM_IDENTITY <> \"\" AND " +
            "d.DECEASED_IDENTITY = m.GROOM_IDENTITY " +
            "MERGE (d)-[:GT_ID { actors: \"Deceased-Groom\" } ]-(m)";

    private static final String DEATH_BRIDE_IDENTITY = "MATCH (d:Death),(m:Marriage) WHERE " +
            "d.DECEASED_IDENTITY <> \"\" AND " +
            "m.BRIDE_IDENTITY <> \"\" AND " +
            "d.DECEASED_IDENTITY = m.BRIDE_IDENTITY " +
            "MERGE (d)-[:GT_ID { actors: \"Deceased-Bride\" } ]-(m)";

    private static final String DEATH_GROOM_SIBLING = "MATCH (d:Death),(m:Marriage) WHERE " +
            "d.MOTHER_IDENTITY <> \"\" AND " +
            "d.FATHER_IDENTITY <> \"\" AND " +
            "m.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "d.MOTHER_IDENTITY = m.GROOM_MOTHER_IDENTITY AND " +
            "d.FATHER_IDENTITY = m.GROOM_FATHER_IDENTITY AND " +
            "d.DECEASED_IDENTITY <> m.GROOM_IDENTITY " +
            "MERGE (d)-[:GT_SIBLING { actors: \"Deceased-Groom\" } ]-(m)";

    private static final String DEATH_BRIDE_SIBLING = "MATCH (d:Death),(m:Marriage) WHERE " +
            "d.MOTHER_IDENTITY <> \"\" AND " +
            "d.FATHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "d.MOTHER_IDENTITY = m.BRIDE_MOTHER_IDENTITY AND " +
            "d.FATHER_IDENTITY = m.BRIDE_FATHER_IDENTITY AND " +
            "d.DECEASED_IDENTITY <> m.BRIDE_IDENTITY " +
            "MERGE (d)-[:GT_SIBLING { actors: \"Deceased-Bride\" } ]-(m)";

    private static final String GROOM_GROOM_IDENTITY = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "NOT (m1)-[:GT_ID { actors: \"Groom-Groom\" } ]-(m2) AND " +
            "m1.GROOM_IDENTITY <> \"\" AND " +
            "m2.GROOM_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "m1.GROOM_IDENTITY = m2.GROOM_IDENTITY " +
            "MERGE (m1)-[:GT_ID { actors: \"Groom-Groom\" } ]-(m2)";

    private static final String BRIDE_BRIDE_IDENTITY = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "NOT (m1)-[:GT_ID { actors: \"Bride-Bride\" }]-(m2) AND " +
            "m1.BRIDE_IDENTITY <> \"\" AND " +
            "m2.BRIDE_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "m1.BRIDE_IDENTITY = m2.BRIDE_IDENTITY " +
            "MERGE (m1)-[:GT_ID { actors: \"Bride-Bride\" } ]-(m2)";

    private static final String GROOM_PARENTS_MARRIAGE_IDENTITY = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "m1.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m1.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_IDENTITY <> \"\" AND " +
            "m2.BRIDE_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "m1.GROOM_MOTHER_IDENTITY = m2.BRIDE_IDENTITY AND " +
            "m1.GROOM_FATHER_IDENTITY = m2.GROOM_IDENTITY " +
            "MERGE (m1)-[:GT_ID { actors: \"Groom-Couple\" } ]-(m2)";

    private static final String BRIDE_PARENTS_MARRIAGE_IDENTITY = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "m1.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_IDENTITY <> \"\" AND " +
            "m2.BRIDE_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "m1.BRIDE_MOTHER_IDENTITY = m2.BRIDE_IDENTITY AND " +
            "m1.BRIDE_FATHER_IDENTITY = m2.GROOM_IDENTITY " +
            "MERGE (m1)-[:GT_ID { actors: \"Bride-Couple\" } ]-(m2)";

    private static final String GROOM_GROOM_SIBLING = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "NOT (m1)-[:GT_SIBLING { actors: \"Groom-Groom\" } ]-(m2) AND " +
            "m1.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m1.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m1.GROOM_MOTHER_IDENTITY = m2.GROOM_MOTHER_IDENTITY AND " +
            "m1.GROOM_FATHER_IDENTITY = m2.GROOM_FATHER_IDENTITY AND " +
            "m1.GROOM_IDENTITY <> m2.GROOM_IDENTITY " +
            "MERGE (m1)-[:GT_SIBLING { actors: \"Groom-Groom\" } ]-(m2)";

    private static final String BRIDE_BRIDE_SIBLING = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "NOT (m1)-[:GT_SIBLING { actors: \"Bride-Bride\" } ]-(m2) AND " +
            "m1.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m2.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m2.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_MOTHER_IDENTITY = m2.BRIDE_MOTHER_IDENTITY AND " +
            "m1.BRIDE_FATHER_IDENTITY = m2.BRIDE_FATHER_IDENTITY AND " +
            "m1.BRIDE_IDENTITY <> m2.BRIDE_IDENTITY " +
            "MERGE (m1)-[:GT_SIBLING { actors: \"Bride-Bride\" } ]-(m2)";

    private static final String BRIDE_GROOM_SIBLING = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "NOT (m1)-[:GT_SIBLING { actors: \"Bride-Groom\" } ]-(m2) AND " +
            "m1.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_MOTHER_IDENTITY = m2.GROOM_MOTHER_IDENTITY AND " +
            "m1.BRIDE_FATHER_IDENTITY = m2.GROOM_FATHER_IDENTITY " +
            "MERGE (m1)-[:GT_SIBLING { actors: \"Bride-Groom\" } ]-(m2)";

    private static final String GROOM_GROOM_HALF_SIBLING = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "NOT (m1)-[:GT_HALF_SIBLING { actors: \"Groom-Groom\" } ]-(m2) AND " +
            "m1.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m1.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "(m1.GROOM_MOTHER_IDENTITY = m2.GROOM_MOTHER_IDENTITY OR m1.GROOM_FATHER_IDENTITY = m2.GROOM_FATHER_IDENTITY ) AND " +
            "NOT (m1.GROOM_MOTHER_IDENTITY = m2.GROOM_MOTHER_IDENTITY AND m1.GROOM_FATHER_IDENTITY = m2.GROOM_FATHER_IDENTITY ) " +
            "MERGE (m1)-[:GT_HALF_SIBLING { actors: \"Groom-Groom\" } ]-(m2)";

    private static final String BRIDE_BRIDE_HALF_SIBLING = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "NOT (m1)-[:GT_HALF_SIBLING { actors: \"Bride-Bride\" } ]-(m2) AND " +
            "m1.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m2.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m2.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "(m1.BRIDE_MOTHER_IDENTITY = m2.BRIDE_MOTHER_IDENTITY OR m1.BRIDE_FATHER_IDENTITY = m2.BRIDE_FATHER_IDENTITY) AND " +
            "NOT (m1.BRIDE_MOTHER_IDENTITY = m2.BRIDE_MOTHER_IDENTITY AND m1.BRIDE_FATHER_IDENTITY = m2.BRIDE_FATHER_IDENTITY) " +
            "MERGE (m1)-[:GT_HALF_SIBLING { actors: \"Bride-Bride\" } ]-(m2)";

    private static final String BRIDE_GROOM_HALF_SIBLING = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "NOT (m1)-[:GT_HALF_SIBLING { actors: \"Bride-Groom\" } ]->(m2) AND " +
            "m1.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "(m1.BRIDE_MOTHER_IDENTITY = m2.GROOM_MOTHER_IDENTITY OR m1.BRIDE_FATHER_IDENTITY = m2.GROOM_FATHER_IDENTITY ) AND " +
            "NOT (m1.BRIDE_MOTHER_IDENTITY = m2.GROOM_MOTHER_IDENTITY AND m1.BRIDE_FATHER_IDENTITY = m2.GROOM_FATHER_IDENTITY) " +
            "MERGE (m1)-[:GT_HALF_SIBLING { actors: \"Bride-Groom\" } ]-(m2)";

    private static final String DEATH_DEATH_SIBLING = "MATCH (d1:Death),(d2:Death) WHERE " +
            "NOT (d1)-[:GT_HALF_SIBLING { actors: \"Bride-Groom\" } ]-(d2) AND " +
            "d1.MOTHER_IDENTITY <> \"\" AND " +
            "d1.FATHER_IDENTITY <> \"\" AND " +
            "d2.MOTHER_IDENTITY <> \"\" AND " +
            "d2.FATHER_IDENTITY <> \"\" AND " +
            "d1 <> d2 AND " +
            "d1.MOTHER_IDENTITY = d2.MOTHER_IDENTITY AND " +
            "d1.FATHER_IDENTITY = d2.FATHER_IDENTITY " +
            "MERGE (d1)-[:GT_HALF_SIBLING { actors: \"Bride-Groom\" } ]-(d2)";

    private static final String DEATH_DEATH_HALF_SIBLING = "MATCH (d1:Death),(d2:Death) WHERE " +
            "NOT (d1)-[:GT_HALF_SIBLING { actors: \"Bride-Groom\" } ]-(d2) AND " +
            "d1.MOTHER_IDENTITY <> \"\" AND " +
            "d2.MOTHER_IDENTITY <> \"\" AND " +
            "d1.FATHER_IDENTITY <> \"\" AND " +
            "d2.FATHER_IDENTITY <> \"\" AND " +
            "d1 <> d2 AND " +
            "(d1.MOTHER_IDENTITY = d2.MOTHER_IDENTITY OR d1.FATHER_IDENTITY = d2.FATHER_IDENTITY) AND " +
            "NOT (d1.MOTHER_IDENTITY = d2.MOTHER_IDENTITY AND d1.FATHER_IDENTITY = d2.FATHER_IDENTITY) " +
            "MERGE (d1)-[:GT_HALF_SIBLING { actors: \"Bride-Groom\" } ]-(d2)";

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
                // Birth - Birth
                FATHER_OWN_BIRTH_IDENTITY, MOTHER_OWN_BIRTH_IDENTITY,
                BIRTH_BIRTH_SIBLING,
//                BIRTH_BIRTH_HALF_SIBLING,

                // Birth - Death
                BIRTH_DEATH_IDENTITY,
                BIRTH_DEATH_SIBLING,
//                BIRTH_DEATH_HALF_SIBLING,

                // Birth - Marriage
                BIRTH_GROOM_IDENTITY, BIRTH_BRIDE_IDENTITY,
                FATHER_GROOM_IDENTITY, MOTHER_BRIDE_IDENTITY,
                BIRTH_PARENTS_MARRIAGE_IDENTITY, DEATH_PARENTS_MARRIAGE_IDENTITY,
                BIRTH_GROOM_SIBLING, BIRTH_BRIDE_SIBLING,

                // Death - Marriage
                DEATH_GROOM_IDENTITY, DEATH_BRIDE_IDENTITY,
                DEATH_GROOM_SIBLING, DEATH_BRIDE_SIBLING,

                // Marriage - Marriage
                GROOM_GROOM_IDENTITY, BRIDE_BRIDE_IDENTITY,
                GROOM_PARENTS_MARRIAGE_IDENTITY, BRIDE_PARENTS_MARRIAGE_IDENTITY,
                GROOM_GROOM_SIBLING, BRIDE_BRIDE_SIBLING, BRIDE_GROOM_SIBLING,
 //               GROOM_GROOM_HALF_SIBLING, BRIDE_BRIDE_HALF_SIBLING, BRIDE_GROOM_HALF_SIBLING,

                // Death - Death
                DEATH_DEATH_SIBLING //,
//                DEATH_DEATH_HALF_SIBLING
        );
    }
}
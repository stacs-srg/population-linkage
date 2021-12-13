/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks;

import org.neo4j.driver.Session;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.time.LocalDateTime;

/*
 * Establishes ground truth links in Neo4J for Umea data set
 *
 * @author al
 */
public class CreateGTLinks {

    private static final String FATHER_OWN_BIRTH_IDENTITY = "MATCH (b1:Birth),(b2:Birth) WHERE " +
            "b1.CHILD_IDENTITY <> \"\" AND " +
            "b2.FATHER_IDENTITY <> \"\" AND " +
            "b1.CHILD_IDENTITY = b2.FATHER_IDENTITY " +
            "MERGE (b1)-[r:GROUND_TRUTH_BIRTH_FATHER_IDENTITY]->(b2)";

    private static final String MOTHER_OWN_BIRTH_IDENTITY = "MATCH (b1:Birth),(b2:Birth) WHERE " +
            "b1.CHILD_IDENTITY <> \"\" AND " +
            "b2.MOTHER_IDENTITY <> \"\" AND " +
            "b1.CHILD_IDENTITY = b2.MOTHER_IDENTITY " +
            "MERGE (b1)-[r:GROUND_TRUTH_BIRTH_MOTHER_IDENTITY]->(b2)";

    private static final String BIRTH_BIRTH_SIBLING = "MATCH (b1:Birth),(b2:Birth) WHERE " +
            "b1.MOTHER_IDENTITY <> \"\" AND " +
            "b1.FATHER_IDENTITY <> \"\" AND " +
            "b2.MOTHER_IDENTITY <> \"\" AND " +
            "b2.FATHER_IDENTITY <> \"\" AND " +
            "b1 <> b2 AND " +
            "b1.MOTHER_IDENTITY = b2.MOTHER_IDENTITY AND " +
            "b1.FATHER_IDENTITY = b2.FATHER_IDENTITY " +
            "MERGE (b1)-[r:GROUND_TRUTH_BIRTH_SIBLING]->(b2) ";

    private static final String BIRTH_BIRTH_HALF_SIBLING = "MATCH (b1:Birth),(b2:Birth) WHERE " +
            "b1.MOTHER_IDENTITY <> \"\" AND " +
            "b2.MOTHER_IDENTITY <> \"\" AND " +
            "b1.FATHER_IDENTITY <> \"\" AND " +
            "b2.FATHER_IDENTITY <> \"\" AND " +
            "b1 <> b2 AND " +
            "(b1.MOTHER_IDENTITY = b2.MOTHER_IDENTITY OR b1.FATHER_IDENTITY = b2.FATHER_IDENTITY) AND " +
            "NOT (b1.MOTHER_IDENTITY = b2.MOTHER_IDENTITY AND b1.FATHER_IDENTITY = b2.FATHER_IDENTITY) " +
            "MERGE (b1)-[r:GROUND_TRUTH_BIRTH_HALF_SIBLING]->(b2)";

    private static final String BIRTH_DEATH_IDENTITY = "MATCH (b:Birth),(d:Death) WHERE " + "" +
            "b.CHILD_IDENTITY <> \"\" AND " +
            "d.DECEASED_IDENTITY <> \"\" AND " +
            "b.CHILD_IDENTITY = d.DECEASED_IDENTITY " +
            "MERGE (b)-[r:GROUND_TRUTH_BIRTH_DEATH_IDENTITY]->(d)";

    private static final String BIRTH_DEATH_SIBLING = "MATCH (b:Birth),(d:Death) WHERE " +
            "b.MOTHER_IDENTITY <> \"\" AND " +
            "b.FATHER_IDENTITY <> \"\" AND " +
            "d.MOTHER_IDENTITY <> \"\" AND " +
            "d.FATHER_IDENTITY <> \"\" AND " +
            "b.MOTHER_IDENTITY = d.MOTHER_IDENTITY AND " +
            "b.FATHER_IDENTITY = d.FATHER_IDENTITY AND " +
            "b.CHILD_IDENTITY <> d.DECEASED_IDENTITY " +
            "MERGE (b)-[r:GROUND_TRUTH_BIRTH_DEATH_SIBLING]->(d)";

    private static final String BIRTH_DEATH_HALF_SIBLING = "MATCH (b:Birth),(d:Death) WHERE " +
            "b.MOTHER_IDENTITY <> \"\" AND " +
            "d.MOTHER_IDENTITY <> \"\" AND " +
            "b.FATHER_IDENTITY <> \"\" AND " +
            "d.FATHER_IDENTITY <> \"\" AND " +
            "(b.MOTHER_IDENTITY = d.MOTHER_IDENTITY OR b.FATHER_IDENTITY = d.FATHER_IDENTITY) AND " +
            "NOT (b.MOTHER_IDENTITY = d.MOTHER_IDENTITY AND b.FATHER_IDENTITY = d.FATHER_IDENTITY) " +
            "MERGE (b)-[r:GROUND_TRUTH_BIRTH_DEATH_HALF_SIBLING]->(d)";

    private static final String BIRTH_GROOM_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.CHILD_IDENTITY <> \"\" AND " +
            "m.GROOM_IDENTITY <> \"\" AND " +
            "b.CHILD_IDENTITY = m.GROOM_IDENTITY " +
            "MERGE (b)-[r:GROUND_TRUTH_BIRTH_GROOM_IDENTITY]->(m)";

    private static final String BIRTH_BRIDE_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.CHILD_IDENTITY <> \"\" AND " +
            "m.BRIDE_IDENTITY <> \"\" AND " +
            "b.CHILD_IDENTITY = m.BRIDE_IDENTITY " +
            "MERGE (b)-[r:GROUND_TRUTH_BIRTH_BRIDE_IDENTITY]->(m)";

    private static final String FATHER_GROOM_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.FATHER_IDENTITY <> \"\" AND " +
            "m.GROOM_IDENTITY <> \"\" AND " +
            "b.FATHER_IDENTITY = m.GROOM_IDENTITY " +
            "MERGE (b)-[r:GROUND_TRUTH_FATHER_GROOM_IDENTITY]->(m)";

    private static final String MOTHER_BRIDE_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.MOTHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_IDENTITY <> \"\" AND " +
            "b.MOTHER_IDENTITY = m.BRIDE_IDENTITY " +
            "MERGE (b)-[r:GROUND_TRUTH_MOTHER_BRIDE_IDENTITY]->(m)";

    private static final String BIRTH_PARENTS_MARRIAGE_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.MOTHER_IDENTITY <> \"\" AND " +
            "b.FATHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_IDENTITY <> \"\" AND " +
            "m.GROOM_IDENTITY <> \"\" AND " +
            "b.MOTHER_IDENTITY = m.BRIDE_IDENTITY AND " +
            "b.FATHER_IDENTITY = m.GROOM_IDENTITY " +
            "MERGE (b)-[r:GROUND_TRUTH_BIRTH_PARENTS_MARRIAGE]->(m)";

    private static final String DEATH_PARENTS_MARRIAGE_IDENTITY = "MATCH (d:Death),(m:Marriage) WHERE " +
            "d.MOTHER_IDENTITY <> \"\" AND " +
            "d.FATHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_IDENTITY <> \"\" AND " +
            "m.GROOM_IDENTITY <> \"\" AND " +
            "d.MOTHER_IDENTITY = m.BRIDE_IDENTITY AND " +
            "d.FATHER_IDENTITY = m.GROOM_IDENTITY " +
            "MERGE (d)-[r:GROUND_TRUTH_DEATH_PARENTS_MARRIAGE]->(m)";

    private static final String BIRTH_GROOM_SIBLING = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.MOTHER_IDENTITY <> \"\" AND " +
            "b.FATHER_IDENTITY <> \"\" AND " +
            "m.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "b.MOTHER_IDENTITY = m.GROOM_MOTHER_IDENTITY AND " +
            "b.FATHER_IDENTITY = m.GROOM_FATHER_IDENTITY AND " +
            "b.CHILD_IDENTITY <> m.GROOM_IDENTITY " +
            "MERGE (b)-[r:GROUND_TRUTH_BIRTH_GROOM_SIBLING]->(m)";

    private static final String BIRTH_BRIDE_SIBLING = "MATCH (b:Birth),(m:Marriage) WHERE " +
            "b.MOTHER_IDENTITY <> \"\" AND " +
            "b.FATHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "b.MOTHER_IDENTITY = m.BRIDE_MOTHER_IDENTITY AND " +
            "b.FATHER_IDENTITY = m.BRIDE_FATHER_IDENTITY AND " +
            "b.CHILD_IDENTITY <> m.BRIDE_IDENTITY " +
            "MERGE (b)-[r:GROUND_TRUTH_BIRTH_BRIDE_SIBLING]->(m)";

    private static final String DEATH_GROOM_IDENTITY = "MATCH (d:Death),(m:Marriage) WHERE " +
            "d.DECEASED_IDENTITY <> \"\" AND " +
            "m.GROOM_IDENTITY <> \"\" AND " +
            "d.DECEASED_IDENTITY = m.GROOM_IDENTITY " +
            "MERGE (d)-[r:GROUND_TRUTH_DEATH_GROOM_IDENTITY]->(m)";

    private static final String DEATH_BRIDE_IDENTITY = "MATCH (d:Death),(m:Marriage) WHERE " +
            "d.DECEASED_IDENTITY <> \"\" AND " +
            "m.BRIDE_IDENTITY <> \"\" AND " +
            "d.DECEASED_IDENTITY = m.BRIDE_IDENTITY " +
            "MERGE (d)-[r:GROUND_TRUTH_DEATH_BRIDE_IDENTITY]->(m)";

    private static final String DEATH_GROOM_SIBLING = "MATCH (d:Death),(m:Marriage) WHERE " +
            "d.MOTHER_IDENTITY <> \"\" AND " +
            "d.FATHER_IDENTITY <> \"\" AND " +
            "m.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "d.MOTHER_IDENTITY = m.GROOM_MOTHER_IDENTITY AND " +
            "d.FATHER_IDENTITY = m.GROOM_FATHER_IDENTITY AND " +
            "d.DECEASED_IDENTITY <> m.GROOM_IDENTITY " +
            "MERGE (d)-[r:GROUND_TRUTH_DEATH_GROOM_SIBLING]->(m)";

    private static final String DEATH_BRIDE_SIBLING = "MATCH (d:Death),(m:Marriage) WHERE " +
            "d.MOTHER_IDENTITY <> \"\" AND " +
            "d.FATHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "d.MOTHER_IDENTITY = m.BRIDE_MOTHER_IDENTITY AND " +
            "d.FATHER_IDENTITY = m.BRIDE_FATHER_IDENTITY AND " +
            "d.DECEASED_IDENTITY <> m.BRIDE_IDENTITY " +
            "MERGE (d)-[r:GROUND_TRUTH_DEATH_BRIDE_SIBLING]->(m)";

    private static final String GROOM_GROOM_IDENTITY = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "m1.GROOM_IDENTITY <> \"\" AND " +
            "m2.GROOM_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "m1.GROOM_IDENTITY = m2.GROOM_IDENTITY " +
            "MERGE (m1)-[r:GROUND_TRUTH_GROOM_GROOM_IDENTITY]->(m2)";

    private static final String BRIDE_BRIDE_IDENTITY = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "m1.BRIDE_IDENTITY <> \"\" AND " +
            "m2.BRIDE_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "m1.BRIDE_IDENTITY = m2.BRIDE_IDENTITY " +
            "MERGE (m1)-[r:GROUND_TRUTH_BRIDE_BRIDE_IDENTITY]->(m2)";

    private static final String GROOM_PARENTS_MARRIAGE_IDENTITY = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "m1.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m1.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_IDENTITY <> \"\" AND " +
            "m2.BRIDE_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "m1.GROOM_MOTHER_IDENTITY = m2.BRIDE_IDENTITY AND " +
            "m1.GROOM_FATHER_IDENTITY = m2.GROOM_IDENTITY " +
            "MERGE (m1)-[r:GROUND_TRUTH_GROOM_PARENTS_MARRIAGE]->(m2)";

    private static final String BRIDE_PARENTS_MARRIAGE_IDENTITY = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "m1.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_IDENTITY <> \"\" AND " +
            "m2.BRIDE_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "m1.BRIDE_MOTHER_IDENTITY = m2.BRIDE_IDENTITY AND " +
            "m1.BRIDE_FATHER_IDENTITY = m2.GROOM_IDENTITY " +
            "MERGE (m1)-[r:GROUND_TRUTH_BRIDE_PARENTS_MARRIAGE]->(m2)";

    private static final String GROOM_GROOM_SIBLING = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "m1.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m1.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m1.GROOM_MOTHER_IDENTITY = m2.GROOM_MOTHER_IDENTITY AND " +
            "m1.GROOM_FATHER_IDENTITY = m2.GROOM_FATHER_IDENTITY AND " +
            "m1.GROOM_IDENTITY <> m2.GROOM_IDENTITY " +
            "MERGE (m1)-[r:GROUND_TRUTH_GROOM_GROOM_SIBLING]->(m2)";

    private static final String BRIDE_BRIDE_SIBLING = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "m1.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m2.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m2.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_MOTHER_IDENTITY = m2.BRIDE_MOTHER_IDENTITY AND " +
            "m1.BRIDE_FATHER_IDENTITY = m2.BRIDE_FATHER_IDENTITY AND " +
            "m1.BRIDE_IDENTITY <> m2.BRIDE_IDENTITY " +
            "MERGE (m1)-[r:GROUND_TRUTH_BRIDE_BRIDE_SIBLING]->(m2)";

    private static final String BRIDE_GROOM_SIBLING = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "m1.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_MOTHER_IDENTITY = m2.GROOM_MOTHER_IDENTITY AND " +
            "m1.BRIDE_FATHER_IDENTITY = m2.GROOM_FATHER_IDENTITY " +
            "MERGE (m1)-[r:GROUND_TRUTH_BRIDE_GROOM_SIBLING]->(m2)";

    private static final String GROOM_GROOM_HALF_SIBLING = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "m1.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m1.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "(m1.GROOM_MOTHER_IDENTITY = m2.GROOM_MOTHER_IDENTITY OR m1.GROOM_FATHER_IDENTITY = m2.GROOM_FATHER_IDENTITY ) AND " +
            "NOT (m1.GROOM_MOTHER_IDENTITY = m2.GROOM_MOTHER_IDENTITY AND m1.GROOM_FATHER_IDENTITY = m2.GROOM_FATHER_IDENTITY ) " +
            "MERGE (m1)-[r:GROUND_TRUTH_GROOM_GROOM_HALF_SIBLING]->(m2)";

    private static final String BRIDE_BRIDE_HALF_SIBLING = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "m1.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m2.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m2.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "(m1.BRIDE_MOTHER_IDENTITY = m2.BRIDE_MOTHER_IDENTITY OR m1.BRIDE_FATHER_IDENTITY = m2.BRIDE_FATHER_IDENTITY) AND " +
            "NOT (m1.BRIDE_MOTHER_IDENTITY = m2.BRIDE_MOTHER_IDENTITY AND m1.BRIDE_FATHER_IDENTITY = m2.BRIDE_FATHER_IDENTITY) " +
            "MERGE (m1)-[r:GROUND_TRUTH_BRIDE_BRIDE_HALF_SIBLING]->(m2)";

    private static final String BRIDE_GROOM_HALF_SIBLING = "MATCH (m1:Marriage),(m2:Marriage) WHERE " +
            "m1.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
            "m1.BRIDE_FATHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_MOTHER_IDENTITY <> \"\" AND " +
            "m2.GROOM_FATHER_IDENTITY <> \"\" AND " +
            "m1 <> m2 AND " +
            "(m1.BRIDE_MOTHER_IDENTITY = m2.GROOM_MOTHER_IDENTITY OR m1.BRIDE_FATHER_IDENTITY = m2.GROOM_FATHER_IDENTITY ) AND " +
            "NOT (m1.BRIDE_MOTHER_IDENTITY = m2.GROOM_MOTHER_IDENTITY AND m1.BRIDE_FATHER_IDENTITY = m2.GROOM_FATHER_IDENTITY) " +
            "MERGE (m1)-[r:GROUND_TRUTH_BRIDE_GROOM_HALF_SIBLING]->(m2)";

    private static final String DEATH_DEATH_SIBLING = "MATCH (d1:Death),(d2:Death) WHERE " +
            "d1.MOTHER_IDENTITY <> \"\" AND " +
            "d1.FATHER_IDENTITY <> \"\" AND " +
            "d2.MOTHER_IDENTITY <> \"\" AND " +
            "d2.FATHER_IDENTITY <> \"\" AND " +
            "d1 <> d2 AND " +
            "d1.MOTHER_IDENTITY = d2.MOTHER_IDENTITY AND " +
            "d1.FATHER_IDENTITY = d2.FATHER_IDENTITY " +
            "MERGE (d1)-[r:GROUND_TRUTH_DEATH_SIBLING]->(d2)";

    private static final String DEATH_DEATH_HALF_SIBLING = "MATCH (d1:Death),(d2:Death) WHERE " +
            "d1.MOTHER_IDENTITY <> \"\" AND " +
            "d2.MOTHER_IDENTITY <> \"\" AND " +
            "d1.FATHER_IDENTITY <> \"\" AND " +
            "d2.FATHER_IDENTITY <> \"\" AND " +
            "d1 <> d2 AND " +
            "(d1.MOTHER_IDENTITY = d2.MOTHER_IDENTITY OR d1.FATHER_IDENTITY = d2.FATHER_IDENTITY) AND " +
            "NOT (d1.MOTHER_IDENTITY = d2.MOTHER_IDENTITY AND d1.FATHER_IDENTITY = d2.FATHER_IDENTITY) " +
            "MERGE (d1)-[r:GROUND_TRUTH_DEATH_HALF_SIBLING]->(d2)";

    private static void doQueries(String... queries) {

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge(); Session session = bridge.getNewSession()) {

            System.out.println("Creating GT links @ " + LocalDateTime.now());

            for (String query : queries) {
                session.run(query);
            }

            System.out.println("Complete @ " + LocalDateTime.now());
        }
    }

    public static void main(String[] args) {

        doQueries(
                // Birth - Birth
                FATHER_OWN_BIRTH_IDENTITY, MOTHER_OWN_BIRTH_IDENTITY,
                BIRTH_BIRTH_SIBLING,
                BIRTH_BIRTH_HALF_SIBLING,

                // Birth - Death
                BIRTH_DEATH_IDENTITY,
                BIRTH_DEATH_SIBLING,
                BIRTH_DEATH_HALF_SIBLING,

                // Birth - Marriage
                BIRTH_GROOM_IDENTITY, BIRTH_BRIDE_IDENTITY, FATHER_GROOM_IDENTITY, MOTHER_BRIDE_IDENTITY,
                BIRTH_PARENTS_MARRIAGE_IDENTITY, DEATH_PARENTS_MARRIAGE_IDENTITY,
                BIRTH_GROOM_SIBLING, BIRTH_BRIDE_SIBLING,

                // Death - Marriage
                DEATH_GROOM_IDENTITY, DEATH_BRIDE_IDENTITY,
                DEATH_GROOM_SIBLING, DEATH_BRIDE_SIBLING,

                // Marriage - Marriage
                GROOM_GROOM_IDENTITY, BRIDE_BRIDE_IDENTITY,
                GROOM_PARENTS_MARRIAGE_IDENTITY, BRIDE_PARENTS_MARRIAGE_IDENTITY,
                GROOM_GROOM_SIBLING, BRIDE_BRIDE_SIBLING, BRIDE_GROOM_SIBLING,
                GROOM_GROOM_HALF_SIBLING, BRIDE_BRIDE_HALF_SIBLING, BRIDE_GROOM_HALF_SIBLING,

                // Death - Death
                DEATH_DEATH_SIBLING,
                DEATH_DEATH_HALF_SIBLING
        );
    }
}
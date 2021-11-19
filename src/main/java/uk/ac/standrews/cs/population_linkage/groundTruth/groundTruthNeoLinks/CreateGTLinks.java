/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks;

import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.time.LocalDateTime;

/*
 * Establishes ground truth links in Neo4J for Umea data set
 *
 * @author al
 */

public class CreateGTLinks {
    private final NeoDbCypherBridge bridge;

    // It might make it easier to cross-check if the ordering was the same as the linkage recipe classes in the package i.e. alphabetical.

    // Should we add the alternative ground truth sources? i.e. "WHERE b.CHILD_IDENTITY = d.DECEASED_IDENTITY OR b.STANDARDISED_ID = d.BIRTH_RECORD_IDENTITY OR b.DEATH_RECORD_IDENTITY = d.STANDARDISED_ID".
    private static final String BIRTH_DEATH_IDENTITY = "MATCH (b:Birth),(d:Death) WHERE b.CHILD_IDENTITY = d.DECEASED_IDENTITY CREATE (b)-[r:GROUND_TRUTH_BIRTH_DEATH_IDENTITY]->(d)";
    // Do we need if bidirectional - think so?
    private static final String DEATH_BIRTH_IDENTITY = "MATCH (b:Birth),(d:Death) WHERE b.CHILD_IDENTITY = d.DECEASED_IDENTITY CREATE (d)-[r:GROUND_TRUTH_DEATH_BIRTH_IDENTITY]->(b)";

    private static final String BIRTH_GROOM_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE b.CHILD_IDENTITY = m.GROOM_IDENTITY CREATE (b)-[r:GROUND_TRUTH_BIRTH_GROOM_IDENTITY]->(m)";
    private static final String BIRTH_BRIDE_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE b.CHILD_IDENTITY = m.BRIDE_IDENTITY CREATE (b)-[r:GROUND_TRUTH_BIRTH_BRIDE_IDENTITY]->(m)";

    private static final String DEATH_GROOM_IDENTITY = "MATCH (d:Death),(m:Marriage) WHERE d.DECEASED_IDENTITY = m.GROOM_IDENTITY CREATE (d)-[r:GROUND_TRUTH_DEATH_GROOM_IDENTITY]->(m)";
    private static final String DEATH_BRIDE_IDENTITY = "MATCH (d:Death),(m:Marriage) WHERE d.DECEASED_IDENTITY = m.BRIDE_IDENTITY CREATE (d)-[r:GROUND_TRUTH_DEATH_BRIDE_IDENTITY]->(m)";

    private static final String BIRTH_DEATH_SIBLING = "MATCH (b:Birth),(d:Death) WHERE b.FATHER_IDENTITY = d.FATHER_IDENTITY AND b.MOTHER_IDENTITY = d.MOTHER_IDENTITY CREATE (b)-[r:GROUND_TRUTH_BIRTH_DEATH_SIBLING]->(d)";

    // Can you just write (a=b) rather than (a.STANDARDISED_ID=b.STANDARDISED_ID)? Think so! - leave comment for now in case that is wrong!
    private static final String BIRTH_BIRTH_SIBLING = "MATCH (a:Birth),(b:Birth) WHERE a.FATHER_IDENTITY = b.FATHER_IDENTITY AND a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND NOT a = b CREATE (a)-[r:GROUND_TRUTH_BIRTH_SIBLING]->(b)";
    private static final String DEATH_DEATH_SIBLING = "MATCH (a:Death),(b:Death) WHERE a.FATHER_IDENTITY = b.FATHER_IDENTITY AND a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND NOT a = b CREATE (a)-[r:GROUND_TRUTH_DEATH_SIBLING]->(b)";

//    private static final String BIRTH_PARENTS_MARRIAGE_IDENTITY = "MATCH (a:Birth),(m:Marriage) WHERE a.FATHER_IDENTITY = m.GROOM_IDENTITY AND a.MOTHER_IDENTITY = m.BRIDE_IDENTITY CREATE (a)-[r:GROUND_TRUTH_BIRTH_PARENTS_MARRIAGE_IDENTITY]->(m)";
    private static final String BIRTH_PARENTS_MARRIAGE = "MATCH (b:Birth),(m:Marriage) WHERE b.FATHER_IDENTITY = m.GROOM_IDENTITY AND b.MOTHER_IDENTITY = m.BRIDE_IDENTITY CREATE (b)-[r:GROUND_TRUTH_BIRTH_PARENTS_MARRIAGE]->(m)";

    // BIRTH_MOTHER_IDENTITY
    private static final String MOTHER_OWNBIRTH_IDENTITY = "MATCH (a:Birth),(b:Birth) WHERE a.CHILD_IDENTITY = b.MOTHER_IDENTITY CREATE (a)-[r:GROUND_TRUTH_BIRTH_MOTHER_IDENTITY]->(b)";
    // BIRTH_FATHER_IDENTITY
    private static final String FATHER_OWNBIRTH_IDENTITY = "MATCH (a:Birth),(b:Birth) WHERE a.CHILD_IDENTITY = b.FATHER_IDENTITY CREATE (a)-[r:GROUND_TRUTH_BIRTH_FATHER_IDENTITY]->(b)";

    // Fine though don't think we have recipes for these.
    private static final String FATHER_GROOM_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE b.FATHER_IDENTITY = m.GROOM_IDENTITY CREATE (b)-[r:GROUND_TRUTH_FATHER_GROOM_IDENTITY]->(m)";
    private static final String MOTHER_BRIDE_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE b.MOTHER_IDENTITY = m.BRIDE_IDENTITY CREATE (b)-[r:GROUND_TRUTH_MOTHER_BRIDE_IDENTITY]->(m)";

    private static final String BRIDE_GROOM_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE b.GROOM_MOTHER_IDENTITY = a.BRIDE_MOTHER_IDENTITY AND b.GROOM_FATHER_IDENTITY = a.BRIDE_FATHER_IDENTITY CREATE (a)-[r:GROUND_TRUTH_BRIDE_GROOM_SIBLING]->(b)";
    // Why do we need this one?
    private static final String GROOM_BRIDE_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE a.GROOM_MOTHER_IDENTITY = b.BRIDE_MOTHER_IDENTITY AND a.GROOM_FATHER_IDENTITY = b.BRIDE_FATHER_IDENTITY CREATE (a)-[r:GROUND_TRUTH_GROOM_BRIDE_SIBLING]->(b)";
    private static final String GROOM_GROOM_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE a.GROOM_MOTHER_IDENTITY = b.GROOM_MOTHER_IDENTITY AND (a.GROOM_FATHER_IDENTITY = b.GROOM_FATHER_IDENTITY)) AND NOT a = b CREATE (a)-[r:GROUND_TRUTH_GROOM_GROOM_SIBLING]->(b)";
    private static final String BRIDE_BRIDE_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE a.BRIDE_MOTHER_IDENTITY = b.BRIDE_MOTHER_IDENTITY AND (a.BRIDE_FATHER_IDENTITY = b.BRIDE_FATHER_IDENTITY)) AND NOT a = b  CREATE (a)-[r:GROUND_TRUTH_BRIDE_BRIDE_SIBLING]->(b)";

    // a.STANDARDISED_ID=b.STANDARDISED_ID vs a = b
    // Why do we worry about blank identities here and not elsewhere?
    private static final String BRIDE_GROOM_HALF_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE (b.GROOM_MOTHER_IDENTITY = a.BRIDE_MOTHER_IDENTITY AND NOT a.BRIDE_MOTHER_IDENTITY='') OR (a.BRIDE_FATHER_IDENTITY = b.GROOM_FATHER_IDENTITY AND NOT a.BRIDE_FATHER_IDENTITY='') AND NOT a = b AND NOT (b.GROOM_MOTHER_IDENTITY = a.BRIDE_MOTHER_IDENTITY AND b.GROOM_FATHER_IDENTITY = a.BRIDE_FATHER_IDENTITY) CREATE (a)-[r:GROUND_TRUTH_BRIDE_GROOM_HALF_SIBLING]->(b)";
    // Why do we need this one?
    private static final String GROOM_BRIDE_HALF_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE (a.GROOM_MOTHER_IDENTITY = b.BRIDE_MOTHER_IDENTITY AND NOT a.GROOM_MOTHER_IDENTITY='') OR (a.GROOM_FATHER_IDENTITY = b.BRIDE_FATHER_IDENTITY AND NOT a.GROOM_FATHER_IDENTITY='') AND NOT a = b AND NOT (a.GROOM_MOTHER_IDENTITY = b.BRIDE_MOTHER_IDENTITY AND a.GROOM_FATHER_IDENTITY = b.BRIDE_FATHER_IDENTITY) CREATE (a)-[r:GROUND_TRUTH_GROOM_BRIDE_HALF_SIBLING]->(b)";
    private static final String GROOM_GROOM_HALF_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE (a.GROOM_MOTHER_IDENTITY = b.GROOM_MOTHER_IDENTITY AND NOT a.GROOM_MOTHER_IDENTITY='') OR (a.GROOM_FATHER_IDENTITY = b.GROOM_FATHER_IDENTITY AND NOT a.GROOM_FATHER_IDENTITY='') AND NOT a = b AND NOT (b.GROOM_MOTHER_IDENTITY = a.GROOM_MOTHER_IDENTITY AND b.GROOM_FATHER_IDENTITY = a.GROOM_FATHER_IDENTITY) CREATE (a)-[r:GROUND_TRUTH_GROOM_GROOM_HALF_SIBLING]->(b)";
    private static final String BRIDE_BRIDE_HALF_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE (a.BRIDE_MOTHER_IDENTITY = b.BRIDE_MOTHER_IDENTITY AND NOT a.BRIDE_MOTHER_IDENTITY='') OR (a.BRIDE_FATHER_IDENTITY = b.BRIDE_FATHER_IDENTITY AND NOT a.BRIDE_FATHER_IDENTITY='') AND NOT a = b AND NOT (b.BRIDE_MOTHER_IDENTITY = a.BRIDE_MOTHER_IDENTITY AND b.BRIDE_FATHER_IDENTITY = a.BRIDE_FATHER_IDENTITY) CREATE (a)-[r:GROUND_TRUTH_BRIDE_BRIDE_HALF_SIBLING]->(b)";

    // Again not matched in recipes. ??? al??

    private static final String BIRTH_BIRTH_HALF_SIBLING =
            "MATCH (a:Birth),(b:Birth) WHERE " +
                    "a.FATHER_IDENTITY = b.FATHER_IDENTITY OR a.MOTHER_IDENTITY = b.MOTHER_IDENTITY " +
                    "AND NOT a = b " +
                    "AND NOT (a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND a.FATHER_IDENTITY = b.FATHER_IDENTITY) " +
                    "AND NOT ( a.MOTHER_IDENTITY = '' OR b.MOTHER_IDENTITY = '' OR a.FATHER_IDENTITY = '' OR b.FATHER_IDENTITY = '') " +
                    "CREATE (a)-[r:GROUND_TRUTH_BIRTH_HALF_SIBLING]->(b)";

    private static final String DEATH_DEATH_HALF_SIBLING =
            "MATCH (a:Death),(b:Death) WHERE " +
                    "a.FATHER_IDENTITY = b.FATHER_IDENTITY OR a.MOTHER_IDENTITY = b.MOTHER_IDENTITY " +
                    "AND NOT a.STANDARDISED_ID = b.STANDARDISED_ID " +
                    "AND NOT (a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND a.FATHER_IDENTITY = b.FATHER_IDENTITY) " +
                    "AND NOT ( a.MOTHER_IDENTITY = '' OR b.MOTHER_IDENTITY = '' OR a.FATHER_IDENTITY = '' OR b.FATHER_IDENTITY = '') " +
                    "CREATE (a)-[r:GROUND_TRUTH_DEATH_HALF_SIBLING]->(b)";

    private static final String BIRTH_DEATH_HALF_SIBLING =
            "MATCH (a:Birth),(b:Death) WHERE " +
                    "a.FATHER_IDENTITY = b.FATHER_IDENTITY OR a.MOTHER_IDENTITY = b.MOTHER_IDENTITY " +
                    "AND NOT a.STANDARDISED_ID = b.STANDARDISED_ID " +
                    "AND NOT (a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND a.FATHER_IDENTITY = b.FATHER_IDENTITY) " +
                    "AND NOT ( a.MOTHER_IDENTITY = '' OR b.MOTHER_IDENTITY = '' OR a.FATHER_IDENTITY = '' OR b.FATHER_IDENTITY = '') " +
                    "CREATE (a)-[r:GROUND_TRUTH_BIRTH_DEATH_HALF_SIBLING]->(b)";

    public CreateGTLinks(NeoDbCypherBridge bridge) {
        this.bridge = bridge;
    }

    /**
     * This creates all the GT neo4J links (relationships) from the information stored in the records
     */
    private void createGTLinks() {
//        timeQuery( "BirthOwnDeath identity", BIRTH_DEATH_IDENTITY,DEATH_BIRTH_IDENTITY );
//        timeQuery( "BirthOwnMarriage identity", BIRTH_GROOM_IDENTITY, BIRTH_BRIDE_IDENTITY );
//        timeQuery( "DeathOwnMarriage identity", DEATH_GROOM_IDENTITY, DEATH_BRIDE_IDENTITY );

//        timeQuery( "Birth-ParentsMarriage identity", BIRTH_PARENTS_MARRIAGE );
//        timeQuery( "FatherGroom/MotherBride identity", FATHER_GROOM_IDENTITY, MOTHER_BRIDE_IDENTITY );
//        timeQuery( "Mother/FatherOwnBirth identity", MOTHER_OWNBIRTH_IDENTITY, FATHER_OWNBIRTH_IDENTITY );

        // ok to here

        timeQuery( "Half sibling marriages", BRIDE_GROOM_HALF_SIBLING );

       // timeQuery( "Half sibling marriages", BRIDE_GROOM_HALF_SIBLING, GROOM_BRIDE_HALF_SIBLING, GROOM_GROOM_HALF_SIBLING, BRIDE_BRIDE_HALF_SIBLING );

       // GROOM_BRIDE_SIBLING, GROOM_GROOM_SIBLING, BRIDE_BRIDE_SIBLING ); // HEAP ERROR

//         timeQuery( "Sibling links",BIRTH_BIRTH_SIBLING, DEATH_DEATH_SIBLING, BIRTH_DEATH_SIBLING ); // HEAP ERROR
//         timeQuery( "Half-sibling links",BIRTH_BIRTH_HALF_SIBLING, DEATH_DEATH_HALF_SIBLING, BIRTH_DEATH_HALF_SIBLING  ); // HEAP ERROR
//
//        timeQuery( "Sibling links",BIRTH_BIRTH_SIBLING, DEATH_DEATH_SIBLING, BIRTH_DEATH_SIBLING ); // HEAP ERROR
//        timeQuery( "Half-sibling links",BIRTH_BIRTH_HALF_SIBLING, DEATH_DEATH_HALF_SIBLING, BIRTH_DEATH_HALF_SIBLING  ); // HEAP ERROR
    }

    /**
     * Performs a timed number of queries with diagnostic output
     * @param kind - what class of query is being performed
     * @param queries - the list of queries to execute
     */
    private void timeQuery( String kind, String... queries ) {
        LocalDateTime t0 = LocalDateTime.now();
        System.out.println(kind + " GT @ " + t0);
        for( String query : queries ) {
            runQuery( query );
        }
        LocalDateTime t1 = LocalDateTime.now();
        System.out.println(kind + " GT finished! @ " + t1 );
    }

    /**
     * Executes the query given as a parameter
     * @param query - the query to execute
     */
    private void runQuery(String query) {
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction()) {
            tx.run(query);
            tx.commit();
        }
    }

    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            CreateGTLinks gt_link_creator = new CreateGTLinks(bridge);
            gt_link_creator.createGTLinks();
            System.out.println("Finished creating GT links");
        } catch (Exception e) {
            System.out.println("Fatal exception during GT linkage creation");
            e.printStackTrace();
            System.exit(-1);
        } finally {
            System.exit(0);
        }
    }
}
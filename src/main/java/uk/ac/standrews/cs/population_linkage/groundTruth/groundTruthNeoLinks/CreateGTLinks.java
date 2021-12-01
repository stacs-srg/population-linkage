/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks;

import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/*
 * Establishes ground truth links in Neo4J for Umea data set
 *
 * @author al
 */

class CreateGTLinks {
    private final NeoDbCypherBridge bridge;

    private static final String CREATE_BIRTH_RECORD_IDENTITY_INDEX = "CREATE INDEX BIRTH_RECORD_IDENTITY_INDEX IF NOT EXISTS FOR (b:Birth) on (d.BIRTH_RECORD_IDENTITY)";
    private static final String CREATE_CHILD_IDENTITY_INDEX = "CREATE INDEX CHILD_IDENTITY_INDEX IF NOT EXISTS FOR (b:Birth) on (b.CHILD_IDENTITY)";
    private static final String CREATE_BIRTH_MOTHER_IDENTITY_INDEX = "CREATE INDEX BIRTH_MOTHER_IDENTITY_INDEX IF NOT EXISTS FOR (b:Birth) on (b.MOTHER_IDENTITY)";
    private static final String CREATE_BIRTH_FATHER_IDENTITY_INDEX = "CREATE INDEX BIRTH_FATHER_IDENTITY_INDEX IF NOT EXISTS FOR (b:Birth) on (b.FATHER_IDENTITY)";

    private static final String CREATE_MARRIAGE_GROOM_IDENTITY_INDEX = "CREATE INDEX GROOM_IDENTITY_INDEX IF NOT EXISTS FOR (m:Marriage) on (m.GROOM_IDENTITY)";
    private static final String CREATE_MARRIAGE_BRIDE_IDENTITY_INDEX = "CREATE INDEX BRIDE_IDENTITY_INDEX IF NOT EXISTS FOR (m:Marriage) on (m.BRIDE_IDENTITY)";
    private static final String CREATE_BRIDE_MOTHER_IDENTITY_INDEX = "CREATE INDEX BRIDE_MOTHER_IDENTITY_INDEX IF NOT EXISTS FOR (m:Marriage) on (m.BRIDE_MOTHER_IDENTITY)";
    private static final String CREATE_GROOM_MOTHER_IDENTITY_INDEX = "CREATE INDEX GROOM_MOTHER_IDENTITY_INDEX IF NOT EXISTS FOR (m:Marriage) on (m.GROOM_MOTHER_IDENTITY)";
    private static final String CREATE_BRIDE_FATHER_IDENTITY_INDEX = "CREATE INDEX BRIDE_FATHER_IDENTITY_INDEX IF NOT EXISTS FOR (d:Marriage) on (d.BRIDE_FATHER_IDENTITY)";
    private static final String CREATE_GROOM_FATHER_IDENTITY_INDEX = "CREATE INDEX GROOM_FATHER_IDENTITY_INDEX IF NOT EXISTS FOR (d:Marriage) on (d.GROOM_FATHER_IDENTITY)";

    private static final String CREATE_DECEASED_IDENTITY_INDEX = "CREATE INDEX DECEASED_IDENTITY_INDEX IF NOT EXISTS FOR (d:Death) on (d.DECEASED_IDENTITY)";
    private static final String CREATE_DEATH_FATHER_IDENTITY_INDEX = "CREATE INDEX DEATH_FATHER_IDENTITY_INDEX IF NOT EXISTS FOR (d:Death) on (d.FATHER_IDENTITY)";
    private static final String CREATE_DEATH_MOTHER_IDENTITY_INDEX = "CREATE INDEX DEATH_MOTHER_IDENTITY_INDEX IF NOT EXISTS FOR (d:Death) on (d.MOTHER_IDENTITY)";
    
    // Should we add the alternative ground truth sources?
    // i.e. "WHERE b.CHILD_IDENTITY = d.DECEASED_IDENTITY OR b.STANDARDISED_ID = d.BIRTH_RECORD_IDENTITY OR b.DEATH_RECORD_IDENTITY = d.STANDARDISED_ID"
    // NO NEED AL

    private static List<String> indices = Arrays.asList(
            CREATE_BIRTH_FATHER_IDENTITY_INDEX,
            CREATE_BIRTH_MOTHER_IDENTITY_INDEX,
            CREATE_BRIDE_MOTHER_IDENTITY_INDEX,
            CREATE_CHILD_IDENTITY_INDEX,
            CREATE_DEATH_FATHER_IDENTITY_INDEX,
            CREATE_DEATH_MOTHER_IDENTITY_INDEX,
            CREATE_GROOM_MOTHER_IDENTITY_INDEX,
            CREATE_DECEASED_IDENTITY_INDEX,
            CREATE_MARRIAGE_BRIDE_IDENTITY_INDEX,
            CREATE_MARRIAGE_GROOM_IDENTITY_INDEX,
            CREATE_BIRTH_RECORD_IDENTITY_INDEX,
            CREATE_GROOM_FATHER_IDENTITY_INDEX,
            CREATE_BRIDE_FATHER_IDENTITY_INDEX);

    private static final String BIRTH_DEATH_IDENTITY = "MATCH (b:Birth),(d:Death) WHERE " + "" +
                                                         "b.CHILD_IDENTITY <> \"\" AND " +
                                                         "d.DECEASED_IDENTITY <> \"\" AND " +
                                                         "b.CHILD_IDENTITY = d.DECEASED_IDENTITY " +
                                                         "MERGE (b)-[r:GROUND_TRUTH_BIRTH_DEATH_IDENTITY]->(d)";

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

    private static final String BIRTH_BIRTH_SIBLING = "MATCH (a:Birth),(b:Birth) WHERE " +
                                                        "a.MOTHER_IDENTITY <> \"\" AND " +
                                                        "a.FATHER_IDENTITY <> \"\" AND " +
                                                        "b.MOTHER_IDENTITY <> \"\" AND " +
                                                        "b.FATHER_IDENTITY <> \"\" AND " +
                                                        "a <> b AND " +
                                                        "a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND " +
                                                        "a.FATHER_IDENTITY = b.FATHER_IDENTITY " +
                                                        "MERGE (a)-[r:GROUND_TRUTH_BIRTH_SIBLING]->(b) ";

    private static final String DEATH_DEATH_SIBLING = "MATCH (a:Death),(b:Death) WHERE " +
                                                        "a.MOTHER_IDENTITY <> \"\" AND " +
                                                        "a.FATHER_IDENTITY <> \"\" AND " +
                                                        "b.MOTHER_IDENTITY <> \"\" AND " +
                                                        "b.FATHER_IDENTITY <> \"\" AND " +
                                                        "a <> b AND " +
                                                        "a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND " +
                                                        "a.FATHER_IDENTITY = b.FATHER_IDENTITY " +
                                                        "MERGE (a)-[r:GROUND_TRUTH_DEATH_SIBLING]->(b)";

    private static final String BIRTH_DEATH_SIBLING = "MATCH (a:Death),(b:Death) WHERE " +
                                                        "a.MOTHER_IDENTITY <> \"\" AND " +
                                                        "a.FATHER_IDENTITY <> \"\" AND " +
                                                        "b.MOTHER_IDENTITY <> \"\" AND " +
                                                        "b.FATHER_IDENTITY <> \"\" AND " +
                                                        "a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND " +
                                                        "a.FATHER_IDENTITY = b.FATHER_IDENTITY " +
                                                        "MERGE (a)-[r:GROUND_TRUTH_BIRTH_DEATH_SIBLING]->(b)";

    private static final String BIRTH_PARENTS_MARRIAGE = "MATCH (b:Birth),(m:Marriage) WHERE " +
                                                           "b.FATHER_IDENTITY <> \"\" AND " +
                                                           "b.MOTHER_IDENTITY <> \"\" AND " +
                                                           "m.GROOM_IDENTITY <> \"\" AND " +
                                                           "m.BRIDE_IDENTITY<> \"\" AND " +
                                                           "b.FATHER_IDENTITY = m.GROOM_IDENTITY AND " +
                                                           "b.MOTHER_IDENTITY = m.BRIDE_IDENTITY " +
                                                           "MERGE (b)-[r:GROUND_TRUTH_BIRTH_PARENTS_MARRIAGE]->(m)";

    private static final String MOTHER_OWNBIRTH_IDENTITY =  "MATCH (a:Birth),(b:Birth) WHERE " +
                                                              "a.CHILD_IDENTITY <> \"\" AND " +
                                                              "b.MOTHER_IDENTITY <> \"\" AND " +
                                                              "a.CHILD_IDENTITY = b.MOTHER_IDENTITY " +
                                                              "MERGE (a)-[r:GROUND_TRUTH_BIRTH_MOTHER_IDENTITY]->(b)";

    private static final String FATHER_OWNBIRTH_IDENTITY =  "MATCH (a:Birth),(b:Birth) WHERE " +
                                                               "a.CHILD_IDENTITY <> \"\" AND " +
                                                               "b.FATHER_IDENTITY <> \"\" AND " +
                                                               "a.CHILD_IDENTITY = b.FATHER_IDENTITY " +
                                                               "MERGE (a)-[r:GROUND_TRUTH_BIRTH_FATHER_IDENTITY]->(b)";

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

    private static final String BRIDE_GROOM_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE " +
                                                        "a.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
                                                        "a.BRIDE_FATHER_IDENTITY <> \"\" AND " +
                                                        "b.GROOM_MOTHER_IDENTITY <> \"\" AND " +
                                                        "b.GROOM_FATHER_IDENTITY <> \"\" AND " +
                                                        "b.GROOM_MOTHER_IDENTITY = a.BRIDE_MOTHER_IDENTITY AND " +
                                                        "b.GROOM_FATHER_IDENTITY = a.BRIDE_FATHER_IDENTITY " +
                                                        "MERGE (a)-[r:GROUND_TRUTH_BRIDE_GROOM_SIBLING]->(b)";

    private static final String GROOM_GROOM_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE " +
                                                        "a.GROOM_MOTHER_IDENTITY <> \"\" AND " +
                                                        "a.GROOM_FATHER_IDENTITY <> \"\" AND " +
                                                        "b.GROOM_MOTHER_IDENTITY <> \"\" AND " +
                                                        "b.GROOM_FATHER_IDENTITY <> \"\" AND " +
                                                        "a.GROOM_MOTHER_IDENTITY = b.GROOM_MOTHER_IDENTITY AND " +
                                                        "a.GROOM_FATHER_IDENTITY = b.GROOM_FATHER_IDENTITY AND " +
                                                        "a <> b " +
                                                        "MERGE (a)-[r:GROUND_TRUTH_GROOM_GROOM_SIBLING]->(b)";

    private static final String BIRTH_BRIDE_SIBLING = "MATCH (b:Birth),(m:Marriage) WHERE " +
                                                        "b.MOTHER_IDENTITY <> \"\" AND " +
                                                        "b.FATHER_IDENTITY <> \"\" AND " +
                                                        "m.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
                                                        "m.BRIDE_FATHER_IDENTITY <> \"\" AND " +
                                                        "b.MOTHER_IDENTITY = m.BRIDE_MOTHER_IDENTITY AND " +
                                                        "b.FATHER_IDENTITY = m.BRIDE_FATHER_IDENTITY " +
                                                        "MERGE (a)-[r:GROUND_TRUTH_BIRTH_BRIDE_SIBLING]->(b)";

    private static final String BIRTH_GROOM_SIBLING = "MATCH (b:Birth),(m:Marriage) WHERE " +
                                                        "b.MOTHER_IDENTITY <> \"\" AND " +
                                                        "b.FATHER_IDENTITY <> \"\" AND " +
                                                        "m.GROOM_MOTHER_IDENTITY <> \"\" AND " +
                                                        "m.GROOM_FATHER_IDENTITY <> \"\" AND " +
                                                        "b.MOTHER_IDENTITY = m.GROOM_MOTHER_IDENTITY AND " +
                                                        "b.FATHER_IDENTITY = m.GROOM_FATHER_IDENTITY " +
                                                        "MERGE (a)-[r:GROUND_TRUTH_BIRTH_GROOM_SIBLING]->(b)";

    private static final String BIRTH_PARENTS_MARRIAGE_IDENTITY = "MATCH (b:Birth),(m:Marriage) WHERE " +
                                                                  "b.MOTHER_IDENTITY <> \"\" AND " +
                                                                  "b.FATHER_IDENTITY <> \"\" AND " +
                                                                  "m.BRIDE_IDENTITY <> \"\" AND " +
                                                                  "m.GROOM_IDENTITY <> \"\" AND " +
                                                                  "b.MOTHER_IDENTITY = m.BRIDE_IDENTITY AND " +
                                                                  "b.FATHER_IDENTITY = m.GROOM_IDENTITY" +
                                                                  "MERGE (a)-[r:GROUND_TRUTH_BIRTH_PARENTS_MARRIAGE]->(b)";

    private static final String DEATH_PARENTS_MARRIAGE_IDENTITY = "MATCH (d:Death),(m:Marriage) WHERE " +
                                                                   "d.MOTHER_IDENTITY <> \"\" AND " +
                                                                   "d.FATHER_IDENTITY <> \"\" AND " +
                                                                   "m.BRIDE_IDENTITY <> \"\" AND " +
                                                                   "m.GROOM_IDENTITY <> \"\" AND " +
                                                                   "d.MOTHER_IDENTITY = m.BRIDE_IDENTITY AND " +
                                                                   "d.FATHER_IDENTITY = m.GROOM_IDENTITY" +
                                                                   "MERGE (a)-[r:GROUND_TRUTH_DEATH_PARENTS_MARRIAGE]->(b)";

    private static final String GROOM_GROOM_IDENTITY = "MATCH (a:Marriage),(b:Marriage) WHERE " +
                                                       "a.GROOM_IDENTITY <> \"\" AND " +
                                                       "b.GROOM_IDENTITY <> \"\" AND " +
                                                       "a <> b AND " +
                                                       "a.GROOM_IDENTITY = b.GROOM_IDENTITY " +
                                                       "MERGE (a)-[r:GROUND_TRUTH_GROOM_GROOM_IDENTITY]->(b)";

    private static final String BRIDE_BRIDE_IDENTITY = "MATCH (a:Marriage),(b:Marriage) WHERE " +
                                                       "a.BRIDE_IDENTITY <> \"\" AND " +
                                                       "b.BRIDE_IDENTITY <> \"\" AND " +
                                                       "a <> b AND " +
                                                       "a.BRIDE_IDENTITY = b.BRIDE_IDENTITY " +
                                                       "MERGE (a)-[r:GROUND_TRUTH_BRIDE_BRIDE_IDENTITY]->(b)";

    private static final String GROOM_PARENTS_MARRIAGE = "MATCH (a:Marriage),(b:Marriage) WHERE " +
                                                         "a.GROOM_FATHER_IDENTITY <> \"\" AND " +
                                                         "a.GROOM_MOTHER_IDENTITY <> \"\" AND " +
                                                         "b.GROOM_IDENTITY <> \"\" AND " +
                                                         "b.BRIDE_IDENTITY <> \"\" AND " +
                                                         "a <> b AND " +
                                                         "a.GROOM_MOTHER_IDENTITY = b.BRIDE_IDENTITY AND " +
                                                         "a.GROOM_FATHER_IDENTITY = b.GROOM_IDENTITY " +
                                                         "MERGE (a)-[r:GROUND_TRUTH_GROOM_PARENTS_MARRIAGE]->(b)";

    private static final String BRIDE_PARENTS_MARRIAGE = "MATCH (a:Marriage),(b:Marriage) WHERE " +
                                                         "a.BRIDE_FATHER_IDENTITY <> \"\" AND " +
                                                         "a.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
                                                         "b.GROOM_IDENTITY <> \"\" AND " +
                                                         "b.BRIDE_IDENTITY <> \"\" AND " +
                                                         "a <> b AND " +
                                                         "a.BRIDE_MOTHER_IDENTITY = b.BRIDE_IDENTITY AND " +
                                                         "a.BRIDE_FATHER_IDENTITY = b.GROOM_IDENTITY " +
                                                         "MERGE (a)-[r:GROUND_TRUTH_BRIDE_PARENTS_MARRIAGE]->(b)";

    private static final String DEATH_BRIDE_SIBLING = "MATCH (d:Death),(m:Marriage) WHERE " +
                                                      "d.MOTHER_IDENTITY <> \"\" AND " +
                                                      "d.FATHER_IDENTITY <> \"\" AND " +
                                                      "m.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
                                                      "m.BRIDE_FATHER_IDENTITY <> \"\" AND " +
                                                      "d.MOTHER_IDENTITY = m.BRIDE_MOTHER_IDENTITY AND " +
                                                      "d.FATHER_IDENTITY = m.BRIDE_FATHER_IDENTITY " +
                                                      "MERGE (d)-[r:GROUND_TRUTH_DEATH_BRIDE_SIBLING]->(m)";

    private static final String DEATH_GROOM_SIBLING = "MATCH (d:Death),(m:Marriage) WHERE " +
                                                      "d.MOTHER_IDENTITY <> \"\" AND " +
                                                      "d.FATHER_IDENTITY <> \"\" AND " +
                                                      "m.GROOM_MOTHER_IDENTITY <> \"\" AND " +
                                                      "m.GROOM_FATHER_IDENTITY <> \"\" AND " +
                                                      "d.MOTHER_IDENTITY = m.GROOM_MOTHER_IDENTITY AND " +
                                                      "d.FATHER_IDENTITY = m.GROOM_FATHER_IDENTITY " +
                                                      "MERGE (d)-[r:GROUND_TRUTH_DEATH_GROOM_SIBLING]->(m)";
            
    private static final String BRIDE_BRIDE_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE " +
                                                        "a.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
                                                        "a.BRIDE_FATHER_IDENTITY <> \"\" AND " +
                                                        "b.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
                                                        "b.BRIDE_FATHER_IDENTITY <> \"\" AND " +
                                                        "a.BRIDE_MOTHER_IDENTITY = b.BRIDE_MOTHER_IDENTITY AND " +
                                                        "a.BRIDE_FATHER_IDENTITY = b.BRIDE_FATHER_IDENTITY AND " +
                                                        "a <> b  " +
                                                        "MERGE (a)-[r:GROUND_TRUTH_BRIDE_BRIDE_SIBLING]->(b)";

    private static final String BRIDE_GROOM_HALF_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE " +
                                                        "a.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
                                                        "a.BRIDE_FATHER_IDENTITY <> \"\" AND " +
                                                        "b.GROOM_MOTHER_IDENTITY <> \"\" AND " +
                                                        "b.GROOM_FATHER_IDENTITY <> \"\" AND " +
                                                        "a <> b AND " +
                                                        "(a.BRIDE_MOTHER_IDENTITY = b.GROOM_MOTHER_IDENTITY OR a.BRIDE_FATHER_IDENTITY = b.GROOM_FATHER_IDENTITY ) AND " +
                                                        "NOT (a.BRIDE_MOTHER_IDENTITY = b.GROOM_MOTHER_IDENTITY AND a.BRIDE_FATHER_IDENTITY = b.GROOM_FATHER_IDENTITY) " +
                                                        "MERGE (a)-[r:GROUND_TRUTH_BRIDE_GROOM_HALF_SIBLING]->(b)";

    private static final String GROOM_GROOM_HALF_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE " +
                                                        "a.GROOM_MOTHER_IDENTITY <> \"\" AND " +
                                                        "a.GROOM_FATHER_IDENTITY <> \"\" AND " +
                                                        "b.GROOM_MOTHER_IDENTITY <> \"\" AND " +
                                                        "b.GROOM_FATHER_IDENTITY <> \"\" AND " +
                                                        "a <> b AND " +
                                                        "(a.GROOM_MOTHER_IDENTITY = b.GROOM_MOTHER_IDENTITY OR a.GROOM_FATHER_IDENTITY = b.GROOM_FATHER_IDENTITY ) AND " +
                                                        "NOT (a.GROOM_MOTHER_IDENTITY = b.GROOM_MOTHER_IDENTITY AND a.GROOM_FATHER_IDENTITY = b.GROOM_FATHER_IDENTITY ) " +
                                                        "MERGE (a)-[r:GROUND_TRUTH_GROOM_GROOM_HALF_SIBLING]->(b)";

    private static final String BRIDE_BRIDE_HALF_SIBLING = "MATCH (a:Marriage),(b:Marriage) WHERE " +
                                                             "a.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
                                                             "a.BRIDE_FATHER_IDENTITY <> \"\" AND " +
                                                             "b.BRIDE_MOTHER_IDENTITY <> \"\" AND " +
                                                             "b.BRIDE_FATHER_IDENTITY <> \"\" AND " +
                                                             "a <> b AND " +
                                                             "(a.BRIDE_MOTHER_IDENTITY = b.BRIDE_MOTHER_IDENTITY OR a.BRIDE_FATHER_IDENTITY = b.BRIDE_FATHER_IDENTITY) AND " +
                                                             "NOT (a.BRIDE_MOTHER_IDENTITY = b.BRIDE_MOTHER_IDENTITY AND a.BRIDE_FATHER_IDENTITY = b.BRIDE_FATHER_IDENTITY) " +
                                                             "MERGE (a)-[r:GROUND_TRUTH_BRIDE_BRIDE_HALF_SIBLING]->(b)";

    private static final String BIRTH_BIRTH_HALF_SIBLING = "MATCH (a:Birth),(b:Birth) WHERE " +
                                                             "a.MOTHER_IDENTITY <> \"\" AND " +
                                                             "b.MOTHER_IDENTITY <> \"\" AND " +
                                                             "a.FATHER_IDENTITY <> \"\" AND " +
                                                             "b.FATHER_IDENTITY <> \"\" AND " +
                                                             "a <> b AND " +
                                                             "(a.MOTHER_IDENTITY = b.MOTHER_IDENTITY OR a.FATHER_IDENTITY = b.FATHER_IDENTITY) AND " +
                                                             "NOT ( a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND a.FATHER_IDENTITY = b.FATHER_IDENTITY ) " +
                                                             "MERGE (a)-[r:GROUND_TRUTH_BIRTH_HALF_SIBLING]->(b)";

    private static final String DEATH_DEATH_HALF_SIBLING = "MATCH (a:Death),(b:Death) WHERE " +
                                                             "a.MOTHER_IDENTITY <> \"\" AND " +
                                                             "b.MOTHER_IDENTITY <> \"\" AND " +
                                                             "a.FATHER_IDENTITY <> \"\" AND " +
                                                             "b.FATHER_IDENTITY <> \"\" AND " +
                                                             "a <> b AND " +
                                                             "(a.MOTHER_IDENTITY = b.MOTHER_IDENTITY OR a.FATHER_IDENTITY = b.FATHER_IDENTITY) AND " +
                                                             "NOT (a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND a.FATHER_IDENTITY = b.FATHER_IDENTITY) " +
                                                             "MERGE (a)-[r:GROUND_TRUTH_DEATH_HALF_SIBLING]->(b)";

    private static final String BIRTH_DEATH_HALF_SIBLING = "MATCH (a:Birth),(b:Death) WHERE " +
                                                             "a.MOTHER_IDENTITY <> \"\" AND " +
                                                             "b.MOTHER_IDENTITY <> \"\" AND " +
                                                             "a.FATHER_IDENTITY <> \"\" AND " +
                                                             "b.FATHER_IDENTITY <> \"\" AND " +
                                                             "(a.MOTHER_IDENTITY = b.MOTHER_IDENTITY OR a.FATHER_IDENTITY = b.FATHER_IDENTITY) AND " +
                                                             "NOT (a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND a.FATHER_IDENTITY = b.FATHER_IDENTITY) " +
                                                             "MERGE (a)-[r:GROUND_TRUTH_BIRTH_DEATH_HALF_SIBLING]->(b)";

    public CreateGTLinks(NeoDbCypherBridge bridge) {
        this.bridge = bridge;
    }

    private void establishIndices() {
        System.out.println( "Establishing indices for GT" );
        for( String query : indices ) {
            runQuery( query );
        }
        System.out.println( "GT indices established" );
    }

    /**
     * This creates all the GT neo4J links (relationships) from the information stored in the records
     */
    private void createGTLinks() {
        timeQuery( "BirthOwnDeath identity", BIRTH_DEATH_IDENTITY );
        timeQuery( "BirthOwnMarriage identity", BIRTH_GROOM_IDENTITY, BIRTH_BRIDE_IDENTITY );
        timeQuery( "DeathOwnMarriage identity", DEATH_GROOM_IDENTITY, DEATH_BRIDE_IDENTITY );

        timeQuery( "Birth-ParentsMarriage identity", BIRTH_PARENTS_MARRIAGE );
        timeQuery( "FatherGroom/MotherBride identity", FATHER_GROOM_IDENTITY, MOTHER_BRIDE_IDENTITY );
        timeQuery( "Mother/FatherOwnBirth identity", MOTHER_OWNBIRTH_IDENTITY, FATHER_OWNBIRTH_IDENTITY );

        timeQuery( "Sibling links",BIRTH_BIRTH_SIBLING, DEATH_DEATH_SIBLING, BIRTH_DEATH_SIBLING );
        timeQuery( "Full sibling marriages", BRIDE_GROOM_SIBLING, GROOM_GROOM_SIBLING, BRIDE_BRIDE_SIBLING );

        timeQuery( "Half-sibling links",BIRTH_BIRTH_HALF_SIBLING, DEATH_DEATH_HALF_SIBLING, BIRTH_DEATH_HALF_SIBLING  );
        timeQuery( "Half sibling marriages", BRIDE_GROOM_HALF_SIBLING, GROOM_GROOM_HALF_SIBLING, BRIDE_BRIDE_HALF_SIBLING );

        timeQuery( "Birth/marriage Sibling", BIRTH_BRIDE_SIBLING, BIRTH_GROOM_SIBLING );
        timeQuery( "Birth/Death Parents Marriage Identity", BIRTH_PARENTS_MARRIAGE_IDENTITY, DEATH_PARENTS_MARRIAGE_IDENTITY );
        timeQuery( "Marriage Marriage Identity", GROOM_GROOM_IDENTITY, BRIDE_BRIDE_IDENTITY );
        timeQuery( "Marriage Parents' Marriage", GROOM_PARENTS_MARRIAGE, BRIDE_PARENTS_MARRIAGE );
        timeQuery( "Birth/Death Sibling Marriage", DEATH_BRIDE_SIBLING, DEATH_GROOM_SIBLING );
    }

    /**
     * Performs a timed number of queries with diagnostic output
     * @param kind - what class of query is being performed
     * @param queries - the list of queries to execute
     */
    protected void timeQuery( String kind, String... queries ) {
        LocalDateTime t0 = LocalDateTime.now();
        System.out.println(kind + " GT @ " + t0);
        for( String query : queries ) {
            runQuery( query );
        }
        LocalDateTime t1 = LocalDateTime.now();
        System.out.println(kind + " GT finished @ " + t1 );
    }

    /**
     * Executes the query given as a parameter
     * @param query - the query to execute
     */
    protected void runQuery(String query) {
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction()) {
            tx.run(query);
            tx.commit();
        }
    }

    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            CreateGTLinks creator = new CreateGTLinks(bridge);
            System.out.println("Creating GT links @ " + LocalDateTime.now() );
            creator.establishIndices();
            creator.createGTLinks();
            System.out.println("Completed creating GT links @ " + LocalDateTime.now() );
        } catch (Exception e) {
            System.out.println("Fatal exception during linkage creation");
            e.printStackTrace();
            System.exit(-1);
        } finally {
            System.exit(0); // all good
        }
    }
}
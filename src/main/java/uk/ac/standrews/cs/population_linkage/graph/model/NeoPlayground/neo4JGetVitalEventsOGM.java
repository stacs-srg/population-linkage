/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.model.NeoPlayground;


import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;
import uk.ac.standrews.cs.population_linkage.graph.model.BirthRecord;
import uk.ac.standrews.cs.population_linkage.graph.model.MarriageRecord;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbOGMBridge;

import java.util.Collections;
import java.util.Map;

/**
 * The GraphDatabaseService comes from the internal Java API of Neo4j.
 * So to obtain it, you should be on the database side (not on the client/driver side).
 *
 *  This is a small example program to show how OGM queries work.
 */
public class neo4JGetVitalEventsOGM {

    private final NeoDbOGMBridge bridge;
    private final Session session;

    public neo4JGetVitalEventsOGM() {
        bridge = new NeoDbOGMBridge();
        session = bridge.getNewSession();
    }

    private static final String QUERY0 = "MATCH (a:BirthRecord), (b:MarriageRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:MOTHER { delete: \"xxx\" } ]->(b)";
    private static final String QUERY1 = "MATCH (a:MarriageRecord) WHERE a.STANDARDISED_ID = \"1237137\" RETURN a";
    private static final String QUERY2 = "MATCH (a:BirthRecord) WHERE a.STANDARDISED_ID = \"1262922\" RETURN a";
    private static final String QUERY3 = "MATCH (a:BirthRecord), (b:MarriageRecord) WHERE a.STANDARDISED_ID = \"1262922\" AND b.STANDARDISED_ID = \"1237137\" CREATE (a)-[r:MOTHER { delete: \"xxx\" } ]->(b)";


    private MarriageRecord query1() {
        try (Transaction tx = session.beginTransaction();) {
            Result result = session.query(QUERY1, Collections.<String, Object>emptyMap());
            Iterable<Map<String, Object>> results = result.queryResults();
            for (Map<String, Object> map : results) {
                return (MarriageRecord) map.get("a"); // only 1
            }
        }
        return null;
    }

    private BirthRecord query2() {
        try (Transaction tx = session.beginTransaction();) {
            Result result = session.query(QUERY2, Collections.<String, Object>emptyMap() );
            Iterable<Map<String, Object>> results = result.queryResults();
            for( Map<String, Object> map : results ) {
                return (BirthRecord) map.get("a"); // only 1
            }
        }
        return null;
    }

    private BirthRecord query3() {
        try (Transaction tx = session.beginTransaction();) {
            Result result = session.query(QUERY3, Collections.<String, Object>emptyMap() );
            Iterable<Map<String, Object>> results = result.queryResults();
            for( Map<String, Object> map : results ) {
                return (BirthRecord) map.get("a"); // only 1
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        neo4JGetVitalEventsOGM db = new neo4JGetVitalEventsOGM();
        MarriageRecord m = db.query1();
        System.out.println( "Found marriage - " + m.BRIDE_FORENAME + " " + m.BRIDE_SURNAME );
        BirthRecord b = db.query2();
        System.out.println( "Found birth - " + b.FORENAME + " " + b.SURNAME );
        b = db.query3();
        System.out.println( "Found birth - " + b.FORENAME + " " + b.SURNAME );

    }
}

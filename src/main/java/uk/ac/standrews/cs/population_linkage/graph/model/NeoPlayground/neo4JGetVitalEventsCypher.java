/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.model.NeoPlayground;

import org.neo4j.driver.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The GraphDatabaseService comes from the internal Java API of Neo4j.
 * So to obtain it, you should be on the database side (not on the client/driver side).
 *
 *  Drivers only speak Cypher (not Java).
 *
 *  This is a small example program to show how Cypher queries work.
 */
public class neo4JGetVitalEventsCypher {

    private final Driver driver;
    private final org.neo4j.driver.Session session;

    public neo4JGetVitalEventsCypher() {
        driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "password" ) );
        session = driver.session();
    }

    private static final String QUERY1 = "MATCH (a:MarriageRecord) WHERE a.STANDARDISED_ID = \"1237137\" RETURN a";
    private static final String QUERY2 = "MATCH (a:BirthRecord) WHERE a.STANDARDISED_ID = \"1262922\" RETURN a";
    private static final String QUERY3 = "MATCH (a:BirthRecord), (b:MarriageRecord) WHERE a.STANDARDISED_ID = \"1262922\" AND b.STANDARDISED_ID = \"1237137\" CREATE (a)-[r:MOTHER { delete: \"xxx\" } ]->(b)";
    private static final String QUERY4 = "MATCH (a:BirthRecord), (b:MarriageRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:FATHER { delete2: \"xxx\" } ]->(b)";

    private List<String> query1() {
            Result result = session.run(QUERY1);
            return result.list(r -> r.get("a").asNode().get("BRIDE_FORENAME").asString());
    }

    private List<String> query2() {
        Result result = session.run(QUERY2);
        return result.list(r -> r.get("a").asNode().get("BRIDE_FORENAME").asString());
    }

    private String query3() {
        try( Transaction tx = session.beginTransaction(); ) {
            Result result = tx.run(QUERY3);
            System.out.println( "Results:");
            result.list().forEach(System.out::println);
            tx.commit();
        }
        return "OK";
    }

    private String query4() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", "1262922" );
        parameters.put("standard_id_to", "1237137" );
        try( Transaction tx = session.beginTransaction(); ) {
            Result result = tx.run(QUERY4,parameters);
            System.out.println( "Results:");
            result.list().forEach(System.out::println);
            tx.commit();
        }
        return "OK";
    }

    public static void main(String[] args) throws Exception {
        neo4JGetVitalEventsCypher db = new neo4JGetVitalEventsCypher();
        List<String> m = db.query1();
        System.out.println( "Marriages:" );
        m.stream().forEach(System.out::println);
        List<String> b = db.query2();
        System.out.println( "Births:" );
        b.stream().forEach(System.out::println);
        System.out.println( "Update:" + db.query3() );
        System.out.println( "Update2:" + db.query4() );
    }
}

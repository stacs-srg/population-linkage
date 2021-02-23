/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graphDbPlay;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.util.List;

/**
 * The GraphDatabaseService comes from the internal Java API of Neo4j.
 * So to obtain it, you should be on the database side (not on the client/driver side).
 *
 *  Drivers only speak Cypher (not Java).
 */
public class neo4JGetThings implements AutoCloseable {

    private final Driver driver;

    public neo4JGetThings(String uri, String user, String password ) {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

 //   @GetMapping(path = "/movies", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getNames() {

        try (Session session = driver.session()) {
            return session.run("MATCH (t:Thing) RETURN t")
                    .list(r -> r.get("t").asNode().get("name").asString());
        }
    }

    public void close() throws Exception {
        driver.close();
    }

    public static void main( String... args ) throws Exception
    {
        try( neo4JGetThings db = new neo4JGetThings( "bolt://localhost:7687", "neo4j", "password" ) ) {
            List<String> names = db.getNames();
            System.out.println( "name1: " + names.get(0) );
            System.out.println( "name2: " +  names.get(1) );
        }
    }


}

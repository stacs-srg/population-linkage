/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */

package uk.ac.standrews.cs.population_linkage.graphDbPlay;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import static org.neo4j.driver.Values.parameters;

/**
 * The GraphDatabaseService comes from the internal Java API of Neo4j.
 * So to obtain it, you should be on the database side (not on the client/driver side).
 *
 *  Drivers only speak Cypher (not Java).
 */
public class Neo4JCreateThings implements AutoCloseable
{
    private final Driver driver;

    public Neo4JCreateThings(String uri, String user, String password )
    {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    @Override
    public void close() throws Exception
    {
        driver.close();
    }

    public void add( final String name )
    {
        try ( Session session = driver.session() )
        {
            session.writeTransaction(tx -> tx.run("MERGE (a:Thing {name: $x})", parameters("x", name)));
        }
    }

    public static void main( String... args ) throws Exception
    {
        try ( Neo4JCreateThings db = new Neo4JCreateThings( "bolt://localhost:7687", "neo4j", "password" ) )
        {
            db.add( "al" );
            db.add( "graham" );
            db.add( "bob" );
            System.out.println( "Added 3 Things");
        }
    }
}

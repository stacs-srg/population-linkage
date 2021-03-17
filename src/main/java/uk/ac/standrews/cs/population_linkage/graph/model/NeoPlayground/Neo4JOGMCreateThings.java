/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */

package uk.ac.standrews.cs.population_linkage.graph.model.NeoPlayground;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 * use the Object-graph binding from here:
 *
 *     <dependency>
 *     <groupId>org.neo4j</groupId>
 *     <artifactId>neo4j-ogm-core</artifactId>
 *     <version>3.1.2</version>
 *
 *  Examples taken from // see https://github.com/neo4j/neo4j-ogm
 *  and here: https://neo4j.com/developer/neo4j-ogm/
 *
 *  Not so useful: https://github.com/neo4j-examples?q=movies
 *
 */
public class Neo4JOGMCreateThings implements AutoCloseable
{

    private final Session session;
    private SessionFactory sessionFactory;

    public Neo4JOGMCreateThings(String uri, String user, String password )
    {



        Configuration conf = new Configuration.Builder()
                                               .uri(uri)
                                               .credentials(user, password)
                                               .build();
        SessionFactory sessionFactory = new SessionFactory(conf, "uk.ac.standrews.cs.population_linkage.graphDbPlay");
        session = sessionFactory.openSession();
    }

    @Override
    public void close() throws Exception
    {
        sessionFactory.close();
    }

    public void make() {
        Thing albert = new Thing("Albert" );
        session.save(albert);
    }


    public static void main( String... args ) throws Exception
    {
        try ( Neo4JOGMCreateThings db = new Neo4JOGMCreateThings( "bolt://localhost:7687", "neo4j", "password" ) )
        {
            db.make();
            System.out.println( "PersonWithChums created using OGM" );
        }
    }
}


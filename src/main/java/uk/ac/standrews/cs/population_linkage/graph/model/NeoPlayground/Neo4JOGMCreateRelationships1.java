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
 * Book? https://neo4j.com/docs/ogm-manual/current/
 *
 */
public class Neo4JOGMCreateRelationships1 implements AutoCloseable
{

    private final Session session;
    private SessionFactory sessionFactory;

    public Neo4JOGMCreateRelationships1(String uri, String user, String password )
    {
        Configuration conf = new Configuration.Builder()
                                               .uri(uri)
                                               .credentials(user, password)
                                               .build();
        sessionFactory = new SessionFactory(conf, "uk.ac.standrews.cs.population_linkage.graphDbPlay");
        session = sessionFactory.openSession();
    }

    @Override
    public void close() throws Exception
    {
        sessionFactory.close();
    }

    public void make() {
        // Transaction trans = session.beginTransaction();

        Thing albert = new Thing("Albert" );
        Thing victoria = new Thing("Victoria" );

        victoria.addFriend( albert );

        session.save(albert);
        session.save( victoria );
        // trans.commit();
        // trans.close();
    }


    public static void main( String... args ) throws Exception
    {
        try ( Neo4JOGMCreateRelationships1 db = new Neo4JOGMCreateRelationships1( "bolt://localhost:7687", "neo4j", "password" ) )
        {
            db.make();
            System.out.println( "V&A created using OGM, A friend of V" );
        }
    }
}


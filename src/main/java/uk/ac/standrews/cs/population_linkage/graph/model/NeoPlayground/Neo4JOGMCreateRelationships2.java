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
public class Neo4JOGMCreateRelationships2 implements AutoCloseable
{

    private final Session session;
    private SessionFactory sessionFactory;

    public Neo4JOGMCreateRelationships2(String uri, String user, String password )
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
        PersonWithChums albert = new PersonWithChums("Albert" );
        PersonWithChums victoria = new PersonWithChums("Victoria" );
        PersonWithChums lizzy = new PersonWithChums("Lizzy" );
        PersonWithChums william = new PersonWithChums("william" );

        victoria.addChum( albert );
        victoria.addChum( william );


        // Save all

        session.save( victoria ); // also saves William and Albert which are in closure.
        session.save( lizzy ); // not in closure of victoria
    }


    public static void main( String... args ) throws Exception
    {
        try ( Neo4JOGMCreateRelationships2 db = new Neo4JOGMCreateRelationships2( "bolt://localhost:7687", "neo4j", "password" ) )
        {
            db.make();
            System.out.println( "Created some relationships" );
        }
    }
}


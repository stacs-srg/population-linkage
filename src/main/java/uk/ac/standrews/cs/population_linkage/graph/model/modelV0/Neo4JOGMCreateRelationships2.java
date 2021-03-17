/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */package uk.ac.standrews.cs.population_linkage.graph.model.modelV0;

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import uk.ac.standrews.cs.population_linkage.graph.model.modelV1.*;

/**
 * Second experiment in recoerding linkage results.
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
        sessionFactory = new SessionFactory(conf,
                "uk.ac.standrews.cs.population_linkage.graph.model.modelV0",
                "uk.ac.standrews.cs.population_linkage.graph.model");
        session = sessionFactory.openSession();
    }

    @Override
    public void close() throws Exception
    {
        sessionFactory.close();
    }

    public void make() {

        // These would all be loaded during the linkage process - the raw records.

        DeathVitalEventRecord fred_death_record = new DeathVitalEventRecord( "Alfred","Dearle" );

        BirthVitalEventRecord al_birth_record = new BirthVitalEventRecord( "Al","Dearle" );
        BirthVitalEventRecord sheila_death_record = new BirthVitalEventRecord( "Sheila","Stewart" );
        BirthVitalEventRecord fiona_birth_record = new BirthVitalEventRecord("Fiona","Dearle"  );
        BirthVitalEventRecord graham_birth_record = new BirthVitalEventRecord("Graham","Dearle"  );
        BirthVitalEventRecord alice_birth_record = new BirthVitalEventRecord("Alice","Dearle"  );

        MarriageVitalEventRecord fred_sheila_marriage = new MarriageVitalEventRecord("Fred Sheila"  );
        MarriageVitalEventRecord al_fiona_marriage = new MarriageVitalEventRecord("Al Fiona");

        // Derived as part of linkage process

        Person fred = new Person("Alfred", "Dearle");
        Person sheila = new Person("Sheila", "Stewart");
        Person al = new Person("Al", "Dearle");
        Person fiona = new Person("Fiona", "Dearle");
        Person graham = new Person("Graham", "Dearle");
        Person alice = new Person("Alice", "Dearle");

        Evidence fred_sheila_evidence = new Evidence(fred_sheila_marriage, 0.9, "some prov 1", 0.8);
        fred.addSpouse(sheila, "GROOM", fred_sheila_evidence);

        Evidence al_br_evidence = new Evidence(al_birth_record, 0.9, "some prov 2", 0.8);
        fred.addChild(al, "ROLE_FATHER", al_br_evidence);

        sheila.addChild(al, "ROLE_MOTHER", al_br_evidence);

        Evidence graham_br_evidence = new Evidence(graham_birth_record, 0.9, "some prov 3", 0.8);
        al.addChild( graham,"ROLE_FATHER", graham_br_evidence );
        fiona.addChild( graham,"ROLE_MOTHER", graham_br_evidence );

        Evidence alice_br_evidence = new Evidence(alice_birth_record, 0.9, "some prov 4", 0.8);
        al.addChild( alice,"ROLE_FATHER", alice_br_evidence );
        fiona.addChild( alice,"ROLE_MOTHER", alice_br_evidence );

        Evidence al_fiona_mr_Evidence = new Evidence(al_fiona_marriage, 0.9, "some prov 5", 0.8);
        al.addSpouse( fiona,"GROOM",al_fiona_mr_Evidence );

        Evidence fred_death_Evidence = new Evidence(fred_death_record, 0.9, "some prov 6", 0.8);
        fred.addDeathRecord( fred_death_Evidence );

        al.addBirthRecord( al_br_evidence );
        // Save all

        session.save( fred );
        session.save( sheila );
        session.save( al );
        session.save( fiona );
        session.save( graham );
        session.save( alice );
    }


    public static void main( String... args ) throws Exception
    {
        try ( Neo4JOGMCreateRelationships2 db = new Neo4JOGMCreateRelationships2( "bolt://localhost:7687", "neo4j", "password" ) )
        {
            db.make();
            System.out.println( "Created some vital event records - model v0" );
        }
    }
}



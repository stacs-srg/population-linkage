package uk.ac.standrews.cs.population_linkage.graphFamily;

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */

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
        sessionFactory = new SessionFactory(conf, "uk.ac.standrews.cs.population_linkage.graphFamily");
        session = sessionFactory.openSession();
    }

    @Override
    public void close() throws Exception
    {
        sessionFactory.close();
    }

    public void make() {
        Person al = new Person( "Al","Dearle" );
        Person fiona = new Person("Fiona","Dearle"  );
        Person graham = new Person("Graham","Dearle"  );
        Person alice = new Person("Alice","Dearle"  );

        PersonRef al_fiona = new PersonRef(al, fiona, "SPOUSE", "ref_al", "GROOM", "ref_fiona", "BRIDE", 0.9, "some prov 1", 0.1);
        PersonRef fiona_al = new PersonRef(fiona, al, "SPOUSE", "marriage_ref", "GROOM", "marriage_ref", "BRIDE", 0.9, "some prov 1", 0.1);
//        PersonRef graham_alice = new PersonRef(graham, alice, "ref_graham", "SIBLING", "ref_alice", "SIBLING", 0.8, "BirthBirh", "some prov 2", 0.15);
        PersonRef fiona_alice = new PersonRef(fiona, alice, "CHILD", "ref_fiona", "marriage_ref", "ref_alice", "CHILD", 0.81, "some prov 3", 0.11);
        PersonRef fiona_graham = new PersonRef(fiona, graham, "CHILD","ref_fiona", "marriage_ref", "ref_graham", "CHILD", 0.815,  "some prov 4", 0.12);
        PersonRef al_alice = new PersonRef(al, alice, "CHILD","ref_al", "marriage_ref", "ref_alice", "CHILD", 0.812, "some prov 5", 0.11);
        PersonRef al_graham = new PersonRef(al, graham, "CHILD","ref_al", "marriage_ref", "ref_graham", "CHILD", 0.817, "some prov 6", 0.13);

        RecordProv al_br = new RecordProv("ref als br", "BABY", 0.9, "BIRTH", "some prov 6", 0.8);
        RecordProv fiona_br = new RecordProv("ref fions br", "BABY", 0.9, "BIRTH", "some prov 7", 0.8);
        RecordProv graham_br = new RecordProv("ref grahams br", "BABY", 0.9, "BIRTH", "some prov 6", 0.8);
        RecordProv alice_br = new RecordProv("ref alices br", "BABY", 0.9, "BIRTH", "some prov 6", 0.8);

        RecordProv al_mr = new RecordProv("ref al-fiona mr", "GROOM", 0.9, "MARRIAGE", "some prov 7", 0.8);
        RecordProv fiona_mr = new RecordProv("ref al-fiona mr", "BRIDE", 0.9, "MARRIAGE", "some prov 7", 0.8);

        al.addBirthRecord(al_br);
        fiona.addBirthRecord(fiona_br);
        alice.addBirthRecord(alice_br);
        graham.addBirthRecord(graham_br);

        al.addChild(al_alice);
        al.addChild(al_graham);

        fiona.addChild(fiona_alice);
        fiona.addChild(fiona_graham);

        fiona.addChild(al_alice);
        fiona.addChild(al_graham);

        fiona.addMarriageRecord(fiona_mr);
        al.addMarriageRecord(al_mr);

        al.addSpouse(al_fiona);

        // Save all

        session.save( al );
    }


    public static void main( String... args ) throws Exception
    {
        try ( Neo4JOGMCreateRelationships1 db = new Neo4JOGMCreateRelationships1( "bolt://localhost:7687", "neo4j", "password" ) )
        {
            db.make();
            System.out.println( "Created some vital event records" );
        }
    }
}



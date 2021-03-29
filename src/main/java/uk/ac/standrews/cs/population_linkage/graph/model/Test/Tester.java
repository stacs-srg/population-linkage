/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.model.Test;


import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import uk.ac.standrews.cs.population_linkage.graph.model.BirthRecord;
import uk.ac.standrews.cs.population_linkage.graph.model.DeathRecord;
import uk.ac.standrews.cs.population_linkage.graph.model.Reference;

public class Tester implements AutoCloseable {

    private final Session session;
    private SessionFactory sessionFactory;

    public Tester(String uri, String user, String password) {
        Configuration conf = new Configuration.Builder()
                .uri(uri)
                .credentials(user, password)
                .build();
        sessionFactory = new SessionFactory(conf,
                "uk.ac.standrews.cs.population_linkage.graph.model.Test",
                "uk.ac.standrews.cs.population_linkage.graph.model");
        session = sessionFactory.openSession();
    }

    @Override
    public void close() throws Exception {
        sessionFactory.close();
    }

    public void make() {

        DeathRecord fred_death_record = new DeathRecord();

        BirthRecord al_birth_record = new BirthRecord();
        BirthRecord graham_birth_record = new BirthRecord();

        Reference r = new Reference(al_birth_record, graham_birth_record, "stuff", 8, 0.5d );

    //    graham_birth_record.addFather(r); TODO put back later

        BirthRecord fiona_birth_record = new BirthRecord();
        fiona_birth_record.STANDARDISED_ID = "123";
        BirthRecord alice_birth_record = new BirthRecord();
        fiona_birth_record.STANDARDISED_ID = "456";

       // session.save(al_birth_record);
      //  session.save(graham_birth_record);

        session.save( alice_birth_record );
        session.save( fiona_birth_record );

        Transaction tx = session.beginTransaction();
        Reference.createBMMotherReference(tx, session, fiona_birth_record.STANDARDISED_ID, alice_birth_record.STANDARDISED_ID, "provenance", 8, 0.983  );
        tx.commit();
    }


    public static void main(String... args) throws Exception {
        try (Tester db = new Tester("bolt://localhost:7687", "neo4j", "password")) {
            db.make();
            System.out.println("Created some vital event records - Tester ");
        }
    }
}

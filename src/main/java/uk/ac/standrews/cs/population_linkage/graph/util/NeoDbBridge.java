/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.util;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

public class NeoDbBridge {

    private final Session session;
    private SessionFactory sessionFactory;

    private static final String default_url = "bolt://localhost:7687";
    private static final String default_user = "neo4j";
    private static final String default_password = "password";

    public NeoDbBridge() {
        this( default_url,default_user,default_password );
    }

    public NeoDbBridge(String uri, String user, String password) {
        Configuration conf = new Configuration.Builder()
                .uri(uri)
                .credentials(user, password)
                .build();
        sessionFactory = new SessionFactory(conf,
                "uk.ac.standrews.cs.population_linkage.graph.model");
        session = sessionFactory.openSession();
    }

    public void close() throws Exception
    {
        sessionFactory.close();
    }

    public Session getSession() {
        return session;
    }
}

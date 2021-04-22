/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.util;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

public class NeoDbOGMBridge extends NeoDbBridge implements AutoCloseable {

    private SessionFactory sessionFactory;
    private Configuration conf;

    public NeoDbOGMBridge() {
        this(default_url, default_user, default_password);
    }

    public NeoDbOGMBridge(String uri, String user, String password) {
        super( uri,user,password );
        conf = new Configuration.Builder()
                .uri(uri)
                .credentials(user, password)
                .build();
        sessionFactory = new SessionFactory(conf, "uk.ac.standrews.cs.population_linkage.graph.model");
    }

    @Override
    public void close() throws Exception
    {
        sessionFactory.close();
    }

    public Session getNewSession() {
        return sessionFactory.openSession();
    }
}

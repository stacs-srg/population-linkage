/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.util;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class NeoDbCypherBridge extends NeoDbBridge implements AutoCloseable {

    private final Driver driver;

    public NeoDbCypherBridge() {
        this( default_url,default_user,default_password );
    }

    public NeoDbCypherBridge(String uri, String user, String password) {
        super(uri, user, password);
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    public Session getNewSession() {
        return driver.session();
    }
}

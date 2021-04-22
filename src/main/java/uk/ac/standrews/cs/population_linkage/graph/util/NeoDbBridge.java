/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.util;

public abstract class NeoDbBridge implements AutoCloseable {

    protected static final String default_url = "bolt://localhost:7687";
    protected static final String default_user = "neo4j";
    protected static final String default_password = "password";

    protected final String uri;
    protected final String user;
    protected final String password;

    public NeoDbBridge() {
        this( default_url,default_user,default_password );
    }

    public NeoDbBridge(String uri, String user, String password) {
        this.uri = uri;
        this.user = user;
        this.password = password;
    }

    public abstract void close() throws Exception;
}

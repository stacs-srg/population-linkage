/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.util;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.Arrays;
import java.util.List;

public class Index {

    private static final String CREATE_CONSTRAINT_QUERY = "CREATE CONSTRAINT ON (n:STANDARDISED_ID) ASSERT n.propertyName IS UNIQUE";
    private static final String BIRTHS_INDEX_QUERY = "CALL db.createUniquePropertyConstraint(\"BirthsIndex\", [\"Birth\"], [\"STANDARDISED_ID\"], \"native-btree-1.0\")";
    private static final String MARRIAGE_INDEX_QUERY = "CALL db.createUniquePropertyConstraint(\"MarriagesIndex\", [\"Marriage\"], [\"STANDARDISED_ID\"], \"native-btree-1.0\")";
    private static final String DEATH_INDEX_QUERY = "CALL db.createUniquePropertyConstraint(\"DeathsIndex\", [\"Death\"], [\"STANDARDISED_ID\"], \"native-btree-1.0\")";

    public static void main( String[] args ) {

        NeoDbCypherBridge bridge = new NeoDbCypherBridge();

        List<String> queries = Arrays.asList( CREATE_CONSTRAINT_QUERY,BIRTHS_INDEX_QUERY,MARRIAGE_INDEX_QUERY,DEATH_INDEX_QUERY );

        doQueries(bridge,queries);
    }

    private static void doQueries(NeoDbCypherBridge bridge, List<String> queries) {
        try (Session session = bridge.getNewSession(); ) {
            for( String query : queries ) {
                try {
                    Result result = session.run(query);
                    System.out.println("Established constraint: " + query);
                } catch ( RuntimeException e ) {
                    System.out.println("Exception in constraint: " + e.getMessage() );
                }
            }
        }
        finally {
            System.out.println( "Run finished" );
            System.exit(0);
        }

    }

}

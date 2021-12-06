/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph;

import org.neo4j.driver.Session;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class IndexOnStandardisedId {

    private static final String CREATE_CONSTRAINT_QUERY = "CREATE CONSTRAINT ON (n:STANDARDISED_ID) ASSERT n.propertyName IS UNIQUE";
    private static final String BIRTHS_INDEX_QUERY = "CALL db.createUniquePropertyConstraint(\"BirthsIndex\", [\"Birth\"], [\"STANDARDISED_ID\"], \"native-btree-1.0\")";
    private static final String MARRIAGE_INDEX_QUERY = "CALL db.createUniquePropertyConstraint(\"MarriagesIndex\", [\"Marriage\"], [\"STANDARDISED_ID\"], \"native-btree-1.0\")";
    private static final String DEATH_INDEX_QUERY = "CALL db.createUniquePropertyConstraint(\"DeathsIndex\", [\"Death\"], [\"STANDARDISED_ID\"], \"native-btree-1.0\")";

    public static void main(String[] args) {

        final List<String> queries = Arrays.asList(CREATE_CONSTRAINT_QUERY, BIRTHS_INDEX_QUERY, MARRIAGE_INDEX_QUERY, DEATH_INDEX_QUERY);

        doQueries(new NeoDbCypherBridge(), queries);
    }

    private static void doQueries(NeoDbCypherBridge bridge, List<String> queries) {

        try (bridge; Session session = bridge.getNewSession()) {

            System.out.println("Creating indices @ " + LocalDateTime.now());

            for (String query : queries) {
                session.run(query);
            }
            System.out.println("Complete @ " + LocalDateTime.now());
        }
    }
}

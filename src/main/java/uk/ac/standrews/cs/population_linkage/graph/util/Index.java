package uk.ac.standrews.cs.population_linkage.graph.util;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

public class Index {

    private static final String CREATE_CONSTRAINT_QUERY = "CREATE CONSTRAINT ON (n:STANDARDISED_ID) ASSERT n.propertyName IS UNIQUE";
    private static final String BIRTHS_INDEX_QUERY = "CALL db.createUniquePropertyConstraint(\"BirthsIndex\", [\"BirthRecord\"], [\"STANDARDISED_ID\"], \"native-btree-1.0\")";
    private static final String MARRIAGE_INDEX_QUERY = "CALL db.createUniquePropertyConstraint(\"MarriagesIndex\", [\"MarriageRecord\"], [\"STANDARDISED_ID\"], \"native-btree-1.0\")";
    private static final String DEATH_INDEX_QUERY = "CALL db.createUniquePropertyConstraint(\"DeathsIndex\", [\"DeathRecord\"], [\"STANDARDISED_ID\"], \"native-btree-1.0\")";

    public static void main( String[] args ) {

        NeoDbCypherBridge bridge = new NeoDbCypherBridge();

        doQuery(bridge,CREATE_CONSTRAINT_QUERY);
        doQuery(bridge,BIRTHS_INDEX_QUERY);
        doQuery(bridge,MARRIAGE_INDEX_QUERY);
        doQuery(bridge,DEATH_INDEX_QUERY);
    }

    private static void doQuery(NeoDbCypherBridge bridge, String query) {
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            Result result = tx.run(query);
            result.stream().forEach( r -> System.out.println(r) );
            tx.commit();
        }
    }

}

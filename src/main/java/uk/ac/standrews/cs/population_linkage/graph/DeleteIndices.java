/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph;

import org.neo4j.driver.Session;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.time.LocalDateTime;

public class DeleteIndices {

    public static void main(String[] args) {

        doQueries(
                CreateIndices.BIRTHS_INDEX_NAME,
                CreateIndices.MARRIAGES_INDEX_NAME,
                CreateIndices.DEATHS_INDEX_NAME,

                CreateIndices.BIRTH_RECORD_IDENTITY_INDEX_NAME,
                CreateIndices.BIRTH_CHILD_IDENTITY_INDEX_NAME,
                CreateIndices.BIRTH_MOTHER_IDENTITY_INDEX_NAME,
                CreateIndices.BIRTH_FATHER_IDENTITY_INDEX_NAME,

                CreateIndices.MARRIAGE_BRIDE_IDENTITY_INDEX_NAME,
                CreateIndices.MARRIAGE_GROOM_IDENTITY_INDEX_NAME,
                CreateIndices.MARRIAGE_BRIDE_MOTHER_IDENTITY_INDEX_NAME,
                CreateIndices.MARRIAGE_GROOM_MOTHER_IDENTITY_INDEX_NAME,
                CreateIndices.MARRIAGE_BRIDE_FATHER_IDENTITY_INDEX_NAME,
                CreateIndices.MARRIAGE_GROOM_FATHER_IDENTITY_INDEX_NAME,

                CreateIndices.DEATH_DECEASED_IDENTITY_INDEX_NAME,
                CreateIndices.DEATH_MOTHER_IDENTITY_INDEX_NAME,
                CreateIndices.DEATH_FATHER_IDENTITY_INDEX_NAME
        );
    }

    private static void doQueries(String... index_names) {

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge(); Session session = bridge.getNewSession()) {

            System.out.println("Deleting indices @ " + LocalDateTime.now());

            for (String index_name : index_names) {
                session.run(makeDeleteIndexQuery(index_name));
            }

            System.out.println("Complete @ " + LocalDateTime.now());
        }
    }

    private static String makeDeleteIndexQuery(String index_name) {

        return String.format("DROP INDEX %s IF EXISTS", index_name);
    }
}

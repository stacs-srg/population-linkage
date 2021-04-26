package uk.ac.standrews.cs.population_linkage.graph.model;


import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbBridge;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbOGMBridge;

import java.util.HashMap;
import java.util.Map;

public class Query {

    // Standard queries
    // BB, BM etc. refer to Births Deaths and Marriages NOT babies, mothers etc.

    private static final String BB_SIBLING_QUERY = "MATCH (a:BirthRecord), (b:BirthRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_matched: $fields, distance: $distance } ]->(b)";

    private static final String BM_FATHER_QUERY = "MATCH (a:BirthRecord), (b:MarriageRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:FATHER { provenance: $prov, fields_matched: $fields, distance: $distance } ]->(b)";
    private static final String BM_MOTHER_QUERY = "MATCH (a:BirthRecord), (b:MarriageRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:MOTHER { provenance: $prov, fields_matched: $fields, distance: $distance } ]->(b)";
    private static final String BM_BIRTH_GROOM_QUERY = "MATCH (a:BirthRecord), (b:MarriageRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:GROOM { provenance: $prov, fields_matched: $fields, distance: $distance } ]->(b)";
    private static final String BM_BIRTH_BRIDE_QUERY = "MATCH (a:BirthRecord), (b:MarriageRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:BRIDE { provenance: $prov, fields_matched: $fields, distance: $distance } ]->(b)";

    private static final String DD_SIBLING_QUERY = "MATCH (a:DeathRecord), (b:DeathRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_matched: $fields, distance: $distance } ]->(b)";
    private static final String DD_DEATH_QUERY = "MATCH (a:BirthRecord), (b:DeathRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:DEATH { provenance: $prov, fields_matched: $fields, distance: $distance } ]->(b)";
    private static final String BM_DEATH_GROOM_QUERY = "MATCH (a:DeathRecord), (b:MarriageRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:GROOM { provenance: $prov, fields_matched: $fields, distance: $distance } ]->(b)";
    private static final String BM_DEATH_BRIDE_QUERY = "MATCH (a:DeathRecord), (b:MarriageRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:BRIDE { provenance: $prov, fields_matched: $fields, distance: $distance } ]->(b)";;

    /**
     * Creates a bride reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Birth and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createBirthBrideOwnMarriageReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        createReference(bridge, BM_BIRTH_BRIDE_QUERY, standard_id_from, standard_id_to, provenance, fields_matched, distance);
    }

    /**
     * Creates a groom reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Birth and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createBirthGroomOwnMarriageReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        createReference(bridge, BM_BIRTH_GROOM_QUERY, standard_id_from, standard_id_to, provenance, fields_matched, distance);
    }


    /**
     * Creates a mother reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Birth and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createBMMotherReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        createReference(bridge, BM_MOTHER_QUERY, standard_id_from, standard_id_to, provenance, fields_matched, distance);
    }

    /**
     * Creates a father reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Birth and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createBMFatherReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        createReference(bridge, BM_FATHER_QUERY, standard_id_from, standard_id_to, provenance, fields_matched, distance);
    }

    /**
     * Creates a sibling reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first and second parameters should be the id of a Births - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createBBSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        createReference(bridge, BB_SIBLING_QUERY, standard_id_from, standard_id_to, provenance, fields_matched, distance);
    }

    /**
     * Creates a sibling reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first and second parameters should be the id of a Deaths - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createDDSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        createReference(bridge, DD_SIBLING_QUERY, standard_id_from, standard_id_to, provenance, fields_matched, distance);
    }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Birth and the second a Death - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createBDReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        createReference(bridge, DD_DEATH_QUERY, standard_id_from, standard_id_to, provenance, fields_matched, distance);
    }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Death and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createDeathGroomOwnMarriageReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        createReference(bridge, BM_DEATH_GROOM_QUERY, standard_id_from, standard_id_to, provenance, fields_matched, distance);
    }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Death and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createDeathBrideOwnMarriageReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        createReference(bridge, BM_DEATH_BRIDE_QUERY, standard_id_from, standard_id_to, provenance, fields_matched, distance);
     }

    /**
     * This is the code that runs the neo4J query and returns the number of relationships created
     *
     * @param bridge           - a db bridge object
     * @param query            - the parameterised query to be used.
     * @param standard_id_from - the STANDARDISED_ID of the node from which we are creating a reference (note some labels are directed - e.g. MOTHER, FATHER etc.)
     * @param standard_id_to   - the STANDARDISED_ID of the node to which we are creating a reference
     * @param provenance       - the provenance of this reference
     * @param fields_matched   - the number of fields used in establishing the link - might need the actual fields and metric (?) but could find this from provenance (if includes classname)
     * @param distance         - the distance between the two nodes being linked
     * @return the number of relationships created
     */
    private static void createReference(NeoDbCypherBridge bridge, String query, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            Map<String, Object> parameters = getparams(standard_id_from, standard_id_to, provenance, fields_matched, distance);
            tx.run(query, parameters);
            tx.commit();
        }
    }

    private static Map<String, Object> getparams(String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        parameters.put("fields", fields_matched);
        parameters.put("prov", provenance);
        parameters.put("distance", distance);
        return parameters;
    }

    // TODO try and integrate the NeoDbCypherBridge and NeoDbOGMBridge interfaces.
    private static void createRef(NeoDbBridge bridge, String query, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        if (bridge instanceof NeoDbCypherBridge) {

        } else if (bridge instanceof NeoDbOGMBridge) {

        } else {
            throw new RuntimeException("createRef passed unknown NeoDbBridge class: " + bridge.getClass().getName());
        }

    }
}

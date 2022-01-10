/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph;


import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Query {

    // Standard creation queries
    // BB, BM etc. refer to Births Deaths and Marriages NOT babies, mothers etc.

    private static final String BB_SIBLING_QUERY = "MATCH (a:Birth), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";

    private static final String BM_FATHER_QUERY = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:FATHER { provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";
    private static final String BM_MOTHER_QUERY = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:MOTHER { provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";

    private static final String BM_BIRTH_GROOM_QUERY = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:GROOM { provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";
    private static final String BM_BIRTH_BRIDE_QUERY = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:BRIDE { provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";

    private static final String DD_SIBLING_QUERY = "MATCH (a:Death), (b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";

    private static final String BD_DEATH_QUERY = "MATCH (a:Birth), (b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:DEATH { provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";

    private static final String DM_DEATH_GROOM_QUERY = "MATCH (a:Death), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:GROOM { provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";
    private static final String DM_DEATH_BRIDE_QUERY = "MATCH (a:Death), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:BRIDE { provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";;

    private static final String MM_BB_SIBLING_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { actors: \"BB\", provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";
    private static final String MM_GG_SIBLING_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { actors: \"GG\", provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";
    private static final String MM_GB_SIBLING_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { actors: \"GB\", provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";

    private static final String DB_SIBLING_QUERY = "MATCH (a:Death), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";

    private static final String MM_GROOM_MARRIAGE_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:GROOM_PARENTS { provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)\";";
    private static final String MM_BRIDE_MARRIAGE_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:BRIDE_PARENTS { provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)\";";

    private static final String BM_GROOM_MARRIAGE_QUERY = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { actors: \"BG\", provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";
    private static final String BM_BRIDE_MARRIAGE_QUERY = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { actors: \"BB\", provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";

    private static final String MM_BRIDE_BRIDE_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { actors: \"BB\", provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";
    private static final String MM_GROOM_GROOM_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { actors: \"GG\", provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";

    private static final String DM_DECEASED_BRIDE_QUERY = "MATCH (a:Death), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { actors: \"DB\", provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";
    private static final String DM_DECEASED_GROOM_QUERY = "MATCH (a:Death), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { actors: \"DG\", provenance: $prov, fields_populated: $fields, distance: $distance } ]->(b)";

    // queries for use in predicates - return a relationship if it exists

    private static final String BB_SIBLING_EXISTS_QUERY = "MATCH (a:Birth)-[r:SIBLING]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String BM_FATHER_EXISTS_QUERY = "MATCH (a:Birth)-[r:FATHER]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String BM_MOTHER_EXISTS_QUERY = "MATCH (a:Birth)-[r:MOTHER]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String BM_BIRTH_GROOM_EXISTS_QUERY = "MATCH (a:Birth)-[r:GROOM]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String BM_BIRTH_BRIDE_EXISTS_QUERY = "MATCH (a:Birth)-[r:BRIDE]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String DD_SIBLING_EXISTS_QUERY = "MATCH (a:Death)-[r:SIBLING]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String BD_DEATH_EXISTS_QUERY = "MATCH (a:Birth)-[r:DEATH]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to  RETURN r";

    private static final String DM_DEATH_GROOM_EXISTS_QUERY = "MATCH (a:Death)-[r:GROOM]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov RETURN r";
    private static final String DM_DEATH_BRIDE_EXISTS_QUERY = "MATCH (a:Death)-[r:BRIDE]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String MM_BB_SIBLING_EXISTS_QUERY = "MATCH (a:Marriage)-[r:SIBLING]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String MM_GG_SIBLING_EXISTS_QUERY = "MATCH (a:Marriage)-[r:SIBLING]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String MM_GB_SIBLING_EXISTS_QUERY = "MATCH (a:Marriage)-[r:SIBLING]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String DB_SIBLING_EXISTS_QUERY = "MATCH (a:Death)-[r:SIBLING]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String MM_GROOM_MARRIAGE_EXISTS_QUERY = "MATCH (a:Marriage)-[r:GROOM_PARENTS]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String MM_BRIDE_MARRIAGE_EXISTS_QUERY = "MATCH (a:Marriage)-[r:BRIDE_PARENTS]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String BM_BRIDE_MARRIAGE_EXISTS_QUERY = "MATCH (a:Birth)-[r:SIBLING]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String BM_GROOM_MARRIAGE_EXISTS_QUERY = "MATCH (a:Birth)-[r:SIBLING]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String MM_BRIDE_BRIDE_EXISTS_QUERY = "MATCH (a:Marriage)-[r:SIBLING]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String MM_GROOM_GROOM_EXISTS_QUERY = "MATCH (a:Marriage)-[r:SIBLING]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String DM_DECEASED_BRIDE_EXISTS_QUERY = "MATCH (a:Death)-[r:SIBLING]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String DM_DECEASED_GROOM_EXISTS_QUERY = "MATCH (a:Death)-[r:SIBLING]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    // Create Operations

    /**
     * Creates a bride reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Birth and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createBirthBrideOwnMarriageReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, BM_BIRTH_BRIDE_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    /**
     * Creates a groom reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Birth and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createBirthGroomOwnMarriageReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, BM_BIRTH_GROOM_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }


    /**
     * Creates a mother reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Birth and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createBMMotherReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, BM_MOTHER_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    /**
     * Creates a father reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Birth and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createBMFatherReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, BM_FATHER_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    /**
     * Creates a sibling reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first and second parameters should be the id of a Births - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createBBSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, BB_SIBLING_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    /**
     * Creates a sibling reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first and second parameters should be the id of a Deaths - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createDDSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, DD_SIBLING_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Birth and the second a Death - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createBDReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, BD_DEATH_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Death and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createDeathGroomOwnMarriageReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, DM_DEATH_GROOM_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Death and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createDeathBrideOwnMarriageReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, DM_DEATH_BRIDE_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
     }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Death and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createMMGroomBrideSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, MM_GB_SIBLING_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Death and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createMMBrideGroomSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, MM_GB_SIBLING_QUERY, standard_id_to, standard_id_from, provenance, fields_populated, distance);
    }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Death and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createMMGroomGroomSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, MM_GG_SIBLING_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Death and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createMMBrideBrideSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, MM_BB_SIBLING_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Death and the second a Birth - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createDBSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, DB_SIBLING_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a marriage and the second a marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createMMGroomMarriageParentsMarriageReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, MM_GROOM_MARRIAGE_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a marriage and the second a marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static void createMMBrideMarriageParentsMarriageReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, MM_BRIDE_MARRIAGE_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    public static void createBMGroomSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        createReference(bridge, BM_GROOM_MARRIAGE_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    public static void createBMBrideSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance,  int fields_populated, double distance) {
        createReference(bridge, BM_BRIDE_MARRIAGE_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    public static void createMMBrideBrideIdReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance,  int fields_populated, double distance) {
        createReference(bridge, MM_BRIDE_BRIDE_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    public static void createMMGroomGroomIdReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance,  int fields_populated, double distance) {
        createReference(bridge, MM_GROOM_GROOM_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }


    public static void createDMBrideSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance,  int fields_populated, double distance) {
        createReference(bridge, DM_DECEASED_BRIDE_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    public static void createDMGroomSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance,  int fields_populated, double distance) {
        createReference(bridge, DM_DECEASED_GROOM_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    // predicates

    public static boolean BBBirthSiblingReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, BB_SIBLING_EXISTS_QUERY, standard_id_from, standard_id_to, provenance );
    }

    public static boolean BMBirthFatherReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, BM_FATHER_EXISTS_QUERY, standard_id_from, standard_id_to, provenance );
    }

    public static boolean BMBirthMotherReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, BM_MOTHER_EXISTS_QUERY, standard_id_from, standard_id_to, provenance );
    }

    public static boolean BMBirthGroomReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, BM_BIRTH_GROOM_EXISTS_QUERY, standard_id_from, standard_id_to, provenance );
    }

    public static boolean BMBirthBrideReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, BM_BIRTH_BRIDE_EXISTS_QUERY, standard_id_from, standard_id_to, provenance );
    }

    public static boolean DDSiblingReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, DD_SIBLING_EXISTS_QUERY, standard_id_from, standard_id_to, provenance );
    }

    public static boolean BDDeathReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, BD_DEATH_EXISTS_QUERY, standard_id_from, standard_id_to, provenance );
    }

    public static boolean DMDeathGroomOwnMarriageReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, DM_DEATH_GROOM_EXISTS_QUERY, standard_id_from, standard_id_to, provenance );
    }

    public static boolean DMDeathBrideOwnMarriageReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, DM_DEATH_BRIDE_EXISTS_QUERY, standard_id_from, standard_id_to, provenance );
    }

    public static boolean MMBrideBrideSiblingReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, MM_BB_SIBLING_EXISTS_QUERY, standard_id_from, standard_id_to, provenance );
    }

    public static boolean MMGroomGroomSiblingReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, MM_GG_SIBLING_EXISTS_QUERY, standard_id_from, standard_id_to, provenance );
    }

    public static boolean MMGroomBrideSiblingReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, MM_GB_SIBLING_EXISTS_QUERY, standard_id_from, standard_id_to, provenance );
    }

    public static boolean MMBrideGroomSiblingReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, MM_GB_SIBLING_EXISTS_QUERY, standard_id_to, standard_id_from, provenance );
    }

    public static boolean DBSiblingReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, DB_SIBLING_EXISTS_QUERY, standard_id_to, standard_id_from, provenance );
    }

    public static boolean MMGroomMarriageParentsMarriageReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, MM_GROOM_MARRIAGE_EXISTS_QUERY, standard_id_to, standard_id_from, provenance );
    }

    public static boolean MMBrideMarriageParentsMarriageReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        return linkExists( bridge, MM_BRIDE_MARRIAGE_EXISTS_QUERY, standard_id_to, standard_id_from, provenance );
    }

    public static boolean BMGroomSiblingReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance ){
        return linkExists(bridge, BM_GROOM_MARRIAGE_EXISTS_QUERY, standard_id_to, standard_id_from, provenance);
    }

    public static boolean BMBrideSiblingReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance ) {
        return linkExists( bridge, BM_BRIDE_MARRIAGE_EXISTS_QUERY, standard_id_to, standard_id_from, provenance );
    }

    public static boolean MMBrideBrideIdReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance ) {
        return linkExists( bridge, MM_BRIDE_BRIDE_EXISTS_QUERY, standard_id_to, standard_id_from, provenance );
    }

    public static boolean MMGroomGroomIdReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance ) {
        return linkExists( bridge, MM_GROOM_GROOM_EXISTS_QUERY, standard_id_to, standard_id_from, provenance );
    }

    public static boolean DMBrideSiblingReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance ) {
        return linkExists( bridge, DM_DECEASED_BRIDE_EXISTS_QUERY, standard_id_to, standard_id_from, provenance );
    }

    public static boolean DMGroomSiblingReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance ) {
        return linkExists( bridge, DM_DECEASED_GROOM_EXISTS_QUERY, standard_id_to, standard_id_from, provenance );
    }

    //=====================// private methods //=====================//

    /**
     * This is the code that runs the neo4J query and returns the number of relationships created
     *
     * @param bridge           - a db bridge object
     * @param query            - the parameterised query to be used.
     * @param standard_id_from - the STANDARDISED_ID of the node from which we are creating a reference (note some labels are directed - e.g. MOTHER, FATHER etc.)
     * @param standard_id_to   - the STANDARDISED_ID of the node to which we are creating a reference
     * @param provenance       - the provenance of this reference
     * @param fields_populated   - the number of fields used in establishing the link - might need the actual fields and metric (?) but could find this from provenance (if includes classname)
     * @return the number of relationships created
     */
    private static void createReference(NeoDbCypherBridge bridge, String query, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            Map<String, Object> parameters = getparams(standard_id_from, standard_id_to, provenance, fields_populated, distance);
            tx.run(query, parameters);
            tx.commit();
        }
    }

    private static boolean linkExists(NeoDbCypherBridge bridge, String query_string, String standard_id_from, String standard_id_to, String provenance) {
        Map<String, Object> parameters = getparams(standard_id_from, standard_id_to, provenance);
        Result result = bridge.getNewSession().run(query_string,parameters);
        List<Relationship> relationships = result.list(r -> r.get("r").asRelationship());
        if( relationships.size() == 0 ) {
            return false;
        }
        return true;
    }

    private static Map<String, Object> getparams(String standard_id_from, String standard_id_to, String provenance) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        parameters.put("prov", provenance);
        return parameters;
    }

    private static Map<String, Object> getparams(String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        parameters.put("fields", fields_populated);
        parameters.put("prov", provenance);
        parameters.put("distance", distance);
        return parameters;
    }
}

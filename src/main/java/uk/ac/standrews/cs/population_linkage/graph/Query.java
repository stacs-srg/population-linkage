/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.graph;


import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Neo4J Cypher queries for use elsewhere.
 *  @author al
 *  Reworked wirh generic labels 31/8/2022
 *
 *  * All the links are labelled as:
 *  * ID for identity linkage
 *  * SIBLING for sibling linkage
 *  * Each relationship has an attribute 'actors' which indicates the actors on the linked certificates.
 *  * The actors are hyphen separated e.g. [:ID { actors: "Child-Father" } ]
 *  * The strings used to identify the actors are: Child, Deceased, Father, Mother, Couple, Bride, Groom
 */
public class Query {

    // Standard creation queries
    // BB,
    // etc. refer to Births Deaths and Marriages NOT babies, mothers etc.

    private static final String BB_SIBLING_QUERY = "MATCH (a:Birth), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Child-Child\" } ]->(b)";

    private static final String BM_FATHER_QUERY = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:ID { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Child-Father\" } ]->(b)";
    private static final String BM_MOTHER_QUERY = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:ID { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Child-Mother\" } ]->(b)";

    private static final String BM_BIRTH_GROOM_QUERY = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:ID { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Child-Groom\"  } ]->(b)";
    private static final String BM_BIRTH_BRIDE_QUERY = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:ID { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Child-Bride\"  } ]->(b)";

    private static final String DD_SIBLING_QUERY = "MATCH (a:Death), (b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Deceased-Deceased\" } ]->(b)";

    private static final String BD_DEATH_QUERY = "MATCH (a:Birth), (b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:ID { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Child-Deceased\" } ]->(b)";

    private static final String DM_DEATH_GROOM_QUERY = "MATCH (a:Death), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:ID { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Deceased-Groom\" } } ]->(b)";
    private static final String DM_DEATH_BRIDE_QUERY = "MATCH (a:Death), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:ID { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Deceased-Bride\" } } ]->(b)";;

    private static final String MM_BB_SIBLING_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Bride-Bride\" } ]->(b)";
    private static final String MM_GG_SIBLING_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Groom-Groom\" } ]->(b)";
    private static final String MM_GB_SIBLING_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Groom-Bride\" } ]->(b)";

    private static final String DB_SIBLING_QUERY = "MATCH (a:Death), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Deceased-Child\" } ]->(b)";
    private static final String DB_SIBLING_QUERY_SIMPLE = "MATCH (a:Death), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, actors: \"Deceased-Child\" } ]->(b)";

    private static final String MM_GROOM_MARRIAGE_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:ID { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Groom-Couple\" } ]->(b)";
    private static final String MM_BRIDE_MARRIAGE_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:ID { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Bride-Couple\" } ]->(b)";

    private static final String BM_GROOM_SIBLING_QUERY = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Child-Groom\" } ]->(b)";
    private static final String BM_BRIDE_SIBLING_QUERY = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Child-Bride\" } ]->(b)";

    private static final String MM_BRIDE_BRIDE_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:ID { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Bride-Bride\" } } ]->(b)";
    private static final String MM_GROOM_GROOM_QUERY = "MATCH (a:Marriage), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:ID { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Groom-Groom\" } } ]->(b)";

    private static final String DM_DECEASED_BRIDE_QUERY = "MATCH (a:Death), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Deceased-Bride\" } } ]->(b)";
    private static final String DM_DECEASED_GROOM_QUERY = "MATCH (a:Death), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_populated: $fields, distance: $distance, actors: \"Deceased-Groom\" } } ]->(b)";

    // queries for use in predicates - return a relationship if it exists

    private static final String BB_SIBLING_EXISTS_QUERY = "MATCH (a:Birth)-[r:SIBLING { actors: \"Child-Child\" }]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String BM_FATHER_EXISTS_QUERY = "MATCH (a:Birth)-[r:ID { actors: \"Child-Father\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String BM_MOTHER_EXISTS_QUERY = "MATCH (a:Birth)-[r:ID { actors: \"Child-Mother\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String BM_BIRTH_GROOM_EXISTS_QUERY = "MATCH (a:Birth)-[r:ID { actors: \"Child-Groom\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String BM_BIRTH_BRIDE_EXISTS_QUERY = "MATCH (a:Birth)-[r:ID { actors: \"Child-Bride\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String DD_SIBLING_EXISTS_QUERY = "MATCH (a:Death)-[r:SIBLING { actors: \"Deceased-Deceased\" }]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String BD_DEATH_EXISTS_QUERY = "MATCH (a:Birth)-[r:ID { actors: \"Child-Deceased\" }]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to  RETURN r";

    private static final String DM_DEATH_GROOM_EXISTS_QUERY = "MATCH (a:Death)-[r:ID actors: \"Deceased-Groom\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov RETURN r";
    private static final String DM_DEATH_BRIDE_EXISTS_QUERY = "MATCH (a:Death)-[r:ID actors: \"Deceased-Bride\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String MM_BB_SIBLING_EXISTS_QUERY = "MATCH (a:Marriage)-[r:SIBLING { actors: \"Bride-Bride\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String MM_GG_SIBLING_EXISTS_QUERY = "MATCH (a:Marriage)-[r:SIBLING { actors: \"Groom-Groom\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String MM_GB_SIBLING_EXISTS_QUERY = "MATCH (a:Marriage)-[r:SIBLING { actors: \"Groom-Bride\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String DB_SIBLING_EXISTS_QUERY = "MATCH (a:Death)-[r:SIBLING { actors: \"Deceased-Child\" }]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String MM_GROOM_MARRIAGE_EXISTS_QUERY = "MATCH (a:Marriage)-[r:ID { actors: \"Groom-Couple\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String MM_BRIDE_MARRIAGE_EXISTS_QUERY = "MATCH (a:Marriage)-[r:ID { actors: \"Bride-Couple\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String BM_GROOM_MARRIAGE_EXISTS_QUERY = "MATCH (a:Birth)-[r:SIBLING { actors: \"Child-Groom\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String BM_BRIDE_MARRIAGE_EXISTS_QUERY = "MATCH (a:Birth)-[r:SIBLING { actors: \"Child-Bride\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String MM_BRIDE_BRIDE_EXISTS_QUERY = "MATCH (a:Marriage)-[r:SIBLING { actors: \"Bride-Bride\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String MM_GROOM_GROOM_EXISTS_QUERY = "MATCH (a:Marriage)-[r:SIBLING { actors: \"Groom-Groom\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

    private static final String DM_DECEASED_BRIDE_EXISTS_QUERY = "MATCH (a:Death)-[r:SIBLING { actors: \"Deceased-Bride\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";
    private static final String DM_DECEASED_GROOM_EXISTS_QUERY = "MATCH (a:Death)-[r:SIBLING { actors: \"Deceased-Groom\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov  RETURN r";

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

    public static void createDBSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance) {
        createReference(bridge, DB_SIBLING_QUERY_SIMPLE, standard_id_from, standard_id_to, provenance);
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
        createReference(bridge, BM_GROOM_SIBLING_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
    }

    public static void createBMBrideSiblingReference(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance,  int fields_populated, double distance) {
        createReference(bridge, BM_BRIDE_SIBLING_QUERY, standard_id_from, standard_id_to, provenance, fields_populated, distance);
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

    private static String getRefutedAlreadyQuery(NeoDbCypherBridge bridge, long id) {
        String query_string = "MATCH (a)-[r:SIBLING]-(b) WHERE id(r) = $id return r.link_refuted_by";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        Result result = bridge.getNewSession().run(query_string,parameters);

        List<Value> results = result.list(r -> r.get("r.link_refuted_by"));
        if( results.isEmpty() ) return null;
        Value qresult = results.get(0);
        if( qresult.isNull() ) return null;
        return qresult.asString();
    }

    public static void updateReference(NeoDbCypherBridge bridge, Relationship r, String query, String new_provenance) {

        long id = r.id();
        String refuted_previously = getRefutedAlreadyQuery(bridge,id);
        String provenance = refuted_previously == null ? new_provenance : refuted_previously + "/" + new_provenance;

        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("id", id);
            parameters.put("provenance", provenance);
            tx.run(query, parameters);
            tx.commit();
        }
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
     * @param fields_populated - the number of fields used in establishing the link - might need the actual fields and measure (?) but could find this from provenance (if includes classname)
     * @param distance         - the metric distance between the nodes.
     */
    private static void createReference(NeoDbCypherBridge bridge, String query, String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            Map<String, Object> parameters = getCreationParameterMap(standard_id_from, standard_id_to, provenance, fields_populated, distance);
            tx.run(query, parameters);
            tx.commit();
        }
    }

    private static void createReference(NeoDbCypherBridge bridge, String query, String standard_id_from, String standard_id_to, String provenance) {
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            Map<String, Object> parameters = getCreationParameterMap(standard_id_from, standard_id_to, provenance);
            tx.run(query, parameters);
            tx.commit();
        }
    }

    private static boolean linkExists(NeoDbCypherBridge bridge, String query_string, String standard_id_from, String standard_id_to, String provenance) {
        Map<String, Object> parameters = getCreationParameterMap(standard_id_from, standard_id_to, provenance);
        Result result = bridge.getNewSession().run(query_string,parameters);
        List<Relationship> relationships = result.list(r -> r.get("r").asRelationship());
        if( relationships.size() == 0 ) {
            return false;
        }
        return true;
    }

    private static Map<String, Object> getCreationParameterMap(String standard_id_from, String standard_id_to, String provenance) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        parameters.put("prov", provenance);
        return parameters;
    }

    private static Map<String, Object> getCreationParameterMap(String standard_id_from, String standard_id_to, String provenance, int fields_populated, double distance) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        parameters.put("fields", fields_populated);
        parameters.put("prov", provenance);
        parameters.put("distance", distance);
        return parameters;
    }
}

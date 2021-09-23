/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks;


import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.util.HashMap;
import java.util.Map;

/*
 *  The queries used to establish GT links for the Umea dataset
 *
 * @author Jianheng Huang jh377@st-andrews.ac.uk
 *         Bolun Wang bw93@st-andrews.ac.uk
 */
public class Query {
    private static final String BM_BIRTH_GROOM_IDENTITY = "MATCH (a:Birth) WHERE a.CHILD_IDENTITY = $child_identity MATCH (b:Marriage) WHERE b.GROOM_IDENTITY = a.CHILD_IDENTITY CREATE (a)-[r:GROUND_TRUTH_BIRTH_GROOM_IDENTITY]->(b)";
    private static final String BM_BIRTH_BRIDE_IDENTITY = "MATCH (a:Birth) WHERE a.CHILD_IDENTITY = $child_identity MATCH (b:Marriage) WHERE b.BRIDE_IDENTITY = a.CHILD_IDENTITY CREATE (a)-[r:GROUND_TRUTH_BIRTH_BRIDE_IDENTITY]->(b)";
    private static final String BD_BIRTH_DEATH_IDENTITY = "MATCH (a:Birth) WHERE a.CHILD_IDENTITY = $child_identity MATCH (b:Death) WHERE b.DECEASED_IDENTITY = a.CHILD_IDENTITY CREATE (a)-[r:GROUND_TRUTH_BIRTH_DEATH_IDENTITY]->(b)";
    private static final String BB_BIRTH_MOTHER_IDENTITY = "MATCH (a:Birth) WHERE a.CHILD_IDENTITY = $child_identity MATCH (b:Birth) WHERE b.MOTHER_IDENTITY = a.CHILD_IDENTITY CREATE (a)-[r:GROUND_TRUTH_BIRTH_MOTHER_IDENTITY]->(b)";
    private static final String BM_BIRTH_PARENTS_MARRIAGE = "MATCH (a:Birth) with a where a.STANDARDISED_ID = $standardised_id  MATCH (b:Marriage) WHERE b.GROOM_IDENTITY = a.FATHER_IDENTITY AND a.MOTHER_IDENTITY = b.BRIDE_IDENTITY CREATE (a)-[r:GROUND_TRUTH_BIRTH_PARENTS_MARRIAGE]->(b)";
    private static final String DB_DEATH_BIRTH_IDENTITY = "MATCH (b:Birth) WHERE b.CHILD_IDENTITY = $child_identity MATCH (a:Death) WHERE a.DECEASED_IDENTITY = b.CHILD_IDENTITY CREATE (a)-[r:GROUND_TRUTH_DEATH_BIRTH_IDENTITY]->(b)";
    private static final String DM_DEATH_BRIDE_OWN_MARRIAGE = "MATCH (a:Death) WHERE a.DECEASED_IDENTITY = $deceased_identity MATCH (b:Marriage) WHERE b.BRIDE_IDENTITY = a.DECEASED_IDENTITY CREATE (a)-[r:GROUND_TRUTH_DEATH_BRIDE_OWN_MARRIAGE]->(b)";
    private static final String DM_DEATH_GROOM_OWN_MARRIAGE_IDENTITY = "MATCH (a:Death) WHERE a.DECEASED_IDENTITY = $deceased_identity MATCH (b:Marriage) WHERE b.GROOM_IDENTITY = a.DECEASED_IDENTITY CREATE (a)-[r:GROUND_TRUTH_DEATH_GROOM_OWN_MARRIAGE_IDENTITY]->(b)";
    private static final String BM_FATHER_GROOM_IDENTITY = "MATCH (a:Birth) with a where a.FATHER_IDENTITY = $father_identity AND a.STANDARDISED_ID = $standardised_id  MATCH (b:Marriage) WHERE b.GROOM_IDENTITY = a.FATHER_IDENTITY CREATE (a)-[r:GROUND_TRUTH_FATHER_GROOM_IDENTITY]->(b)";

    private static final String BD_BIRTH_DEATH_SIBLING = "MATCH (a:Birth) with a WHERE a.STANDARDISED_ID = $standardised_id MATCH (b:Death) WHERE b.FATHER_IDENTITY = a.FATHER_IDENTITY AND a.MOTHER_IDENTITY = b.MOTHER_IDENTITY CREATE (a)-[r:GROUND_TRUTH_BIRTH_DEATH_SIBLING]->(b)";

    // same field searching
    private static final String BB_BIRTH_SIBLING_LINKAGE1 = "MATCH (a:Birth) WHERE a.STANDARDISED_ID = $standardised_id MATCH (b:Birth) WHERE (((a.MOTHER_IDENTITY = b.MOTHER_IDENTITY) AND (a.FATHER_IDENTITY = b.FATHER_IDENTITY)) AND NOT (a.STANDARDISED_ID=b.STANDARDISED_ID)) AND not exists ((b)-[:GROUND_TRUTH_BIRTH_SIBLING_LINKAGE]->(a)) CREATE (a)-[r:GROUND_TRUTH_BIRTH_SIBLING_LINKAGE]->(b)";
    private static final String BB_BIRTH_HALF_SIBLING_LINKAGE = "MATCH (a:Birth) WHERE a.STANDARDISED_ID = $standardised_id MATCH (b:Birth) WHERE (((a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND NOT a.MOTHER_IDENTITY='') OR (a.FATHER_IDENTITY = b.FATHER_IDENTITY AND NOT a.FATHER_IDENTITY=''))AND NOT (a.STANDARDISED_ID=b.STANDARDISED_ID)) AND NOT (b.MOTHER_IDENTITY = a.MOTHER_IDENTITY AND b.FATHER_IDENTITY = a.FATHER_IDENTITY) MERGE (a)-[r:GROUND_TRUTH_BIRTH_HALF_SIBLING_LINKAGE]->(b)";

    private static final String MM_BRIDE_BRIDE_SIBLING_LINKAGE = "MATCH (a:Marriage) with a where a.STANDARDISED_ID = $standardised_id MATCH (b:Marriage) WHERE (((a.BRIDE_MOTHER_IDENTITY = b.BRIDE_MOTHER_IDENTITY) AND (a.BRIDE_FATHER_IDENTITY = b.BRIDE_FATHER_IDENTITY)) AND NOT (a.STANDARDISED_ID=b.STANDARDISED_ID)) AND not exists ((b)-[:GROUND_TRUTH_BRIDE_BRIDE_SIBLING_LINKAGE]->(a)) CREATE (a)-[r:GROUND_TRUTH_BRIDE_BRIDE_SIBLING_LINKAGE]->(b)";
    private static final String MM_BRIDE_BRIDE_HALF_SIBLING_LINKAGE = "MATCH (a:Marriage) with a where a.STANDARDISED_ID = $standardised_id MATCH (b:Marriage) WHERE (((a.BRIDE_MOTHER_IDENTITY = b.BRIDE_MOTHER_IDENTITY AND NOT a.BRIDE_MOTHER_IDENTITY='') OR (a.BRIDE_FATHER_IDENTITY = b.BRIDE_FATHER_IDENTITY AND NOT a.BRIDE_FATHER_IDENTITY='')) AND NOT (a.STANDARDISED_ID=b.STANDARDISED_ID)) AND NOT (b.BRIDE_MOTHER_IDENTITY = a.BRIDE_MOTHER_IDENTITY AND b.BRIDE_FATHER_IDENTITY = a.BRIDE_FATHER_IDENTITY) CREATE (a)-[r:GROUND_TRUTH_BRIDE_BRIDE_HALF_SIBLING_LINKAGE]->(b)";

    private static final String MM_BRIDE_GROOM_SIBLING_LINKAGE = "MATCH (a:Marriage) WHERE a.STANDARDISED_ID = $standardised_id MATCH (b:Marriage) WHERE b.GROOM_MOTHER_IDENTITY = a.BRIDE_MOTHER_IDENTITY AND b.GROOM_FATHER_IDENTITY = a.BRIDE_FATHER_IDENTITY CREATE (a)-[r:GROUND_TRUTH_BRIDE_GROOM_SIBLING_LINKAGE]->(b)";
    private static final String MM_GROOM_BRIDE_SIBLING_LINKAGE = "MATCH (a:Marriage) WHERE a.STANDARDISED_ID = $standardised_id MATCH (b:Marriage) WHERE a.GROOM_MOTHER_IDENTITY = b.BRIDE_MOTHER_IDENTITY AND a.GROOM_FATHER_IDENTITY = b.BRIDE_FATHER_IDENTITY CREATE (a)-[r:GROUND_TRUTH_GROOM_BRIDE_SIBLING_LINKAGE]->(b)";
    private static final String MM_GROOM_GROOM_SIBLING = "MATCH (a:Marriage) with a where a.STANDARDISED_ID = $standardised_id MATCH (b:Marriage) WHERE (((a.GROOM_MOTHER_IDENTITY = b.GROOM_MOTHER_IDENTITY) AND (a.GROOM_FATHER_IDENTITY = b.GROOM_FATHER_IDENTITY)) AND NOT (a.STANDARDISED_ID=b.STANDARDISED_ID)) AND not exists ((b)-[:GROUND_TRUTH_GROOM_GROOM_SIBLING]->(a)) CREATE (a)-[r:GROUND_TRUTH_GROOM_GROOM_SIBLING]->(b)";
    private static final String MM_GROOM_GROOM_HALF_SIBLING = "MATCH (a:Marriage) WHERE a.STANDARDISED_ID = $standardised_id MATCH (b:Marriage) WHERE (((a.GROOM_MOTHER_IDENTITY = b.GROOM_MOTHER_IDENTITY AND NOT a.GROOM_MOTHER_IDENTITY='') OR (a.GROOM_FATHER_IDENTITY = b.GROOM_FATHER_IDENTITY AND NOT a.GROOM_FATHER_IDENTITY='')) AND NOT (a.STANDARDISED_ID=b.STANDARDISED_ID)) AND NOT (b.GROOM_MOTHER_IDENTITY = a.GROOM_MOTHER_IDENTITY AND b.GROOM_FATHER_IDENTITY = a.GROOM_FATHER_IDENTITY) CREATE (a)-[r:GROUND_TRUTH_GROOM_GROOM_HALF_SIBLING]->(b)";
    private static final String DD_DEATH_SIBLING_LINKAGE = "MATCH (a:Death) with a where a.STANDARDISED_ID = $standardised_id MATCH (b:Death) WHERE (((a.MOTHER_IDENTITY = b.MOTHER_IDENTITY) AND (a.FATHER_IDENTITY = b.FATHER_IDENTITY)) AND NOT (a.STANDARDISED_ID=b.STANDARDISED_ID)) AND not exists ((b)-[:GROUND_TRUTH_DEATH_SIBLING_LINKAGE]->(a)) CREATE (a)-[r:GROUND_TRUTH_DEATH_SIBLING_LINKAGE]->(b)";
    private static final String DD_DEATH_HALF_SIBLING_LINKAGE = "MATCH (a:Death) WHERE a.STANDARDISED_ID = $standardised_id MATCH (b:Death) WHERE (((a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND NOT a.MOTHER_IDENTITY='') OR (a.FATHER_IDENTITY = b.FATHER_IDENTITY AND NOT a.FATHER_IDENTITY='')) AND NOT (a.STANDARDISED_ID=b.STANDARDISED_ID)) AND NOT (b.MOTHER_IDENTITY = a.MOTHER_IDENTITY AND b.FATHER_IDENTITY = a.FATHER_IDENTITY) CREATE (a)-[r:GROUND_TRUTH_DEATH_HALF_SIBLING_LINKAGE]->(b)";

    private static final String BD_BIRTH_DEATH_HALF_SIBLING = "MATCH (a:Birth) with a WHERE a.STANDARDISED_ID = $standardised_id MATCH (b:Death) WHERE ((b.FATHER_IDENTITY = a.FATHER_IDENTITY AND NOT a.FATHER_IDENTITY='') OR (a.MOTHER_IDENTITY = b.MOTHER_IDENTITY AND NOT a.MOTHER_IDENTITY='')) AND NOT (b.FATHER_IDENTITY = a.FATHER_IDENTITY AND a.MOTHER_IDENTITY = b.MOTHER_IDENTITY) CREATE (a)-[r:GROUND_TRUTH_BIRTH_DEATH_HALF_SIBLING]->(b)";
    private static final String MM_BRIDE_GROOM_HALF_SIBLING_LINKAGE = "MATCH (a:Marriage) WHERE a.STANDARDISED_ID = $standardised_id MATCH (b:Marriage) WHERE (((a.BRIDE_MOTHER_IDENTITY = b.GROOM_MOTHER_IDENTITY AND NOT a.BRIDE_MOTHER_IDENTITY='') OR (a.BRIDE_FATHER_IDENTITY = b.GROOM_FATHER_IDENTITY AND NOT a.BRIDE_FATHER_IDENTITY=''))AND NOT (a.STANDARDISED_ID=b.STANDARDISED_ID)) AND NOT (b.GROOM_MOTHER_IDENTITY = a.BRIDE_MOTHER_IDENTITY AND b.GROOM_FATHER_IDENTITY = a.BRIDE_FATHER_IDENTITY) CREATE (a)-[r:GROUND_TRUTH_BRIDE_GROOM_HALF_SIBLING_LINKAGE]->(b)";
    private static final String MM_GROOM_BRIDE_HALF_SIBLING_LINKAGE = "MATCH (a:Marriage) WHERE a.STANDARDISED_ID = $standardised_id MATCH (b:Marriage) WHERE (((a.GROOM_MOTHER_IDENTITY = b.BRIDE_MOTHER_IDENTITY AND NOT a.GROOM_MOTHER_IDENTITY='') OR (a.GROOM_FATHER_IDENTITY = b.BRIDE_FATHER_IDENTITY AND NOT a.GROOM_FATHER_IDENTITY=''))AND NOT (a.STANDARDISED_ID=b.STANDARDISED_ID)) AND NOT (a.GROOM_MOTHER_IDENTITY = b.BRIDE_MOTHER_IDENTITY AND a.GROOM_FATHER_IDENTITY = b.BRIDE_FATHER_IDENTITY) CREATE (a)-[r:GROUND_TRUTH_GROOM_BRIDE_HALF_SIBLING_LINKAGE]->(b)";

    /**
     * Create Groom Identity
     *
     * @param bridge
     * @param firstFieldName
     * @param child_identity
     */

    public static void createBirthGroomOwnMarriageReference(NeoDbCypherBridge bridge, String firstFieldName, String child_identity) {
        createReference(bridge, BM_BIRTH_GROOM_IDENTITY, firstFieldName, child_identity);
    }

    /**
     * Create Bride Identity
     *
     * @param bridge
     * @param firstFieldName
     * @param child_identity
     */

    public static void createBirthBrideOwnMarriageReference(NeoDbCypherBridge bridge, String firstFieldName, String child_identity) {
        createReference(bridge, BM_BIRTH_BRIDE_IDENTITY, firstFieldName, child_identity);
    }

    /**
     * Create Birth Death Identity
     *
     * @param bridge
     * @param firstFieldName
     * @param child_identity
     */
    public static void createBirthDeathIdentityReference(NeoDbCypherBridge bridge, String firstFieldName, String child_identity) {
        createReference(bridge, BD_BIRTH_DEATH_IDENTITY, firstFieldName, child_identity);
    }

    /**
     * Create Birth Mother Identity
     *
     * @param bridge
     * @param firstFieldName
     * @param child_identity
     */

    public static void createBirthMotherIdentityReference(NeoDbCypherBridge bridge, String firstFieldName, String child_identity) {
        createReference(bridge, BB_BIRTH_MOTHER_IDENTITY, firstFieldName, child_identity);
    }

    /**
     * Create Birth Parents Marriage
     *
     * @param bridge
     * @param firstFieldName
     */

    public static void createBirthParentsMarriageReference(NeoDbCypherBridge bridge, String firstFieldName, String standardised_id) {
        createReference(bridge, BM_BIRTH_PARENTS_MARRIAGE, firstFieldName, standardised_id);
    }

    /**
     * Create Death Birth Identity
     *
     * @param bridge
     * @param firstFieldName
     * @param child_identity
     */
    public static void createDeathBirthIdentityReference(NeoDbCypherBridge bridge, String firstFieldName, String child_identity) {
        createReference(bridge, DB_DEATH_BIRTH_IDENTITY, firstFieldName, child_identity);
    }

    /**
     * Create Death Bride Own Marriage
     *
     * @param bridge
     * @param firstFieldName
     * @param deceased_identity
     */

    public static void createDeathBrideOwnMarriageReference(NeoDbCypherBridge bridge, String firstFieldName, String deceased_identity) {
        createReference(bridge, DM_DEATH_BRIDE_OWN_MARRIAGE, firstFieldName, deceased_identity);
    }

    /**
     * Create Death Groom Own Marriage Identity
     *
     * @param bridge
     * @param firstFieldName
     * @param deceased_identity
     */
    public static void createDeathGroomOwnMarriageIdentityReference(NeoDbCypherBridge bridge, String firstFieldName, String deceased_identity) {
        createReference(bridge, DM_DEATH_GROOM_OWN_MARRIAGE_IDENTITY, firstFieldName, deceased_identity);
    }

    /**
     * Create Father Groom Identity
     *
     * @param bridge
     * @param firstFieldName
     * @param secondFieldName
     * @param father_identity
     * @param groom_identity
     */
    public static void createFatherGroomIdentityReference(NeoDbCypherBridge bridge, String firstFieldName, String secondFieldName, String father_identity, String groom_identity) {
        createReference(bridge, BM_FATHER_GROOM_IDENTITY, firstFieldName, secondFieldName, father_identity, groom_identity);
    }

    /**
     * Create Birth Death Sibling
     *
     * @param bridge
     * @param firstFieldName
     * @param standardised_id
     */
    public static void createBirthDeathSiblingReference(NeoDbCypherBridge bridge, String firstFieldName, String standardised_id) {
        createReference(bridge, BD_BIRTH_DEATH_SIBLING, firstFieldName, standardised_id);
    }

    public static void createBirthDeathHalfSiblingReference(NeoDbCypherBridge bridge, String firstFieldName, String birth_father_identity) {
        createReference(bridge, BD_BIRTH_DEATH_HALF_SIBLING, firstFieldName, birth_father_identity);
    }

    public static void createBirthSiblingLinkageReference1(NeoDbCypherBridge bridge, String firstFieldName, String standardised_id1) {
        createReference(bridge, BB_BIRTH_SIBLING_LINKAGE1, firstFieldName, standardised_id1);
    }

    public static void createBirthHalfSiblingLinkageReference(NeoDbCypherBridge bridge, String firstFieldName, String standardised_id1) {
        createReference(bridge, BB_BIRTH_HALF_SIBLING_LINKAGE, firstFieldName, standardised_id1);
    }

    public static void createBrideBrideSiblingLinkageReference(NeoDbCypherBridge bridge, String firstFieldName, String bride_mother_identity1) {
        createReference(bridge, MM_BRIDE_BRIDE_SIBLING_LINKAGE, firstFieldName, bride_mother_identity1);
    }

    public static void createBrideBrideHalfSiblingLinkageReference(NeoDbCypherBridge bridge, String firstFieldName, String bride_mother_identity1) {
        createReference(bridge, MM_BRIDE_BRIDE_HALF_SIBLING_LINKAGE, firstFieldName, bride_mother_identity1);
    }

    public static void createBrideGroomSiblingLinkageReference(NeoDbCypherBridge bridge, String firstFieldName, String standardised_id) {
        createReference(bridge, MM_BRIDE_GROOM_SIBLING_LINKAGE, firstFieldName, standardised_id);
    }

    public static void createBrideGroomHalfSiblingLinkageReference(NeoDbCypherBridge bridge, String firstFieldName, String standardised_id) {
        createReference(bridge, MM_BRIDE_GROOM_HALF_SIBLING_LINKAGE, firstFieldName, standardised_id);
    }

    public static void createGroomBrideSiblingLinkageReference(NeoDbCypherBridge bridge, String firstFieldName, String standardised_id) {
        createReference(bridge, MM_GROOM_BRIDE_SIBLING_LINKAGE, firstFieldName, standardised_id);
    }

    public static void createGroomBrideHalfSiblingLinkageReference(NeoDbCypherBridge bridge, String firstFieldName, String standardised_id) {
        createReference(bridge, MM_GROOM_BRIDE_HALF_SIBLING_LINKAGE, firstFieldName, standardised_id);
    }

    public static void createGroomGroomSiblingReference(NeoDbCypherBridge bridge, String firstFieldName, String standardised_id) {
        createReference(bridge, MM_GROOM_GROOM_SIBLING, firstFieldName, standardised_id);
    }

    public static void createGroomGroomHalfSiblingReference(NeoDbCypherBridge bridge, String firstFieldName, String standardised_id) {
        createReference(bridge, MM_GROOM_GROOM_HALF_SIBLING, firstFieldName, standardised_id);
    }

    public static void createDeathSiblingLinkageReference(NeoDbCypherBridge bridge, String firstFieldName, String standardised_id) {
        createReference(bridge, DD_DEATH_SIBLING_LINKAGE, firstFieldName, standardised_id);
    }

    public static void createDeathHalfSiblingLinkageReference(NeoDbCypherBridge bridge, String firstFieldName, String standardised_id) {
        createReference(bridge, DD_DEATH_HALF_SIBLING_LINKAGE, firstFieldName, standardised_id);
    }

    private static void createReference(NeoDbCypherBridge bridge, String query, String firstFieldName, String secondFieldName, String firstField, String secondField) {
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction()) {
            Map<String, Object> parameters = getparams(firstFieldName, secondFieldName, firstField, secondField);
            tx.run(query, parameters);
            tx.commit();
        }
    }

    private static void createReference(NeoDbCypherBridge bridge, String query, String firstFieldName, String firstField) {
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction()) {
            Map<String, Object> parameters = getparams(firstFieldName, firstField);
            tx.run(query, parameters);
            tx.commit();
        }
    }

    private static Map<String, Object> getparams(String firstFieldName, String firstField) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(firstFieldName, firstField);
        return parameters;
    }

    private static Map<String, Object> getparams(String firstFieldName, String secondFieldName, String firstField, String secondField) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(firstFieldName, firstField);
        parameters.put(secondFieldName, secondField);
        return parameters;
    }
}

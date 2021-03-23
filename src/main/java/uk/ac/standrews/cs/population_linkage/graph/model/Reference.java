package uk.ac.standrews.cs.population_linkage.graph.model;

import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;

import java.util.HashMap;
import java.util.Map;

@RelationshipEntity
public class Reference {

    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private VitalEventRecord start;
    @EndNode
    private VitalEventRecord end;
    @Property
    private String provenance;
    @Property
    private int fields_matched;
    @Property
    private double distance;

    // Standard queries
    // BB, BM etc. refer to Births Deaths and Marriages NOT babies, mothers etc.

    private static final String CREATE_BB_SIBLING_REFERENCE_QUERY = "MATCH (a:BirthRecord), (b:BirthRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:SIBLING { provenance: $prov, fields_matched: $fields, distance: $distance } ]->(b)";
    private static final String CREATE_BM_FATHER_REFERENCE_QUERY = "MATCH (a:BirthRecord), (b:MarriageRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:FATHER { provenance: $prov, fields_matched: $fields, distance: $distance } ]->(b)";
    public static final String CREATE_BM_MOTHER_REFERENCE_QUERY = "MATCH (a:BirthRecord), (b:MarriageRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:MOTHER { provenance: $prov, fields_matched: $fields, distance: $distance } ]->(b)";
    private static final String CREATE_DEATH_REFERENCE_QUERY = "MATCH (a:BirthRecord), (b:DeathRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to CREATE (a)-[r:MOTHER { provenance: $prov, fields_matched: $fields, distance: $distance } ]->(b)";


    // Constructors

    public Reference() {
    }

    public Reference(VitalEventRecord start, VitalEventRecord end, String provenance, int fields_matched, double distance) {
        this.start = start;
        this.end = end;
        this.provenance = provenance;
        this.fields_matched = fields_matched;
        this.distance = distance;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public VitalEventRecord getStart() {
        return start;
    }

    public void setStart(VitalEventRecord start) {
        this.start = start;
    }

    public VitalEventRecord getEnd() {
        return end;
    }

    public void setEnd(VitalEventRecord end) {
        this.end = end;
    }

    /**
     * Creates a mother reference between node with standard_id_from and standard_id_to and returns the number of relationships create
     * The first parameter should be the id of a Birth and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static int createBMMotherReference(Transaction tx, Session session, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        return createReference( tx, session, CREATE_BM_MOTHER_REFERENCE_QUERY,  standard_id_from,  standard_id_to,  provenance,  fields_matched,  distance);
    }

    /**
     * Creates a father reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Birth and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static int createBMFatherReference(Transaction tx, Session session, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        return createReference( tx, session, CREATE_BM_FATHER_REFERENCE_QUERY,  standard_id_from,  standard_id_to,  provenance,  fields_matched,  distance);
    }

    /**
     * Creates a sibling reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Birth and the second a Marriage - it will not work if this is not the case!
     * See createReference for param details
     */
    public static int createBBSiblingReference(Transaction tx, Session session, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        return createReference( tx, session, CREATE_BB_SIBLING_REFERENCE_QUERY,  standard_id_from,  standard_id_to,  provenance,  fields_matched,  distance);
    }

    /**
     * Creates a reference between node with standard_id_from and standard_id_to and returns the number of relationships created
     * The first parameter should be the id of a Birth and the second a Death - it will not work if this is not the case!
     * See createReference for param details
     */
    public static int createBDReference(Transaction tx, Session session, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        return createReference( tx, session, CREATE_DEATH_REFERENCE_QUERY,  standard_id_from,  standard_id_to,  provenance,  fields_matched,  distance);
    }



    /**
     * This is the code that runs the neo4J query and returns the number of relationships created
     * @param tx - the current transaction
     * @param session - the neo4J session object currently being used.
     * @param query - the paramterised query to be used.
     * @param standard_id_from - the STANDARDISED_ID of the node from which we are creating a reference (note some labels are directed - e.g. MOTHER, FATHER etc.)
     * @param standard_id_to - the STANDARDISED_ID of the node to which we are creating a reference
     * @param provenance - the provenance of this reference
     * @param fields_matched - the number of fields used in establishing the link - might need the actual fields and metric (?) but could find this from provenance (if includes classname)
     * @param distance - the distance between the two nodes being linked
     * @return the number of relationships created
     */
    private static int createReference(Transaction tx, Session session, String query, String standard_id_from, String standard_id_to, String provenance, int fields_matched, double distance) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        parameters.put("fields", fields_matched);
        parameters.put("prov", provenance);
        parameters.put("distance", distance);

        Result r = session.query(query, parameters);
        int count = r.queryStatistics().getRelationshipsCreated();
        return count;
    }
}

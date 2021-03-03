package uk.ac.standrews.cs.population_linkage.graphFamilyV1;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity
public class PersonRef {
    @JsonProperty("id")
    private Long id;
    @StartNode
    private Person start;
    @EndNode
    private Person end;
    @Property
    private String type;
    @Property
    private String record1_LXPReference;
    @Property
    private String role1;
    @Property
    private String record2_LXPReference;
    @Property
    private String role2;
    @Property
    private double confidence;
    @Property
    private String provenance;
    @Property
    private double distance;

    public PersonRef(Person start, Person end, String type, String record1_LXPReference, String role1, String record2_LXPReference, String role2, double confidence, String provenance, double distance) {
        this.start = start;
        this.end = end;
        this.type = type;
        this.record1_LXPReference = record1_LXPReference;
        this.role1 = role1;
        this.record2_LXPReference = record2_LXPReference;
        this.role2 = role2;
        this.confidence = confidence;
        this.provenance = provenance;
        this.distance = distance;
    }

    // Getters and Setters

    public Person getEnd() {
        return end;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getStart() {
        return start;
    }

    public void setStart(Person start) {
        this.start = start;
    }

    public void setEnd(Person end) {
        this.end = end;
    }

    public String getRecord1_LXPReference() {
        return record1_LXPReference;
    }

    public void setRecord1_LXPReference(String record1_LXPReference) {
        this.record1_LXPReference = record1_LXPReference;
    }

    public String getRole1() {
        return role1;
    }

    public void setRole1(String role1) {
        this.role1 = role1;
    }

    public String getRecord2_LXPReference() {
        return record2_LXPReference;
    }

    public void setRecord2_LXPReference(String record2_LXPReference) {
        this.record2_LXPReference = record2_LXPReference;
    }

    public String getRole2() {
        return role2;
    }

    public void setRole2(String role2) {
        this.role2 = role2;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getProvenance() {
        return provenance;
    }

    public void setProvenance(String provenance) {
        this.provenance = provenance;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}

package uk.ac.standrews.cs.population_linkage.graphFamilyV1;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity
public class RecordProv {
    @JsonProperty("id")
    private Long id;
    @Property
    private String record1_LXPReference;
    @Property
    private String role;
    @Property
    private double confidence;
    @Property
    private String link_type;
    @Property
    private String provenance;
    @Property
    private double distance;

    // Getters and Setters

    public RecordProv(String record_LXPReference, String role, double confidence, String link_type, String provenance, double distance) {
        this.record1_LXPReference = record1_LXPReference;
        this.role = role;
        this.confidence = confidence;
        this.link_type = link_type;
        this.provenance = provenance;
        this.distance = distance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecord1_LXPReference() {
        return record1_LXPReference;
    }

    public void setRecord1_LXPReference(String record1_LXPReference) {
        this.record1_LXPReference = record1_LXPReference;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role1) {
        this.role = role1;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String get_link_type() {
        return link_type;
    }

    public void set_link_type(String link_type) {
        this.link_type = link_type;
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

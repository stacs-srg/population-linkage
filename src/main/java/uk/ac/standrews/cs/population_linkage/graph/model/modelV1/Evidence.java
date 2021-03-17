/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.model.modelV1;

import org.neo4j.ogm.annotation.*;

@NodeEntity
public class Evidence {
    @Id
    @GeneratedValue
    private Long id;
    @Relationship
    private VitalEventRecord record;
    @Property
    private double confidence;
    @Property
    private String provenance;
    @Property
    private double distance;


    // Getters and Setters

    public Evidence(VitalEventRecord record, double confidence, String provenance, double distance) {
        this.record = record;
        this.confidence = confidence;
        this.provenance = provenance;
        this.distance = distance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VitalEventRecord getRecord() {
        return record;
    }

    public void setRecord(VitalEventRecord record) {
        this.record = record;
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

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.model;

import org.neo4j.ogm.annotation.*;

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
    private int fields_populated;
    @Property
    private double distance;

    // Constructors

    public Reference() {
    }

    public Reference(VitalEventRecord start, VitalEventRecord end, String provenance, int fields_populated, double distance) {
        this.start = start;
        this.end = end;
        this.provenance = provenance;
        this.fields_populated = fields_populated;
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
}

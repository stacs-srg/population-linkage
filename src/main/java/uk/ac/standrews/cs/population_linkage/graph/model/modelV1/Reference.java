package uk.ac.standrews.cs.population_linkage.graph.model.modelV1;

import org.neo4j.ogm.annotation.*;
/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
import java.util.ArrayList;
import java.util.List;

@NodeEntity
public class Reference {
    @Id
    @GeneratedValue
    private Long id;
//    @Relationship
//    private Person source;
//    @Relationship
//    private Person destination;
    @Relationship
    private List<Evidence> evidence = new ArrayList<>();

    private Reference(Person source, String source_role, Person destination, String destination_role) {
//        this.source = source;
//        this.destination = destination;
    }

    public Reference(Person source, String source_role, Person destination, String destination_role,  List<Evidence> evidence ) {
        this(source,source_role,destination,destination_role);
        this.evidence.addAll(evidence);
    }

    public Reference(Person source, String source_role, Person destination, String destination_role,  Evidence evidence ) {
        this(source,source_role,destination,destination_role);
        this.evidence.add( evidence );
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    Person getSource() {
//        return source;
//    }
//
//    public void setSource(Person source) {
//        this.source = source;
//    }
//
//    public Person getDestination() {
//        return destination;
//    }
//
//    public void setDestination(Person outgoing) {
//        this.destination = destination;
//    }

    public List<Evidence> getEvidence() { return evidence; }

    public void addEvidence(Evidence e) { evidence.add(e); }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.model.modelV1;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class VitalEventRecord {

    @Id
    @GeneratedValue
    protected Long id;

    public VitalEventRecord() {}

    public Long getId() {
        return id;
    }
}

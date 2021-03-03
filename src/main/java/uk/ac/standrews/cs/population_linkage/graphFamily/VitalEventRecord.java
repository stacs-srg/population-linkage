package uk.ac.standrews.cs.population_linkage.graphFamily;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class VitalEventRecord {

    @Id
    @GeneratedValue
    protected Long id;

    public VitalEventRecord() {}
}

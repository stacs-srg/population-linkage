package uk.ac.standrews.cs.population_linkage.graphFamily;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity
public class MarriageVitalEventRecord extends VitalEventRecord {

    @Property
    private String names;

    public MarriageVitalEventRecord() { super(); }

    public MarriageVitalEventRecord(String names) {
        super();
        this.names = names;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }
}

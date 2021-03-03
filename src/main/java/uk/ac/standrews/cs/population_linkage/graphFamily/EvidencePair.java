package uk.ac.standrews.cs.population_linkage.graphFamily;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity
public class EvidencePair extends Evidence {

    @Property
    private VitalEventRecord record2;

    // Getters and Setters

    public EvidencePair(VitalEventRecord record, VitalEventRecord record2, double confidence, String link_type, String provenance, double distance) {
        super(record,confidence,provenance,distance);
        this.record2 = record2;
    }

    public VitalEventRecord getRecord2() {
        return record2;
    }

    public void setRecord2(VitalEventRecord role2) {
        this.record2 = record2;
    }
}

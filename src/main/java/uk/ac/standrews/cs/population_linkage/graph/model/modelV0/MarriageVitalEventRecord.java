/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */

package uk.ac.standrews.cs.population_linkage.graph.model.modelV0;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import uk.ac.standrews.cs.population_linkage.graph.model.modelV1.VitalEventRecord;

@NodeEntity
class MarriageVitalEventRecord extends VitalEventRecord {

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

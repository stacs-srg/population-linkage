/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.model.modelV0;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import uk.ac.standrews.cs.population_linkage.graph.model.modelV1.VitalEventRecord;

@NodeEntity
public class BirthVitalEventRecord extends VitalEventRecord {
    @Property
    private String first_names;
    @Property
    private String second_name;

    public BirthVitalEventRecord() { super(); }

    public BirthVitalEventRecord(String first_names, String second_name) {
        super();
        this.first_names = first_names;
        this.second_name = second_name;
    }

    public String getFirst_names() {
        return first_names;
    }

    public void setFirst_names(String first_names) {
        this.first_names = first_names;
    }

    public String getSecond_name() {
        return second_name;
    }

    public void setSecond_name(String second_name) {
        this.second_name = second_name;
    }

}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.model.NeoPlayground;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class PersonWithChums {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @Relationship(type = "CHUM", direction = Relationship.OUTGOING)  // CHUM is a label on the relationship arc
    Set<PersonWithChums> chums = new HashSet<>();

    public PersonWithChums(String name ) {
        this.name = name;
    }

    public void addChum(PersonWithChums p ) {
        chums.add( p );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for( PersonWithChums p : chums) {
            sb.append( p.getName() );
            sb.append(" ");
        }
        return "Chums = " + chums.size() + sb.toString();
    }

    private String getName() { return name; }

    Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }
}

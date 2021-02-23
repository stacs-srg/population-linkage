/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graphDbPlay;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Thing {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;

    // In Relationship there is:
    //    String TYPE = "type";
    //    String DIRECTION = "direction";
    //    String INCOMING = "INCOMING";
    //    String OUTGOING = "OUTGOING";
    //    String UNDIRECTED = "UNDIRECTED";

    @Relationship(direction = Relationship.UNDIRECTED )
    private Thing friend;

    public Thing( String name ) { this.name = name; }

    public String getName() {
        return name;
    }

    public void addFriend(Thing t) {
        this.friend = t;
    }

    Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graphDbPlay;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

@NodeEntity
public class Friends {

    @JsonProperty("id")
    private Long id;

    public Friends() {
    }

    public Friends(PersonWithFriends p ) {
        this();
        this.add( p );
    }

    @Relationship(type = "FRIEND") // , direction = Relationship.OUTGOING)
    List<PersonWithFriends> friends = new ArrayList<>();

    public void add( PersonWithFriends p ) {
        friends.add( p );
        // p.addFriend(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for( PersonWithFriends p : friends ) {
            sb.append( p );
            sb.append(" ");
        }
        return "Friends size= " + friends.size() + ", friends = " + sb.toString();
    }

    Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graphDbPlay;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class PersonWithFriends {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

//    @BlueNode(type = "FRIEND", direction = BlueNode.OUTGOING)  // FRIENDS is a label on the relationship arc
//    Friends friends;

    public PersonWithFriends(String name ) {
        this.name = name;
    }

//    public void addFriend( Friends f ) {
//        friends = f;
//    }

    @Override
    public String toString() {
        return id + ":" + name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {  return name; }
}

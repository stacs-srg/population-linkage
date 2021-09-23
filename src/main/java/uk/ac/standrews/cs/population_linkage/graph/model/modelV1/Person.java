package uk.ac.standrews.cs.population_linkage.graph.model.modelV1;

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
import org.neo4j.ogm.annotation.*;
import uk.ac.standrews.cs.population_records.record_types.Birth;

import java.util.ArrayList;
import java.util.List;

@NodeEntity
public class Person {
    @Id
    @GeneratedValue
    private Long id;

    @Property
    private String first_names;
    @Property
    private String second_name;

    @Relationship(type="MOTHER")
    private List<Reference> mothers = new ArrayList<>();				// alts and multiples

    @Relationship(type="FATHER")
    private List<Reference> fathers = new ArrayList<>();				// alts and multiples

    @Relationship(type="CHILD")
    private List<Reference> children = new ArrayList<>();				// alts and multiples

    @Relationship(type="COPARENT")
    private List<Reference> coparents = new ArrayList<>(); 				// alts and multiples

    @Relationship(type="SPOUSE")
    private List<Reference> spouses = new ArrayList<>(); 				// alts and multiples

    @Relationship(type="BIRTH")
    private List<Evidence> birth_records = new ArrayList<>();  			// alts

    @Relationship(type="DEATH")
    private List<Evidence> death_records = new ArrayList<>();  			// alts

    public Person() {}

    public Person(String first_name, String second_name) {
        this.first_names = first_name;
        this.second_name = second_name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirst_names() {
        return first_names;
    }

    public String getSecond_name() {
        return second_name;
    }

    public List<Reference> getChildren() {
        return children;
    }

    private void addMother(Reference parent) {
        this.mothers.add(parent);
    }

    private void addFather(Reference parent) {
        this.fathers.add(parent);
    }

    public List<Reference> getCoparents() {
        return coparents;
    }

    public void addCoparent(Reference parent) {
        this.coparents.add(parent);
    }

    public List<Reference> getSpouses() {
        return spouses;
    }

    public List<Evidence> getBirthRecords() {
        return birth_records;
    }

    public void addBirthRecord(Evidence birth_record) {
        this.birth_records.add(birth_record);
    }

    public List<Evidence> getDeathRecords() {
        return death_records;
    }

    public void addDeathRecord(Evidence death_record) {
        this.death_records.add(death_record);
    }

    public void addChild(Person child, String parent_role, Evidence evidence ) {
        Reference ref = new Reference(this, parent_role, child, "BABY",evidence );
        this.children.add( ref);
        if( parent_role.equals(Birth.ROLE_MOTHER ) ) {
            child.mothers.add(ref);
        } else {
            child.fathers.add(ref);
        }
    }

    public void addSpouse(Person bride_or_groom, String this_role, Evidence evidence) {
        Reference ref = new Reference(this, this_role, bride_or_groom, this_role.equals("GROOM") ? "BRIDE" : "GROOM", evidence);
        this.spouses.add( ref);
        bride_or_groom.spouses.add( ref );
    }
}
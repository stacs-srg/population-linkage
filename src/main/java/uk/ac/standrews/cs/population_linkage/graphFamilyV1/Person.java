package uk.ac.standrews.cs.population_linkage.graphFamilyV1;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

@NodeEntity
public class Person {
    @JsonProperty("id")   // These should be     @Id from org.neo4j.ogm.annotation.Id
    private Long id;

    @Property
    private String first_names;
    @Property
    private String second_name;

    @Relationship(type="CHILD")
    private List<PersonRef> children = new ArrayList<>();				// alts and multiples

    @Relationship(type="COPARENT")
    private List<PersonRef> coparents = new ArrayList<>(); 				// alts and multiples

    @Relationship(type="SPOUSE")
    private List<PersonRef> spouses = new ArrayList<>(); 				// alts and multiples

    @Relationship(type="BIRTH")
    private List<RecordProv> birth_record = new ArrayList<>();				// alts and multiples

    @Relationship(type="MARRIAGE")
    private List<RecordProv> mariage_records = new ArrayList<>(); 			// alts and multiples

    @Relationship(type="DEATH")
    private List<RecordProv> death_records = new ArrayList<>();  			// alts

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

    public List<PersonRef> getChildren() {
        return children;
    }

    public void addChild(PersonRef child) {
        this.children.add(child);
    }

    public List<PersonRef> getCoparents() {
        return coparents;
    }

    public void addCoparent(PersonRef parent) {
        this.coparents.add(parent);
    }

    public List<PersonRef> getSpouses() {
        return spouses;
    }

    public void addSpouse(PersonRef spouse) {
        this.spouses.add(spouse);
    }

    public List<RecordProv> getBirthRecord() {
        return birth_record;
    }

    public void addBirthRecord(RecordProv birth_record) {
        this.birth_record.add(birth_record);
    }

    public List<RecordProv> getMariageRecords() {
        return mariage_records;
    }

    public void addMarriageRecord(RecordProv marriage_record) {
        this.mariage_records.add(marriage_record);
    }

    public List<RecordProv> getDeathRecords() {
        return death_records;
    }

    public void addDeathRecord(RecordProv death_record) {
        this.birth_record.add(death_record);
    }
}

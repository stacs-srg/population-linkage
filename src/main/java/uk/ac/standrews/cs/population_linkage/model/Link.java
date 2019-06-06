package uk.ac.standrews.cs.population_linkage.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Link {

    private final Role role1;
    private final Role role2;
    private float confidence;
    private String link_type;
    private List<String> provenance;

    public Link(Role role1, Role role2, float confidence, String link_type, String... provenance) {

        this.role1 = role1;
        this.role2 = role2;
        this.confidence = confidence;
        this.link_type = link_type;
        this.provenance = Arrays.asList(provenance);
    }

    public Role getRole1() {
        return role1;
    }

    public Role getRole2() {
        return role2;
    }

    public float getConfidence() { return confidence; }

    public String getLinkType() { return link_type; }

    public List<String> getProvenance() { return provenance; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(role1, link.role1) &&
                Objects.equals(role2, link.role2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role1, role2);
    }

    @Override
    public String toString() {
        return role1 + " - " + role2;
    }
}

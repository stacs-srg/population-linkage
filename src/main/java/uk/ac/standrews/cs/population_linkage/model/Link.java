package uk.ac.standrews.cs.population_linkage.model;

import java.util.Objects;

public class Link {

    private final String id1;
    private final String id2;

    public Link(String id1, String id2) {

        this.id1 = id1;
        this.id2 = id2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(id1, link.id1) &&
                Objects.equals(id2, link.id2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2);
    }
}

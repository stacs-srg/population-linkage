package uk.ac.standrews.cs.population_linkage.model;

import java.util.Objects;

public class Role {

    private String record_id;
    private String role;

    public Role(String record_id, String role) {

        this.record_id = record_id;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role1 = (Role) o;
        return Objects.equals(record_id, role1.record_id) &&
                Objects.equals(role, role1.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(record_id, role);
    }
}

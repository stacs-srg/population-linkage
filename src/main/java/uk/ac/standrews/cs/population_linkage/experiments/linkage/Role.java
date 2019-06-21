package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.storr.interfaces.IStoreReference;

import java.util.Objects;

public class Role {

    private IStoreReference record_id;
    private String role_type;

    public Role(IStoreReference record_id, String role_type) {

        this.record_id = record_id;
        this.role_type = role_type;
    }

    public IStoreReference getRecordId() {

        return record_id;
    }

    public String getRoleType() {

        return role_type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role1 = (Role) o;
        return Objects.equals(record_id, role1.record_id) &&
                Objects.equals(role_type, role1.role_type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(record_id, role_type);
    }

    @Override
    public String toString() {
        return record_id.toString();
    }
}

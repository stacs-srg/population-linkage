package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.storr.impl.JPO;
import uk.ac.standrews.cs.storr.impl.JPOMetadata;
import uk.ac.standrews.cs.storr.interfaces.IStoreReference;
import uk.ac.standrews.cs.storr.types.JPO_FIELD;

import java.util.Objects;

public class Role extends JPO {

    @JPO_FIELD
    private IStoreReference record_id;
    @JPO_FIELD
    private String role_type;

    public Role() {}

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

    /* Storr support mechanism - ALL STORR JPO OBJECTS MUST HAVE THIS BOILERPLATE CODE */

    /*
     * This field is used to store the metadata for the class.
     */
    private static final JPOMetadata static_metadata;

    /*
     * This selector returns the class metadata.
     */
    @Override
    public JPOMetadata getMetaData() {
        return static_metadata;
    }

    /*
     * This static initialiser initialises the static meta data
     * The two parameters to the JPOMetadata constructor are the name of this class and the name the type is given in the store.
     */
    static {
        try {
            static_metadata = new JPOMetadata(Link.class,"JPOLink");
        } catch (Exception var1) {
            throw new RuntimeException(var1);
        }
    }
}

/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;


import uk.ac.standrews.cs.neoStorr.impl.JPO;
import uk.ac.standrews.cs.neoStorr.impl.JPOMetaData;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.LXPReference;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.interfaces.IStoreReference;
import uk.ac.standrews.cs.neoStorr.types.JPO_FIELD;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Link extends JPO {

    @JPO_FIELD
    private LXPReference<LXP> record1;
    @JPO_FIELD
    private String role1;
    @JPO_FIELD
    private LXPReference<LXP> record2;
    @JPO_FIELD
    private String role2;
    @JPO_FIELD
    private double confidence;
    @JPO_FIELD
    private String link_type;
    @JPO_FIELD
    private List<String> provenance;
    @JPO_FIELD
    private double distance;

    public Link() {}

    public Link(String id, Map map, IBucket bucket ) throws PersistentObjectException {
        super( id, map, bucket );
    }

    public Link(LXP record1, String role1, LXP record2, String role2, float confidence, String link_type, double distance, String... provenance) throws PersistentObjectException {

        this.record1 = (LXPReference) record1.getThisRef();
        this.role1 = role1;
        this.record2 = (LXPReference) record2.getThisRef();
        this.role2 = role2;
        this.confidence = confidence;
        this.link_type = link_type;
        this.provenance = Arrays.asList(provenance);
        this.distance = distance;
    }

    public IStoreReference<LXP> getRecord1() {
        return record1;
    }

    public String getRole1() {
        return role1;
    }

    public IStoreReference<LXP> getRecord2() {
        return record2;
    }

    public String getRole2() {
        return role2;
    }

    public double getConfidence() { return confidence; }

    public String getLinkType() { return link_type; }

    public List<String> getProvenance() { return provenance; }

    public double getDistance() { return distance; }

    public void setDistance(double distance) { this.distance = distance; }

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

    /* Storr support mechanism - ALL STORR JPO OBJECTS MUST HAVE THIS BOILERPLATE CODE */

    /*
     * This field is used to store the metadata for the class.
     */
    private static final JPOMetaData static_metadata;

    /*
     * This selector returns the class metadata.
     */
    @Override
    public JPOMetaData getJPOMetaData() {
        return static_metadata;
    }

    /*
     * This static initialiser initialises the static meta data
     * The two parameters to the JPOMetadata constructor are the name of this class and the name the type is given in the store.
     */
    static {
        try {
            static_metadata = new JPOMetaData(Link.class,"JPOLink");
        } catch (Exception var1) {
            throw new RuntimeException(var1);
        }
    }

}

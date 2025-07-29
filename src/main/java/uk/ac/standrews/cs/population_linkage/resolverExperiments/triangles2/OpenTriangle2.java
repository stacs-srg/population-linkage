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
package uk.ac.standrews.cs.population_linkage.resolverExperiments.triangles2;

import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

/**
 * Used to encode unmatched triangles with nodes x and y and z
 * x and y are xy_distance apart, y and z are yz_distance apart
 * but xz is not connected.
 * All the ids are storr ids of Nodes.
 */
public class OpenTriangle2 {
    public final Node x;
    public final Node y;
    public final Node z;
    public final Relationship xy;
    public final Relationship yz;

    public OpenTriangle2(Node x, Node y, Node z, Relationship xy, Relationship yz) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xy = xy;
        this.yz = yz;
    }

    public static double getDistance( Relationship r ) {
        return r.get("distance").asDouble();
    }

    public static String getStorrId( Node n ) {
        return n.get("STORR_ID").asString();
    }

    public static String getNeoId(Node n ) {
        return n.elementId();
    }

    public String toString() {
        return "X = " + getStorrId( x ) + " Y = " + getStorrId( y  ) + " Z = " + getStorrId( z ) + "\n" +
        " xy= " + getDistance( xy ) + " yz = " + getDistance( yz) ;
    }
}

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
package uk.ac.standrews.cs.population_linkage.resolverExperiments.msed;

import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

/**
 * Used to encode unmatched triangles with nodes x and y and z
 * x and y are xy_distance apart, y and z are yz_distance apart
 * but xz is not connected.
 * All the ids are storr ids of Nodes.
 */
public class LinkPair {
    public final Node x;
    public final Node y;
    public final Relationship xy;

    public LinkPair(Node x, Node y, Relationship xy) {
        this.x = x;
        this.y = y;
        this.xy = xy;
    }

    public static double getDistance( Relationship r ) {
        return r.get("distance").asDouble();
    }

    public static String getStorrId( Node n ) {
        return n.get("STORR_ID").asString();
    }

    public String toString() {
        return "X = " + getStorrId( x ) + " Y = " + getStorrId( y  ) + "\n" + " xy= " + getDistance( xy )  ;
    }
}

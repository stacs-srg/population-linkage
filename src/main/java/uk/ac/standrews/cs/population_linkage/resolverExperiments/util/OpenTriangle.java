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
package uk.ac.standrews.cs.population_linkage.resolverExperiments.util;

/**
 * Used to encode unmatched triangles with nodes x and y and z
 * x and y are xy_distance apart, y and z are yz_distance apart
 * but xz is not connected.
 * All the ids are storr ids of Nodes.
 */
public class OpenTriangle {
    public final long x;
    public final long y;
    public final long z;
    public final double xy_distance;
    public final double yz_distance;


    public OpenTriangle(long x, long y, long z, double xy_distance, double yz_distance ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xy_distance = xy_distance;
        this.yz_distance = yz_distance;
    }

    public String toString() {
        return "X = " + x + " Y = " + y + " Z = " + z + "\n" +
        " xy= " + xy_distance + " yz = " + yz_distance;
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.resolver.util;

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

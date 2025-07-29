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
 * Used to encode twp unmatched triangles with nodes t1,t2,t3 and s1,s2,s3
 * t1 and s1 are t1s1_distance apart, t2 and s2 are t2s2_distance apart
 * but t3 and s3 is not connected.
 * All the ids are storr ids of Nodes.
 */
public class TwoTriangles {
    public final String t1;
    public final String t2;
    public final String t3;
    public final String s1;
    public final String s2;
    public final String s3;


    public TwoTriangles(String t1, String t2, String t3, String s1, String s2, String s3 ) {
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
    }

    public String toString() {
        return "t1 = " + t1 + " t2 = " + t2 + " t3 = " + t3 + "\n" +
                "s1 = " + s1 + " s2 = " + s2 + " s3 = " + s3 + "\n";
    }
}

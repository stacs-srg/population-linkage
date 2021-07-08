/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.resolver.util;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_records.record_types.Birth;

public class UsefulCodeUnused {


    private int TP;
    private int cut_count;

    // Cut the biggest distance = perhaps use fields
    private void cutLinks(OpenTriangle open_triangle, LXP b_x, LXP b_y, LXP b_z) {   // TODO Cut both??? either 0 or 3.
        // System.out.println("Would DO CUT XY");
        if (!b_x.getString(Birth.FATHER_IDENTITY).equals(b_y.getString(Birth.FATHER_IDENTITY))) {
            TP++;
        }
        cut_count++;
        // System.out.println("Would DO CUT YZ");
        if (!b_y.getString(Birth.FATHER_IDENTITY).equals(b_z.getString(Birth.FATHER_IDENTITY))) {
            TP++;
        }
        cut_count++;
    }

    // Cut the biggest distance = perhaps use fields??
    private void cutOne( OpenTriangle open_triangle, LXP b_x, LXP b_y, LXP b_z) {
        if (open_triangle.xy_distance > open_triangle.yz_distance) {
            System.out.println("Would DO CUT XY");
            if (!b_x.getString(Birth.FATHER_IDENTITY).equals(b_y.getString(Birth.FATHER_IDENTITY))) {
                TP++;
            }
            cut_count++;
        } else {
            System.out.println("Would DO CUT YZ");
            if (!b_y.getString(Birth.FATHER_IDENTITY).equals(b_z.getString(Birth.FATHER_IDENTITY))) {
                TP++;
            }
            cut_count++;
        }
    }
    
}

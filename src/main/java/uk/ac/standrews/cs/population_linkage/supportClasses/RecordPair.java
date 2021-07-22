/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;


import uk.ac.standrews.cs.neoStorr.impl.LXP;

public class RecordPair {

    public LXP record1;     // This is the stored type? TODO relabel these? - al
    public LXP record2;     // This is the query type?
    public double distance;

    public RecordPair(LXP record1, LXP record2, double distance) {

        this.record1 = record1;
        this.record2 = record2;
        this.distance = distance;
    }

    public String toString() {

        return "{" + record1 + ", " + record2 + "}";
    }
}

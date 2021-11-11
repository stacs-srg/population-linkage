/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;


import uk.ac.standrews.cs.neoStorr.impl.LXP;

public class RecordPair {

    public LXP stored_record;
    public LXP query_record;
    public double distance;

    public RecordPair(LXP stored_record, LXP query_record, double distance) {

        this.stored_record = stored_record;
        this.query_record = query_record;
        this.distance = distance;
    }

    public String toString() {

        return "{" + stored_record + ", " + query_record + "}";
    }
}

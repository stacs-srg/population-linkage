/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.util;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Util {

    private static final double CLOSE_TO_ZERO = 0.0000001;

    /**
     * Extracts the LXPs from links and returns a set
     * @param this_family
     * @return
     */
    public static Set<LXP> getBirthSiblings(List<Link> this_family) throws BucketException, RepositoryException {
        Set<LXP> results = new TreeSet<>();
        for( Link l : this_family ) {
            results.add( l.getRecord1().getReferend() );
            results.add( l.getRecord2().getReferend());
        }
        return results;
    }

    public static ArrayList<LXP> toArray(Iterable<LXP> filtered_source_records) {
        if( filtered_source_records instanceof ArrayList ) {
            return (ArrayList<LXP>) filtered_source_records;
        } else {
            ArrayList<LXP> result = new ArrayList<>();
            for( LXP record : filtered_source_records ) { result.add(record); }
            return result;
        }
    }

    public static boolean closeTo(double distance, double target) {
        return Math.abs(target - distance) < CLOSE_TO_ZERO;
    }

}

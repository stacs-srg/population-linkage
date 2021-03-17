package uk.ac.standrews.cs.population_linkage.EndtoEnd.runners;

import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Util {

    /**
     * Extracts the LXPs from links and returns a set
     * @param this_family
     * @return
     */
    public static Set<LXP> getBirthSiblings(List<Link> this_family) throws BucketException {
        Set<LXP> results = new TreeSet<>();
        for( Link l : this_family ) {
            results.add( l.getRecord1().getReferend() );
            results.add( l.getRecord2().getReferend());
        }
        return results;
    }


}

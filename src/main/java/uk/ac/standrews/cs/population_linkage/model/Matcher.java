package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.storr.impl.LXP;

public interface Matcher {

    boolean match(LXP record1, LXP record2);
}

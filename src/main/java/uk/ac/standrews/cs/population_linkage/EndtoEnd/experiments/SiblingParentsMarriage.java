/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.experiments;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;

import java.util.List;

public class SiblingParentsMarriage {

    public LXP sibling;
    public List<DataDistance<LXP>> parents_marriages;

    public SiblingParentsMarriage(LXP sibling, List<DataDistance<LXP>> parents_marriages) {
        this.sibling = sibling;
        this.parents_marriages = parents_marriages;
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.builders;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;

import java.util.List;

public class SiblingDeath {

    public LXP sibling_birth_record;
    public List<DataDistance<LXP>> deaths;

    public SiblingDeath(LXP sibling, List<DataDistance<LXP>> deaths) {
        this.sibling_birth_record = sibling;
        this.deaths = deaths;
    }
}

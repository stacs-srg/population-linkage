/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMetrics.SigmaNotNorm;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

/**
 * Links a person appearing as the child on a birth record with the same person appearing as the deceased on a death record.
 */
public class BirthDeathIdentityLinkageRecipeNotNorm extends BirthDeathIdentityLinkageRecipe {

    public BirthDeathIdentityLinkageRecipeNotNorm(String source_repository_name, String number_of_records, String links_persistent_name, NeoDbCypherBridge bridge) {
        super(source_repository_name, number_of_records, links_persistent_name, bridge);
        THRESHOLD = 0.612903226;
    }


    @Override
    public Metric<LXP> getCompositeMetric() {
        return new SigmaNotNorm( getBaseMetric(),getLinkageFields(),ID_FIELD_INDEX1 );
    }
}

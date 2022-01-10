/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.missingData.recipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.compositeMetrics.SigmaMeanOfPresent;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

/*
 * Created by al on 30/9/2021
 */

public class BDLinkageRecipeMeanOfPresent extends BirthDeathIdentityLinkageRecipe {

    public BDLinkageRecipeMeanOfPresent(String source_repository_name, String number_of_records, String links_persistent_name, NeoDbCypherBridge bridge) {
        super(source_repository_name, number_of_records, links_persistent_name, bridge);
    }

    @Override
    public Metric<LXP> getCompositeMetric() {
        return new SigmaMeanOfPresent(getBaseMetric(), getLinkageFields(), Birth.STANDARDISED_ID);
    }
}

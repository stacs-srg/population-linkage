/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.missingData.linkageRunners;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.missingData.compositeMetrics.SigmaIntolerant;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

/*
 * Created by al on 30/9/2021
 */

public class BBLinkageRunnerMean extends BitBlasterLinkageRunner {

    @Override
    protected Metric<LXP> getCompositeMetric(final LinkageRecipe linkageRecipe) {
        return new SigmaIntolerant(getBaseMetric(), linkageRecipe.getLinkageFields(), 0);
    }

}

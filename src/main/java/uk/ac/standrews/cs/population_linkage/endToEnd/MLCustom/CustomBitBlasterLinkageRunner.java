/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.MLCustom;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class CustomBitBlasterLinkageRunner extends BitBlasterLinkageRunner {

    @Override
    protected Metric<LXP> getCompositeMetric(final LinkageRecipe linkageRecipe) {

        return new CustomMetric(0);
    }
}

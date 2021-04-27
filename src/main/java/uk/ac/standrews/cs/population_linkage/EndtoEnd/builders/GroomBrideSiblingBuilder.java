/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.builders;

import uk.ac.standrews.cs.population_linkage.EndtoEnd.SubsetRecipies.GroomBrideSubsetSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

/**
 * This class attempts to perform marriage-marriage sibling linkage.
 */
public class GroomBrideSiblingBuilder {

    public static void main(String[] args) throws Exception {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        try( NeoDbCypherBridge bridge = new NeoDbCypherBridge(); ) {

            GroomBrideSubsetSiblingLinkageRecipe linkageRecipe = new GroomBrideSubsetSiblingLinkageRecipe(sourceRepo, resultsRepo, bridge, GroomBrideSiblingBuilder.class.getCanonicalName());

            BitBlasterLinkageRunner runner = new BitBlasterLinkageRunner();
            LinkageResult lr = runner.run(linkageRecipe, new JensenShannon(2048),false, false, true, true);

            LinkageQuality quality = lr.getLinkageQuality();
            quality.print(System.out);
        }
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.builders;

import uk.ac.standrews.cs.population_linkage.EndtoEnd.SubsetRecipies.BirthSiblingSubsetLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

/**
 * This class attempts to perform birth-birth sibling linkage.
 */
public class BirthSiblingBundleBuilder {

    public static void main(String[] args) throws Exception {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        try( NeoDbCypherBridge bridge = new NeoDbCypherBridge(); ) {

            BirthSiblingSubsetLinkageRecipe linkageRecipe = new BirthSiblingSubsetLinkageRecipe(sourceRepo, resultsRepo, bridge, BirthSiblingBundleBuilder.class.getCanonicalName());

            BitBlasterLinkageRunner runner = new BitBlasterLinkageRunner();
            LinkageResult lr = runner.run(linkageRecipe, new JensenShannon(2048),false, false, false, true);

            LinkageQuality quality = lr.getLinkageQuality();
            quality.print(System.out);
        } finally {
            System.out.println("Run finished");
            System.exit(0); // make sure process dies.
        }
    }

}

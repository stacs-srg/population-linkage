/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.builders;

import uk.ac.standrews.cs.population_linkage.EndtoEnd.Recipies.DeathSiblingSubsetLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

/**
 * This class attempts to perform birth-birth sibling linkage.
 * It creates a Map of families indexed (at the momement TODO) from birth ids to families
 */
public class DeathSiblingBundleExperiment {

    public static final int PREFILTER_REQUIRED_FIELDS = 4;
    private static final double DISTANCE_THRESHOLD = 0.53;

    public static void main(String[] args) throws Exception {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        try( NeoDbCypherBridge bridge = new NeoDbCypherBridge(); ) {

            DeathSiblingSubsetLinkageRecipe linkageRecipe = new DeathSiblingSubsetLinkageRecipe(sourceRepo, resultsRepo, bridge, DeathSiblingSubsetLinkageRecipe.LINKAGE_TYPE + "-links", PREFILTER_REQUIRED_FIELDS);

            BitBlasterLinkageRunner runner = new BitBlasterLinkageRunner();
            LinkageResult lr = runner.run(linkageRecipe, new JensenShannon(2048), DISTANCE_THRESHOLD, true, PREFILTER_REQUIRED_FIELDS, false, false, true, true);

            LinkageQuality quality = lr.getLinkageQuality();
            quality.print(System.out);
        } finally {
            System.out.println( "Run finished" );
            System.exit(1); // make sure process dies.
        }
    }
}

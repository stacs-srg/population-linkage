/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.builders;

import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.TestLinkExistsRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;

/**
 *  This class attempts to find death-groom links: links a deceased on a death to the same person as a groom on a marriage.
 *  This is NOT STRONG: uses the 3 names: the groom/deceased and the names of the mother and father.
 */
public class TestBuilder {

    public static void main(String[] args) throws Exception {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {
            LinkageRecipe linkageRecipe = new TestLinkExistsRecipe(sourceRepo, bridge, TestBuilder.class.getCanonicalName() );

            new BitBlasterLinkageRunner().run(linkageRecipe, null, true, true);
        } finally {
            System.out.println( "Run finished" );
            System.exit(0); // Make sure it all shuts down properly.
        }
    }
}

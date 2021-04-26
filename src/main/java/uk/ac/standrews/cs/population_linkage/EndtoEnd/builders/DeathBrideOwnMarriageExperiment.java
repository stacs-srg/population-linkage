/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.builders;

import uk.ac.standrews.cs.population_linkage.EndtoEnd.Recipies.DeathBrideIdentitySubsetLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

/**
 *  This class attempts to find death-bride links: links a deceased on a death to the same person as a bride on a marriage.
 *  This is NOT STRONG: uses the 3 names: the groom/deceased and the names of the mother and father.
 */
public class DeathBrideOwnMarriageExperiment {

    private static final int PREFILTER_FIELDS = 6; // 6 is all of them but not occupation - FORENAME,SURNAME,FATHER_FORENAME,FATHER_SURNAME,MOTHER_FORENAME,MOTHER_SURNAME
    public static final double THRESHOLD = 0.4;

    public static void main(String[] args) throws Exception {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge(); ) {
            LinkageRecipe linkageRecipe = new DeathBrideIdentitySubsetLinkageRecipe(sourceRepo, resultsRepo, bridge,DeathBrideIdentitySubsetLinkageRecipe.LINKAGE_TYPE + "-links", PREFILTER_FIELDS);

            LinkageConfig.numberOfROs = 20;

            new BitBlasterLinkageRunner().run(linkageRecipe, new JensenShannon(2048), THRESHOLD, false, PREFILTER_FIELDS, false, false, true, false);
        } finally {
            System.out.println( "Run finished" );
        }
    }
}

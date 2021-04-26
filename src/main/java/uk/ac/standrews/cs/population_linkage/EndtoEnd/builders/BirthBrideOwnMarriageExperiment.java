/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.builders;

import uk.ac.standrews.cs.population_linkage.EndtoEnd.Recipies.BirthBrideIdentitySubsetLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthBrideIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

/**
 *  This class attempts to find birth-groom links: links a baby on a birth to the same person as a groom on a marriage.
 *  This is NOT STRONG: uses the 3 names: the groom/baby and the names of the mother and father.
 */
public class BirthBrideOwnMarriageExperiment {

    private static final int PREFILTER_REQUIRED_FIELDS = 6; // 6 is all of them
    private static final double DISTANCE_THRESHOLD = 0.49;

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge(); ) {
            LinkageRecipe linkageRecipe = new BirthBrideIdentitySubsetLinkageRecipe(sourceRepo, resultsRepo, bridge, BirthBrideIdentityLinkageRecipe.LINKAGE_TYPE + "-links", PREFILTER_REQUIRED_FIELDS);

            LinkageConfig.numberOfROs = 20;

            new BitBlasterLinkageRunner().run(linkageRecipe, new JensenShannon(2048), DISTANCE_THRESHOLD, false, PREFILTER_REQUIRED_FIELDS, false, false, true, false);
        } catch (Exception e) {
            System.out.println( "Exception closing bridge" );
        } finally {
            System.out.println( "Run finished" );
        }
    }
}

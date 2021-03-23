/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.experiments;

import uk.ac.standrews.cs.population_linkage.EndtoEnd.runners.BitBlasterSubsetOfDataEndtoEndBirthDeathLinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

/**
 *  This class attempts to find birth-groom links: links a baby on a birth to the same person as a groom on a marriage.
 *  This is NOT STRONG: uses the 3 names: the groom/baby and the names of the mother and father.
 */
public class BirthOwnDeathExperiment {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe linkageRecipe = new BirthDeathIdentityLinkageRecipe(sourceRepo, resultsRepo, BirthDeathIdentityLinkageRecipe.LINKAGE_TYPE + "-links");

        LinkageConfig.numberOfROs = 20;
        // was 0.67
        new BitBlasterSubsetOfDataEndtoEndBirthDeathLinkageRunner().run(linkageRecipe, new JensenShannon(2048), 0.38, true, 6, false, false, false, false);
    }
}

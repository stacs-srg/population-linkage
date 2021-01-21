/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.unused.BirthParentsMarriageLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

/**
 *  This class attempts to find birth-groom links: links a baby on a birth to the same person as a groom on a marriage.
 *  This is NOT STRONG: uses the 3 names: the groom/bany and the names of the mother and father.
 */
public class BirthParentsMarriageExperimentAl {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe linkageRecipe = new BirthParentsMarriageLinkageRecipe(sourceRepo, resultsRepo, BirthParentsMarriageLinkageRecipe.LINKAGE_TYPE + "-links");

        LinkageConfig.numberOfROs = 20;

        new BitBlasterSubsetOfDataEndtoEndBirthParentsMarriageLinkageRunner().run(linkageRecipe, new JensenShannon(2048), 0.67, true, 8, false, false, false, false);

        System.exit(1); // shut down the VM - stop BB running.
    }
}

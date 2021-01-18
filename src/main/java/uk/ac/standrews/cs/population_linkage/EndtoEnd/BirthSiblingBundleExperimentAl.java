/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

/**
 * This class attempts to perform birth-birth sibling linkage.
 * It creates a Map of families indexed (at the momement TODO) from birth ids to families
 */
public class BirthSiblingBundleExperimentAl extends BirthSiblingLinkageRecipe {

    public BirthSiblingBundleExperimentAl(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
    }

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe linkageRecipe = new BirthSiblingLinkageRecipe(sourceRepo, resultsRepo, LINKAGE_TYPE + "-links");

        new AlBitBlasterEndtoEndLinkageRunner().run(linkageRecipe, new JensenShannon(2048), 0.67, true, 8, false, false, false, false);
// 8 fields is all of them => very conservative.
//        Birth.FATHER_FORENAME,
//        Birth.FATHER_SURNAME,
//        Birth.MOTHER_FORENAME,
//        Birth.MOTHER_MAIDEN_SURNAME,
//        Birth.PARENTS_PLACE_OF_MARRIAGE,
//        Birth.PARENTS_DAY_OF_MARRIAGE,
//        Birth.PARENTS_MONTH_OF_MARRIAGE,
//        Birth.PARENTS_YEAR_OF_MARRIAGE
        System.exit(1); // shut down the VM - stop BB running.
    }
}

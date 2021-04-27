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
            LinkageResult lr = runner.run(linkageRecipe, new JensenShannon(2048),false, false, true, false);

            LinkageQuality quality = lr.getLinkageQuality();
            quality.print(System.out);
        }

        // 8 fields is all of them => very conservative.
//        Birth.FATHER_FORENAME,
//        Birth.FATHER_SURNAME,
//        Birth.MOTHER_FORENAME,
//        Birth.MOTHER_MAIDEN_SURNAME,
//        Birth.PARENTS_PLACE_OF_MARRIAGE,
//        Birth.PARENTS_DAY_OF_MARRIAGE,
//        Birth.PARENTS_MONTH_OF_MARRIAGE,
//        Birth.PARENTS_YEAR_OF_MARRIAGE
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.builders;

import uk.ac.standrews.cs.population_linkage.EndtoEnd.Recipies.BirthSiblingSubsetLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

/**
 * This class attempts to perform birth-birth sibling linkage.
 * It creates a Map of families indexed (at the momement TODO) from birth ids to families
 */
public class BirthSiblingBundleExperiment {

    public static final int PREFILTER_REQUIRED_FIELDS = 8;
    private static final double DISTANCE_THRESHOLD = 0.67;

    public static void main(String[] args) throws Exception {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        try( NeoDbCypherBridge bridge = new NeoDbCypherBridge(); ) {

            BirthSiblingSubsetLinkageRecipe linkageRecipe = new BirthSiblingSubsetLinkageRecipe(sourceRepo, resultsRepo, bridge, BirthSiblingSubsetLinkageRecipe.LINKAGE_TYPE + "-links", PREFILTER_REQUIRED_FIELDS);

            BitBlasterLinkageRunner runner = new BitBlasterLinkageRunner();
            LinkageResult lr = runner.run(linkageRecipe, new JensenShannon(2048), DISTANCE_THRESHOLD, true, PREFILTER_REQUIRED_FIELDS, false, false, true, false);

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

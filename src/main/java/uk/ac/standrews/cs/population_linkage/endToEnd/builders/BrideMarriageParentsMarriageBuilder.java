/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.builders;

import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.endToEnd.subsetRecipes.BrideMarriageParentsMarriageSubsetIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;

/**
 *  This class attempts to find marriage-marriage links: links a groom's parents on a marriage record to the parents marriage.
 *  This not STRONG: uses the 2 names of the mother and father plus Father's OCC.
 */
public class BrideMarriageParentsMarriageBuilder {

    public static void main(String[] args) throws Exception {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge();) {

            BrideMarriageParentsMarriageSubsetIdentityLinkageRecipe linkageRecipe = new BrideMarriageParentsMarriageSubsetIdentityLinkageRecipe(sourceRepo, resultsRepo, bridge, BrideMarriageParentsMarriageBuilder.class.getCanonicalName());

            LinkageConfig.numberOfROs = 20;

            int linkage_fields = linkageRecipe.ALL_LINKAGE_FIELDS;
            int half_fields = linkage_fields - (linkage_fields / 2 ) + 1;

            while( linkage_fields >= half_fields ) {
                linkageRecipe.setNumberLinkageFieldsRequired(linkage_fields);

                LinkageResult lr = new BitBlasterLinkageRunner().run(linkageRecipe, false, false, false, false);

                LinkageQuality quality = lr.getLinkageQuality();
                quality.print(System.out);
                linkage_fields--;
            }
        } catch (Exception e) {
            System.out.println( "Runtime exception:" );
            e.printStackTrace();
        } finally {
            System.out.println("Run finished");
            System.exit(0); // make sure process dies.
        }
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.builders;

import uk.ac.standrews.cs.population_linkage.endToEnd.subsetRecipes.DeathSiblingSubsetLinkageRecipe;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;

/**
 * This class attempts to perform birth-birth sibling linkage.
 * It creates a Map of families indexed (at the momement) from birth ids to families
 */
public class DeathSiblingBundleBuilder {

    public static void main(String[] args) throws Exception {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. EVERYTHING or 10000 etc.

        try( NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {

            DeathSiblingSubsetLinkageRecipe linkageRecipe = new DeathSiblingSubsetLinkageRecipe(sourceRepo, number_of_records, bridge, DeathSiblingBundleBuilder.class.getCanonicalName());

            int linkage_fields = linkageRecipe.ALL_LINKAGE_FIELDS;
            int half_fields = linkage_fields - (linkage_fields / 2 ) + 1;

            while( linkage_fields >= half_fields ) {
                BitBlasterLinkageRunner runner = new BitBlasterLinkageRunner();
                LinkageResult lr = runner.run(linkageRecipe, false, false, false, true);

                LinkageQuality quality = lr.getLinkageQuality();
                quality.print(System.out);

                linkage_fields--;
            }
        } catch (Exception e) {
            System.out.println( "Runtime exception:" );
            e.printStackTrace();
        } finally {
            System.out.println( "Run finished" );
            System.exit(0); // make sure process dies.
        }
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.builders;

import uk.ac.standrews.cs.population_linkage.endToEnd.subsetRecipes.DeathGroomIdentitySubsetLinkageRecipe;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;

/**
 *  This class attempts to find death-groom links: links a deceased on a death to the same person as a groom on a marriage.
 *  This is NOT STRONG: uses the 3 names: the groom/deceased and the names of the mother and father.
 */
public class DeathGroomOwnMarriageBuilder {

    public static void main(String[] args) throws Exception {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {
            DeathGroomIdentitySubsetLinkageRecipe linkageRecipe = new DeathGroomIdentitySubsetLinkageRecipe(sourceRepo, resultsRepo, bridge,DeathGroomOwnMarriageBuilder.class.getCanonicalName() );

            LinkageConfig.numberOfROs = 20;

            int linkage_fields = linkageRecipe.ALL_LINKAGE_FIELDS;
            int half_fields = linkage_fields - (linkage_fields / 2 ) + 1;

            while( linkage_fields >= half_fields ) {
                linkageRecipe.setNumberLinkageFieldsRequired(linkage_fields);

                new BitBlasterLinkageRunner().run(linkageRecipe, false, false, false, false);
                linkage_fields--;
            }
        } catch (Exception e) {
            System.out.println( "Runtime exception:" );
            e.printStackTrace();
        } finally {
            System.out.println( "Run finished" );
            System.exit(0); // Make sure it all shuts down properly.
        }
    }
}

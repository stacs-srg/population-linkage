/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.builders;

import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.endToEnd.subsetRecipes.BirthDeathSubsetIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

/**
 *  This class attempts to find birth-death links: links a baby on a birth to the same person as the deceased on a death record.
 *  This is NOT STRONG: uses the 3 names: the groom/baby and the names of the mother and father.
 */
public class BirthOwnDeathBuilder {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. EVERYTHING or 10000 etc.

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {
            BirthDeathSubsetIdentityLinkageRecipe linkageRecipe = new BirthDeathSubsetIdentityLinkageRecipe(sourceRepo, number_of_records, bridge, BirthOwnDeathBuilder.class.getCanonicalName());

            LinkageConfig.numberOfROs = 20;

            int linkage_fields = linkageRecipe.ALL_LINKAGE_FIELDS;
            int half_fields = linkage_fields - (linkage_fields / 2 ) + 1;

            while( linkage_fields >= half_fields ) {
                linkageRecipe.setNumberLinkageFieldsRequired(linkage_fields);
                new BitBlasterLinkageRunner().run(linkageRecipe, false, false, false, true);

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

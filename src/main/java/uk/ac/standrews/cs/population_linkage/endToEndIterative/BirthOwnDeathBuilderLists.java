/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEndIterative;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.graph.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRunners.MakePersistent;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;

/**
 *  This class attempts to find birth-death links: links a baby on a birth to the same person as the deceased on a death record.
 *  This is NOT STRONG: uses the 3 names: the groom/baby and the names of the mother and father.
 */
public class BirthOwnDeathBuilderLists implements MakePersistent {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. EVERYTHING or 10000 etc.

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {
            BirthDeathIdentityLinkageRecipe linkageRecipe = new BirthDeathIdentityLinkageRecipe(sourceRepo, number_of_records, BirthOwnDeathBuilderLists.class.getCanonicalName(), bridge);
            linkageRecipe.setNumberLinkageFieldsRequired(0); // No restrictions on fields
            new BitBlasterLinkageRunner().run2(linkageRecipe, new BirthOwnDeathBuilderLists(), false, true, true);

        } catch (Exception e) {
            System.out.println( "Runtime exception:" );
            e.printStackTrace();
        } finally {
            System.out.println( "Run finished" );
            System.exit(0); // Make sure it all shuts down properly.
        }
    }

    @Override
    public void makePersistent(LinkageRecipe recipe, Link link) {
        try {
            final String std_id1 = link.getRecord1().getReferend().getString(Birth.STANDARDISED_ID);
            final String std_id2 = link.getRecord2().getReferend().getString(Death.STANDARDISED_ID);

            if( ! Query.BDDeathReferenceExists(recipe.getBridge(), std_id1, std_id2, recipe.getLinks_persistent_name())) {

                Query.createBDReference(
                        recipe.getBridge(),
                        std_id1,
                        std_id2,
                        recipe.getLinks_persistent_name(),
                        recipe.getNoLinkageFieldsRequired(),
                        link.getDistance());
            }

        } catch (BucketException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}

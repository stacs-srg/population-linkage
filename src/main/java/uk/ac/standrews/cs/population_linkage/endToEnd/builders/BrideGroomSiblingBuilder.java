/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.builders;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.graph.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BrideGroomSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRunners.MakePersistent;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

/**
 * This class attempts to perform marriage-marriage sibling linkage.
 */
public class BrideGroomSiblingBuilder implements MakePersistent {

    public static void main(String[] args) throws Exception {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. EVERYTHING or 10000 etc.

        try( NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {

            BrideGroomSiblingLinkageRecipe linkageRecipe = new BrideGroomSiblingLinkageRecipe(sourceRepo, number_of_records, BrideGroomSiblingBuilder.class.getName(), bridge);

            BitBlasterLinkageRunner runner = new BitBlasterLinkageRunner();

            int linkage_fields = linkageRecipe.ALL_LINKAGE_FIELDS;
            int half_fields = linkage_fields - (linkage_fields / 2 ) + 1;

            while( linkage_fields >= half_fields ) {
                LinkageResult lr = runner.run(linkageRecipe, new BrideGroomSiblingBuilder(), false, true);

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

    public void makePersistent(LinkageRecipe recipe,Link link) {
        try {
            final String std_id1 = link.getRecord1().getReferend().getString(Marriage.STANDARDISED_ID);
            final String std_id2 = link.getRecord2().getReferend().getString(Marriage.STANDARDISED_ID);

            if( ! Query.MMBrideGroomSiblingReferenceExists(recipe.getBridge(), std_id1, std_id2, recipe.getLinksPersistentName())) {
                Query.createMMBrideGroomSiblingReference(
                        recipe.getBridge(),
                        std_id1,
                        std_id2,
                        recipe.getLinksPersistentName(),
                        recipe.getNumberOfLinkageFieldsRequired(),
                        link.getDistance());
            }
        } catch (BucketException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}

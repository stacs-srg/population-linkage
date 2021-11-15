/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.builders;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.graph.model.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.GroomGroomIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRunners.MakePersistent;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

/**
 *  This class attempts to find bride-bride links: links a bride on wedding to another bride on a wedding
 *  Multiple marriages of a single party (the bride).
 *  This is  STRONG.
 */
public class GroomGroomIdentityBuilder implements MakePersistent {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. EVERYTHING or 10000 etc.

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {
            GroomGroomIdentityLinkageRecipe linkageRecipe = new GroomGroomIdentityLinkageRecipe(sourceRepo, number_of_records, GroomGroomIdentityBuilder.class.getCanonicalName(), bridge);

            int linkage_fields = linkageRecipe.ALL_LINKAGE_FIELDS;
            int half_fields = linkage_fields - (linkage_fields / 2 ) + 1;

            while( linkage_fields >= half_fields ) {
                linkageRecipe.setNumberLinkageFieldsRequired(linkage_fields);
                new BitBlasterLinkageRunner().run(linkageRecipe, new GroomGroomIdentityBuilder(), false, false, false, false);

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

    public void makePersistent(LinkageRecipe recipe, Link link) {
        try {
            final String std_id1 = link.getRecord1().getReferend().getString(Marriage.STANDARDISED_ID);
            final String std_id2 = link.getRecord2().getReferend().getString(Marriage.STANDARDISED_ID);

            if( ! Query.MMGroomGroomIdReferenceExists(recipe.getBridge(), std_id1, std_id2, recipe.getLinks_persistent_name())) {

                Query.createMMGroomGroomIdReference(
                        recipe.getBridge(),
                        std_id1,
                        std_id2,
                        recipe.getLinks_persistent_name(),
                        recipe.getNoLinkageFieldsRequired(),
                        link.getDistance());
            }
        } catch (uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}

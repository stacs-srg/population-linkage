/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEndIterative;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.graph.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.MakePersistent;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

import java.util.ArrayList;
import java.util.List;

/**
 *  This class attempts to find bride-bride links: links a bride on wedding to another bride on a wedding
 *  Multiple marriages of a single party (the bride).
 *  This is  STRONG.
 */
public class BirthBrideIdentityBuilder implements MakePersistent {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. EVERYTHING or 10000 etc.

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {

            List<LXP> search_matched = new ArrayList<>();
            List<LXP> stored_matched = new ArrayList<>();

            for( double thresh = 0.001; thresh < BirthBrideIdentityLinkageRecipeSpecial.DISTANCE_THRESHOLD; thresh += 0.05 ) {

                BirthBrideIdentityLinkageRecipeSpecial linkageRecipe = new BirthBrideIdentityLinkageRecipeSpecial(sourceRepo, number_of_records, search_matched, stored_matched, BirthBrideIdentityBuilder.class.getCanonicalName(), thresh, bridge);

                linkageRecipe.setNumberLinkageFieldsRequired(linkageRecipe.ALL_LINKAGE_FIELDS);

                LinkageResultSpecial lrs = (LinkageResultSpecial) new BitBlasterLinkageRunnerSpecial().run(linkageRecipe, new BirthBrideIdentityBuilder(), true, true);
                search_matched.addAll( lrs.getLinkedSearchRecords() );
                stored_matched.addAll( lrs.getLinkedStoredRecords() );
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

            if( ! Query.MMBrideBrideIdReferenceExists(recipe.getBridge(), std_id1, std_id2, recipe.getLinks_persistent_name())) {

                Query.createMMBrideBrideIdReference(
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

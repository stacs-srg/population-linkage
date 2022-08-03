/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEndIterative;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.graph.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthBrideIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRunners.MakePersistent;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

/**
 * This class attempts to find birth-bride links: links a bride on wedding to another bride on a wedding
 * Multiple marriages of a single party (the bride).
 * This is  STRONG.
 */
public class BirthBrideIdentityBuilderLists2 implements MakePersistent {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. EVERYTHING or 10000 etc.

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {

 //           BirthBrideIdentityLinkageRecipeLol linkageRecipe = new BirthBrideIdentityLinkageRecipeLol(sourceRepo, number_of_records, BirthBrideIdentityBuilderIterative.class.getName(), bridge);

            BirthBrideIdentityLinkageRecipe linkageRecipe = new BirthBrideIdentityLinkageRecipe(sourceRepo, number_of_records, BirthBrideIdentityBuilderIterative.class.getName(), bridge);
            linkageRecipe.setNumberLinkageFieldsRequired(0); // No restrictions on fields
            BitBlasterLinkageRunner bb = new BitBlasterLinkageRunner();
            LinkageResult lrs = bb.investigateRun(linkageRecipe, new BirthBrideIdentityBuilderLists2(), true, false, true, bridge);

        } catch (Exception e) {
            System.out.println("Runtime exception:");
                e.printStackTrace();
            } finally{
                System.out.println("Run finished");
                System.exit(0); // Make sure it all shuts down properly.
            }
        }

        public void makePersistent (LinkageRecipe recipe, Link link){
            try {
                final String std_id1 = link.getRecord1().getReferend().getString(Marriage.STANDARDISED_ID);
                final String std_id2 = link.getRecord2().getReferend().getString(Marriage.STANDARDISED_ID);

                if (!Query.MMBrideBrideIdReferenceExists(recipe.getBridge(), std_id1, std_id2, recipe.getLinksPersistentName())) {

                    Query.createMMBrideBrideIdReference(
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

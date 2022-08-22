/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.endToEndIterative;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.graph.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRunners.MakePersistent;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.util.ArrayList;
import java.util.List;

/**
 * This class attempts to find bride-bride links: links a bride on wedding to another bride on a wedding
 * Multiple marriages of a single party (the bride).
 * This is  STRONG.
 */
public class BirthBrideIdentityBuilderIterative implements MakePersistent {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. EVERYTHING or 10000 etc.

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {

            List<LXP> search_matched = new ArrayList<>();
            List<LXP> stored_matched = new ArrayList<>();

            LinkageQuality overall_quality = new LinkageQuality(0, 0, 0);

            final int all_fields = BirthBrideIdentityLinkageRecipeMatchLists.ALL_LINKAGE_FIELDS;
            final int half_fields = all_fields - (all_fields / 2) + 1;

            for (int linkage_fields = all_fields; linkage_fields >= half_fields; linkage_fields--) {

                for (double thresh = 0.05; thresh < BirthBrideIdentityLinkageRecipeMatchLists.DISTANCE_THRESHOLD; thresh += 0.05) {

                    System.out.println( "Running with " + linkage_fields + " required and threshold " + thresh );
                    BirthBrideIdentityLinkageRecipeMatchLists linkageRecipe = new BirthBrideIdentityLinkageRecipeMatchLists(sourceRepo, number_of_records, linkage_fields, search_matched, stored_matched, BirthBrideIdentityBuilderIterative.class.getName(), thresh, bridge);

                    BitBlasterLinkageRunner bb = new BitBlasterLinkageRunner();
                    LinkageResult lrs = bb.run(linkageRecipe, new BirthBrideIdentityBuilderIterative(), true, true);
                    accumulateQuality(overall_quality, lrs.getLinkageQuality());
                    search_matched.addAll(lrs.getLinkedSearchRecords());
                    stored_matched.addAll(lrs.getLinkedStoredRecords());
                }
            }
        } catch (Exception e) {
            System.out.println("Runtime exception:");
                e.printStackTrace();
            } finally{
                System.out.println("Run finished");
                System.exit(0); // Make sure it all shuts down properly.
            }
        }

        private static void accumulateQuality (LinkageQuality overall_quality, LinkageQuality linkageQuality){
            overall_quality.setFn(overall_quality.getFn() + linkageQuality.getFn());
            overall_quality.setTp(overall_quality.getTp() + linkageQuality.getTp());
            overall_quality.setFp(overall_quality.getFp() + linkageQuality.getFp());
            overall_quality.updatePRF();
            System.out.println("*** Accumulated Quality ***");
            overall_quality.print(System.out);
        }

        public void makePersistent (LinkageRecipe recipe, Link link){
            try {
                final String std_id1 = link.getRecord1().getReferend(Marriage.class).getString(Marriage.STANDARDISED_ID);
                final String std_id2 = link.getRecord2().getReferend(Marriage.class).getString(Marriage.STANDARDISED_ID);

                if (!Query.MMBrideBrideIdReferenceExists(recipe.getBridge(), std_id1, std_id2, recipe.getLinksPersistentName())) {

                    Query.createMMBrideBrideIdReference(
                            recipe.getBridge(),
                            std_id1,
                            std_id2,
                            recipe.getLinksPersistentName(),
                            recipe.getNumberOfLinkageFieldsRequired(),
                            link.getDistance());
                }
            } catch (uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException | RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
    }

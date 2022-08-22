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
package uk.ac.standrews.cs.population_linkage.endToEnd.builders;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.graph.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthBrideIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRunners.MakePersistent;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

/**
 *  This class attempts to find birth-bride links: links a baby on a birth to the same person as a groom on a marriage.
 *  This is NOT STRONG: uses the 3 names: the groom/baby and the names of the mother and father.
 */
public class BirthBrideOwnMarriageBuilder implements MakePersistent {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean or Umea
        String number_of_records = args[1]; // e.g. EVERYTHING or 10000 etc.

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {
            BirthBrideIdentityLinkageRecipe linkageRecipe = new BirthBrideIdentityLinkageRecipe(sourceRepo, number_of_records, BirthBrideOwnMarriageBuilder.class.getName(),bridge);

            int linkage_fields = BirthBrideIdentityLinkageRecipe.ALL_LINKAGE_FIELDS;
            int half_fields = linkage_fields - (linkage_fields / 2 );

            while( linkage_fields >= half_fields ) {
                linkageRecipe.setNumberLinkageFieldsRequired(linkage_fields);

                new BitBlasterLinkageRunner().run(linkageRecipe, new BirthBrideOwnMarriageBuilder(), false, true);

                linkage_fields--;
                System.out.println( "Run finished" );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void makePersistent(LinkageRecipe linkage_recipe, Link link) {
        try {
            final String std_id1 = link.getRecord1().getReferend(Birth.class).getString(Birth.STANDARDISED_ID);
            final String std_id2 = link.getRecord2().getReferend(Marriage.class).getString(Marriage.STANDARDISED_ID);

            if (!Query.BMBirthBrideReferenceExists(linkage_recipe.getBridge(), std_id1, std_id2, linkage_recipe.getLinksPersistentName())) {

                Query.createBirthBrideOwnMarriageReference(
                        linkage_recipe.getBridge(),
                        std_id1,
                        std_id2,
                        linkage_recipe.getLinksPersistentName(),
                        linkage_recipe.getNumberOfLinkageFieldsRequired(),
                        link.getDistance());
            }
        } catch (BucketException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}

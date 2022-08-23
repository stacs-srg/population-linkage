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
package uk.ac.standrews.cs.population_linkage.endToEnd.MLCustom;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.graph.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Death;

/**
 * EvidencePair Recipe
 * In all linkage recipes the naming convention is:
 *     the stored type is the first part of the name
 *     the query type is the second part of the name
 * So for example in BirthBrideIdentityLinkageRecipeMatchLists the stored type (stored in the search structure) is a birth and Marriages are used to query.
 * In all recipes if the query and the stored types are not the same the query type is converted to a stored type using getQueryMappingFields() before querying.
 */
public class MLCustomBirthSiblingLinkageRecipe extends BirthSiblingLinkageRecipe {

    public static final int ALL_LINKAGE_FIELDS = 8;
    private final NeoDbCypherBridge bridge;

    public int linkage_fields = ALL_LINKAGE_FIELDS;
    private Iterable<LXP> cached_records = null;
    private double threshold = 0.360571156;

    public MLCustomBirthSiblingLinkageRecipe(String source_repository_name, String number_of_records, NeoDbCypherBridge bridge, String links_persistent_name) {
        super( source_repository_name, links_persistent_name );
        this.bridge = bridge;
    }

    @Override
    public LXPMeasure getCompositeMeasure() {
        return new CustomMeasure();
    }

    public void setNumberLinkageFieldsRequired( int number ) {
        linkage_fields = number;
    }

    @Override
    public double getThreshold() { return threshold; }

    /**
     * @return
     */
    @Override
    public Iterable<LXP> getBirthRecords() {
        if( cached_records == null ) {
            // cached_records = filter(linkage_fields, NUMBER_OF_BIRTHS, super.getBirthRecords(), getLinkageFields());
            cached_records = super.getBirthRecords();
        }
        return cached_records;
    }

    public void makeLinkPersistent(Link link) {
        try {

            String std_id1 = link.getRecord1().getReferend(Death.class).getString(Death.STANDARDISED_ID);
            String std_id2 = link.getRecord2().getReferend(Death.class).getString( Death.STANDARDISED_ID );

            if( !std_id1.equals(std_id2 ) ) {

                if (!Query.BBBirthSiblingReferenceExists(bridge, std_id1, std_id2, getLinksPersistentName())) {
                    Query.createBBSiblingReference(
                            bridge,
                            std_id1,
                            std_id2,
                            getLinksPersistentName(),
                            linkage_fields,
                            link.getDistance());
                }
            }
        } catch (BucketException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}

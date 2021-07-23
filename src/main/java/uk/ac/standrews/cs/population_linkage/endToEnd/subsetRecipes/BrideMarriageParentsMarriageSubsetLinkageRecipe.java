/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.subsetRecipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.graph.model.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BrideMarriageParentsMarriageLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.util.ArrayList;

/**
 * EvidencePair Recipe
 * In all linkage recipies the naming convention is:
 *     the stored type is the first part of the name
 *     the query type is the second part of the name
 * So for example in BirthBrideIdentityLinkageRecipe the stored type (stored in the search structure) is a birth and Marriages are used to query.
 * In all recipes if the query and the stored types are not the same the query type is converted to a stored type using getQueryMappingFields() before querying.
 *
 */

public class BrideMarriageParentsMarriageSubsetLinkageRecipe extends BrideMarriageParentsMarriageLinkageRecipe {

    public static final int ALL_LINKAGE_FIELDS = 5;
    private int NUMBER_OF_MARRIAGES;
    private final NeoDbCypherBridge bridge;

    public int linkage_fields = ALL_LINKAGE_FIELDS;
    private ArrayList<LXP> cached_records = null;


    public BrideMarriageParentsMarriageSubsetLinkageRecipe(String source_repository_name, String number_of_records, NeoDbCypherBridge bridge, String links_persistent_name) {
        super(source_repository_name, links_persistent_name);
        if( number_of_records.equals(EVERYTHING_STRING) ) {
            NUMBER_OF_MARRIAGES = EVERYTHING;
        } else {
            NUMBER_OF_MARRIAGES = Integer.parseInt(number_of_records);
        }
        this.bridge = bridge;
    }

    public void setNumberLinkageFieldsRequired(int number) {
        linkage_fields = number;
    }


    /**
     * @return
     */
    @Override
    protected Iterable<LXP> getMarriageRecords() {
        if( cached_records == null ) {
            cached_records = filter(linkage_fields, NUMBER_OF_MARRIAGES, super.getMarriageRecords(), getLinkageFields());
        }
        return cached_records;
    }

    // NOTE that Marriage fields are not filtered in this recipe.

    @Override
    public void makeLinkPersistent(Link link) {
        try {
            final String std_id1 = link.getRecord1().getReferend().getString(Marriage.STANDARDISED_ID);
            final String std_id2 = link.getRecord2().getReferend().getString(Marriage.STANDARDISED_ID);

            if (!Query.MMBrideMarriageParentsMarriageReferenceExists(bridge, std_id1, std_id2, getLinks_persistent_name())) {

                Query.createMMBrideMarriageParentsMarriageReference(
                        bridge,
                        std_id1,
                        std_id2,
                        links_persistent_name,
                        linkage_fields,
                        link.getDistance());
            }

        } catch (BucketException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}

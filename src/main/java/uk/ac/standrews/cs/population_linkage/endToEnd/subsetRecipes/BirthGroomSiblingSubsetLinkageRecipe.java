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
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthGroomSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;

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
public class BirthGroomSiblingSubsetLinkageRecipe extends BirthGroomSiblingLinkageRecipe {

    private int NUMBER_OF_BIRTHS;
    private final NeoDbCypherBridge bridge;
    public static final int ALL_LINKAGE_FIELDS = 5;

    public int linkage_fields = ALL_LINKAGE_FIELDS;
    private ArrayList<LXP> cached_records = null;

    public BirthGroomSiblingSubsetLinkageRecipe(String source_repository_name, String number_of_records, NeoDbCypherBridge bridge, String links_persistent_name) {
        super( source_repository_name,links_persistent_name );
        if( number_of_records.equals(EVERYTHING_STRING) ) {
            NUMBER_OF_BIRTHS = EVERYTHING;
        } else {
            NUMBER_OF_BIRTHS = Integer.parseInt(number_of_records);
        }
        this.bridge = bridge;
    }

    public void setNumberLinkageFieldsRequired( int number ) {
        linkage_fields = number;
    }

    /**
     * @return
     */
    @Override
    protected Iterable<LXP> getBirthRecords() {
        if( cached_records == null ) {
            cached_records = filter(linkage_fields, NUMBER_OF_BIRTHS, super.getBirthRecords(), getLinkageFields());
        }
        return cached_records;
    }

    @Override
    public void makeLinkPersistent(Link link) {
        try {

            String std_id1 = link.getRecord1().getReferend().getString(Death.STANDARDISED_ID);
            String std_id2 = link.getRecord2().getReferend().getString(Birth.STANDARDISED_ID );

            if( !std_id1.equals(std_id2 ) ) {

                if (!Query.BMGroomSiblingReferenceExists(bridge, std_id1, std_id2, getLinks_persistent_name())) {
                    Query.createBMGroomSiblingReference(
                            bridge,
                            std_id1,
                            std_id2,
                            getLinks_persistent_name(),
                            linkage_fields,
                            link.getDistance());
                }
            }
        } catch (BucketException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}

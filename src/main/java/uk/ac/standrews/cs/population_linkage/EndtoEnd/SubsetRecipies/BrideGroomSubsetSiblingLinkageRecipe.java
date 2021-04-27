/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.SubsetRecipies;

import uk.ac.standrews.cs.population_linkage.graph.model.Query;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BrideGroomSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

/**
 * EvidencePair Recipe
 * In all linkage recipies the naming convention is:
 *     the stored type is the first part of the name
 *     the query type is the second part of the name
 * So for example in BirthBrideIdentityLinkageRecipe the stored type (stored in the search structure) is a birth and Marriages are used to query.
 * In all recipes if the query and the stored types are not the same the query type is converted to a stored type using getQueryMappingFields() before querying.
 *
 */

public class BrideGroomSubsetSiblingLinkageRecipe extends BrideGroomSiblingLinkageRecipe {

    private static final int NUMBER_OF_MARRIAGES = 10000;
    private static final int EVERYTHING = Integer.MAX_VALUE;
    private final NeoDbCypherBridge bridge;

    public static final int PREFILTER_REQUIRED_FIELDS = 4;

    public BrideGroomSubsetSiblingLinkageRecipe(String source_repository_name, String results_repository_name, NeoDbCypherBridge bridge, String links_persistent_name) {
        super( source_repository_name,results_repository_name,links_persistent_name );
        this.bridge = bridge;
    }

    /**
     * @return
     */
    @Override
    protected Iterable<LXP> getMarriageRecords() {
        return filter(PREFILTER_REQUIRED_FIELDS, NUMBER_OF_MARRIAGES, super.getMarriageRecords(), getLinkageFields());
    }

    @Override
    public void makeLinkPersistent(Link link) {
        try {
            Query.createMMSiblingReference(
                    bridge,
                    link.getRecord1().getReferend().getString( Marriage.STANDARDISED_ID ),
                    link.getRecord2().getReferend().getString( Marriage.STANDARDISED_ID ),
                    links_persistent_name,
                    PREFILTER_REQUIRED_FIELDS,
                    link.getDistance() );
        } catch (BucketException e) {
            throw new RuntimeException(e);
        }
    }

}

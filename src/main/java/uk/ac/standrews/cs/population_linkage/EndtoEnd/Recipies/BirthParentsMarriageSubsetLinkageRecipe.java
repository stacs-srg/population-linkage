/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.Recipies;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthParentsMarriageLinkageRecipe;
import uk.ac.standrews.cs.storr.impl.LXP;

/**
 * EvidencePair Recipe
 * In all linkage recipies the naming convention is:
 *     the stored type is the first part of the name
 *     the query type is the second part of the name
 * So for example in BirthBrideIdentityLinkageRecipe the stored type (stored in the search structure) is a birth and Marriages are used to query.
 * In all recipes if the query and the stored types are not the same the query type is converted to a stored type using getQueryMappingFields() before querying.
 *
 */

public class BirthParentsMarriageSubsetLinkageRecipe extends BirthParentsMarriageLinkageRecipe {

    private int prefilterRequiredFields;

    private static final int NUMBER_OF_BIRTHS = 10000;
    private static final int EVERYTHING = Integer.MAX_VALUE;

    public BirthParentsMarriageSubsetLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name, int prefilterRequiredFields ) {
        super( source_repository_name,results_repository_name,links_persistent_name );
        this.prefilterRequiredFields = prefilterRequiredFields;
    }

    /**
     * @return the birth records to be used in this recipe
     */
    @Override
    protected Iterable<LXP> getBirthRecords() {
        System.out.println( "***" );
        return filter(prefilterRequiredFields, NUMBER_OF_BIRTHS, super.getBirthRecords(), getLinkageFields());
    }

    // NOTE that Marriage fields are not filtered in this recipe.

}

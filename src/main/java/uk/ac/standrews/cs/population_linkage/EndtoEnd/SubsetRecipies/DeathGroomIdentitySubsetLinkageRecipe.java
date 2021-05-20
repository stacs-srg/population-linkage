/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.SubsetRecipies;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.population_linkage.graph.model.Query;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathGroomOwnMarriageIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * EvidencePair Recipe
 * In all linkage recipies the naming convention is:
 *     the stored type is the first part of the name
 *     the query type is the second part of the name
 * So for example in BirthBrideIdentityLinkageRecipe the stored type (stored in the search structure) is a birth and Marriages are used to query.
 * In all recipes if the query and the stored types are not the same the query type is converted to a stored type using getQueryMappingFields() before querying.
 *
 */
public class DeathGroomIdentitySubsetLinkageRecipe extends DeathGroomOwnMarriageIdentityLinkageRecipe {

    private final NeoDbCypherBridge bridge;

    private static final int NUMBER_OF_DEATHS = 10000;
    private static final int EVERYTHING = Integer.MAX_VALUE;

    public  static final int ALL_LINKAGE_FIELDS = 6; // 6 is all of them but not occupation - FORENAME,SURNAME,FATHER_FORENAME,FATHER_SURNAME,MOTHER_FORENAME,MOTHER_SURNAME

    public int linkage_fields = ALL_LINKAGE_FIELDS;

    public DeathGroomIdentitySubsetLinkageRecipe(String source_repository_name, String results_repository_name, NeoDbCypherBridge bridge, String links_persistent_name ) {
        super( source_repository_name,results_repository_name,links_persistent_name );
        this.bridge = bridge;
    }

    public void setNumberLinkageFieldsRequired( int number ) {
        linkage_fields = number;
    }

    /**
     * @return the death records to be used in this recipe
     */
    @Override
    public Iterable<LXP> getDeathRecords() {
        return filter(linkage_fields, NUMBER_OF_DEATHS, super.getDeathRecords() , getLinkageFields() );
    }

    private Iterable<LXP> reverse(Iterable<LXP> records) {
        Iterator<LXP> iterator = StreamSupport.stream(records.spliterator(), true)
                .collect(Collectors.toCollection(ArrayDeque::new))
                .descendingIterator();
        return () -> iterator;
    }

    // NOTE Marriage are not filtered in this recipe

    @Override
    public void makeLinkPersistent(Link link) {
        try {
            Query.createDeathGroomOwnMarriageReference(
                    bridge,
                    link.getRecord1().getReferend().getString( Birth.STANDARDISED_ID ),
                    link.getRecord2().getReferend().getString( Marriage.STANDARDISED_ID ),
                    links_persistent_name,
                    linkage_fields,
                    link.getDistance() );
        } catch (BucketException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

}

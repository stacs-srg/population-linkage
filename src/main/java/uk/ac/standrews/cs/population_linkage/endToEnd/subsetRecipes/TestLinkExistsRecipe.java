/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.subsetRecipes;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.population_linkage.graph.model.Query;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathGroomIdentityLinkageRecipe;
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
public class TestLinkExistsRecipe extends DeathGroomIdentityLinkageRecipe {

    private final NeoDbCypherBridge bridge;

    private static final int NUMBER_OF_DEATHS = 10000;
    private static final int EVERYTHING = Integer.MAX_VALUE;

    private static final int PREFILTER_FIELDS = 6; // 6 is all of them but not occupation - FORENAME,SURNAME,FATHER_FORENAME,FATHER_SURNAME,MOTHER_FORENAME,MOTHER_SURNAME


    public TestLinkExistsRecipe(String source_repository_name, NeoDbCypherBridge bridge, String links_persistent_name ) {
        super( source_repository_name,links_persistent_name );
        this.bridge = bridge;
    }

    /**
     * @return the death records to be used in this recipe
     */
    @Override
    public Iterable<LXP> getDeathRecords() {
        return RecordFiltering.filter( PREFILTER_FIELDS, NUMBER_OF_DEATHS, super.getDeathRecords() , getLinkageFields() );
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
//        try {
//            Query.createDeathGroomOwnMarriageReference(
//                    bridge,
//                    link.getRecord1().getReferend().getString( Birth.STANDARDISED_ID ),
//                    link.getRecord2().getReferend().getString( Marriage.STANDARDISED_ID ),
//                    links_persistent_name,
//                    PREFILTER_FIELDS,
//                    link.getDistance() );
//        } catch (BucketException e) {
//            throw new RuntimeException(e);
//        }
//    }

        try {
            String link1_std_id = link.getRecord1().getReferend().getString(Birth.STANDARDISED_ID);
            String link2_std_id = link.getRecord2().getReferend().getString( Marriage.STANDARDISED_ID );
            if( Query.DMDeathGroomOwnMarriageReferenceExists( bridge,link1_std_id,link2_std_id, links_persistent_name ) ) {
                System.out.printf("Link exists from (std_ids): %s %s %s\n", link1_std_id, link2_std_id, links_persistent_name );
            } else {
                System.out.printf("Link does not exist from (std_ids): %s %s %s\n", link1_std_id, link2_std_id, links_persistent_name );
            }
        } catch (BucketException | RepositoryException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge(); ) {
            TestLinkExistsRecipe linkageRecipe = new TestLinkExistsRecipe(sourceRepo, bridge,"");

            boolean exists = Query.DMDeathGroomOwnMarriageReferenceExists( bridge, "9395194","9389272","uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.DeathGroomOwnMarriageBuilder" );

            if( exists ) {
                System.out.println( "Found it and should have");
            } else {
                System.out.println( "Did not find it and should have");
            }

            exists = Query.DMDeathGroomOwnMarriageReferenceExists( bridge, "5194","9389272","uk.ac.standrews.cs.population_linkage.EndtoEnd.builders.DeathGroomOwnMarriageBuilder" );

            if( exists ) {
                System.out.println( "Did not find it and should not have");
            } else {
                System.out.println( "Found it and should not have");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

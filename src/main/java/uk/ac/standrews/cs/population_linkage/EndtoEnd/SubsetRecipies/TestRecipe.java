/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.SubsetRecipies;

import org.neo4j.driver.Result;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathGroomOwnMarriageIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

import java.util.*;
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
public class TestRecipe extends DeathGroomOwnMarriageIdentityLinkageRecipe {

    private final NeoDbCypherBridge bridge;

    private static final int NUMBER_OF_DEATHS = 10000;
    private static final int EVERYTHING = Integer.MAX_VALUE;

    private static final int PREFILTER_FIELDS = 6; // 6 is all of them but not occupation - FORENAME,SURNAME,FATHER_FORENAME,FATHER_SURNAME,MOTHER_FORENAME,MOTHER_SURNAME


    public TestRecipe(String source_repository_name, String results_repository_name, NeoDbCypherBridge bridge, String links_persistent_name ) {
        super( source_repository_name,results_repository_name,links_persistent_name );
        this.bridge = bridge;
    }

    /**
     * @return the death records to be used in this recipe
     */
    @Override
    public Iterable<LXP> getDeathRecords() {
        return filter( PREFILTER_FIELDS, NUMBER_OF_DEATHS, getDeathRecords() , getLinkageFields() );
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
            String link1_id = link.getRecord1().getReferend().getString(Birth.STANDARDISED_ID);
            String link2_id = link.getRecord2().getReferend().getString( Marriage.STANDARDISED_ID );
            if( linkExists( bridge,link1_id,link2_id, links_persistent_name ) ) {
                System.out.printf("Link exists from %s %s %s\n", link1_id,link2_id, links_persistent_name );
            } else {
                System.out.println("Link does not exist");
            }
        } catch (BucketException e) {
            e.printStackTrace();
        }

    }

    private static final String DM_DEATH_GROOM_QUERY_EXISTS = "MATCH (a:DeathRecord)-[r:GROOM]-(b:MarriageRecord) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to AND r.provenance = $prov RETURN r";

    private boolean linkExists( NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to, String provenance ) {
        Map<String, Object> parameters = getparams(standard_id_from, standard_id_to, provenance);
        Result result = bridge.getNewSession().run(DM_DEATH_GROOM_QUERY_EXISTS,parameters);
        List<Integer> field = result.list(r -> r.get("r").asRelationship().get("fields_matched").asInt());
        if( field == null ) {
            return false;
        }
        return true;
    }

    private static Map<String, Object> getparams(String standard_id_from, String standard_id_to, String provenance) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        parameters.put("prov", provenance);
        return parameters;
    }
}

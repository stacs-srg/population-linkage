/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */

package uk.ac.standrews.cs.population_linkage.missingData;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.graph.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering.filter;

/**
 * EvidencePair Recipe
 * In all linkage recipes the naming convention is:
 *     the stored type is the first part of the name
 *     the query type is the second part of the name
 * So for example in BirthBrideIdentityLinkageRecipe the stored type (stored in the search structure) is a birth and Marriages are used to query.
 * In all recipes if the query and the stored types are not the same the query type is converted to a stored type using getQueryMappingFields() before querying.
 *
 */

// THIS IS SCHEDULED FOR DELETION BUT CONTAINS IMPORTANT GT CODE
// DOD NOT DELETE YET!
public class BirthDeathSubsetIdentityLinkageRecipe extends BirthDeathIdentityLinkageRecipe {

    private int NUMBER_OF_DEATHS = EVERYTHING; // 10000; // for testing
    private final NeoDbCypherBridge bridge;

    public int ALL_LINKAGE_FIELDS = 6;

    public int linkage_fields = ALL_LINKAGE_FIELDS;
    private ArrayList<LXP> cached_records = null;

    public BirthDeathSubsetIdentityLinkageRecipe(String source_repository_name, String number_of_records,  NeoDbCypherBridge bridge, String links_persistent_name ) {
        super( source_repository_name, "EVERYTHING", links_persistent_name, bridge );

        if( number_of_records.equals(EVERYTHING_STRING) ) {
            NUMBER_OF_DEATHS = EVERYTHING;
        } else {
            NUMBER_OF_DEATHS = Integer.parseInt(number_of_records);
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
    protected Iterable<LXP> getDeathRecords() {
        if( cached_records == null ) {
            cached_records = filter(linkage_fields, NUMBER_OF_DEATHS, super.getDeathRecords(), getQueryMappingFields());
        }
        return cached_records;
    }

//    @Override
//    protected Iterable<LXP> getBirthRecords() {
//        if( cached_records == null ) {
//            cached_records = filter(linkage_fields, EVERYTHING, super.getBirthRecords(), getLinkageFields()); // was NUMBER_OF_BIRTHS not EVERYTHING.
//        }
//        return cached_records;
//    }

    /* When above was in we got:
    tolerant option selected with 6 fields required to be non-empty
    Filtering: accepted: 100 rejected: 3 from 103
    Number of GT links = 26
    Number of GroundTruth true Links = 26
    TP: 0
    FN: 26
    FP: 1
    precision: 0.00
    recall: 0.00
    f measure: 0.00
    Now swapped filtering to Death records - the query type.
     */

    public void makeLinkPersistent(Link link) {
        try {
            final String std_id1 = link.getRecord1().getReferend().getString(Birth.STANDARDISED_ID);
            final String std_id2 = link.getRecord2().getReferend().getString(Death.STANDARDISED_ID);

            if( ! Query.BDDeathReferenceExists(bridge, std_id1, std_id2, getLinks_persistent_name())) {

                Query.createBDReference(
                        bridge,
                        std_id1,
                        std_id2,
                        getLinks_persistent_name(),
                        linkage_fields,
                        link.getDistance());
            }

        } catch (BucketException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getNumberOfGroundTruthTrueLinks() {
        int count = 0;
        int num = 0;
        for( LXP query_record : getQueryRecords() ) {
            count += countBirthDeathIdentityGTLinks( bridge, query_record );
            num++;
        }
        System.out.println( "Number of GT links = " + count);
        System.out.println( "Number of queries = " + num );
        return count;
    }

    private static final String BD_DEATH_GT_IDENTITY_LINKS_QUERY = "MATCH (a:Birth)-[r:GROUND_TRUTH_BIRTH_DEATH_IDENTITY]-(b:Death) WHERE b.STANDARDISED_ID = $standard_id_from RETURN r";

    public static int countBirthDeathIdentityGTLinks(NeoDbCypherBridge bridge, LXP death_query_record ) {
        String standard_id_from = death_query_record.getString(Death.STANDARDISED_ID );

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        Result result = bridge.getNewSession().run(BD_DEATH_GT_IDENTITY_LINKS_QUERY,parameters);
        List<Relationship> relationships = result.list(r -> r.get("r").asRelationship());
        return relationships.size();
    }

}


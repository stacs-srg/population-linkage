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
package uk.ac.standrews.cs.population_linkage.resolver.msed;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.utilities.measures.JensenShannon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to explore the use of MSED
 * The example used was found by grobbing around by hand!
 */
public class Explore {

    private final NeoDbCypherBridge bridge;
    private final IBucket<Birth> births;
    private final JensenShannon baseMeasure;

    // TODO There is a problem here - if n is > 2 in query below it takes forever to execute - not sure how to constrain to Births
    // TODO in the example of example_query_stor_id6 the bundle should be about 10 records

    private static final String GET_BIRTH_SIBLINGS = "MATCH (a:Birth)-[r:SIBLING*1..2]-(b:Birth) WHERE a.STORR_ID = $stor_id_from RETURN b";
    protected static final String SIBLING_QUERY = "MATCH (x:Birth)-[xy:SIBLING]-(y:Birth) WHERE x.STORR_ID = $stor_id_from  return x,xy,y";
    private final BirthSiblingLinkageRecipe recipe;

    public Explore(NeoDbCypherBridge bridge, String source_repo_name, BirthSiblingLinkageRecipe recipe) {
        this.bridge = bridge;
        RecordRepository record_repository = new RecordRepository(source_repo_name);
        this.births = record_repository.getBucket("birth_records");
        this.recipe = recipe;
        this.baseMeasure = Constants.JENSEN_SHANNON;
    }

    protected LXPMeasure getCompositeMeasure() {
        return new LXPMeasure(recipe.getLinkageFields(), recipe.getQueryMappingFields(), baseMeasure);
    }

    /**
     *
     * @param bridge
     * @param query_string  - must return bs
     * @param stor_id_from
     * @return a list of STORR ids
     */
    private static List<Long> getSiblings(NeoDbCypherBridge bridge, String query_string, long stor_id_from) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("stor_id_from", stor_id_from);
        Result result = bridge.getNewSession().run(query_string, parameters);
        return result.list(r -> r.get("b").get("STORR_ID").asLong());
    }

    public Stream<LinkPair> getSiblingRelationships(NeoDbCypherBridge bridge, long stor_id_from) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("stor_id_from", stor_id_from);
        Result result = bridge.getNewSession().run(SIBLING_QUERY, parameters);
        return result.stream().map(r -> {
                    return new LinkPair(
                            ((Node) r.asMap().get("x")),
                            ((Node) r.asMap().get("y")),
                            ((Relationship) r.asMap().get("xy")));
                }
        );
    }

    public List<Long> getSiblingStorIds(long lxp1_storr_id ) throws BucketException {
        List<Long> sibs = getSiblings(bridge, GET_BIRTH_SIBLINGS, lxp1_storr_id);
        List<Long> siblings= sibs.stream().distinct().collect(Collectors.toList()); // get rid of duplicates
        for( long l : siblings ) { showRecord(l); }
        return siblings;
    }

    private void showRecord(long stor_id) throws BucketException {
        Birth b = births.getObjectById(stor_id);
        System.out.println( stor_id + ":" + b.getString(Birth.FORENAME) + " " + b.getString(Birth.SURNAME) );
    }

    private void showUnlinked() throws BucketException {

        Birth b1 = births.getObjectById(3158851268898028935l);
        Birth b2 = births.getObjectById( 6580773452447460714l );

        System.out.println( "Distance between unlinked: " + getCompositeMeasure().distance( b1,b2 ));
        System.out.println();
    }

    private OrderedList<List<Birth>,Double> examineDivergences(List<Long> sibling_ids) throws BucketException {
        // TreeMap<List<Birth>,Double> all_mseds = new TreeMap<>();
        OrderedList<List<Birth>,Double> all_mseds = new OrderedList<>(Integer.MAX_VALUE); // don't want a limit!
        List<Birth> bs  = getBirths(sibling_ids);
        for( int n = 2; n < bs.size() - 1; n++ ) {
            List<List<Integer>> indices = Binomials.pickAll(sibling_ids.size(), n);
            for (List<Integer> choices : indices) {
                List<Birth> births = getBirthsFromChoices(bs, choices);
                double distance = getMSEDForCluster(births);
                all_mseds.add(births,distance);
            }
        }
        return all_mseds;
    }

    private OrderedList<List<Birth>,Double> getDivergenceForK(List<Long> sibling_ids, int k) throws BucketException {
        OrderedList<List<Birth>,Double> all_mseds = new OrderedList<>(Integer.MAX_VALUE); // don't want a limit!
        List<Birth> bs  = getBirths(sibling_ids);

        List<List<Integer>> indices = Binomials.pickAll(sibling_ids.size(), k);
        for (List<Integer> choices : indices) {
            List<Birth> births = getBirthsFromChoices(bs, choices);
            double distance = getMSEDForCluster(births);
            all_mseds.add(births,distance);
        }
        return all_mseds;
}


    private List<Birth> getBirthsFromChoices(List<Birth> bs, List<Integer> choices) {
        List<Birth> births = new ArrayList<>();
        for (int index : choices) {
            births.add( bs.get(index) );
        }
        return births;
    }

    private double getMSEDForCluster(List<Birth> choices) {
        /* Calculate the MESD for the cluster represented by the indices choices into bs */
        List<String> fields_from_choices = new ArrayList<>(); // a list of the concatenated linkage fields from the selected choices.
        List<Integer> linkage_fields = recipe.getLinkageFields(); // the linkage field indexes to be used
        for (Birth a_birth : choices) {
            StringBuilder sb = new StringBuilder();              // make a string of values for this record drawn from the recipe linkage fields
            for (int field_selector : linkage_fields) {
                sb.append(a_birth.get(field_selector) + "/");
            }
            fields_from_choices.add(sb.toString()); // add the linkage fields for this choice to the list being assessed
        }
        return MSED.distance(fields_from_choices);
    }

    private List<Birth> getBirths(List<Long> sibling_ids) throws BucketException {
        ArrayList<Birth> bs = new ArrayList();
        for( long id : sibling_ids) {
            bs.add(births.getObjectById(id));
        }
        return bs;
    }

    public void showMsedDists(OrderedList<List<Birth>, Double> all_msed_dists, int main_group_size, int k ) {
        System.out.println( "Choosing " + k + " from a group of size " + main_group_size );
        List<Double> distances = all_msed_dists.getComparators();
        List<List<Birth>> births = all_msed_dists.getList();

        for( int i = 0; i < distances.size(); i++ ) {

            System.out.print( distances.get(i) + ":" );
            showBirths( births.get(i));
            System.out.println();
        }
    }

    private void showBirths(List<Birth> births) {
        for( int i = 0; i < births.size(); i++ ) {
            System.out.print( births.get(i).get( Birth.FORENAME ) + ", " );
        }
    }

    private void showBirthRecords(List<Long> sibling_ids) throws BucketException {
        for( long stor_id : sibling_ids) {
            showRecord(stor_id);
            Stream<LinkPair> xx = getSiblingRelationships(bridge, stor_id);
            xx.forEach(linkPair -> System.out.println("distance: " + LinkPair.getDistance(linkPair.xy) + linkPair.y.get("FORENAME")));   // note y is a node
            System.out.println();
        }
    }

    private void examineAll(List<Long> sibling_ids) throws BucketException {
        // showBirthRecords(sibling_ids);
        OrderedList<List<Birth>, Double> all_msed_dists = examineDivergences(sibling_ids);
        showMsedDists(all_msed_dists, sibling_ids.size(), -1);
    }

    private void examineAll(long query_from_bundle_stor_ids, long extra_stor_id) throws BucketException {
        List<Long> all_stor_ids = getSiblingStorIds(query_from_bundle_stor_ids);
        all_stor_ids.add(query_from_bundle_stor_ids); // add the query too
        all_stor_ids.add(extra_stor_id);                // add the extra member
        examineAll( all_stor_ids );                     // now examine the whole lot.
    }

    private void examineAll(long query_from_bundle_stor_ids) throws BucketException {
        List<Long> all_stor_ids = getSiblingStorIds(query_from_bundle_stor_ids);
        all_stor_ids.add(query_from_bundle_stor_ids); // add the query too
        examineAll( all_stor_ids );                     // now examine the whole lot.
    }

    private void examineGroupsOfSizeK(List<Long> sibling_ids, int k) throws BucketException {
        // showBirthRecords(sibling_ids);
        OrderedList<List<Birth>, Double> all_msed_dists = getDivergenceForK(sibling_ids,k);
        showMsedDists(all_msed_dists, sibling_ids.size(), k);
    }

    private void examineGroupsOfSizeK(long query_from_bundle_stor_ids, long extra_stor_id, int k) throws BucketException {
        List<Long> all_stor_ids =getSiblingStorIds(query_from_bundle_stor_ids); // A set of partially interconnected siblings
        all_stor_ids.add(query_from_bundle_stor_ids); // add the query too
        all_stor_ids.add(extra_stor_id);                // add the extra member
        examineGroupsOfSizeK( new ArrayList<>( all_stor_ids ), k); // now examine the whole lot.
    }

    private void examineGroupsOfSizeK(long query_from_bundle_stor_ids, int k) throws BucketException {
        List<Long> all_stor_ids = getSiblingStorIds(query_from_bundle_stor_ids); // A set of partially interconnected siblings
        all_stor_ids.add(query_from_bundle_stor_ids); // add the query too
        examineGroupsOfSizeK( new ArrayList<>( all_stor_ids ),k ); // now examine the whole lot.
    }

    public static void main(String[] args) throws BucketException {

        String sourceRepo = "umea";

        long example_query_stor_id1 = 4377094037612468415l; // this is one of a group 4 true siblings with one link missing.
        long example_query_stor_id2 = 1869999260706456703l; // this one is totally unrelated to the above.
        long example_query_stor_id3 = 4377094037612468415l; // part of another grroup of 4 with a missing link
        long example_query_stor_id5 = 8472462191637179711l; // part of a network with possibly extra links.
        long example_query_stor_id6 = 8951651435219393113l; // this only has two links but should be part of a complex of 10 siblings

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge();
             BirthSiblingLinkageRecipe linkageRecipe = new BirthSiblingLinkageRecipe(sourceRepo, "EVERYTHING", BirthSiblingBundleBuilder.class.getName())) {

            Explore ex = new Explore(bridge, sourceRepo, linkageRecipe); // this class
//            System.out.println( "****************** Related ******************");
//            ex.examineAll(example_query_stor_id1);
//            System.out.println( "****************** Unrelated ******************");
//            ex.examineAll(example_query_stor_id1,example_query_stor_id2);

//            System.out.println( "****************** Unrelated ******************");
//            ex.examineGroupsOfSizeK(example_query_stor_id1,example_query_stor_id2,4);

//            System.out.println( "****************** Related ******************");
//            ex.examineGroupsOfSizeK(example_query_stor_id3,3);
//            System.out.println( "****************** Unrelated ******************");
//            ex.examineGroupsOfSizeK(example_query_stor_id3,example_query_stor_id2,3);

//            System.out.println( "****************** Related ******************");
//            ex.examineGroupsOfSizeK(example_query_stor_id5,3);
//
//            System.out.println( "****************** Unrelated ******************");
//            ex.examineGroupsOfSizeK(example_query_stor_id5,example_query_stor_id2,3);

            System.out.println( "****************** Related ******************");
            ex.examineGroupsOfSizeK(example_query_stor_id6,3);  //Super it finds all the sibs in the group with a max of 0.0677 (higher than I had hoped for)

        }
    }
}

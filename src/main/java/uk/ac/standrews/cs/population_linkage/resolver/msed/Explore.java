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

import java.util.*;
import java.util.stream.Stream;

/**
 * Class to explore the use of MSED
 * The example used was found by grobbing around by hand!
 */
public class Explore {

    private final NeoDbCypherBridge bridge;
    private final IBucket<Birth> births;
    private final JensenShannon baseMeasure;

    private static final String GET_BIRTH_SIBLINGS = "MATCH (a)-[r:SIBLING]-(b:Birth) WHERE a.STORR_ID = $stor_id_from RETURN b";
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
        return new SumOfFieldDistances(baseMeasure, recipe.getLinkageFields());
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
        return getSiblings(bridge, GET_BIRTH_SIBLINGS, lxp1_storr_id);
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
        OrderedList<List<Birth>,Double> all_mseds = new OrderedList<>(1000); // don't want a limit!
        List<Birth> bs  = getBirths(sibling_ids);
        for( int n = 2; n < bs.size() + 1; n++ ) {
            List<List<Integer>> indices = Binomials.pickAll(sibling_ids.size(), n);
            for (List<Integer> choices : indices) {
                List<Birth> births = getBirthsFromChoices(bs, choices);
                double distance = getMSEDForCluser(births);
                all_mseds.add(births,distance);
            }
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

    private double getMSEDForCluser(List<Birth> choices) {
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

    public void showMsedDists(OrderedList<List<Birth>, Double> all_msed_dists) {
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


    public static void main(String[] args) throws BucketException {

        String sourceRepo = "umea";

        long example_query_stor_id = 4377094037612468415l;

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge();
             BirthSiblingLinkageRecipe linkageRecipe = new BirthSiblingLinkageRecipe(sourceRepo, "EVERYTHING", BirthSiblingBundleBuilder.class.getName()) ) {
             Explore ex = new Explore(bridge, sourceRepo, linkageRecipe); // this class
             List<Long> sibling_ids = ex.getSiblingStorIds( example_query_stor_id );
             sibling_ids.add( example_query_stor_id );
             for( long stor_id : sibling_ids ) {
                 ex.showRecord(stor_id);
                 Stream<LinkPair> xx = ex.getSiblingRelationships(bridge, stor_id);
                 xx.forEach(linkPair -> System.out.println("distance: " + LinkPair.getDistance(linkPair.xy) + linkPair.y.get("FORENAME")));   // note y is a node
                 System.out.println();
             }
             ex.showUnlinked();
            OrderedList<List<Birth>,Double> all_msed_dists = ex.examineDivergences(sibling_ids);
            ex.showMsedDists(all_msed_dists);
        }
    }
}

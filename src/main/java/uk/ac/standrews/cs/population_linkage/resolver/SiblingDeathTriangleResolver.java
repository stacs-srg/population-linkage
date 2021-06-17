/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.resolver;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.endToEnd.subsetRecipes.DeathSiblingSubsetLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Sigma;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.*;
import java.util.stream.Stream;

public class SiblingDeathTriangleResolver {

    public static final int SIBLING_COUNT_SUPPORT_THRESHOLD = 2;
    private static final int MAX_AGE_DIFFERENCE = 15; // max age difference of siblings - plausible but conservative
    public static final double LOW_DISTANCE_MATCH_THRESHOLD = 0.2;
    public static final double HIGH_DISTANCE_REJECT_THRESHOLD = 0.5;
    private final RecordRepository record_repository;
    private final NeoDbCypherBridge bridge;
    private final IBucket deaths;
    private final DeathSiblingSubsetLinkageRecipe recipe;

    private final JensenShannon base_metric;
    private final Metric<LXP> metric;

    private static final String DEATH_SIBLING_ILLEGAL_TRIANGLE_QUERY = "MATCH (x:Death)-[xy:SIBLING]-(y:Death)-[yz:SIBLING]-(z:Death) WHERE NOT (x)-[:SIBLING]-(z) return x,y,z,xy,yz";
    private static final String DD_GET_SIBLINGS = "MATCH (a:Death)-[r:SIBLING]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";

    private int link_count = 0;// ALL A HACK DELETE
    private int cut_count = 0;
    private int new_link_correct = 0;
    private int cut_link_correct = 0;
    private int as_is_correct = 0;
    private int as_is_incorrect = 0;
    private int count = 0;


    public SiblingDeathTriangleResolver(NeoDbCypherBridge bridge, String source_repo_name, DeathSiblingSubsetLinkageRecipe recipe) {
        this.bridge = bridge;
        this.recipe = recipe;
        this.record_repository = new RecordRepository(source_repo_name);
        this.deaths = record_repository.getBucket("death_records");
        this.base_metric = new JensenShannon(2048);
        this.metric = getCompositeMetric(recipe);
    }

    protected Metric<LXP> getCompositeMetric(final LinkageRecipe linkageRecipe) {
        return new Sigma(base_metric, linkageRecipe.getLinkageFields(), 0);
    }

    private void resolve() {
        Stream<OpenTriangle> oddballs = findIllegalBirthSiblingTriangles();
//            System.out.println( "Found " + oddballs.count() );
        oddballs.forEach(this::process);
        System.out.println("Would have established: " + link_count + " correctly established = " + new_link_correct );
        System.out.println("Would have cut: " + cut_count + " correctly cut = " + cut_link_correct );
        System.out.println("As is correct: " + as_is_correct );
        System.out.println("As is incorrect: " + as_is_incorrect );
        System.out.println( "Total = " + count + " remain incorrect after intervention = " + ( count - new_link_correct - cut_link_correct - as_is_correct ) );
    }

    private void process(OpenTriangle open_triangle) {
        System.out.println(open_triangle.toString());
        try {

            LXP b_x = (LXP) deaths.getObjectById(open_triangle.x);
            LXP b_y = (LXP) deaths.getObjectById(open_triangle.y);
            LXP b_z = (LXP) deaths.getObjectById(open_triangle.z);
            String std_id_x = b_x.getString(Death.STANDARDISED_ID);
            String std_id_y = b_x.getString(Death.STANDARDISED_ID);
            String std_id_z = b_z.getString(Death.STANDARDISED_ID);

            count++;

            // They might all be the same person - how to fix that?
            // Not accounted for fields matched

            if (plausibleBirthDates(b_x,b_y,b_z)) {
                if (isLowDistance(open_triangle.xy_distance, open_triangle.yz_distance)) {
                    // If distances are low then establish irrespective of other links
                    // Query.createDDSiblingReference(NeoDbCypherBridge bridge, std_id_x, std_id_z, "open-triangle-processing",0,open_distance(open_triangle));
                    System.out.println("Would establish link between " + std_id_x + " and " + std_id_z);
                    if (b_x.getString(Death.DECEASED_IDENTITY).equals(b_z.getString(Death.DECEASED_IDENTITY))) {
                        new_link_correct++;
                    }
                    link_count++;
                } else if (countLinksBetween(std_id_x, std_id_z) >= SIBLING_COUNT_SUPPORT_THRESHOLD) {
                    // we have support for the link so establish it
                    // Query.createDDSiblingReference(NeoDbCypherBridge bridge, std_id_x, std_id_z, "open-triangle-processing",0,open_distance(open_triangle));
                    System.out.println("Would establish link between " + std_id_x + " and " + std_id_z);
                    if (b_x.getString(Death.FATHER_IDENTITY).equals(b_z.getString(Death.FATHER_IDENTITY))) {
                        new_link_correct++;
                    }
                    link_count++;
                } else if (isHighDistance( open_triangle.xy_distance, open_triangle.yz_distance ) ) {  // only cut if distances are high
                        cutOne(open_triangle, b_x, b_y, b_z);
                    } else{
                        System.out.println("Would leave link as is: " + std_id_x + " and " + std_id_z);
                        if (!(b_x.getString(Death.DECEASED_IDENTITY).equals(b_z.getString(Death.DECEASED_IDENTITY)))) {
                            as_is_correct++;
                        } else {
                            as_is_incorrect++;
                        }
                    }
                } else { // not plausible dates - cut
                cutOne(open_triangle, b_x, b_y, b_z);
            }
        } catch (BucketException e) {
            e.printStackTrace();
        }
    }

    // Cut the biggest distance = perhaps use fields
    private void cutOne( OpenTriangle open_triangle, LXP b_x, LXP b_y, LXP b_z) {
        if (open_triangle.xy_distance > open_triangle.yz_distance) {
            System.out.println("Would DO CUT XY");
            if (!b_x.getString(Death.FATHER_IDENTITY).equals(b_y.getString(Death.FATHER_IDENTITY))) {
                cut_link_correct++;
            }
            cut_count++;
        } else {
            System.out.println("Would DO CUT YZ");
            if (!b_y.getString(Death.FATHER_IDENTITY).equals(b_z.getString(Death.FATHER_IDENTITY))) {
                cut_link_correct++;
            }
            cut_count++;
        }
    }

    private boolean plausibleBirthDates(LXP a, LXP b, LXP c) {
        try {
            int a_birth_year = Integer.parseInt(a.getString(Death.DEATH_YEAR)) - Integer.parseInt(a.getString(Death.AGE_AT_DEATH));
            int b_birth_year = Integer.parseInt(b.getString(Death.DEATH_YEAR)) - Integer.parseInt(b.getString(Death.AGE_AT_DEATH));
            int c_birth_year = Integer.parseInt(c.getString(Death.DEATH_YEAR)) - Integer.parseInt(c.getString(Death.AGE_AT_DEATH));

            return Math.max(Math.abs(a_birth_year - b_birth_year), Math.abs(b_birth_year - c_birth_year)) < MAX_AGE_DIFFERENCE;
        } catch( NumberFormatException e ) {
            return true;
        }
    }


    private boolean isLowDistance(double d1, double d2) {
        return d1 + d2 > LOW_DISTANCE_MATCH_THRESHOLD;
    }

    private boolean isHighDistance(double d1, double d2) {
        return d1 + d2 > HIGH_DISTANCE_REJECT_THRESHOLD;
    }

    /**
     * std_id_x - the standard id of node x
     * std_id_z - the standard id of node z
     * @return the number of links that x and z share
     * @throws BucketException
     */
    private int countLinksBetween(String std_id_x, String std_id_z) throws BucketException {
        Set<Long> siblings_of_x = getSiblingIds( std_id_x );
        Set<Long> siblings_of_z = getSiblingIds( std_id_z );
        Set<Long> intersection = intersectionOf( siblings_of_x,siblings_of_z );
        return intersection.size(); // we know it is at least one because x,y,z are connected.
    }

    private Set<Long> intersectionOf(Set<Long> X, Set<Long> Y) {
        Set<Long> result = new HashSet(X);
        result.retainAll(Y);
        return result;
    }

    private Set<Long> getSiblingIds(String std_id) throws BucketException {
        Set<Long> result = new HashSet<>();
        result.addAll( getLinks(bridge,DD_GET_SIBLINGS,std_id) );
        return result;
    }

    private double open_distance(OpenTriangle open_triangle) throws BucketException {
        return get_BB_distance( open_triangle.x, open_triangle.z );
    }

    private double get_BB_distance(long birth_id1, long birth_id2) throws BucketException {
        LXP b1 = (LXP) deaths.getObjectById(birth_id1);
        LXP b2 = (LXP) deaths.getObjectById(birth_id2);
        return metric.distance( b1, b2 );
    }

    // Queries

    /**
     * @return a Stream of OpenTriangles
     */
    public Stream<OpenTriangle> findIllegalBirthSiblingTriangles() {
        Result result = bridge.getNewSession().run(DEATH_SIBLING_ILLEGAL_TRIANGLE_QUERY); // returns x,y,z where x and y and z are connected and zx is not.
        return result.stream().map( r -> {
            return new OpenTriangle(
                            ( (Node) r.asMap().get("x")).get( "STORR_ID" ).asLong(),
                            ( (Node) r.asMap().get("y")).get( "STORR_ID" ).asLong(),
                            ( (Node) r.asMap().get("z")).get( "STORR_ID" ).asLong(),
                            ( (Relationship) r.asMap().get("xy")).get( "distance" ).asDouble(),
                            ( (Relationship) r.asMap().get("yz")).get( "distance" ).asDouble()
                    );
                }
        );
    }

    private static List<Long> getLinks(NeoDbCypherBridge bridge, String query_string, String standard_id_from) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        Result result = bridge.getNewSession().run(query_string,parameters);
        return result.list(r -> r.get("b").get( "STORR_ID" ).asLong());
    }


    public static void main(String[] args) {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge(); ) {

            DeathSiblingSubsetLinkageRecipe linkageRecipe = new DeathSiblingSubsetLinkageRecipe(sourceRepo, resultsRepo, bridge, BirthSiblingBundleBuilder.class.getCanonicalName());
            SiblingDeathTriangleResolver resolver = new SiblingDeathTriangleResolver( bridge,sourceRepo,linkageRecipe );
            resolver.resolve();

        } catch (Exception e) {
            System.out.println( "Exception closing bridge" );
        } finally {
            System.out.println( "Run finished" );
            System.exit(0); // Make sure it all shuts down properly.
        }
    }

}

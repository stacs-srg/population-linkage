/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.resolver.triangles;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMetrics.Sigma;
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.DeathSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.resolver.util.OpenTriangle;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.*;
import java.util.stream.Stream;

public class SiblingDeathOpenTriangleResolver {

    public static final int SIBLING_COUNT_SUPPORT_THRESHOLD = 2;
    private static final int MAX_AGE_DIFFERENCE = 15; // max age difference of siblings - plausible but conservative
    public static double LOW_DISTANCE_MATCH_THRESHOLD = 0.2;
    public static double HIGH_DISTANCE_REJECT_THRESHOLD = 0.5;
    private final RecordRepository record_repository;
    private final NeoDbCypherBridge bridge;
    private final IBucket deaths;
    private final DeathSiblingLinkageRecipe recipe;

    private final JensenShannon base_metric;
    private final Metric<LXP> metric;

    protected static String DEATH_SIBLING_TRIANGLE_QUERY = "MATCH (x:Death)-[xy:SIBLING]-(y:Death)-[yz:SIBLING]-(z:Death) WHERE NOT (x)-[:SIBLING]-(z) return x,y,z,xy,yz";
    private static final String DD_GET_SIBLINGS = "MATCH (a:Death)-[r:SIBLING]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";

    private static final String DD_GET_INDIRECT_SIBLING_LINKS = "MATCH (a:Death)-[r:SIBLING*1..5]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to RETURN r";

    private int intersection_support_count = 0; // All for diagnostics only
    private int distance_link_count = 0;
    private int names_count = 0;
    private int names_correct = 0;
    private int intersection_support_correct = 0;
    private int new_link_distance_correct = 0;
    private int count = 0;


    public SiblingDeathOpenTriangleResolver(NeoDbCypherBridge bridge, String source_repo_name, DeathSiblingLinkageRecipe recipe) {
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
        Stream<OpenTriangle> oddballs = findIllegalDeathSiblingTriangles();
//            System.out.println( "Found " + oddballs.count() );
        oddballs.forEach(this::process);
        printResults();
    }

    protected void resolve(int min_cluster_size, double ldmt, double hdrt) {
        LOW_DISTANCE_MATCH_THRESHOLD = ldmt;
        HIGH_DISTANCE_REJECT_THRESHOLD = hdrt;
        resolve();
    }

    private void printResults() {
        System.out.println("Processed: " + count + " open triangles" );
        System.out.println("Would have established (intersection neighbours) " + intersection_support_count + " correctly established = " + intersection_support_correct);
        System.out.println("Would have established (distance): " + distance_link_count + " correctly established = " + new_link_distance_correct );
        System.out.println("Would have established names: " + names_count + " correctly established = " + names_correct );
        System.out.println( "Incorrect after intervention = " + ( count - names_correct - new_link_distance_correct - intersection_support_correct ) );
    }

    private void process(OpenTriangle open_triangle) {
        // System.out.println(open_triangle.toString());
        try {

            LXP x = (LXP) deaths.getObjectById(open_triangle.x);
            LXP y = (LXP) deaths.getObjectById(open_triangle.y);
            LXP z = (LXP) deaths.getObjectById(open_triangle.z);
            String std_id_x = x.getString(Death.STANDARDISED_ID);
            String std_id_y = y.getString(Death.STANDARDISED_ID);
            String std_id_z = z.getString(Death.STANDARDISED_ID);

            count++;

            if( !allDifferent( x,y,z ) ) {  // They might all be the same person with different ids - how to fix that?
                return;
            }

            // Not accounted for fields matched

            if (plausibleBirthDates(x,y,z)) {
                if (isLowDistance(open_triangle.xy_distance, open_triangle.yz_distance)) {

                    // if open_distance(open_triangle) is big then prob not a link?
                    // Graham - 2 points -
                    // 1. if sum high => then might be 0 links rather than 3
                    // 2. Know that xz is not within threshold.
                    // e.g. co-linear

                    // If distances are low then establish irrespective of other links
                    // Query.createDDSiblingReference(NeoDbCypherBridge bridge, std_id_x, std_id_z, "open-triangle-processing",0,open_distance(open_triangle));
                    if (x.getString(Death.FATHER_IDENTITY).equals(z.getString(Death.FATHER_IDENTITY))) {
                        new_link_distance_correct++;
                    }
                    distance_link_count++;
                } else if (countIntersectionDirectSiblingsBetween(std_id_x, std_id_z) >= SIBLING_COUNT_SUPPORT_THRESHOLD) {
                    // we have support for the link so establish it
                    // Query.createDDSiblingReference(NeoDbCypherBridge bridge, std_id_x, std_id_z, "open-triangle-processing",0,open_distance(open_triangle));
                    if (x.getString(Death.FATHER_IDENTITY).equals(z.getString(Death.FATHER_IDENTITY))) {
                        intersection_support_correct++;
                    }
                    intersection_support_count++;
                } else if (surnamesStrictlyMatch(x, z)) {
                    // Query.createDDSiblingReference(NeoDbCypherBridge bridge, std_id_x, std_id_z, "open-triangle-processing",0,open_distance(open_triangle));
                    if (x.getString(Death.FATHER_IDENTITY).equals(z.getString(Death.FATHER_IDENTITY))) {
                        names_correct++;
                    }
                    names_count++;
                }
            }
        } catch (BucketException e) {
            e.printStackTrace();
        }
    }

    private boolean surnamesStrictlyMatch(LXP x, LXP z) {
        return  x.getString( Death.MOTHER_MAIDEN_SURNAME ).equals( z.getString( Death.MOTHER_MAIDEN_SURNAME ) ) &&
                x.getString( Death.FATHER_SURNAME ).equals( z.getString( Death.FATHER_SURNAME) );
    }

    private boolean allDifferent(LXP x, LXP y, LXP z) {
        return x.getId() != y.getId() && y.getId() != z.getId() && x.getId() != z.getId();
    }

    private boolean plausibleBirthDates(LXP a, LXP b, LXP c) {
        try {
            int a_birth_year = Integer.parseInt( a.getString(Death.DEATH_YEAR)) - Integer.parseInt(a.getString(Death.AGE_AT_DEATH));
            int b_birth_year = Integer.parseInt( b.getString(Death.DEATH_YEAR)) - Integer.parseInt(b.getString(Death.AGE_AT_DEATH));
            int c_birth_year = Integer.parseInt( c.getString(Death.DEATH_YEAR)) - Integer.parseInt(c.getString(Death.AGE_AT_DEATH));

            return Math.max(Math.abs(a_birth_year - b_birth_year), Math.abs(b_birth_year - c_birth_year)) < MAX_AGE_DIFFERENCE;
        } catch( NumberFormatException e ) {
            return true;
        }
    }


    private boolean isLowDistance(double d1, double d2) {
        return d1 + d2 < LOW_DISTANCE_MATCH_THRESHOLD;  // count be determined properly by a human (or AI) inspecting these.
    }

    private boolean isHighDistance(double d1, double d2) {
        return d1 + d2 > HIGH_DISTANCE_REJECT_THRESHOLD;
    }

    /**
     * std_id_x - the standard id of node x
     * std_id_z - the standard id of node z
     * @return the number of node that x and z share as neighbours
     * @throws BucketException
     */
    private int countIntersectionDirectSiblingsBetween(String std_id_x, String std_id_z) throws BucketException {
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
        result.addAll( getSiblings(bridge, DD_GET_SIBLINGS,std_id) );
        return result;
    }

    private double open_distance(OpenTriangle open_triangle) throws BucketException {
        return get_distance( open_triangle.x, open_triangle.z );
    }

    private double get_distance(long id1, long id2) throws BucketException {
        LXP b1 = (LXP) deaths.getObjectById(id1);
        LXP b2 = (LXP) deaths.getObjectById(id2);
        return metric.distance( b1, b2 );
    }

    // Queries

    /**
     * @return a Stream of OpenTriangles
     */
    public Stream<OpenTriangle> findIllegalDeathSiblingTriangles() {
        Result result = bridge.getNewSession().run(DEATH_SIBLING_TRIANGLE_QUERY); // returns x,y,z where x and y and z are connected and zx is not.
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

    private long countTransitiveSiblingPaths(String standard_id_from, String standard_id_to) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        Result result = bridge.getNewSession().run(DD_GET_INDIRECT_SIBLING_LINKS,parameters);
//        return result.stream().map( r -> { // debug
//            System.out.println(r); return r;
//        } ).count();
        return result.stream().count();
    }

    private static List<Long> getSiblings(NeoDbCypherBridge bridge, String query_string, String standard_id_from) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        Result result = bridge.getNewSession().run(query_string,parameters);
        return result.list(r -> r.get("b").get( "STORR_ID" ).asLong());
    }


    public static void main(String[] args) {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge(); ) {

            DeathSiblingLinkageRecipe linkageRecipe = new DeathSiblingLinkageRecipe(sourceRepo, resultsRepo, DeathSiblingBundleBuilder.class.getCanonicalName(), bridge);
            SiblingDeathOpenTriangleResolver resolver = new SiblingDeathOpenTriangleResolver( bridge,sourceRepo,linkageRecipe );
            resolver.resolve();

        } catch (Exception e) {
            System.out.println( "Exception closing bridge" );
        } finally {
            System.out.println( "Run finished" );
            System.exit(0); // Make sure it all shuts down properly.
        }
    }

}

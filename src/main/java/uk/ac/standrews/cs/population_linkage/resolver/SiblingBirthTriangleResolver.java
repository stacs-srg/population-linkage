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
import uk.ac.standrews.cs.population_linkage.endToEnd.subsetRecipes.BirthSiblingSubsetLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Sigma;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.*;
import java.util.stream.Stream;

public class SiblingBirthTriangleResolver {

    public static final int SIBLING_COUNT_SUPPORT_THRESHOLD = 2;
    public static final int SIBLING_TRANSITIVE_PATH_SUPPORT_THRESHOLD = 2;
    private static final int MAX_AGE_DIFFERENCE = 15; // max age difference of siblings - plausible but conservative
    public static final double LOW_DISTANCE_MATCH_THRESHOLD = 0.2;
    public static final double HIGH_DISTANCE_REJECT_THRESHOLD = 0.5;
    private final RecordRepository record_repository;
    private final NeoDbCypherBridge bridge;
    private final IBucket births;
    private final BirthSiblingSubsetLinkageRecipe recipe;

    private final JensenShannon base_metric;
    private final Metric<LXP> metric;

    private static final String BIRTH_SIBLING_ILLEGAL_TRIANGLE_QUERY = "MATCH (x:Birth)-[xy:SIBLING]-(y:Birth)-[yz:SIBLING]-(z:Birth) WHERE NOT (x)-[:SIBLING]-(z) return x,y,z,xy,yz";
    private static final String BB_GET_SIBLINGS = "MATCH (a:Birth)-[r:SIBLING]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";

    private static final String BB_GET_INDIRECT_SIBLING_LINKS = "MATCH (a:Birth)-[r:SIBLING*1..5]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to RETURN r";

    private int intersection_support_count = 0;// ALL A HACK DELETE
    private int transitive_link_count = 0;
    private int distance_link_count = 0;
    private int cut_count = 0;
    private int names_count = 0;
    private int names_correct = 0;
    private int intersection_support_correct = 0;
    private int transitive_support_correct = 0;
    private int new_link_distance_correct = 0;
    private int cut_link_correct = 0;
    private int as_is_correct = 0;
    private int as_is_incorrect = 0;
    private int not_all_different_count = 0;
    private int count = 0;


    public SiblingBirthTriangleResolver(NeoDbCypherBridge bridge, String source_repo_name, BirthSiblingSubsetLinkageRecipe recipe) {
        this.bridge = bridge;
        this.recipe = recipe;
        this.record_repository = new RecordRepository(source_repo_name);
        this.births = record_repository.getBucket("birth_records");
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
        System.out.println("Would have established (intersection neighbours) " + intersection_support_count + " correctly established = " + intersection_support_correct);
        System.out.println("Would have established (transitive links): " + transitive_link_count + " correctly established = " + transitive_support_correct);
        System.out.println("Would have established (distance): " + distance_link_count + " correctly established = " + new_link_distance_correct );
        System.out.println("Would have established names: " + names_count + " correctly established = " + names_correct );
        System.out.println("Would have cut: " + cut_count + " correctly cut = " + cut_link_correct );
        System.out.println("As is correct: " + as_is_correct );
        System.out.println("As is incorrect: " + as_is_incorrect );
        System.out.println( "Total = " + count + " remain incorrect after intervention = " + ( count -names_correct - new_link_distance_correct - transitive_support_correct - intersection_support_correct - cut_link_correct - as_is_correct ) );
    }

    private void process(OpenTriangle open_triangle) {
        System.out.println(open_triangle.toString());
        try {

            LXP x = (LXP) births.getObjectById(open_triangle.x);
            LXP y = (LXP) births.getObjectById(open_triangle.y);
            LXP z = (LXP) births.getObjectById(open_triangle.z);
            String std_id_x = x.getString(Birth.STANDARDISED_ID);
            String std_id_y = y.getString(Birth.STANDARDISED_ID);
            String std_id_z = z.getString(Birth.STANDARDISED_ID);

            count++;

            if( ! allDifferent( x,y,z ) ) {  // They might all be the same person with different ids - how to fix that?
                not_all_different_count++;
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
                    System.out.println("Would establish link between " + std_id_x + " and " + std_id_z);
                    if (x.getString(Birth.FATHER_IDENTITY).equals(z.getString(Birth.FATHER_IDENTITY))) {
                        new_link_distance_correct++;
                    } else {
                        //
                    }
                    distance_link_count++;
                } else if (countIntersectionDirectSiblingsBetween(std_id_x, std_id_z) >= SIBLING_COUNT_SUPPORT_THRESHOLD) {
                    // we have support for the link so establish it
                    // Query.createDDSiblingReference(NeoDbCypherBridge bridge, std_id_x, std_id_z, "open-triangle-processing",0,open_distance(open_triangle));
                    System.out.println("Would establish link between " + std_id_x + " and " + std_id_z);
                    if (x.getString(Birth.FATHER_IDENTITY).equals(z.getString(Birth.FATHER_IDENTITY))) {
                        intersection_support_correct++;
                    } else {
                        //
                    }
                    intersection_support_count++;
                } else if( surnamesStrictlyMatch(x,z) ) {
                    System.out.println("Would establish link between " + std_id_x + " and " + std_id_z);
                    if (x.getString(Birth.FATHER_IDENTITY).equals(z.getString(Birth.FATHER_IDENTITY))) {
                        names_correct++;
                    }
                    names_count++;
//                } else if( countTransitiveSiblingPaths(std_id_x, std_id_z) >= SIBLING_TRANSITIVE_PATH_SUPPORT_THRESHOLD ) {
//                    System.out.println("Would establish link between " + std_id_x + " and " + std_id_z);
//                    if (x.getString(Birth.FATHER_IDENTITY).equals(z.getString(Birth.FATHER_IDENTITY))) {
//                        transitive_support_correct++;
//                    }
//                    transitive_link_count++;
                } else if (isHighDistance( open_triangle.xy_distance, open_triangle.yz_distance ) ) {  // only cut if distances are high
//                   cutLinks(open_triangle, x, y, z);
                    // This is not a good reason - what is?
                    System.out.println("Would leave link as is: " + std_id_x + " and " + std_id_z);
                    if (!(x.getString(Birth.FATHER_IDENTITY).equals(z.getString(Birth.FATHER_IDENTITY)))) {
                        as_is_correct++;
                    } else {
                        as_is_incorrect++;
                    }
                } else{
                    System.out.println("Would leave link as is: " + std_id_x + " and " + std_id_z);
                    if (!(x.getString(Birth.FATHER_IDENTITY).equals(z.getString(Birth.FATHER_IDENTITY)))) {
                        as_is_correct++;
                    } else {
                        as_is_incorrect++;
                    }
                }
            } else { // not plausible dates - cut??
                // need more analysis - 2 might be close and one outlier.
                // cutLinks(open_triangle, x, y, z);
                System.out.println("Would leave link as is: " + std_id_x + " and " + std_id_z);
                if (!(x.getString(Birth.FATHER_IDENTITY).equals(z.getString(Birth.FATHER_IDENTITY)))) {
                    as_is_correct++;
                } else {
                    as_is_incorrect++;
                }
            }
        } catch (BucketException e) {
            e.printStackTrace();
        }
    }

    private boolean surnamesStrictlyMatch(LXP x, LXP z) {
        return  x.getString( Birth.MOTHER_MAIDEN_SURNAME ).equals( z.getString( Birth.MOTHER_MAIDEN_SURNAME ) ) &&
                x.getString( Birth.FATHER_SURNAME ).equals( z.getString( Birth.FATHER_SURNAME) );

        //        x.getString( Birth.FATHER_FORENAME ).equals( z.getString( Birth.FATHER_FORENAME) ) &&
        //        x.getString( Birth.MOTHER_FORENAME ).equals( z.getString( Birth.MOTHER_FORENAME) );
    }

    private boolean allDifferent(LXP x, LXP y, LXP z) {
        return x.getId() != y.getId() && y.getId() != z.getId() && x.getId() != z.getId();
    }

    // Cut the biggest distance = perhaps use fields
    private void cutLinks( OpenTriangle open_triangle, LXP b_x, LXP b_y, LXP b_z) {   // TODO Cut both??? either 0 or 3.
        System.out.println("Would DO CUT XY");
        if (!b_x.getString(Birth.FATHER_IDENTITY).equals(b_y.getString(Birth.FATHER_IDENTITY))) {
            cut_link_correct++;
        }
        cut_count++;
        System.out.println("Would DO CUT YZ");
        if (!b_y.getString(Birth.FATHER_IDENTITY).equals(b_z.getString(Birth.FATHER_IDENTITY))) {
            cut_link_correct++;
        }
        cut_count++;
    }

    // Cut the biggest distance = perhaps use fields??
    private void cutOne( OpenTriangle open_triangle, LXP b_x, LXP b_y, LXP b_z) {
        if (open_triangle.xy_distance > open_triangle.yz_distance) {
            System.out.println("Would DO CUT XY");
            if (!b_x.getString(Birth.FATHER_IDENTITY).equals(b_y.getString(Birth.FATHER_IDENTITY))) {
                cut_link_correct++;
            }
            cut_count++;
        } else {
            System.out.println("Would DO CUT YZ");
            if (!b_y.getString(Birth.FATHER_IDENTITY).equals(b_z.getString(Birth.FATHER_IDENTITY))) {
                cut_link_correct++;
            }
            cut_count++;
        }
    }

    private boolean plausibleBirthDates(LXP a, LXP b, LXP c) {
        try {
            int a_birth_year = Integer.parseInt(a.getString(Birth.BIRTH_YEAR));
            int b_birth_year = Integer.parseInt(b.getString(Birth.BIRTH_YEAR));
            int c_birth_year = Integer.parseInt(c.getString(Birth.BIRTH_YEAR));

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
        result.addAll( getSiblings(bridge, BB_GET_SIBLINGS,std_id) );
        return result;
    }

    private double open_distance(OpenTriangle open_triangle) throws BucketException {
        return get_distance( open_triangle.x, open_triangle.z );
    }

    private double get_distance(long id1, long id2) throws BucketException {
        LXP b1 = (LXP) births.getObjectById(id1);
        LXP b2 = (LXP) births.getObjectById(id2);
        return metric.distance( b1, b2 );
    }

    // Queries

    /**
     * @return a Stream of OpenTriangles
     */
    public Stream<OpenTriangle> findIllegalBirthSiblingTriangles() {
        Result result = bridge.getNewSession().run(BIRTH_SIBLING_ILLEGAL_TRIANGLE_QUERY); // returns x,y,z where x and y and z are connected and zx is not.
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
        Result result = bridge.getNewSession().run(BB_GET_INDIRECT_SIBLING_LINKS,parameters);
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

            BirthSiblingSubsetLinkageRecipe linkageRecipe = new BirthSiblingSubsetLinkageRecipe(sourceRepo, resultsRepo, bridge, BirthSiblingBundleBuilder.class.getCanonicalName());
            SiblingBirthTriangleResolver resolver = new SiblingBirthTriangleResolver( bridge,sourceRepo,linkageRecipe );
            resolver.resolve();

        } catch (Exception e) {
            System.out.println( "Exception closing bridge" );
        } finally {
            System.out.println( "Run finished" );
            System.exit(0); // Make sure it all shuts down properly.
        }
    }

}

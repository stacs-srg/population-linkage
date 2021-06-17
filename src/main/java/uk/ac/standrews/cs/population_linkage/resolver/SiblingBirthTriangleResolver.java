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
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.*;
import java.util.stream.Stream;

public class SiblingBirthTriangleResolver {

    private final RecordRepository record_repository;
    private final NeoDbCypherBridge bridge;
    private final IBucket births;
    private final BirthSiblingSubsetLinkageRecipe recipe;

    private final JensenShannon base_metric;
    private final Metric<LXP> metric;

    private static final String BIRTH_SIBLING_ILLEGAL_TRIANGLE_QUERY = "MATCH (x:Birth)-[xy:SIBLING]-(y:Birth)-[yz:SIBLING]-(z:Birth) WHERE NOT (x)-[:SIBLING]-(z) return x,y,z,xy,yz";
    private static final String BB_GET_SIBLINGS = "MATCH (a:Birth)-[r:SIBLING]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";

    private int link_count = 0;// ALL thSEE ARE A HACK DELETE
    private int cut_count = 0;
    private int new_link_correct = 0;
    private int cut_link_correct = 0;
    private int as_is_correct = 0;
    private int as_is_incorrect = 0;
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
        System.out.println("Would have established: " + link_count + " correctly established = " + new_link_correct );
        System.out.println("Would have cut: " + cut_count + " correctly cut = " + cut_link_correct );
        System.out.println("As is correct: " + as_is_correct );
        System.out.println("As is incorrect: " + as_is_incorrect );
        System.out.println( "Total = " + count + " remain incorrect after intervention = " + ( count - new_link_correct - cut_link_correct - as_is_correct ) );
    }

    private void process(OpenTriangle open_triangle) {
        System.out.println(open_triangle.toString());
        try {

            LXP b_x = (LXP) births.getObjectById(open_triangle.x);
            LXP b_z = (LXP) births.getObjectById(open_triangle.z);
            String std_id_x = b_x.getString(Birth.STANDARDISED_ID);
            String std_id_z = b_z.getString(Birth.STANDARDISED_ID);

            count++;

            if( isLowDistance( open_triangle.xy_distance, open_triangle.yz_distance ) ) {
                // If distances are low then establish irrespective of other links
                System.out.println("Would establish link between " + std_id_x + " and " + std_id_z);
                if( b_x.getString( Death.FATHER_IDENTITY ).equals( b_z.getString( Death.FATHER_IDENTITY ) ) ) { new_link_correct++; }
                link_count++;
            } else if ( countLinksBetween(std_id_x, std_id_z) > 1 ) {
                // TODO could look at distances here and fields populated and do what?
                // we have support for the link so establish it
                // Query.createBBSiblingReference(NeoDbCypherBridge bridge, std_id_x, std_id_z, "open-triangle-processing",0,open_distance(open_triangle));
                System.out.println("Would establish link between " + std_id_x + " and " + std_id_z);
                if( b_x.getString( Birth.FATHER_IDENTITY ).equals( b_z.getString( Birth.FATHER_IDENTITY ) ) ) { new_link_correct++; }
                link_count++;
            } else if( isHighDistance( open_triangle.xy_distance, open_triangle.yz_distance ) ){  // only cut if distances are high
                System.out.println("Would CUT link between " + std_id_x + " and " + std_id_z);
                if( ! b_x.getString( Birth.FATHER_IDENTITY ).equals( b_z.getString( Birth.FATHER_IDENTITY ) ) ) { cut_link_correct++; }
                cut_count++;
            } else {
                System.out.println("Would leave link as is: " + std_id_x + " and " + std_id_z);
                if( !( b_x.getString( Birth.FATHER_IDENTITY ).equals( b_z.getString( Birth.FATHER_IDENTITY ) ) ) ) { as_is_correct++; } else { as_is_incorrect++; }
            }
        } catch (BucketException e) {
            e.printStackTrace();
        }
    }

    private boolean isLowDistance(double d1, double d2) {
        return d1 + d2 < 0.2;
    }

    private boolean isHighDistance(double d1, double d2) {
        return d1 + d2 > 1.2;
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
        result.addAll( getLinks(bridge,BB_GET_SIBLINGS,std_id) );
        return result;
    }

    private double open_distance(OpenTriangle open_triangle) throws BucketException {
        return get_BB_distance( open_triangle.x, open_triangle.z );
    }

    private double get_BB_distance(long birth_id1, long birth_id2) throws BucketException {
        LXP b1 = (LXP) births.getObjectById(birth_id1);
        LXP b2 = (LXP) births.getObjectById(birth_id2);
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

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

    private final RecordRepository record_repository;
    private final NeoDbCypherBridge bridge;
    private final IBucket births;
    private final BirthSiblingSubsetLinkageRecipe recipe;

    private final JensenShannon base_metric;
    private final Metric<LXP> metric;

    private static final String BIRTH_SIBLING_ILLEGAL_TRIANGLE_QUERY = "MATCH (x:Birth)-[xy:SIBLING]-(y:Birth)-[yz:SIBLING]-(z:Birth) WHERE NOT (x)-[:SIBLING]-(z) return x,y,z,xy,yz";
    private static final String BB_GET_SIBLINGS = "MATCH (a:Birth)-[r:SIBLING]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";

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
        oddballs.forEach( this::process );
    }


    static boolean first = true;

    private void process(OpenTriangle open_triangle) {
        if( first ) {
            System.out.println( open_triangle.toString() );
            try {

                Birth b_x = (Birth) births.getObjectById(open_triangle.x);
                Birth b_z = (Birth) births.getObjectById(open_triangle.z);
                String std_id_x = b_x.getString(Birth.STANDARDISED_ID);
                String std_id_z = b_z.getString(Birth.STANDARDISED_ID);
                int support = openLinkSupport(std_id_x,std_id_z);
                // TODO not used fields_populated!
                if( support > 1 ) {
                    // we have support for the link so establish it
                    // Query.createBBSiblingReference(NeoDbCypherBridge bridge, std_id_x, std_id_z, "open-triangle-processing",0,open_distance(open_triangle));
                    System.out.println( "Establish link" );
                } else {
                    // TODO - soem cutting?
                }
            } catch (BucketException e) {
                e.printStackTrace();
            }
            first = false;
        }
    }

    /**
     *
     * std_id_x - the standard id of node x
     * std_id_z - the standard id of node z
     * @return the number of links that x and z share
     * @throws BucketException
     */
    private int openLinkSupport(String std_id_x, String std_id_z) throws BucketException {
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
        Birth b1 = (Birth) births.getObjectById(birth_id1);
        Birth b2 = (Birth) births.getObjectById(birth_id2);
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

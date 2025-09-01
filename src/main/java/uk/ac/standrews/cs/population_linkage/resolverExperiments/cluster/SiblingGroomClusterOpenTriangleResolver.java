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
package uk.ac.standrews.cs.population_linkage.resolverExperiments.cluster;

import HierachicalClustering.AverageLinkage;
import HierachicalClustering.Cluster;
import HierachicalClustering.ClusterAlgorithm;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.GroomGroomSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.resolverExperiments.util.Distance;
import uk.ac.standrews.cs.population_linkage.resolverExperiments.util.OpenTriangle;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;

public class SiblingGroomClusterOpenTriangleResolver {

    public final int DEFAULT_CLUSTER_ELIGIBLE_FOR_SPLIT_SIZE = 7;
    private final double DEFAULT_LOW_DISTANCE_MATCH_THRESHOLD = 0.1;
    private final double DEFAULT_HIGH_DISTANCE_REJECT_THRESHOLD = 0.6;

    private final RecordRepository record_repository;
    private final NeoDbCypherBridge bridge;
    private final IBucket marriages;
    private final GroomGroomSiblingLinkageRecipe recipe;

    private final StringMeasure base_measure;
    private final LXPMeasure composite_measure;

    static String GROOM_SIBLING_TRIANGLE_QUERY = "MATCH (x:Marriage)-[xy:SIBLING]-(y:Marriage)-[yz:SIBLING]-(z:Marriage) WHERE NOT (x)-[:SIBLING]-(z) return x,y,z,xy,yz";
    private static final String MM_GET_SIBLINGS = "MATCH (a:Marriage)-[r:SIBLING]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";

    private static final String MM_GET_INDIRECT_SIBLING_LINKS = "MATCH (a:Marriage)-[r:SIBLING*1..5]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to RETURN r";

    private int cluster_eligible_for_split_size = DEFAULT_CLUSTER_ELIGIBLE_FOR_SPLIT_SIZE;
    private double low_distance_match_threshold = DEFAULT_LOW_DISTANCE_MATCH_THRESHOLD;
    private double high_distance_reject_threshold = DEFAULT_HIGH_DISTANCE_REJECT_THRESHOLD;

    private int count = 0;
    private int TP = 0;
    private int FN = 0;
    private int FP = 0;

    DecimalFormat df = new DecimalFormat("0.00");

    public SiblingGroomClusterOpenTriangleResolver(NeoDbCypherBridge bridge, String source_repo_name, GroomGroomSiblingLinkageRecipe recipe) {
        this.bridge = bridge;
        this.recipe = recipe;
        this.record_repository = new RecordRepository(source_repo_name);
        this.marriages = record_repository.getBucket("marriage_records");
        this.base_measure = Constants.JENSEN_SHANNON;
        this.composite_measure = getCompositeMetric(recipe);
    }

    protected LXPMeasure getCompositeMetric(final LinkageRecipe linkageRecipe) {
        return new SumOfFieldDistances(base_measure, linkageRecipe.getLinkageFields());
    }

    protected void resolve(int min_cluster_size, double ldmt, double hdrt) {
        cluster_eligible_for_split_size = min_cluster_size;
        low_distance_match_threshold = ldmt;
        high_distance_reject_threshold = hdrt;
        resolve();
    }

    private void resolve() {
        Stream<OpenTriangle> oddballs = findIllegalmarriagesiblingTriangles();
        oddballs.forEach(this::process);
        printResults();
    }

    protected static void printHeaders() {
        System.out.println("CLUSTER_ELIGIBLE_FOR_SPLIT_SIZE\tLOW_DISTANCE_MATCH_THRESHOLD\tHIGH_DISTANCE_REJECT_THRESHOLD\tCount\tTP\tFP\tFN\tprecision\trecall\tF1");
    }

    private void printResults() {
        double precision = ClassificationMetrics.precision(TP, FP);
        double recall = ClassificationMetrics.recall(TP, FN);
        double f1 = ClassificationMetrics.F1(TP, FP, FN);
        System.out.println(
                cluster_eligible_for_split_size + "\t" + low_distance_match_threshold + "\t" + high_distance_reject_threshold + "\t" +
                        +count + "\t" + TP + "\t" + FP + "\t" + FN + "\t" +
                        df.format(precision) + "\t" + df.format(recall) + "\t" + df.format(f1));
    }

    private void process(OpenTriangle open_triangle) {

        try {

            count++;

            LXP x = (LXP) marriages.getObjectById(open_triangle.x);
            LXP y = (LXP) marriages.getObjectById(open_triangle.y);
            LXP z = (LXP) marriages.getObjectById(open_triangle.z);
            String std_id_x = x.getString(Marriage.STANDARDISED_ID);
            String std_id_y = y.getString(Marriage.STANDARDISED_ID);
            String std_id_z = z.getString(Marriage.STANDARDISED_ID);

            if (!allDifferent(x, y, z)) {  // They might all be the same person with different ids - how to fix that?
                return;
            }

            Set<Distance> all_pairs_between = getTransitiveSiblingPaths(std_id_x, std_id_z);
//            System.out.println( "ALL PAIRS COUNT = " + all_pairs_between.size() );
            Set<String> all_node_ids = getIds(all_pairs_between);
//            System.out.println( "Nodes in cluster = " + all_node_ids.size() );

            // All Clusters are referenced using Neo4J Id space.

            // Average linkage minimizes the average of the distances between all observations of pairs of clusters.
            // Single linkage minimizes the distance between the closest observations of pairs of clusters.
            // Use average.
            AverageLinkage<String> linkage = new AverageLinkage<>(this::getDistanceNyNeoId);

            ClusterAlgorithm<String> ca = new ClusterAlgorithm<>(all_node_ids, linkage, 9);
            ca.cluster();
            Cluster<String> top_cluster = ca.getFirstCluster();
            //showDistances( top_cluster,"T" );
            analyseClusters(top_cluster, all_pairs_between);
        } catch (BucketException e) {
            e.printStackTrace();
        }
    }

    private void analyseClusters(Cluster<String> cluster, Set<Distance> all_pairs_between) throws BucketException {
        if (cluster.size > cluster_eligible_for_split_size) {
            if (cluster.distance > high_distance_reject_threshold && oneSubClustersIsTight(cluster)) {
                splitCluster(cluster, all_pairs_between);
            } else {
                DoNotsplitCluster(cluster, all_pairs_between);
            }
            analyseClusters(cluster.left_child, all_pairs_between);
            analyseClusters(cluster.right_child, all_pairs_between);
        }
    }

    /**
     * Do not split cluster analyse to see if any links are not appropriate.
     *
     * @param cluster
     * @param all_pairs_between
     * @throws BucketException
     */
    private void DoNotsplitCluster(Cluster<String> cluster, Set<Distance> all_pairs_between) throws BucketException {
        Collection<String> left_elements = cluster.left_child.getClusterElements();
        Collection<String> right_elements = cluster.right_child.getClusterElements();
        for (String neo_id_left : left_elements) {
            for (String neo_id_right : right_elements) {
                for (Distance d : all_pairs_between) {
                    if ((d.startNodeId == neo_id_left || d.startNodeId == neo_id_right) && (d.endNodeId == neo_id_left || d.endNodeId == neo_id_right)) {
                        LXP x = (LXP) marriages.getObjectById(getStorrId(neo_id_left));
                        LXP y = (LXP) marriages.getObjectById(getStorrId(neo_id_right));
                        if (!x.getString(Marriage.GROOM_FATHER_IDENTITY).equals(y.getString(Marriage.GROOM_FATHER_IDENTITY))) {
                            // if they don't have the same fathers we should have split so FN.
                            FN++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Splits the cluster supplied as a parameter into two by cutting links.
     *
     * @param cluster
     * @param all_pairs_between
     */
    private void splitCluster(Cluster<String> cluster, Set<Distance> all_pairs_between) throws BucketException {
        Collection<String> left_elements = cluster.left_child.getClusterElements();
        Collection<String> right_elements = cluster.right_child.getClusterElements();
        for (String neo_id_left : left_elements) {
            for (String neo_id_right : right_elements) {
                cutClusterLinksFromAllPairs(all_pairs_between, neo_id_left, neo_id_right);
            }
        }
    }

    private void cutClusterLinksFromAllPairs(Set<Distance> all_pairs_between, String neo_id_left, String neo_id_right) throws BucketException {
        for (Distance d : all_pairs_between) {
            if ((d.startNodeId == neo_id_left || d.startNodeId == neo_id_right) && (d.endNodeId == neo_id_left || d.endNodeId == neo_id_right)) {
                cutLink(neo_id_left, neo_id_right, "Cut cluster");
            }
        }
    }

    /**
     * @param cluster
     * @return true if either or both of the sub clusters internal distance < LOW_DISTANCE_MATCH_THRESHOLD
     */
    private boolean oneSubClustersIsTight(Cluster<String> cluster) {
        Cluster<String> left_cluster = cluster.left_child;
        Cluster<String> right_cluster = cluster.right_child;
        return left_cluster != null && left_cluster.size >= 2 && left_cluster.distance < low_distance_match_threshold ||
                right_cluster != null && right_cluster.size >= 2 && right_cluster.distance < low_distance_match_threshold;
    }

    private void showDistances(Cluster<String> top_cluster, String symbol) {
        if (top_cluster.size > 1) {
            double distance = top_cluster.distance;
            System.out.println(symbol + " Cluster distance: " + distance);
            System.out.println(symbol + " Cluster Size:     " + top_cluster.getClusterElements().size());
            System.out.println(symbol + " Cluster elements: " + top_cluster.getClusterElements());
            Cluster<String> left_cluster = top_cluster.left_child;
            Cluster<String> right_cluster = top_cluster.right_child;
            showDistances(left_cluster, symbol + "\tL");
            showDistances(right_cluster, symbol + "\tR");
        }
    }

    private Set<String> getIds(Set<Distance> all_pairs_between) {
        Set<String> all_ids = new TreeSet<>();
        for (Distance d : all_pairs_between) {
            all_ids.add(d.startNodeId);
            all_ids.add(d.endNodeId);
        }
        return all_ids;
    }

    private boolean allDifferent(LXP x, LXP y, LXP z) {
        return x.getId() != y.getId() && y.getId() != z.getId() && x.getId() != z.getId();
    }

    // Cut the biggest distance = perhaps use fields
    private void cutLinks(OpenTriangle open_triangle, LXP b_x, LXP b_y, LXP b_z) {   // TODO Cut both??? either 0 or 3.
        cutLink(b_x, b_y, "Would DO CUT XY");
        cutLink(b_y, b_z, "Would DO CUT YZ");
    }

    // Cut the biggest distance = perhaps use fields??
    private void cutOne(OpenTriangle open_triangle, LXP b_x, LXP b_y, LXP b_z) {
        if (open_triangle.xy_distance > open_triangle.yz_distance) {
            cutLink(b_x, b_y, "Would DO CUT XY");
        } else {
            cutLink(b_y, b_z, "Would DO CUT YZ");
        }
    }

    private void cutLink(LXP x, LXP y, String message) {
        if (!x.getString(Marriage.GROOM_FATHER_IDENTITY).equals(y.getString(Marriage.GROOM_FATHER_IDENTITY))) {   // Cut links should have different fathers!
            TP++;
        } else {
            FP++;
        }
    }

    private int counter = 0; // debug tracer - triggers printout of query.

    private void cutLink(LXP x, LXP y, String nid_x, String nid_y, String message) {
//        System.out.println(message);
        if (!x.getString(Marriage.GROOM_FATHER_IDENTITY).equals(y.getString(Marriage.GROOM_FATHER_IDENTITY))) {   // Cut links should have different fathers!
            TP++;
        } else {
            FP++;
            counter++;
            if (counter == 100) {
                // Debug method only
                System.out.println("MATCH (x:Marriage)-[r:SIBLING]-(y:Marriage) WHERE ID(x) = " + nid_x + " AND ID(y) = " + nid_y + " RETURN x,y");
            }
        }
    }

    /**
     * @param nid_x   - the neo4J id of an object
     * @param nid_x   - the neo4J id of an object
     */
    private void cutLink(String nid_x, String nid_y, String message) throws BucketException {
        LXP x = (LXP) marriages.getObjectById(getStorrId(nid_x));
        LXP y = (LXP) marriages.getObjectById(getStorrId(nid_y));
        cutLink(x, y, message);
    }

    private boolean isLowDistance(double d1, double d2) {
        return d1 + d2 < low_distance_match_threshold;  // count be determined properly by a human (or AI) inspecting these.
    }

    private Set<String> getSiblingIds(String std_id) {
        return new HashSet<>(getSiblings(bridge, MM_GET_SIBLINGS, std_id));
    }

    private double openDistance(OpenTriangle open_triangle) {
        return getDistanceNyNeoId(open_triangle.x, open_triangle.z);
    }

    private double getDistanceNyNeoId(String id1, String id2) {
        try {
            LXP b1 = (LXP) marriages.getObjectById(getStorrId(id1));
            LXP b2 = (LXP) marriages.getObjectById(getStorrId(id2));
            return composite_measure.distance(b1, b2);
        } catch (BucketException e) {
            return -1L; // can't throw exception and use as Lambda
        }
    }

    private static final String NODE_BY_NEO_ID = "MATCH (a:Marriage) WHERE Id( a ) = $node_id RETURN a";

    private String getStorrId(String id) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("node_id", id);
        Result result = bridge.getNewSession().run(NODE_BY_NEO_ID, parameters);
        List<Node> nodes = result.list(r -> r.get("a").asNode());
        return nodes.get(0).get("STORR_ID").asString();
    }

    // Queries

    /**
     * @return a Stream of OpenTriangles
     */
    public Stream<OpenTriangle> findIllegalmarriagesiblingTriangles() {
        Result result = bridge.getNewSession().run(GROOM_SIBLING_TRIANGLE_QUERY); // returns x,y,z where x and y and z are connected and zx is not.
        return result.stream().map(r -> {
                    return new OpenTriangle(
                            ((Node) r.asMap().get("x")).get("STORR_ID").asString(),
                            ((Node) r.asMap().get("y")).get("STORR_ID").asString(),
                            ((Node) r.asMap().get("z")).get("STORR_ID").asString(),
                            ((Relationship) r.asMap().get("xy")).get("distance").asDouble(),
                            ((Relationship) r.asMap().get("yz")).get("distance").asDouble()
                    );
                }
        );
    }

    private Set<Distance> getTransitiveSiblingPaths(String standard_id_from, String standard_id_to) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        Result result = bridge.getNewSession().run(MM_GET_INDIRECT_SIBLING_LINKS, parameters);

        Set<Distance> set = new HashSet<>();
        List<Record> results = result.list();
        for (Record record : results) {
            List<Object> rels = record.get("r").asList();
            for (Object o : rels) {
                Relationship r = (Relationship) o;
                set.add(new Distance(r.startNodeElementId(), r.endNodeElementId(), (Double) r.asMap().get("distance")));
            }
        }
        return set;
    }

    private static List<String> getSiblings(NeoDbCypherBridge bridge, String query_string, String standard_id_from) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        Result result = bridge.getNewSession().run(query_string, parameters);
        return result.list(r -> r.get("b").get("STORR_ID").asString());
    }


    public static void main(String[] args) {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge();
            GroomGroomSiblingLinkageRecipe linkageRecipe = new GroomGroomSiblingLinkageRecipe(sourceRepo, resultsRepo, BirthSiblingBundleBuilder.class.getName())) {
            SiblingGroomClusterOpenTriangleResolver resolver = new SiblingGroomClusterOpenTriangleResolver(bridge, sourceRepo, linkageRecipe);

            printHeaders();

            for (int min_cluster = 2; min_cluster < 10; min_cluster++) {
                for (double hdrt = 2; hdrt < 8; hdrt += 1) {
                    for (double ldrt = 10; ldrt < 40; ldrt += 5) {
                        resolver.resolve(min_cluster, ldrt / 100d, hdrt / 10d);
                    }
                }
            }
            //              resolver.resolve();

        } catch (Exception e) {
            System.out.println("Exception closing bridge");
        } finally {
            System.out.println("Run finished");
            System.exit(0); // Make sure it all shuts down properly.
        }
    }

}

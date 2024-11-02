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
package uk.ac.standrews.cs.population_linkage.aleks;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.types.Node;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.Store;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthGroomOwnMarriageBundleAccuracy;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.*;


public class BirthMarriageIDOpenTriangleResolver {
    private static NeoDbCypherBridge bridge;


    //Cypher queries used in predicates
    private static final String BB_SIBLING_QUERY_DEL = "MATCH (a:Birth)-[r:SIBLING]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to DELETE r";
    private static final String BM_ID_QUERY_DEL_PROV = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:DELETED { provenance: $prov, actors: $actor } ]-(b)";

    //Names of predicates to be used as prov
    private static final String[] creationPredicates = {};
    private static final String[] deletionPredicates = {"diff_birth"};

    public static void main(String[] args) throws BucketException {
        bridge = Store.getInstance().getBridge();
        RecordRepository record_repository = new RecordRepository("umea");
        final StringMeasure base_measure = Constants.LEVENSHTEIN;
        final StringMeasure base_measure_n = Constants.JENSEN_SHANNON;
        IBucket births = record_repository.getBucket("birth_records");
        IBucket marriages = record_repository.getBucket("marriage_records");

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Birth", "Marriage"); //get number of triangles before resolution
        new BirthGroomOwnMarriageBundleAccuracy(bridge);

        System.out.println("Locating triangles...");
        List<Long[]> triangles = findIllegalBirthMarriageTriangles(bridge, "Groom"); //get all open triangles in their clusters
        System.out.println("Triangles found: " + triangles.size());

        System.out.println("Resolving triangles with predicates...");
        for (Long[] triangle : triangles) {
            LXP[] tempKids = {(LXP) births.getObjectById(triangle[0]), (LXP) marriages.getObjectById(triangle[1]), (LXP) births.getObjectById(triangle[2])};
            String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
            String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
            String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

            for (int i = 0; i < triangle.length; i+=2) {
                if(!Objects.equals(tempKids[0].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Marriage.GROOM_AGE_OR_DATE_OF_BIRTH), "--/--/----")){
                    if(!Objects.equals(tempKids[0].getString(Birth.BIRTH_YEAR), tempKids[1].getString(Marriage.GROOM_AGE_OR_DATE_OF_BIRTH).substring(6))){
                        deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[0]);
                    }
                }
            }
        }

        System.out.println("After");
        System.out.println("\n");
        new PredicateEfficacy(creationPredicates, deletionPredicates, "Birth", "Marriage"); //get efficacy of each predicate
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Birth", "Marriage"); //count number of open triangles after resolution
        new BirthGroomOwnMarriageBundleAccuracy(bridge);
    }

    /**
     * Method to locate all open triangles in the database
     *
     * @param bridge Neo4j Bridge
     * @return List of open triangle clusters
     */
    private static List<Long[]> findIllegalBirthMarriageTriangles(NeoDbCypherBridge bridge, String partner) {
        final String BIRTH_MARRIAGE_TRIANGLE_QUERY = String.format("MATCH (x:Birth)-[:ID {actors: \"Child-%1$s\"}]-(y:Marriage)-[:ID {actors: \"Child-%1$s\"}]-(z:Birth)\n" +
                "WHERE id(x) < id(z) AND NOT (x)-[:DELETED]-(y) AND NOT (z)-[:DELETED]-(y)\n" +
                "RETURN x, y, z", partner);

        //run query to get all open triangles
        Result result = bridge.getNewSession().run(BIRTH_MARRIAGE_TRIANGLE_QUERY);
        List<Long[]> triangles = new ArrayList<>();
        result.stream().forEach(r -> {
            long x = ((Node) r.asMap().get("x")).get("STORR_ID").asLong();
            long y = ((Node) r.asMap().get("y")).get("STORR_ID").asLong();
            long z = ((Node) r.asMap().get("z")).get("STORR_ID").asLong();

            Long[] tempList = {x, y, z};
            triangles.add(tempList);
        });

        return triangles;
    }

    /**
     * Method to create a delete link between two records, used in testing
     *
     * @param bridge Neo4j bridge
     * @param std_id_x standardised id of record x
     * @param std_id_y standardised id of record y
     */
    private static void deleteLink(NeoDbCypherBridge bridge, String std_id_x, String std_id_y, String prov){
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            Map<String, Object> parameters = getCreationParameterMap(std_id_x, std_id_y, prov);
            tx.run(BM_ID_QUERY_DEL_PROV, parameters);
            tx.commit();
        }
    }

    /**
     * Method to get map of parameters to be used in cypher queries
     *
     * @param standard_id_from record ID to link from
     * @param standard_id_to record ID to link to
     * @param prov provenance of resolver
     * @return map of parameters
     */
    private static Map<String, Object> getCreationParameterMap(String standard_id_from, String standard_id_to, String prov) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        parameters.put("prov", prov);
        return parameters;
    }

}

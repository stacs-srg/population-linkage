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
package uk.ac.standrews.cs.population_linkage.aleks.resolvers;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.types.Node;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.Store;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthBrideOwnMarriageAccuracy;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthGroomOwnMarriageBundleAccuracy;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe.list;


public class BirthMarriageIDOpenTriangleResolver {
    private static NeoDbCypherBridge bridge;

    private static final int MIN_MARRIAGE_AGE = 16;
    private static final int MAX_MARRIAGE_AGE = 50;
    private static final double NAME_THRESHOLD = 0.5;

    //Cypher queries used in predicates
    private static final String BB_SIBLING_QUERY_DEL = "MATCH (a:Birth)-[r:SIBLING]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to DELETE r";
    private static final String BM_ID_QUERY_DEL_PROV = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:DELETED { provenance: $prov, actors: $actor } ]-(b)";

    //Names of predicates to be used as prov
    private static final String[] creationPredicates = {};
    private static final String[] deletionPredicates = {"diff_birth", "born_after", "too_young", "too_old", "bad_name"};

    public static void main(String[] args) throws BucketException {
        bridge = Store.getInstance().getBridge();
        RecordRepository record_repository = new RecordRepository("umea");
        final StringMeasure base_measure = Constants.JENSEN_SHANNON;
        LXPMeasure composite_measure;
        IBucket births = record_repository.getBucket("birth_records");
        IBucket marriages = record_repository.getBucket("marriage_records");
        String[] partners = {"Groom", "Bride"};

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Birth", "Marriage"); //get number of triangles before resolution
        new BirthGroomOwnMarriageBundleAccuracy(bridge);
        new BirthBrideOwnMarriageAccuracy(bridge);

        for (String partner : partners) {
            composite_measure = getCompositeMeasureBirthMarriage(base_measure, partner);
            System.out.println("Resolving " + partner);
            System.out.println("Locating triangles...");
            List<Long[]> triangles = findIllegalBirthMarriageTriangles(bridge, partner); //get all open triangles in their clusters
            System.out.println("Triangles found: " + triangles.size());

            System.out.println("Resolving triangles with predicates...");
            for (Long[] triangle : triangles) {
                boolean isDeleted = false;
                LXP[] tempKids = {(LXP) births.getObjectById(triangle[0]), (LXP) marriages.getObjectById(triangle[1]), (LXP) births.getObjectById(triangle[2])};
                String[] stds = {tempKids[0].getString(Birth.STANDARDISED_ID), tempKids[1].getString(Birth.STANDARDISED_ID), tempKids[2].getString(Birth.STANDARDISED_ID)};

                int day = 1;
                if(!Objects.equals(tempKids[1].getString(Marriage.MARRIAGE_DAY), "--")){
                    day = Integer.parseInt(tempKids[1].getString(Marriage.MARRIAGE_DAY));
                }
                int month = 1;
                if(!Objects.equals(tempKids[1].getString(Marriage.MARRIAGE_MONTH), "--")){
                    month = Integer.parseInt(tempKids[1].getString(Marriage.MARRIAGE_MONTH));
                }

                LocalDate dateM = null;
                if(!Objects.equals(tempKids[1].getString(Marriage.MARRIAGE_YEAR), "----")){
                    dateM = LocalDate.of(Integer.parseInt(tempKids[1].getString(Marriage.MARRIAGE_YEAR)), month, day);
                }

                for (int i = 0; i < triangle.length; i += 2) {
                    day = 1;
                    if(!Objects.equals(tempKids[i].getString(Birth.BIRTH_DAY), "--")){
                        day = Integer.parseInt(tempKids[i].getString(Birth.BIRTH_DAY));
                    }
                    month = 1;
                    if(!Objects.equals(tempKids[i].getString(Birth.BIRTH_MONTH), "--")){
                        month = Integer.parseInt(tempKids[i].getString(Birth.BIRTH_MONTH));
                    }

                    LocalDate date = null;
                    if(!Objects.equals(tempKids[i].getString(Birth.BIRTH_YEAR), "----")){
                        date = LocalDate.of(Integer.parseInt(tempKids[i].getString(Birth.BIRTH_YEAR)), month, day);
                    }

                    //1. Match birthdays
                    if(partner.equals("Groom")){
                        if (!Objects.equals(tempKids[i].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Marriage.GROOM_AGE_OR_DATE_OF_BIRTH).substring(6), "----")) {
                            if (!Objects.equals(tempKids[i].getString(Birth.BIRTH_YEAR), tempKids[1].getString(Marriage.GROOM_AGE_OR_DATE_OF_BIRTH).substring(6))) {
                                deleteLink(bridge, stds[i], stds[1], partner, deletionPredicates[0]);
                                isDeleted = true;
                            }
                        }
                    }else{
                        if (!Objects.equals(tempKids[i].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH).substring(6), "----")) {
                            if (!Objects.equals(tempKids[i].getString(Birth.BIRTH_YEAR), tempKids[1].getString(Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH).substring(6))) {
                                deleteLink(bridge, stds[i], stds[1], partner, deletionPredicates[0]);
                                isDeleted = true;
                            }
                        }
                    }

                    if (!isDeleted && date != null && dateM != null) { //2. If born after marriage
                        if (date.isAfter(dateM)) {
                            deleteLink(bridge, stds[i], stds[1], partner, deletionPredicates[1]);
                            isDeleted = true;
                        } else if (ChronoUnit.YEARS.between(date, dateM) >= 0 && //3. If too young to marry
                                ChronoUnit.YEARS.between(date, dateM) < MIN_MARRIAGE_AGE) {
                            deleteLink(bridge, stds[i], stds[1], partner, deletionPredicates[2]);
                            isDeleted = true;
                        } else if (ChronoUnit.YEARS.between(date, dateM) >= 0 && //4. If born way before marriage
                                ChronoUnit.YEARS.between(date, dateM)> MAX_MARRIAGE_AGE) {
                            deleteLink(bridge, stds[i], stds[1], partner, deletionPredicates[3]);
                            isDeleted = true;
                        }
                    }

                    //5. Check names
                    if (!isDeleted && getDistance(tempKids[i], tempKids[1], composite_measure) > NAME_THRESHOLD) {
                        deleteLink(bridge, stds[i], stds[1], partner, deletionPredicates[4]);
                    }
                }
            }
        }

        System.out.println("After");
        System.out.println("\n");
        PredicateEfficacy pef = new PredicateEfficacy(); //get efficacy of each predicate
        for (String partner : partners) {
            System.out.println("\n" + partner + " efficacy:");
            pef.countIDEfficacy(deletionPredicates, "Birth", "Marriage", "Child-" + partner);
        }
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Birth", "Marriage"); //count number of open triangles after resolution
        new BirthGroomOwnMarriageBundleAccuracy(bridge);
        new BirthBrideOwnMarriageAccuracy(bridge);
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
    private static void deleteLink(NeoDbCypherBridge bridge, String std_id_x, String std_id_y, String actor, String prov){
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            Map<String, Object> parameters = getCreationParameterMap(std_id_x, std_id_y, prov, actor);
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
    private static Map<String, Object> getCreationParameterMap(String standard_id_from, String standard_id_to, String prov, String actor) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        parameters.put("prov", prov);
        parameters.put("actor", actor);
        return parameters;
    }

    protected static LXPMeasure getCompositeMeasureBirthMarriage(StringMeasure base_measure, String partner) {
        final List<Integer> LINKAGE_FIELDS_BIRTH = list(
                Birth.FORENAME,
                Birth.SURNAME
        );

        final List<Integer> LINKAGE_FIELDS_MARRIAGE;

        if(Objects.equals(partner, "Groom")){
            LINKAGE_FIELDS_MARRIAGE = list(
                    Marriage.GROOM_FORENAME,
                    Marriage.GROOM_SURNAME
            );
        }else{
            LINKAGE_FIELDS_MARRIAGE = list(
                    Marriage.BRIDE_FORENAME,
                    Marriage.BRIDE_SURNAME
            );
        }


        return new SumOfFieldDistances(base_measure, LINKAGE_FIELDS_BIRTH, LINKAGE_FIELDS_MARRIAGE);
    }

    private static double getDistance(LXP id1, LXP id2, LXPMeasure composite_measure) throws BucketException {
        return composite_measure.distance(id1, id2);
    }

}

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
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthBirthSiblingAccuracy;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe.list;

public class ComplexBBPattern {

    final static int NUM_OF_CHILDREN  = 12;
    final static int MAX_AGE_DIFFERENCE  = 20;
    private static final String BB_SIBLING_QUERY = "MATCH (a:Birth), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:SIBLING { provenance: $prov, actors: \"Child-Child\" } ]-(b)";
    private static final String BB_SIBLING_QUERY_DEL = "MATCH (a:Birth)-[r:SIBLING]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to DELETE r";
    private static final String BB_SIBLING_QUERY_DEL_PROV = "MATCH (a:Birth), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:DELETED { provenance: $prov, actors: \"Child-Child\" } ]-(b)";

    private static NeoDbCypherBridge bridge;

    //counters for predicates
    private static int maxAgeCount = 0;
    private static int nineMonthsCount = 0;
    private static int badMarriageDateCount = 0;
    private static int newLinks = 0;
    private static int birthplaceCount = 0;

    private static String[] creationPredicates = {"match_m_date"};
    private static String[] deletionPredicates = {"max_age_range", "min_b_interval", "birthplace_mode"};

    public static void main(String[] args) throws BucketException {
        bridge = Store.getInstance().getBridge();
        RecordRepository record_repository = new RecordRepository("umea");
        final StringMeasure base_measure = Constants.LEVENSHTEIN;;
        final LXPMeasure composite_measure_name = getCompositeMeasure(base_measure);
        final LXPMeasure composite_measure_date = getCompositeMeasureDate(base_measure);
        IBucket births = record_repository.getBucket("birth_records");

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Birth");
        new BirthBirthSiblingAccuracy(bridge);

        System.out.println("Locating triangles...");
        List<OpenTriangleClusterBB> triangles = findIllegalBirthDeathSiblingTriangles(bridge);

        System.out.println("Resolving triangles...");
        for (OpenTriangleClusterBB triangle : triangles) {
            for (List<Long> chain : triangle.getTriangleChain()){
                LXP[] tempKids = {(LXP) births.getObjectById(triangle.x), (LXP) births.getObjectById(chain.get(0)), (LXP) births.getObjectById(chain.get(1))};
                String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
                String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
                String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

                if(Objects.equals(tempKids[0].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----") ||
                        Objects.equals(tempKids[2].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----")){
                    triangle.getYearStatistics();
                    boolean hasChanged = false;

                    //1. Check age of child not outside of max difference
                    hasChanged = maxRangePredicate(triangle, tempKids, hasChanged, 0);

                    //2. check DOB at least 9 months away from rest
                    hasChanged = minBirthIntervalPredicate(triangle, tempKids, hasChanged, 1);

//                    if(Objects.equals(std_id_z, "907863")){
//                        System.out.println("fsd");
//                    }

                    //3. Get mode of birthplace
                    hasChanged = mostCommonBirthPlacePredicate(triangle, hasChanged, tempKids, 2);

                } else if (getDistance(triangle.x, chain.get(1), composite_measure_date, births) < 2) {
                    createLink(bridge, std_id_x, std_id_z, creationPredicates[0]);
                    newLinks++;
                }else{
//                    deleteLink(bridge, std_id_x, std_id_y);
                    badMarriageDateCount++;
                }
            }
        }

        System.out.println("After");
        System.out.println("Bad Marriage Date: " + badMarriageDateCount);
        System.out.println("Max Age Difference " + maxAgeCount);
        System.out.println("9 Months minimum " + nineMonthsCount);
        System.out.println("Birth place " + birthplaceCount);
        System.out.println("New links " + newLinks);
        System.out.println("\n");
        new PredicateEfficacy(creationPredicates, deletionPredicates, "Birth", "Birth");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Birth");
        new BirthBirthSiblingAccuracy(bridge);
    }

    private static List<OpenTriangleClusterBB> findIllegalBirthDeathSiblingTriangles(NeoDbCypherBridge bridge) {
        final String BIRTH_SIBLING_TRIANGLE_QUERY = "MATCH (x:Birth)-[:SIBLING]-(y:Birth)-[:SIBLING]-(z:Birth)\n" +
                "WHERE NOT (x)-[:SIBLING]-(z)\n" +
                "RETURN x, collect([y, z]) AS openTriangles";
        Result result = bridge.getNewSession().run(BIRTH_SIBLING_TRIANGLE_QUERY);
        return result.stream().map(r -> {
            long x = ((Node) r.asMap().get("x")).get("STORR_ID").asLong();
            List<List<Node>> openTrianglesNodes = (List<List<Node>>) r.asMap().get("openTriangles");

            List<List<Long>> openTrianglesList = openTrianglesNodes
                    .stream()
                    .map(innerList -> innerList.stream()
                            .map(obj -> {
                                if (obj instanceof Node) {
                                    return ((Node) obj).get("STORR_ID").asLong();
                                } else {
                                    throw new IllegalArgumentException("Expected a Node but got: " + obj.getClass());
                                }
                            })
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());

            return new OpenTriangleClusterBB(x, openTrianglesList);
        }).collect(Collectors.toList());
    }

    private static boolean maxRangePredicate(OpenTriangleClusterBB triangle, LXP[] tempKids, boolean hasChanged, int predNumber) {
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        if(!Objects.equals(tempKids[0].getString(Birth.BIRTH_YEAR), "----") && Math.abs(triangle.getYearMedian() - Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE){
//                        deleteLink(bridge, std_id_x, std_id_y);
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber]);
            maxAgeCount++;
            hasChanged = true;
        } else if (!Objects.equals(tempKids[1].getString(Birth.BIRTH_YEAR), "----") && Math.abs(triangle.getYearMedian() - Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE) {
//                        deleteLink(bridge, std_id_z, std_id_y);
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber]);
            maxAgeCount++;
            hasChanged = true;
        } else if (!Objects.equals(tempKids[2].getString(Birth.BIRTH_YEAR), "----") && Math.abs(triangle.getYearMedian() - Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE) {
//                        deleteLink(bridge, std_id_z, std_id_y);
//                        deleteLink(bridge, std_id_x, std_id_y);
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber]);
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber]);
            maxAgeCount++;
            hasChanged = true;
        }

        return hasChanged;
    }

    //https://stackoverflow.com/a/67767630
    private static boolean minBirthIntervalPredicate(OpenTriangleClusterBB triangle, LXP[] tempKids, boolean hasChanged, int predNumber) {
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        try{
            LocalDate dateX = LocalDate.of(Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR)), Integer.parseInt(tempKids[0].getString(Birth.BIRTH_MONTH)), Integer.parseInt(tempKids[0].getString(Birth.BIRTH_DAY)));
            Optional<LocalDate> closestDateX = triangle.getBirthDays().stream().sorted(Comparator.comparingLong(x -> Math.abs(ChronoUnit.DAYS.between(x, dateX))))
                    .skip(1)
                    .findFirst();
            if(!hasChanged && closestDateX.isPresent() && ChronoUnit.DAYS.between(closestDateX.get(), dateX) < 280 && ChronoUnit.DAYS.between(closestDateX.get(), dateX) > 2){
//                            deleteLink(bridge, std_id_x, std_id_y);
                deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber]);
                nineMonthsCount++;
                hasChanged = true;
            }
        }catch (Exception e){

        }

        try{
            LocalDate dateZ = LocalDate.of(Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR)), Integer.parseInt(tempKids[2].getString(Birth.BIRTH_MONTH)), Integer.parseInt(tempKids[2].getString(Birth.BIRTH_DAY)));
            Optional<LocalDate> closestDateZ = triangle.getBirthDays().stream().sorted(Comparator.comparingLong(x -> Math.abs(ChronoUnit.DAYS.between(x, dateZ))))
                    .skip(1)
                    .findFirst();
            if(!hasChanged && closestDateZ.isPresent() && ChronoUnit.DAYS.between(closestDateZ.get(), dateZ) < 280 && ChronoUnit.DAYS.between(closestDateZ.get(), dateZ) > 2){
//                            deleteLink(bridge, std_id_z, std_id_y);
                deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber]);
                nineMonthsCount++;
                hasChanged = true;
            }
        } catch (Exception e) {

        }

        return hasChanged;
    }

    private static boolean mostCommonBirthPlacePredicate(OpenTriangleClusterBB triangle, boolean hasChanged, LXP[] tempKids, int predNumber) {
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        if(!hasChanged && !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), "") && !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), triangle.getMostCommonBirthplace()) && triangle.getNumOfChildren() > 3){
//                        deleteLink(bridge, std_id_x, std_id_y);
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber]);
            birthplaceCount++;
            hasChanged = true;
        } else if (!hasChanged && !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), "") && !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), triangle.getMostCommonBirthplace()) && triangle.getNumOfChildren() > 3) {
//                        deleteLink(bridge, std_id_z, std_id_y);
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber]);
            birthplaceCount++;
            hasChanged = true;
        }

        return hasChanged;
    }

    private static void createLink(NeoDbCypherBridge bridge, String std_id_x, String std_id_z, String prov) {
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction()) {
            Map<String, Object> parameters = getCreationParameterMap(std_id_x, std_id_z, prov);
            tx.run(BB_SIBLING_QUERY, parameters);
            tx.commit();
        }
    }

    private static void deleteLink(NeoDbCypherBridge bridge, String std_id_x, String std_id_y){
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            Map<String, Object> parameters = getCreationParameterMap(std_id_x, std_id_y);
            tx.run(BB_SIBLING_QUERY_DEL, parameters);
            tx.commit();
        }
    }

    private static void deleteLink(NeoDbCypherBridge bridge, String std_id_x, String std_id_y, String prov){
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            Map<String, Object> parameters = getCreationParameterMap(std_id_x, std_id_y, prov);
            tx.run(BB_SIBLING_QUERY_DEL_PROV, parameters);
            tx.commit();
        }
    }

    protected static LXPMeasure getCompositeMeasure(StringMeasure base_measure) {
        final List<Integer> LINKAGE_FIELDS_NAME = list(
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME,
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME
        );

        return new SumOfFieldDistances(base_measure, LINKAGE_FIELDS_NAME);
    }

    protected static LXPMeasure getCompositeMeasureDate(StringMeasure base_measure) {
        final List<Integer> LINKAGE_FIELDS = list(
                Birth.PARENTS_DAY_OF_MARRIAGE,
                Birth.PARENTS_MONTH_OF_MARRIAGE,
                Birth.PARENTS_YEAR_OF_MARRIAGE
        );

        return new SumOfFieldDistances(base_measure, LINKAGE_FIELDS);
    }

    private static double getDistance(long id1, long id2, LXPMeasure composite_measure, IBucket births) throws BucketException {
        LXP b1 = (LXP) births.getObjectById(id1);
        LXP b2 = (LXP) births.getObjectById(id2);
        return composite_measure.distance(b1, b2);
    }

    private static Map<String, Object> getCreationParameterMap(String standard_id_from, String standard_id_to) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        return parameters;
    }

    private static Map<String, Object> getCreationParameterMap(String standard_id_from, String standard_id_to, String prov) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        parameters.put("prov", prov);
        return parameters;
    }
}

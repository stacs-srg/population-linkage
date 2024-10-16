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
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthDeathSiblingAccuracy;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe.list;

public class ComplexBDPattern {

    final static int NUM_OF_CHILDREN  = 12;
    final static int MAX_AGE_DIFFERENCE  = 23;
    final static double DATE_THRESHOLD = 0.8;
    final static int BIRTH_INTERVAL = 280;
    private static final String BB_SIBLING_QUERY = "MATCH (a:Birth), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:SIBLING { provenance: $prov, actors: \"Child-Child\" } ]-(b)";
    private static final String BB_SIBLING_QUERY_DEL = "MATCH (a:Birth)-[r:SIBLING]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to DELETE r";
    private static final String BB_SIBLING_QUERY_DEL_PROV = "MATCH (a:Birth), (b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:DELETED { provenance: $prov } ]-(b)";

    private static NeoDbCypherBridge bridge;

    private static String[] creationPredicates = {"match_m_date", "match_strict_name"};
    private static String[] deletionPredicates = {"max_age_range", "min_b_interval", "birthplace_mode", "bad_m_date", "bad_strict_name"};

    public static void main(String[] args) throws BucketException {
        bridge = Store.getInstance().getBridge();
        RecordRepository record_repository = new RecordRepository("umea");
        final StringMeasure base_measure = Constants.LEVENSHTEIN;;
        final LXPMeasure composite_measure_name = getCompositeMeasure(base_measure);
        final LXPMeasure composite_measure_date = getCompositeMeasureDate(base_measure);
        final LXPMeasure composite_measure_bd = getCompositeMeasureBirthDeath(base_measure);
        IBucket births = record_repository.getBucket("birth_records");
        IBucket deaths = record_repository.getBucket("death_records");

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Death");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Birth");
        new BirthDeathSiblingAccuracy(bridge);
        new BirthBirthSiblingAccuracy(bridge);

        System.out.println("Locating triangles...");
        List<OpenTriangleCluster> triangles = findIllegalBirthDeathSiblingTriangles(bridge);
        System.out.println("Resolving triangles...");
        for (OpenTriangleCluster triangle : triangles) {
            for (List<Long> chain : triangle.getTriangleChain()){
                LXP[] tempKids = {(LXP) births.getObjectById(triangle.x), (LXP) deaths.getObjectById(chain.get(0)), (LXP) births.getObjectById(chain.get(1))};
                String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
                String std_id_y = tempKids[1].getString(Death.STANDARDISED_ID);
                String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

                triangle.getYearStatistics();
                boolean hasChanged = false;

                String toFind = "7106096";
//                    if(Objects.equals(std_id_z, toFind) || Objects.equals(std_id_y, toFind) || Objects.equals(std_id_x, toFind)){
//                        System.out.println("fsd");
//                    }

                //1. Check age of child not outside of max difference
                hasChanged = maxRangePredicate(triangle, tempKids, hasChanged, 0);

                //2. check DOB at least 9 months away from rest
                hasChanged = minBirthIntervalPredicate(triangle, tempKids, hasChanged, 1);

                //3. Get mode of birthplace
                hasChanged = mostCommonBirthPlacePredicate(triangle, hasChanged, tempKids, 2);

                //4. If same marriage date and pass other checks, create link. Match for same birthplace as well?
                if(!hasChanged && getDistance(triangle.x, chain.get(1), composite_measure_date, births) < DATE_THRESHOLD &&
                        !Objects.equals(tempKids[0].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----") &&
                        !Objects.equals(tempKids[2].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----")){
                    createLink(bridge, std_id_x, std_id_z, creationPredicates[0]);
                }
            }
        }

        System.out.println("After");
        new PredicateEfficacy(creationPredicates, deletionPredicates, "Birth", "Death");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Death");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Birth");
        new BirthDeathSiblingAccuracy(bridge);
        new BirthBirthSiblingAccuracy(bridge);
    }

    private static boolean maxRangePredicate(OpenTriangleCluster triangle, LXP[] tempKids, boolean hasChanged, int predNumber) {
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Death.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        if(!Objects.equals(tempKids[0].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                Math.abs(triangle.getYearMedian() - Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE && Math.abs(Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) - Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE){
//                        deleteLink(bridge, std_id_x, std_id_y);
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber]);
            hasChanged = true;
        } else if (!Objects.equals(tempKids[2].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                Math.abs(triangle.getYearMedian() - Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE && Math.abs(Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6))- Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE){
//                        deleteLink(bridge, std_id_z, std_id_y);
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber]);
            hasChanged = true;
        } else if (!Objects.equals(tempKids[0].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----")  &&
                Math.abs(triangle.getYearMedian() - Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6))) > MAX_AGE_DIFFERENCE && Math.abs(Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) - Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE) {
//                        deleteLink(bridge, std_id_z, std_id_y);
//                        deleteLink(bridge, std_id_x, std_id_y);
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber]);
            hasChanged = true;
        } else if (!Objects.equals(tempKids[2].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----")  &&
                Math.abs(triangle.getYearMedian() - Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6))) > MAX_AGE_DIFFERENCE && Math.abs(Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) - Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE){
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber]);
            hasChanged = true;
        }

        return hasChanged;
    }

    //https://stackoverflow.com/a/67767630
    private static boolean minBirthIntervalPredicate(OpenTriangleCluster triangle, LXP[] tempKids, boolean hasChanged, int predNumber) {
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Death.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        try{
            int day = 1;
            if(!Objects.equals(tempKids[0].getString(Birth.BIRTH_DAY), "--")){
                day = Integer.parseInt(tempKids[0].getString(Birth.BIRTH_DAY));
            }

            LocalDate dateX = LocalDate.of(Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR)), Integer.parseInt(tempKids[0].getString(Birth.BIRTH_MONTH)), day);
            Optional<LocalDate> closestDateX = triangle.getBirthDays().stream().sorted(Comparator.comparingLong(x -> Math.abs(ChronoUnit.DAYS.between(x, dateX))))
                    .skip(1)
                    .findFirst();
            if(!hasChanged && closestDateX.isPresent() && ChronoUnit.DAYS.between(closestDateX.get(), dateX) < BIRTH_INTERVAL && ChronoUnit.DAYS.between(closestDateX.get(), dateX) > 2){
//                            deleteLink(bridge, std_id_x, std_id_y);
                deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber]);
                hasChanged = true;
                try{
                    LocalDate dateY = LocalDate.of(Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) , Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(3, 5)), Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(0, 2)));
                    if(dateY.equals(closestDateX.get())){
                        triangle.removeBirthday(dateX);
                    }
                } catch (Exception e) {

                }
            }
        }catch (Exception e){

        }

        try{
            int day = 1;
            if(!Objects.equals(tempKids[2].getString(Birth.BIRTH_DAY), "--")){
                day = Integer.parseInt(tempKids[2].getString(Birth.BIRTH_DAY));
            }

            LocalDate dateZ = LocalDate.of(Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR)), Integer.parseInt(tempKids[2].getString(Birth.BIRTH_MONTH)), day);
            Optional<LocalDate> closestDateZ = triangle.getBirthDays().stream().sorted(Comparator.comparingLong(x -> Math.abs(ChronoUnit.DAYS.between(x, dateZ))))
                    .skip(1)
                    .findFirst();
            if(!hasChanged && closestDateZ.isPresent() && ChronoUnit.DAYS.between(closestDateZ.get(), dateZ) < BIRTH_INTERVAL && ChronoUnit.DAYS.between(closestDateZ.get(), dateZ) > 2){
//                            deleteLink(bridge, std_id_z, std_id_y);
                deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber]);
                hasChanged = true;
            }
        } catch (Exception e) {

        }

        return hasChanged;
    }

    private static boolean mostCommonBirthPlacePredicate(OpenTriangleCluster triangle, boolean hasChanged, LXP[] tempKids, int predNumber) {
        int MIN_FAMILY_SIZE = 3;
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Death.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        if(!hasChanged && !Objects.equals(tempKids[1].getString(Death.PLACE_OF_DEATH), "----") && ((!Objects.equals(tempKids[1].getString(Death.AGE_AT_DEATH), "") && !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), "----") && Integer.parseInt(tempKids[1].getString(Death.AGE_AT_DEATH)) < triangle.getAgeRange() / 2) ||
                (!Objects.equals(tempKids[1].getString(Death.DEATH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                        Integer.parseInt(tempKids[1].getString(Death.DEATH_YEAR)) - Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) < triangle.getAgeRange() / 2)) &&
                !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), tempKids[1].getString(Death.PLACE_OF_DEATH)) && !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), triangle.getMostCommonBirthplace()) && triangle.getNumOfChildren() > MIN_FAMILY_SIZE){
//                        deleteLink(bridge, std_id_x, std_id_y);
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber]);;
            hasChanged = true;
        } else if (!hasChanged && !Objects.equals(tempKids[1].getString(Death.PLACE_OF_DEATH), "----") && ((!Objects.equals(tempKids[1].getString(Death.AGE_AT_DEATH), "") && !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), "----") && Integer.parseInt(tempKids[1].getString(Death.AGE_AT_DEATH)) < triangle.getAgeRange() / 2) ||
                (!Objects.equals(tempKids[1].getString(Death.DEATH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                        Integer.parseInt(tempKids[1].getString(Death.DEATH_YEAR)) - Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) < triangle.getAgeRange() / 2)) &&
                !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), tempKids[1].getString(Death.PLACE_OF_DEATH)) && !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), triangle.getMostCommonBirthplace()) && triangle.getNumOfChildren() > MIN_FAMILY_SIZE) {
//                        deleteLink(bridge, std_id_z, std_id_y);
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber]);
            hasChanged = true;
        }

        return hasChanged;
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

    private static void createLink(NeoDbCypherBridge bridge, String std_id_x, String std_id_z, String prov) {
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction()) {
            Map<String, Object> parameters = getCreationParameterMap(std_id_x, std_id_z, prov);
            tx.run(BB_SIBLING_QUERY, parameters);
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

    protected static LXPMeasure getCompositeMeasureBirthDeath(StringMeasure base_measure) {
        final List<Integer> LINKAGE_FIELDS_BIRTH = list(
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME,
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME
        );

        final List<Integer> LINKAGE_FIELDS_DEATH = list(
                Death.MOTHER_FORENAME,
                Death.MOTHER_MAIDEN_SURNAME,
                Death.FATHER_FORENAME,
                Death.FATHER_SURNAME
        );

        return new SumOfFieldDistances(base_measure, LINKAGE_FIELDS_BIRTH, LINKAGE_FIELDS_DEATH);
    }

    private static List<OpenTriangleCluster> findIllegalBirthDeathSiblingTriangles(NeoDbCypherBridge bridge) {
        final String BIRTH_SIBLING_TRIANGLE_QUERY = "MATCH (x:Birth)-[:SIBLING]-(y:Death)-[:SIBLING]-(z:Birth)\n"+
                "WHERE NOT (x)-[:SIBLING]-(z) AND NOT (x)-[:DELETED]-(y) AND NOT (z)-[:DELETED]-(y)\n" +
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

            return new OpenTriangleCluster(x, openTrianglesList);
        }).collect(Collectors.toList());
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

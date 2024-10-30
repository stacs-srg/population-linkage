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
import org.neo4j.exceptions.Neo4jException;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.Store;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthBirthSiblingAccuracy;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.resolver.msed.MSED;
import uk.ac.standrews.cs.population_linkage.resolver.msed.OrderedList;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe.list;

public class BirthBirthOpenTriangleResolver {

    private static NeoDbCypherBridge bridge;

    //Various constants for predicates
    private static final int MAX_AGE_DIFFERENCE  = 23;
    private static final double DATE_THRESHOLD = 0.8;
    private static final int BIRTH_INTERVAL = 280;

    //Cypher queries used ijn predicates
    private static final String BB_SIBLING_QUERY = "MATCH (a:Birth), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:SIBLING { provenance: $prov, actors: \"Child-Child\" } ]-(b)";
    private static final String BB_SIBLING_QUERY_DEL = "MATCH (a:Birth)-[r:SIBLING]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to DELETE r";
    private static final String BB_SIBLING_QUERY_DEL_PROV = "MATCH (a:Birth), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:DELETED { provenance: $prov, actors: \"Child-Child\" } ]-(b)";
    private static final String BB_SIBLING_WITH_PARENTS = "MATCH (x:Birth)-[:SIBLING]-(y:Birth)-[:SIBLING]-(z:Birth),\n" +
            "(x)-[s:ID]-(m:Marriage),\n" +
            "(y)-[t:ID]-(m)\n" +
            "WHERE (s.actors = \"Child-Father\" or s.actors = \"Child-Mother\") and (t.actors = \"Child-Father\" or t.actors = \"Child-Mother\") and NOT (x)-[:SIBLING]-(z) and NOT (z)-[:ID]-(m) and z.PARENTS_YEAR_OF_MARRIAGE <> m.MARRIAGE_YEAR and x.PARENTS_YEAR_OF_MARRIAGE = m.MARRIAGE_YEAR and y.PARENTS_YEAR_OF_MARRIAGE = m.MARRIAGE_YEAR MERGE (y)-[r:DELETED { provenance: \"m_pred\",actors: \"Child-Child\" } ]-(z)";

    //Names of predicates to be used as prov
    private static final String[] creationPredicates = {"match_m_date", "match_fixed_name", "msed"};
    private static final String[] deletionPredicates = {"max_age_range", "min_b_interval", "birthplace_mode", "bad_m_date", "bad_strict_name", "m_pred", "msed"};

    public static void main(String[] args) throws BucketException {
        bridge = Store.getInstance().getBridge();
        RecordRepository record_repository = new RecordRepository("umea");
        final StringMeasure base_measure = Constants.LEVENSHTEIN;
        final StringMeasure base_measure_n = Constants.JENSEN_SHANNON;
        final LXPMeasure composite_measure_name = getCompositeMeasure(base_measure_n);
        final LXPMeasure composite_measure_date = getCompositeMeasureDate(base_measure);
        IBucket births = record_repository.getBucket("birth_records");
        BirthSiblingLinkageRecipe recipe = new BirthSiblingLinkageRecipe("umea", "EVERYTHING", BirthSiblingBundleBuilder.class.getName());
        boolean MSED = true;

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Birth"); //get number of triangles before resolution
        new BirthBirthSiblingAccuracy(bridge);

        System.out.println("Running graph predicates...");
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            tx.run(BB_SIBLING_WITH_PARENTS); //run birth-marriage graph pattern
            tx.commit();
        }

        System.out.println("Locating triangles...");
        List<OpenTriangleClusterBB> triangles = findIllegalBirthDeathSiblingTriangles(bridge); //get all open triangles in their clusters
        System.out.println("Triangle clusters found: " + triangles.size());

        System.out.println("Resolving triangles with MSED...");
        for (OpenTriangleClusterBB triangle : triangles) {
            resolveTrianglesMSED(triangle.getTriangleChain(), triangle.x, record_repository, recipe, 2, 6);
        }

//        triangles = findIllegalBirthDeathSiblingTriangles(bridge);

        System.out.println("Resolving triangles with predicates...");
//        for (OpenTriangleClusterBB cluster : triangles) { //loop through each triangle cluster
//            for (List<Long> chain : cluster.getTriangleChain()){ //loop through each chain of open triangles in cluster
//                LXP[] tempKids = {(LXP) births.getObjectById(cluster.x), (LXP) births.getObjectById(chain.get(0)), (LXP) births.getObjectById(chain.get(1))}; //get node objects
//                String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
//                String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
//                String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);
//
//                cluster.getYearStatistics(); //get statistics for brith years
//                boolean hasChanged = false; //prevent resolution if chain has already been resolved
//
////                    String toFind = "417705";
////                    if(Objects.equals(std_id_z, toFind) || Objects.equals(std_id_y, toFind) || Objects.equals(std_id_x, toFind)){
////                        System.out.println("fsd");
////                    }
//
//                //1. Check age of child not outside of max difference
//                hasChanged = maxRangePredicate(cluster, tempKids, hasChanged, 0);
//
//                //2. check DOB at least 9 months away from rest
//                hasChanged = minBirthIntervalPredicate(cluster, tempKids, hasChanged, 1);
//
//                //3. Get mode of birthplace
//                hasChanged = mostCommonBirthPlacePredicate(cluster, hasChanged, tempKids, 2);
//
//                //4. If name of parents the same after fixes, create
////                hasChanged = matchingNamesPredicate(tempKids, hasChanged, 1, composite_measure_name);
//
//                //5. If same marriage date and pass other checks, create link
//                if(!hasChanged && getDistance(cluster.x, chain.get(1), composite_measure_date, births) < DATE_THRESHOLD &&
//                        !Objects.equals(tempKids[0].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----") &&
//                        !Objects.equals(tempKids[2].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----")){
//                    createLink(bridge, std_id_x, std_id_z, creationPredicates[0]);
//                }else{
//                    if(!hasChanged && getDistance(cluster.x, chain.get(0), composite_measure_date, births) > DATE_THRESHOLD &&
//                            !Objects.equals(tempKids[0].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----") &&
//                            !Objects.equals(tempKids[1].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----")){
//                        deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[3]);
//                    } else if (!hasChanged && getDistance(chain.get(0), chain.get(1), composite_measure_date, births) > DATE_THRESHOLD &&
//                            !Objects.equals(tempKids[1].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----") &&
//                            !Objects.equals(tempKids[2].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----")){
//                        deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[3]);
//                    }
//                }
//            }
//        }

        System.out.println("After");
        System.out.println("\n");
        new PredicateEfficacy(creationPredicates, deletionPredicates, "Birth", "Birth"); //get efficacy of each predicate
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Birth"); //count number of open triangles after resolution
        new BirthBirthSiblingAccuracy(bridge);
    }

    /**
     * Method to locate all open triangles in the database
     *
     * @param bridge Neo4j Bridge
     * @return List of open triangle clusters
     */
    private static List<OpenTriangleClusterBB> findIllegalBirthDeathSiblingTriangles(NeoDbCypherBridge bridge) {
        final String BIRTH_SIBLING_TRIANGLE_QUERY = "MATCH (x:Birth)-[:SIBLING]-(y:Birth)-[:SIBLING]-(z:Birth)\n" +
                "WHERE NOT (x)-[:SIBLING]-(z) AND NOT (x)-[:DELETED]-(y) AND NOT (z)-[:DELETED]-(y)\n" +
                "RETURN x, collect([y, z]) AS openTriangles";

        //run query to get all open triangles
        Result result = bridge.getNewSession().run(BIRTH_SIBLING_TRIANGLE_QUERY);
        List<OpenTriangleClusterBB> clusters = new ArrayList<>();
        List<List<Long>> temp = new ArrayList<>();

        //loop through each cluster
        result.stream().forEach(r -> {
            long x = ((Node) r.asMap().get("x")).get("STORR_ID").asLong();
            List<List<Node>> openTrianglesNodes = (List<List<Node>>) r.asMap().get("openTriangles");

            for (List<Node> innerList : openTrianglesNodes) {
                List<Long> openTriangleList = innerList.stream()
                        .map(obj -> {
                            if (obj instanceof Node) {
                                return ((Node) obj).get("STORR_ID").asLong();
                            } else {
                                throw new IllegalArgumentException("Expected a Node but got: " + obj.getClass());
                            }
                        })
                        .collect(Collectors.toList());

                temp.add(openTriangleList); //add triangles to a temporary list

                if (temp.size() == 360) { //limit number of triangles in cluster
                    clusters.add(new OpenTriangleClusterBB(x, new ArrayList<>(temp)));
                    temp.clear();
                }
            }

            if (!temp.isEmpty()) { //if not reached limit, create a cluster object with whatever is left
                clusters.add(new OpenTriangleClusterBB(x, new ArrayList<>(temp)));
                temp.clear();
            }
        });

        return clusters;
    }

    /**
     * Predicate to resolve triangles based on the maximum age range two siblings can have
     * If a record's birthday is more than MAX_AGE_DIFFERENCE away from its link or the median of the cluster
     * then it is deleted
     *
     * @param cluster cluster of open triangles
     * @param tempKids three children in the open triangle
     * @param hasChanged check if triangle already resolved
     * @param predNumber index of predicate name
     * @return if triangle has been resolved
     */
    private static boolean maxRangePredicate(OpenTriangleClusterBB cluster, LXP[] tempKids, boolean hasChanged, int predNumber) {
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        //Check if record x is outside of range
        if(!Objects.equals(tempKids[0].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Birth.BIRTH_YEAR), "----") &&
                Math.abs(cluster.getYearMedian() - Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE &&
                Math.abs(Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR)) - Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE){
//                        deleteLink(bridge, std_id_x, std_id_y);
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber]);
            hasChanged = true;

        //Check if record z is outside of range
        } else if (!Objects.equals(tempKids[2].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Birth.BIRTH_YEAR), "----") &&
                Math.abs(cluster.getYearMedian() - Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE &&
                Math.abs(Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR)) - Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE){
//                        deleteLink(bridge, std_id_z, std_id_y);
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber]);
            hasChanged = true;

        //Check if record y is outside of range compared to x
        } else if (!Objects.equals(tempKids[0].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Birth.BIRTH_YEAR), "----") &&
                Math.abs(cluster.getYearMedian() - Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE &&
                Math.abs(Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR)) - Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE) {
//                        deleteLink(bridge, std_id_z, std_id_y);
//                        deleteLink(bridge, std_id_x, std_id_y);
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber]);
            hasChanged = true;

        //Check if record y is outside of range compared to z
        } else if (!Objects.equals(tempKids[2].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Birth.BIRTH_YEAR), "----") &&
                Math.abs(cluster.getYearMedian() - Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE &&
                Math.abs(Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR)) - Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE){
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber]);
            hasChanged = true;
        }

        return hasChanged;
    }

    //

    /**
     * Predicate to resolve triangles based on a minimum birth interval between two records
     * Either the interval between two connected records needs to be above BIRTH_INTERVAL
     * Or the interval between two closest siblings based on the birthday inside the cluster needs to be above BIRTH_INTERVAL
     *
     * Code for finding closest date has been amended from https://stackoverflow.com/a/67767630
     *
     * @param cluster cluster of open triangles
     * @param tempKids three children in the open triangle
     * @param hasChanged check if triangle already resolved
     * @param predNumber index of predicate name
     * @return if triangle has been resolved
     */
    private static boolean minBirthIntervalPredicate(OpenTriangleClusterBB cluster, LXP[] tempKids, boolean hasChanged, int predNumber) {
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        //Check if the interval between two connected records is above BIRTH_INTERVAL of they are not twins
        //Check on x against y
        int day = 1;
        try{
            //if missing day, set to first of month
            if(!Objects.equals(tempKids[0].getString(Birth.BIRTH_DAY), "--")){
                day = Integer.parseInt(tempKids[0].getString(Birth.BIRTH_DAY));
            }

            //get dates
            LocalDate dateX = LocalDate.of(Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR)), Integer.parseInt(tempKids[0].getString(Birth.BIRTH_MONTH)), day);
            LocalDate dateY = LocalDate.of(Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR)), Integer.parseInt(tempKids[1].getString(Birth.BIRTH_MONTH)), Integer.parseInt(tempKids[1].getString(Birth.BIRTH_DAY)));

            //check if difference between dates is below threshold
            if(!hasChanged && Math.abs(ChronoUnit.DAYS.between(dateY, dateX)) < BIRTH_INTERVAL && Math.abs(ChronoUnit.DAYS.between(dateY, dateX)) > 2){
                deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber]);
                hasChanged = true;
            }

        }catch (Exception e){

        }

        //Check on z against y
        try{
            //if missing day, set to first of month
            if(!Objects.equals(tempKids[2].getString(Birth.BIRTH_DAY), "--")){
                day = Integer.parseInt(tempKids[2].getString(Birth.BIRTH_DAY));
            }

            //get dates
            LocalDate dateZ = LocalDate.of(Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR)), Integer.parseInt(tempKids[2].getString(Birth.BIRTH_MONTH)), day);
            LocalDate dateY = LocalDate.of(Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR)), Integer.parseInt(tempKids[1].getString(Birth.BIRTH_MONTH)), Integer.parseInt(tempKids[1].getString(Birth.BIRTH_DAY)));

            //check if difference between dates is below threshold
            if(!hasChanged && Math.abs(ChronoUnit.DAYS.between(dateY, dateZ)) < BIRTH_INTERVAL && Math.abs(ChronoUnit.DAYS.between(dateY, dateZ)) > 2){
                deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber]);
                hasChanged = true;
            }

        }catch (Exception e){

        }

        //Check if the interval between two closest siblings based on the birthday inside the cluster is above BIRTH_INTERVAL
        try{
            //if missing day, set to first of month
            if(!Objects.equals(tempKids[0].getString(Birth.BIRTH_DAY), "--")){
                day = Integer.parseInt(tempKids[0].getString(Birth.BIRTH_DAY));
            }

            //get two closest birthdays of x
            LocalDate dateX = LocalDate.of(Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR)), Integer.parseInt(tempKids[0].getString(Birth.BIRTH_MONTH)), day);
            Optional<Map.Entry<String, LocalDate>> closestDateX1 = cluster.getBirthDays().entrySet().stream()
                    .sorted(Comparator.comparingLong(entry -> Math.abs(ChronoUnit.DAYS.between(entry.getValue(), dateX))))
                    .skip(1)
                    .findFirst();
            Optional<Map.Entry<String, LocalDate>> closestDateX2 = cluster.getBirthDays().entrySet().stream()
                    .sorted(Comparator.comparingLong(entry -> Math.abs(ChronoUnit.DAYS.between(entry.getValue(), dateX))))
                    .skip(2)
                    .findFirst();

            //check if both birthdays are below interval threshold
            if(!hasChanged && closestDateX1.isPresent() && closestDateX2.isPresent() &&
                    Math.abs(ChronoUnit.DAYS.between(closestDateX1.get().getValue(), dateX)) < BIRTH_INTERVAL && Math.abs(ChronoUnit.DAYS.between(closestDateX1.get().getValue(), dateX)) > 2 &&
                    Math.abs(ChronoUnit.DAYS.between(closestDateX2.get().getValue(), dateX)) < BIRTH_INTERVAL && Math.abs(ChronoUnit.DAYS.between(closestDateX2.get().getValue(), dateX)) > 2){
//                            deleteLink(bridge, std_id_x, std_id_y);
                deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber]);
                hasChanged = true;
            }
        }catch (Neo4jException e){
            e.printStackTrace();
        }catch (Exception ignored){

        }

        try{
            if(!Objects.equals(tempKids[2].getString(Birth.BIRTH_DAY), "--")){
                day = Integer.parseInt(tempKids[2].getString(Birth.BIRTH_DAY));
            }

            //get two closest birthdays of z
            LocalDate dateZ = LocalDate.of(Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR)), Integer.parseInt(tempKids[2].getString(Birth.BIRTH_MONTH)), day);
            Optional<Map.Entry<String, LocalDate>> closestDateZ1 = cluster.getBirthDays().entrySet().stream()
                    .sorted(Comparator.comparingLong(entry -> Math.abs(ChronoUnit.DAYS.between(entry.getValue(), dateZ))))
                    .skip(1)
                    .findFirst();
            Optional<Map.Entry<String, LocalDate>> closestDateZ2 = cluster.getBirthDays().entrySet().stream()
                    .sorted(Comparator.comparingLong(entry -> Math.abs(ChronoUnit.DAYS.between(entry.getValue(), dateZ))))
                    .skip(2)
                    .findFirst();

            //check if both birthdays are below interval threshold
            if(!hasChanged && closestDateZ1.isPresent() && closestDateZ2.isPresent() &&
                    Math.abs(ChronoUnit.DAYS.between(closestDateZ1.get().getValue(), dateZ)) < BIRTH_INTERVAL && Math.abs(ChronoUnit.DAYS.between(closestDateZ1.get().getValue(), dateZ)) > 2 &&
                    Math.abs(ChronoUnit.DAYS.between(closestDateZ2.get().getValue(), dateZ)) < BIRTH_INTERVAL && Math.abs(ChronoUnit.DAYS.between(closestDateZ2.get().getValue(), dateZ)) > 2){
//                            deleteLink(bridge, std_id_z, std_id_y);
                deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber]);
                hasChanged = true;
            }
        }catch (Neo4jException e){
            e.printStackTrace();
        }catch (Exception ignored){

        }

        return hasChanged;
    }

    /**
     * Predicate to resolve triangles based on most common birthplace. If neighbouring node and birthplace mode of cluster don't match
     * record, then delete link
     *
     * @param cluster cluster of open triangles
     * @param tempKids three children in the open triangle
     * @param hasChanged check if triangle already resolved
     * @param predNumber index of predicate name
     * @return if triangle has been resolved
     */
    private static boolean mostCommonBirthPlacePredicate(OpenTriangleClusterBB cluster, boolean hasChanged, LXP[] tempKids, int predNumber) {
        int MIN_FAMILY_SIZE = 3; //delete link only if cluster contains more children than threshold
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        //check on x
        if(!hasChanged && !Objects.equals(tempKids[1].getString(Birth.BIRTH_ADDRESS), "----") && !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), "----") &&
                !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), tempKids[1].getString(Birth.BIRTH_ADDRESS)) && !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), cluster.getMostCommonBirthplace()) && cluster.getNumOfChildren() > MIN_FAMILY_SIZE ){
//                        deleteLink(bridge, std_id_x, std_id_y);
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber]);
            hasChanged = true;

        //check on z
        } else if (!hasChanged && !Objects.equals(tempKids[1].getString(Birth.BIRTH_ADDRESS), "----") && !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), "----") &&
                !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), tempKids[1].getString(Birth.BIRTH_ADDRESS)) && !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), cluster.getMostCommonBirthplace()) && cluster.getNumOfChildren() > MIN_FAMILY_SIZE) {
//                        deleteLink(bridge, std_id_z, std_id_y);
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber]);
            hasChanged = true;
        }

        return hasChanged;
    }

    /**
     * Predicate to resolve triangles based on issues relating to names
     *
     * @param tempKids three children in the open triangle
     * @param hasChanged check if triangle already resolved
     * @param predNumber index of predicate name
     * @param composite_measure_name for calculating distance between names
     * @return if triangle has been resolved
     */
    private static boolean matchingNamesPredicate(LXP[] tempKids, boolean hasChanged, int predNumber, LXPMeasure composite_measure_name) {
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);
        double NAME_THRESHOLD = 0.25;
        boolean fix = false;

        if(hasChanged){
            return true;
        }

        //IMPORTANT: All these changes will be saved or no?
        for (int i = 0; i < tempKids.length; i+=2) {
            //1. DOTTER/SON
            String dotterRegex = "D[.:RT](?!.*D[.:RT])";
            Pattern pattern = Pattern.compile(dotterRegex);
            Matcher matcher = pattern.matcher(tempKids[i].getString(Birth.MOTHER_MAIDEN_SURNAME));
            if (matcher.find()) {
                String newString = tempKids[i].getString(Birth.MOTHER_MAIDEN_SURNAME).substring(0, matcher.start()) + "DOTTER";
                tempKids[i].put(Birth.MOTHER_MAIDEN_SURNAME, newString);
                fix = true;
            }

            String sonRegex = "S[.]";
            pattern = Pattern.compile(sonRegex);
            matcher = pattern.matcher(tempKids[i].getString(Birth.FATHER_SURNAME));
            if (matcher.find()) {
                String newString = tempKids[i].getString(Birth.FATHER_SURNAME).substring(0, matcher.start()) + "SON";
                tempKids[i].put(Birth.FATHER_SURNAME, newString);
                fix = true;
            }

            //2. Initials or incomplete names
            String initialRegex = "^[A-Z]*\\.$";
            pattern = Pattern.compile(initialRegex);
            matcher = pattern.matcher(tempKids[i].getString(Birth.FATHER_FORENAME));
            if (matcher.find() && i == 0 && tempKids[2].getString(Birth.FATHER_FORENAME).length() >= matcher.end() - 1 &&
                    tempKids[2].getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(tempKids[0].getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1))) {
                tempKids[2].put(Birth.FATHER_FORENAME, tempKids[2].getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                fix = true;
            } else if (matcher.find() && i == 2 && tempKids[0].getString(Birth.FATHER_FORENAME).length() >= matcher.end() - 1 &&
                    tempKids[2].getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(tempKids[0].getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1))) {
                tempKids[0].put(Birth.FATHER_FORENAME, tempKids[0].getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                fix = true;
            }

            matcher = pattern.matcher(tempKids[i].getString(Birth.MOTHER_FORENAME));
            if (matcher.find() && i == 0 && tempKids[2].getString(Birth.MOTHER_FORENAME).length() >= matcher.end() - 1 &&
                    tempKids[2].getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(tempKids[0].getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1))) {
                tempKids[2].put(Birth.MOTHER_FORENAME, tempKids[2].getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                fix = true;
            } else if (matcher.find() && i == 2 && tempKids[0].getString(Birth.MOTHER_FORENAME).length() >= matcher.end() - 1 &&
                    tempKids[2].getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(tempKids[0].getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1))) {
                tempKids[0].put(Birth.MOTHER_FORENAME, tempKids[0].getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                fix = true;
            }

            int[] fields = {Birth.FATHER_FORENAME, Birth.MOTHER_FORENAME, Birth.FATHER_SURNAME, Birth.MOTHER_MAIDEN_SURNAME};

            //3. Middle names and double barrel surnames
            for (int field : fields) {
                if (tempKids[i].getString(field).contains(" ")) {
                    if (i == 0 && !tempKids[2].getString(field).contains(" ")) {
                        String[] names = tempKids[0].getString(field).split("\\s+");
                        for (String name : names) {
                            if (name.equals(tempKids[2].getString(field))) {
                                tempKids[0].put(field, name);
                                fix = true;
                                break;
                            }
                        }
                    } else if(i == 2 && !tempKids[0].getString(field).contains(" ")) {
                        String[] names = tempKids[2].getString(field).split("\\s+");
                        for (String name : names) {
                            if (name.equals(tempKids[0].getString(field))) {
                                tempKids[2].put(field, name);
                                fix = true;
                                break;
                            }
                        }
                    }
//                    else if(!fix && tempKids[0].getString(field).contains(" ") && tempKids[2].getString(field).contains(" ")){
//                        String[] namesX = tempKids[0].getString(field).split("\\s+");
//                        String[] namesZ = tempKids[2].getString(field).split("\\s+");
//                        for (String nameX : namesX) {
//                            for (String nameZ : namesZ) {
//                                if (nameX.equals(nameZ)) {
//                                    tempKids[0].put(field, nameX);
//                                    tempKids[2].put(field, nameZ);
//                                    fix = true;
//                                    break;
//                                }
//                            }
//                        }
//                    }
                }
            }

            //4. Parentheses
            for (int field : fields) {
                String parenthesesRegex = "\\(([^)]+)\\)";
                pattern = Pattern.compile(parenthesesRegex);
                matcher = pattern.matcher(tempKids[i].getString(field));

                if (matcher.find() && matcher.start() > 0) {
                    String newString = tempKids[i].getString(field).substring(0, matcher.start()).strip();
                    tempKids[i].put(field, newString);
                    fix = true;
                }
            }
        }

        try {
            if(fix && getDistance(tempKids[0], tempKids[2], composite_measure_name) < NAME_THRESHOLD){
                createLink(bridge, std_id_x, std_id_z, creationPredicates[predNumber]);
                hasChanged = true;
            }
        } catch (BucketException e) {
            throw new RuntimeException(e);
        }

        return hasChanged;
    }

    public static void resolveTrianglesMSED(List<List<Long>> triangleChain, Long x, RecordRepository record_repository, BirthSiblingLinkageRecipe recipe, int cPred, int dPred) throws BucketException {
        double THRESHOLD = 0.02;
        List<Set<Birth>> familySets = new ArrayList<>();
        List<List<Birth>> toDelete = new ArrayList<>();
//        List<Long> allStorIDs = new ArrayList<>(children);
        OrderedList<List<Birth>, Double> allMSEDDist = new OrderedList<>(Integer.MAX_VALUE);
        int[] fields = {Birth.FATHER_FORENAME, Birth.MOTHER_FORENAME, Birth.FATHER_SURNAME, Birth.MOTHER_MAIDEN_SURNAME};


        for (List<Long> chain : triangleChain){
            List<Long> listWithX = new ArrayList<>(Arrays.asList(x));
            listWithX.addAll(chain);
            List<Birth> bs = getBirths(listWithX, record_repository);

            for (int i = 0; i < bs.size(); i++) {
                //1. DOTTER/SON
                String dotterRegex = "D[.:RT](?!.*D[.:RT])";
                Pattern pattern = Pattern.compile(dotterRegex);
                Matcher matcher = pattern.matcher(bs.get(i).getString(Birth.MOTHER_MAIDEN_SURNAME));
                if (matcher.find()) {
                    String newString = bs.get(i).getString(Birth.MOTHER_MAIDEN_SURNAME).substring(0, matcher.start()) + "DOTTER";
                    bs.get(i).put(Birth.MOTHER_MAIDEN_SURNAME, newString);
                }

                String sonRegex = "S[.]";
                pattern = Pattern.compile(sonRegex);
                matcher = pattern.matcher(bs.get(i).getString(Birth.FATHER_SURNAME));
                if (matcher.find()) {
                    String newString = bs.get(i).getString(Birth.FATHER_SURNAME).substring(0, matcher.start()) + "SON";
                    bs.get(i).put(Birth.FATHER_SURNAME, newString);
                }

                //2. Initials or incomplete names
                String initialRegex = "^[A-Z]*\\.$";
                pattern = Pattern.compile(initialRegex);
                matcher = pattern.matcher(bs.get(i).getString(Birth.FATHER_FORENAME));
                if (matcher.find() && i == 0 && bs.get(2).getString(Birth.FATHER_FORENAME).length() >= matcher.end() - 1 && bs.get(1).getString(Birth.FATHER_FORENAME).length() >= matcher.end() - 1 &&
                        bs.get(2).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(bs.get(0).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1)) &&
                        bs.get(1).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(bs.get(0).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1))) {

                    bs.get(1).put(Birth.FATHER_FORENAME, bs.get(0).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                    bs.get(2).put(Birth.FATHER_FORENAME, bs.get(0).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                } else if (matcher.find() && i == 1 && bs.get(0).getString(Birth.FATHER_FORENAME).length() >= matcher.end() - 1 && bs.get(2).getString(Birth.FATHER_FORENAME).length() >= matcher.end() - 1 &&
                        bs.get(2).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(bs.get(1).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1)) &&
                        bs.get(0).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(bs.get(1).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1))) {

                    bs.get(0).put(Birth.FATHER_FORENAME, bs.get(1).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                    bs.get(2).put(Birth.FATHER_FORENAME, bs.get(1).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                } else if (matcher.find() && i == 2 && bs.get(0).getString(Birth.FATHER_FORENAME).length() >= matcher.end() - 1 && bs.get(1).getString(Birth.FATHER_FORENAME).length() >= matcher.end() - 1 &&
                        bs.get(2).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(bs.get(0).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1)) &&
                        bs.get(1).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(bs.get(2).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1))) {

                    bs.get(0).put(Birth.FATHER_FORENAME, bs.get(2).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                    bs.get(1).put(Birth.FATHER_FORENAME, bs.get(2).getString(Birth.FATHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                }

                matcher = pattern.matcher(bs.get(i).getString(Birth.MOTHER_FORENAME));
                if (matcher.find() && i == 0 && bs.get(2).getString(Birth.MOTHER_FORENAME).length() >= matcher.end() - 1 && bs.get(1).getString(Birth.MOTHER_FORENAME).length() >= matcher.end() - 1 &&
                        bs.get(2).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(bs.get(0).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1)) &&
                        bs.get(1).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(bs.get(0).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1))) {
                    bs.get(1).put(Birth.MOTHER_FORENAME, bs.get(0).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                    bs.get(2).put(Birth.MOTHER_FORENAME, bs.get(0).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                } else if (matcher.find() && i == 1 && bs.get(0).getString(Birth.MOTHER_FORENAME).length() >= matcher.end() - 1 && bs.get(2).getString(Birth.MOTHER_FORENAME).length() >= matcher.end() - 1 &&
                        bs.get(2).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(bs.get(1).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1)) &&
                        bs.get(0).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(bs.get(1).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1))) {
                    bs.get(0).put(Birth.MOTHER_FORENAME, bs.get(1).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                    bs.get(2).put(Birth.MOTHER_FORENAME, bs.get(1).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                } else if (matcher.find() && i == 2 && bs.get(0).getString(Birth.MOTHER_FORENAME).length() >= matcher.end() - 1 && bs.get(1).getString(Birth.MOTHER_FORENAME).length() >= matcher.end() - 1 &&
                        bs.get(2).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(bs.get(0).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1)) &&
                        bs.get(1).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1).equals(bs.get(2).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1))) {
                    bs.get(0).put(Birth.MOTHER_FORENAME, bs.get(2).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                    bs.get(1).put(Birth.MOTHER_FORENAME, bs.get(2).getString(Birth.MOTHER_FORENAME).substring(matcher.start(), matcher.end() - 1));
                }

                //3. Middle names and double barrel surnames
                for (int field : fields) {
                    if (bs.get(i).getString(field).contains(" ")) {
                        if (i == 0 && !bs.get(2).getString(field).contains(" ")) {
                            String[] names = bs.get(0).getString(field).split("\\s+");
                            for (String name : names) {
                                if (name.equals(bs.get(2).getString(field))) {
                                    bs.get(0).put(field, name);
                                    break;
                                }
                            }
                        } else if(i == 1 && !bs.get(0).getString(field).contains(" ") && !bs.get(2).getString(field).contains(" ")) {
                            String[] names = bs.get(1).getString(field).split("\\s+");
                            for (String name : names) {
                                if (name.equals(bs.get(0).getString(field))) {
                                    bs.get(1).put(field, name);
                                    break;
                                }
                            }
                            for (String name : names) {
                                if (name.equals(bs.get(2).getString(field))) {
                                    bs.get(1).put(field, name);
                                    break;
                                }
                            }
                        } else if(i == 2 && !bs.get(0).getString(field).contains(" ")) {
                            String[] names = bs.get(2).getString(field).split("\\s+");
                            for (String name : names) {
                                if (name.equals(bs.get(0).getString(field))) {
                                    bs.get(2).put(field, name);
                                    break;
                                }
                            }
                        }
                    }
                }

                //4. Parentheses
                for (int field : fields) {
                    String parenthesesRegex = "\\(([^)]+)\\)";
                    pattern = Pattern.compile(parenthesesRegex);
                    matcher = pattern.matcher(bs.get(i).getString(field));

                    if (matcher.find() && matcher.start() > 0) {
                        String newString = bs.get(i).getString(field).substring(0, matcher.start()).strip();
                        bs.get(i).put(field, newString);
                    }
                }
            }

//            String toFind = "7210569";
//            if(Objects.equals(bs.get(0).getString(Birth.STANDARDISED_ID), toFind) || Objects.equals(bs.get(1).getString(Birth.STANDARDISED_ID), toFind) || Objects.equals(bs.get(2).getString(Birth.STANDARDISED_ID), toFind)){
//                System.out.println("fsd");
//            }

            double distance = getMSEDForCluster(bs, recipe);
            if(distance < THRESHOLD) {
                if(familySets.isEmpty()) {
                    familySets.add(new HashSet<>(bs));
                }else{
                    boolean familyFound = false;
                    for(Set<Birth> fSet : familySets) {
                        if(familyFound){
                            break;
                        }
                        for (int i = 0; i < bs.size(); i++) {
                            if(fSet.contains(bs.get(i))) {
                                if(distance < 0.01){
//                                    createLink(bridge, bs.get(0).getString(Birth.STANDARDISED_ID), bs.get(2).getString(Birth.STANDARDISED_ID), creationPredicates[cPred]);
                                }
                                fSet.addAll(bs);
                                familyFound = true;
                                break;
                            }
                        }
                    }
                    if(!familyFound) {
                        familySets.add(new HashSet<>(bs));
                    }
                }
            }else if(distance > THRESHOLD){
                toDelete.add(bs);
//                for(Set<Birth> fSet : familySets) {
//                    int kidsFound = 0;
//                    List<Integer> kidsIndex = new ArrayList<>(Arrays.asList(0, 1, 2));
//                    for (int i = 0; i < bs.size(); i++) {
//                        if(fSet.contains(bs.get(i))) {
//                            kidsIndex.remove((Integer.valueOf(i)));
//                            kidsFound++;
//                        }
//                    }
//
//                    if(kidsFound == 2 && kidsIndex.size() == 1) {
//                        if(kidsIndex.get(0) == 0){
//                            deleteLink(bridge, bs.get(0).getString(Birth.STANDARDISED_ID), bs.get(1).getString(Birth.STANDARDISED_ID), deletionPredicates[dPred]);
//                        } else if (kidsIndex.get(0) == 2) {
//                            deleteLink(bridge, bs.get(2).getString(Birth.STANDARDISED_ID), bs.get(1).getString(Birth.STANDARDISED_ID), deletionPredicates[dPred]);
//                        }
//                    }
//                }
            }
        }

        for (List<Birth> triangleToDelete : toDelete) {
            for(Set<Birth> fSet : familySets) {
                int kidsFound = 0;
                List<Integer> kidsIndex = new ArrayList<>(Arrays.asList(0, 1, 2));
                for (int i = 0; i < triangleToDelete.size(); i++) {
                    if(fSet.contains(triangleToDelete.get(i))) {
                        kidsIndex.remove((Integer.valueOf(i)));
                        kidsFound++;
                    }
                }

                if(kidsFound == 2 && kidsIndex.size() == 1) {
                    if(kidsIndex.get(0) == 0){
                        deleteLink(bridge, triangleToDelete.get(0).getString(Birth.STANDARDISED_ID), triangleToDelete.get(1).getString(Birth.STANDARDISED_ID), deletionPredicates[dPred]);
                    } else if (kidsIndex.get(0) == 2) {
                        deleteLink(bridge, triangleToDelete.get(2).getString(Birth.STANDARDISED_ID), triangleToDelete.get(1).getString(Birth.STANDARDISED_ID), deletionPredicates[dPred]);
                    }
                }else if(kidsFound == 0 && kidsIndex.size() == 3) {
                    deleteLink(bridge, triangleToDelete.get(0).getString(Birth.STANDARDISED_ID), triangleToDelete.get(1).getString(Birth.STANDARDISED_ID), deletionPredicates[dPred]);
                    deleteLink(bridge, triangleToDelete.get(2).getString(Birth.STANDARDISED_ID), triangleToDelete.get(1).getString(Birth.STANDARDISED_ID), deletionPredicates[dPred]);
                }
            }
        }
    }

    /**
     * Method to create a link between two records
     *
     * @param bridge Neo4j bridge
     * @param std_id_x standardised id of record x
     * @param std_id_z standardised id of record z
     * @param prov provenance of resolver
     */
    private static void createLink(NeoDbCypherBridge bridge, String std_id_x, String std_id_z, String prov) {
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction()) {
            Map<String, Object> parameters = getCreationParameterMap(std_id_x, std_id_z, prov);
            tx.run(BB_SIBLING_QUERY, parameters);
            tx.commit();
        }
    }

    /**
     * Method to permanently delete a link between two records
     *
     * @param bridge Neo4j bridge
     * @param std_id_x standardised id of record x
     * @param std_id_y standardised id of record y
     */
    private static void deleteLink(NeoDbCypherBridge bridge, String std_id_x, String std_id_y){
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            Map<String, Object> parameters = getCreationParameterMap(std_id_x, std_id_y);
            tx.run(BB_SIBLING_QUERY_DEL, parameters);
            tx.commit();
        }
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
            tx.run(BB_SIBLING_QUERY_DEL_PROV, parameters);
            tx.commit();
        }
    }

    /**
     * Method to get composite measure for names to calculate distance
     *
     * @param base_measure base measure to be used
     * @return composite measure
     */
    protected static LXPMeasure getCompositeMeasure(StringMeasure base_measure) {
        final List<Integer> LINKAGE_FIELDS_NAME = list(
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME,
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME
        );

        return new SumOfFieldDistances(base_measure, LINKAGE_FIELDS_NAME);
    }

    /**
     * Method to get composite measure for dates to calculate distance
     *
     * @param base_measure base measure to be used
     * @return composite measure
     */
    protected static LXPMeasure getCompositeMeasureDate(StringMeasure base_measure) {
        final List<Integer> LINKAGE_FIELDS = list(
                Birth.PARENTS_DAY_OF_MARRIAGE,
                Birth.PARENTS_MONTH_OF_MARRIAGE,
                Birth.PARENTS_YEAR_OF_MARRIAGE
        );

        return new SumOfFieldDistances(base_measure, LINKAGE_FIELDS);
    }

    /**
     * Method to get distance between two nodes based on their storr ID
     *
     * @param id1 ID of record 1
     * @param id2 ID of record 2
     * @param composite_measure measure to be used
     * @param births births bucket
     * @return distance between two records
     * @throws BucketException
     */
    private static double getDistance(long id1, long id2, LXPMeasure composite_measure, IBucket births) throws BucketException {
        LXP b1 = (LXP) births.getObjectById(id1);
        LXP b2 = (LXP) births.getObjectById(id2);
        return composite_measure.distance(b1, b2);
    }

    /**
     * Method to get distance between two nodes based on their object
     *
     * @param b1 object 1
     * @param b2 object 2
     * @param composite_measure measure to be used
     * @return distance between two records
     * @throws BucketException
     */
    private static double getDistance(LXP b1, LXP b2, LXPMeasure composite_measure) throws BucketException {
        return composite_measure.distance(b1, b2);
    }

    /**
     * Method to get map of parameters to be used in cypher queries
     *
     * @param standard_id_from record ID to link from
     * @param standard_id_to record ID to link to
     * @return map of parameters
     */
    private static Map<String, Object> getCreationParameterMap(String standard_id_from, String standard_id_to) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        return parameters;
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

    public static double getMSEDForCluster(List<Birth> choices, BirthSiblingLinkageRecipe recipe) {
        /* Calculate the MESD for the cluster represented by the indices choices into bs */
        List<String> fields_from_choices = new ArrayList<>(); // a list of the concatenated linkage fields from the selected choices.
        List<Integer> linkage_fields = recipe.getLinkageFieldsMSED(); // the linkage field indexes to be used
        for (Birth a_birth : choices) {
            StringBuilder sb = new StringBuilder();              // make a string of values for this record drawn from the recipe linkage fields
            for (int field_selector : linkage_fields) {
                sb.append(a_birth.get(field_selector) + "/");
            }
            fields_from_choices.add(sb.toString()); // add the linkage fields for this choice to the list being assessed
        }
        return MSED.distance(fields_from_choices);
    }

    /**
     * Method to get birth objects based on storr IDs
     *
     * @param sibling_ids ids of records to find
     * @param record_repository repository of where records stored
     * @return list of birth objects
     * @throws BucketException
     */
    public static List<Birth> getBirths(List<Long> sibling_ids, RecordRepository record_repository) throws BucketException {
        IBucket<Birth> births = record_repository.getBucket("birth_records");
        ArrayList<Birth> bs = new ArrayList();
        for( long id : sibling_ids) {
            bs.add(births.getObjectById(id));
        }
        return bs;
    }
}

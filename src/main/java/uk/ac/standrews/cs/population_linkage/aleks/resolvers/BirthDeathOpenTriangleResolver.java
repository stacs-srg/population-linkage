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
import org.neo4j.driver.types.Node;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthDeathSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthBirthSiblingAccuracy;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthDeathSiblingAccuracy;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BirthDeathOpenTriangleResolver extends SiblingOpenTriangleResolver {
    //Cypher queries used in predicates
    private final String BB_SIBLING_QUERY = "MATCH (a:Birth), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:SIBLING { provenance: $prov, actors: \"Child-Child\" } ]-(b)";
    private final String BD_SIBLING_QUERY_DEL_PROV = "MATCH (a:Birth), (b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:DELETED { provenance: $prov } ]-(b)";

    //Names of predicates to be used as prov
    private final String[] creationPredicates = {"match_m_date_bd"};
    private final String[] deletionPredicates = {"max_age_range", "min_b_interval", "birthplace_mode", "bad_m_date", "msed"};

    public static void main(String[] args) throws BucketException {
        String sourceRepo = args[0]; // e.g. umea
        String numberOfRecords = args[1]; // e.g. EVERYTHING or 10000 etc.

        if(args.length != 2){
            throw new IllegalArgumentException("Invalid number of arguments");
        }

        try {
            new BirthDeathOpenTriangleResolver(sourceRepo, numberOfRecords);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public BirthDeathOpenTriangleResolver(String sourceRepo, String numberOfRecords) throws BucketException, InterruptedException {
        super(sourceRepo);
        final StringMeasure base_measure = Constants.LEVENSHTEIN;
        final LXPMeasure composite_measure_date = getCompositeMeasureDate(base_measure);
        IBucket births = record_repository.getBucket("birth_records");
        IBucket deaths = record_repository.getBucket("death_records");
        BirthDeathSiblingLinkageRecipe recipe = new BirthDeathSiblingLinkageRecipe(sourceRepo, numberOfRecords, BirthDeathSiblingBundleBuilder.class.getName(), null);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Death");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Birth");
        new BirthDeathSiblingAccuracy(bridge);
        new BirthBirthSiblingAccuracy(bridge);

        System.out.println("Locating triangles...");
        List<OpenTriangleClusterBD> triangles = findIllegalBirthDeathSiblingTriangles(bridge, sourceRepo);
        System.out.println("Triangle clusters found: " + triangles.size());

        System.out.println("Resolving triangles with MSED...");
        for (OpenTriangleClusterBD cluster : triangles) {
            executorService.submit(() ->
                {
                    try {
                        resolveTrianglesMSED(cluster.getTriangleChain(), cluster.x, recipe, deletionPredicates[4], Birth.STANDARDISED_ID, BD_SIBLING_QUERY_DEL_PROV);
                    } catch (BucketException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
        }

        System.out.println("Resolving triangles with predicates...");
        for (OpenTriangleCluster cluster : triangles) {
            executorService.submit(() ->
                {
                    try {
                        resolveTrianglesPredicates(cluster, births, deaths, composite_measure_date);
                    } catch (BucketException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);

        System.out.println("After");
        PredicateEfficacy pef = new PredicateEfficacy(); //get efficacy of each predicate
        System.out.println("Birth-Death");
        pef.countSiblingEfficacy(new String[0], deletionPredicates, "Birth", "Death");
        System.out.println("Birth-Birth");
        pef.countSiblingEfficacy(creationPredicates, new String[0], "Birth", "Birth");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Death");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Birth");
        new BirthDeathSiblingAccuracy(bridge);
        new BirthBirthSiblingAccuracy(bridge);
    }

    /**
     * Method to resolve open triangles using logical predicates
     *
     * @param cluster cluster of open triangles to resolve
     * @param births births bucket
     * @param composite_measure_date composite measure for date
     * @throws BucketException
     */
    private void resolveTrianglesPredicates(OpenTriangleCluster cluster, IBucket births, IBucket deaths, LXPMeasure composite_measure_date) throws BucketException {
        for (List<Long> chain : cluster.getTriangleChain()){
            LXP[] tempKids = {(LXP) births.getObjectById(cluster.x), (LXP) deaths.getObjectById(chain.get(0)), (LXP) births.getObjectById(chain.get(1))};
            String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
            String std_id_y = tempKids[1].getString(Death.STANDARDISED_ID);
            String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

            cluster.getYearStatistics();
            boolean hasChanged = false;

            //1. Check age of child not outside of max difference
            hasChanged = maxRangePredicate(cluster, tempKids, hasChanged, 0);

            //2. check DOB at least 9 months away from rest
            hasChanged = minBirthIntervalPredicate(cluster, tempKids, hasChanged, 1);

            //3. Get mode of birthplace
            hasChanged = mostCommonBirthPlacePredicate(cluster, hasChanged, tempKids, 2);

            //4. If same marriage date and pass other checks, create link. Match for same birthplace as well?
            if(!hasChanged && getDistance(cluster.x, chain.get(1), composite_measure_date, births) < DATE_THRESHOLD &&
                    !Objects.equals(tempKids[0].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----") &&
                    !Objects.equals(tempKids[2].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----")){
                createLink(bridge, std_id_x, std_id_z, creationPredicates[0], BB_SIBLING_QUERY);
            }
        }
    }

    /**
     * Method to locate all open triangles in the database
     *
     * @param bridge Neo4j Bridge
     * @return List of open triangle clusters
     */
    private List<OpenTriangleClusterBD> findIllegalBirthDeathSiblingTriangles(NeoDbCypherBridge bridge, String recordRepo) {
        final String BIRTH_SIBLING_TRIANGLE_QUERY = "MATCH (x:Birth)-[:SIBLING]-(y:Death)-[:SIBLING]-(z:Birth)\n"+
                "WHERE NOT (x)-[:SIBLING]-(z) AND NOT (x)-[:DELETED]-(y) AND NOT (z)-[:DELETED]-(y)\n" +
                "RETURN x, collect([y, z]) AS openTriangles";

        //run query to get all open triangles
        Result result = bridge.getNewSession().run(BIRTH_SIBLING_TRIANGLE_QUERY);
        List<OpenTriangleClusterBD> clusters = new ArrayList<>();
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
                    clusters.add(new OpenTriangleClusterBD(x, new ArrayList<>(temp), recordRepo));
                    temp.clear();
                }
            }

            if (!temp.isEmpty()) { //if not reached limit, create a cluster object with whatever is left
                clusters.add(new OpenTriangleClusterBD(x, new ArrayList<>(temp), recordRepo));
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
    @Override
    protected boolean maxRangePredicate(OpenTriangleCluster cluster, LXP[] tempKids, boolean hasChanged, int predNumber) {
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Death.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        //Check if record x is outside of range
        if(!Objects.equals(tempKids[0].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                (Math.abs(cluster.getYearMedian() - Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE ||
                Math.abs(Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) - Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE)){
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber], BD_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;

        //Check if record z is outside of range
        } else if (!Objects.equals(tempKids[2].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                (Math.abs(cluster.getYearMedian() - Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE ||
                Math.abs(Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6))- Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE)){
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber], BD_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;

        //Check if record y is outside of range compared to x
        } else if (!Objects.equals(tempKids[0].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----")  &&
                (Math.abs(cluster.getYearMedian() - Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6))) > MAX_AGE_DIFFERENCE ||
                Math.abs(Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) - Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE)) {
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber], BD_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;

        //Check if record y is outside of range compared to z
        } else if (!Objects.equals(tempKids[2].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----")  &&
                (Math.abs(cluster.getYearMedian() - Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6))) > MAX_AGE_DIFFERENCE ||
                Math.abs(Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) - Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE)){
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber], BD_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;
        }

        return hasChanged;
    }

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
    @Override
    protected boolean minBirthIntervalPredicate(OpenTriangleCluster cluster, LXP[] tempKids, boolean hasChanged, int predNumber) {
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Death.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        for (int i = 0; i < tempKids.length; i+=2) {
            try{
                LocalDate childDate = getBirthdayAsDate(tempKids[i], false); //get birth date of node being analysed
                LocalDate dateY = getBirthdayAsDate(tempKids[1], true); //get birth date of middle node
                if(!hasChanged && Math.abs(ChronoUnit.DAYS.between(dateY, childDate)) < BIRTH_INTERVAL && Math.abs(ChronoUnit.DAYS.between(dateY, childDate)) > 2){
                    if(i == 0){
                        deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber], BD_SIBLING_QUERY_DEL_PROV);
                    }else{
                        deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber], BD_SIBLING_QUERY_DEL_PROV);
                    }
                    hasChanged = true;
                }
            }catch (Exception e){

            }
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
    @Override
    protected boolean mostCommonBirthPlacePredicate(OpenTriangleCluster cluster, boolean hasChanged, LXP[] tempKids, int predNumber) {
        int MIN_FAMILY_SIZE = 3;
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Death.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        //check on x
        if(!hasChanged && !Objects.equals(tempKids[1].getString(Death.PLACE_OF_DEATH), "----") &&
                ((!Objects.equals(tempKids[1].getString(Death.AGE_AT_DEATH), "") && !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), "----") && Integer.parseInt(tempKids[1].getString(Death.AGE_AT_DEATH)) < cluster.getAgeRange() / 2) ||
                (!Objects.equals(tempKids[1].getString(Death.DEATH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                        Integer.parseInt(tempKids[1].getString(Death.DEATH_YEAR)) - Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) < cluster.getAgeRange() / 2)) &&
                !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), tempKids[1].getString(Death.PLACE_OF_DEATH)) && !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), cluster.getMostCommonBirthplace()) && cluster.getNumOfChildren() > MIN_FAMILY_SIZE){

            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber], BD_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;

        //check on z
        } else if (!hasChanged && !Objects.equals(tempKids[1].getString(Death.PLACE_OF_DEATH), "----") && ((!Objects.equals(tempKids[1].getString(Death.AGE_AT_DEATH), "") && !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), "----") && Integer.parseInt(tempKids[1].getString(Death.AGE_AT_DEATH)) < cluster.getAgeRange() / 2) ||
                (!Objects.equals(tempKids[1].getString(Death.DEATH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                        Integer.parseInt(tempKids[1].getString(Death.DEATH_YEAR)) - Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) < cluster.getAgeRange() / 2)) &&
                !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), tempKids[1].getString(Death.PLACE_OF_DEATH)) && !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), cluster.getMostCommonBirthplace()) && cluster.getNumOfChildren() > MIN_FAMILY_SIZE) {

            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber], BD_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;
        }

        return hasChanged;
    }

    /**
     * Method to clean and standardise strings
     *
     * @param triangle triangle to clean
     */
    @Override
    protected void cleanStrings(List<LXP> triangle) {
        int[] fieldsB = {Birth.FATHER_FORENAME, Birth.MOTHER_FORENAME, Birth.FATHER_SURNAME, Birth.MOTHER_MAIDEN_SURNAME};
        int[] fieldsD = {Death.FATHER_FORENAME, Death.MOTHER_FORENAME, Death.FATHER_SURNAME, Death.MOTHER_MAIDEN_SURNAME};

        for (int i = 0; i < triangle.size(); i++) {
            //1. DOTTER/SON
            String dotterRegex = "D[.:ORT](?!.*D[.:RT])";
            Pattern pattern = Pattern.compile(dotterRegex);
            Matcher matcher = pattern.matcher(triangle.get(i).getString(Birth.MOTHER_MAIDEN_SURNAME));
            if (matcher.find()) {
                String newString = triangle.get(i).getString(Birth.MOTHER_MAIDEN_SURNAME).substring(0, matcher.start()) + "DOTTER";
                triangle.get(i).put(Birth.MOTHER_MAIDEN_SURNAME, newString);
            }

            String sonRegex = "S[.]";
            pattern = Pattern.compile(sonRegex);
            matcher = pattern.matcher(triangle.get(i).getString(Birth.FATHER_SURNAME));
            if (matcher.find()) {
                String newString = triangle.get(i).getString(Birth.FATHER_SURNAME).substring(0, matcher.start()) + "SON";
                triangle.get(i).put(Birth.FATHER_SURNAME, newString);
            }

            //2. Initials or incomplete names
            String initialRegex = "^[A-Z]*\\.$";
            pattern = Pattern.compile(initialRegex);
            for (int j = 0; j < fieldsB.length - 3; j++) {
                if(i == 1){
                    matcher = pattern.matcher(triangle.get(i).getString(fieldsD[j]));
                }else{
                    matcher = pattern.matcher(triangle.get(i).getString(fieldsB[j]));
                }

                if(matcher.find()){
                    String substringX = triangle.get(0).getString(fieldsB[j]).length() >= matcher.end() - 1 ? triangle.get(0).getString(fieldsB[j]).substring(matcher.start(), matcher.end() - 1) : triangle.get(0).getString(j);
                    String substringY = triangle.get(1).getString(fieldsD[j]).length() >= matcher.end() - 1 ? triangle.get(1).getString(fieldsD[j]).substring(matcher.start(), matcher.end() - 1) : triangle.get(1).getString(j);
                    String substringZ = triangle.get(2).getString(fieldsB[j]).length() >= matcher.end() - 1 ? triangle.get(2).getString(fieldsB[j]).substring(matcher.start(), matcher.end() - 1) : triangle.get(2).getString(j);

                    if (i == 0 && substringX.equals(substringY) && substringX.equals(substringZ)) {
                        triangle.get(0).put(fieldsB[j], triangle.get(0).getString(fieldsB[j]).replace(".", ""));
                        triangle.get(1).put(fieldsD[j], triangle.get(0).getString(fieldsD[j]).substring(matcher.start(), matcher.end() - 1));
                        triangle.get(2).put(fieldsB[j], triangle.get(0).getString(fieldsB[j]).substring(matcher.start(), matcher.end() - 1));
                    } else if (i == 1 && substringY.equals(substringX) && substringY.equals(substringZ)) {
                        triangle.get(1).put(fieldsB[j], triangle.get(1).getString(fieldsB[j]).replace(".", ""));
                        triangle.get(0).put(fieldsD[j], triangle.get(1).getString(fieldsD[j]).substring(matcher.start(), matcher.end() - 1));
                        triangle.get(2).put(fieldsB[j], triangle.get(1).getString(fieldsB[j]).substring(matcher.start(), matcher.end() - 1));
                    } else if (i == 2 && substringZ.equals(substringX) && substringZ.equals(substringY)) {
                        triangle.get(2).put(fieldsB[j], triangle.get(2).getString(fieldsB[j]).replace(".", ""));
                        triangle.get(0).put(fieldsD[j], triangle.get(2).getString(fieldsD[j]).substring(matcher.start(), matcher.end() - 1));
                        triangle.get(1).put(fieldsB[j], triangle.get(2).getString(fieldsB[j]).substring(matcher.start(), matcher.end() - 1));
                    }
                }
            }

            //3. Middle names and double barrel surnames
            for (int j = 0; j < fieldsB.length - 1; j++) {
                if (triangle.get(i).getString(fieldsB[j]).contains(" ") || triangle.get(i).getString(fieldsD[j]).contains(" ")) {
                    if (i == 0 && !triangle.get(2).getString(fieldsB[j]).contains(" ")) {
                        String[] names = triangle.get(0).getString(fieldsB[j]).split("\\s+");
                        for (String name : names) {
                            if (name.equals(triangle.get(2).getString(fieldsB[j]))) {
                                triangle.get(0).put(fieldsB[j], name);
                                break;
                            }
                        }
                    } else if(i == 1 && (!triangle.get(0).getString(fieldsB[j]).contains(" ") || !triangle.get(2).getString(fieldsB[j]).contains(" "))) {
                        String[] names = triangle.get(1).getString(fieldsD[j]).split("\\s+");
                        for (String name : names) {
                            if (name.equals(triangle.get(0).getString(fieldsB[j]))) {
                                triangle.get(1).put(fieldsD[j], name);
                                break;
                            }
                        }
                        for (String name : names) {
                            if (name.equals(triangle.get(2).getString(fieldsB[j]))) {
                                triangle.get(1).put(fieldsD[j], name);
                                break;
                            }
                        }
                    } else if(i == 2 && !triangle.get(0).getString(fieldsB[j]).contains(" ")) {
                        String[] names = triangle.get(2).getString(fieldsB[j]).split("\\s+");
                        for (String name : names) {
                            if (name.equals(triangle.get(0).getString(fieldsB[j]))) {
                                triangle.get(2).put(fieldsB[j], name);
                                break;
                            }
                        }
                    }
                }
            }

            //4. Parentheses
            for (int j = 0; j < fieldsB.length - 1; j++) {
                String parenthesesRegex = "\\(([^)]+)\\)";
                pattern = Pattern.compile(parenthesesRegex);
                if(i == 1){
                    matcher = pattern.matcher(triangle.get(i).getString(fieldsD[j]));

                    if (matcher.find() && matcher.start() > 0) {
                        String newString = triangle.get(i).getString(fieldsD[j]).substring(0, matcher.start()).strip();
                        triangle.get(i).put(fieldsD[j], newString);
                    }
                }else{
                    matcher = pattern.matcher(triangle.get(i).getString(fieldsB[j]));

                    if (matcher.find() && matcher.start() > 0) {
                        String newString = triangle.get(i).getString(fieldsB[j]).substring(0, matcher.start()).strip();
                        triangle.get(i).put(fieldsB[j], newString);
                    }
                }
            }
        }
    }

    /**
     * Method to get birth/death objects based on storr IDs
     *
     * @param sibling_ids ids of records to find
     * @param record_repository repository of where records stored
     * @return list of birth/death objects
     * @throws BucketException
     */
    @Override
    protected List<LXP> getRecords(List<Long> sibling_ids, RecordRepository record_repository) throws BucketException {
        IBucket<Birth> births = record_repository.getBucket("birth_records");
        IBucket<Death> deaths = record_repository.getBucket("death_records");
        ArrayList<LXP> bs = new ArrayList();

        for (int i = 0; i < sibling_ids.size(); i++) {
            if(i == 1){
                bs.add(deaths.getObjectById(sibling_ids.get(i)));
            }else{
                bs.add(births.getObjectById(sibling_ids.get(i)));
            }
        }

        return bs;
    }
}

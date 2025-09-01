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
package uk.ac.standrews.cs.population_linkage.resolvers;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.types.Node;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthBirthSiblingAccuracy;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
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

public class BirthBirthOpenTriangleResolver extends SiblingOpenTriangleResolver {
    //Cypher queries used in predicates
    private final String BB_SIBLING_QUERY = "MATCH (a:Birth), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:SIBLING { provenance: $prov, actors: \"Child-Child\" } ]-(b)";
    private final String BB_SIBLING_QUERY_DEL_PROV = "MATCH (a:Birth), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:DELETED { provenance: $prov, actors: \"Child-Child\" } ]-(b)";
    private final String BB_SIBLING_WITH_PARENTS = "MATCH (x:Birth)-[:SIBLING]-(y:Birth)-[:SIBLING]-(z:Birth),\n" +
            "(x)-[s:ID]-(m:Marriage),\n" +
            "(y)-[t:ID]-(m)\n" +
            "WHERE (s.actors = \"Child-Father\" or s.actors = \"Child-Mother\") and (t.actors = \"Child-Father\" or t.actors = \"Child-Mother\") and NOT (x)-[:SIBLING]-(z) and NOT (z)-[:ID]-(m) and z.PARENTS_YEAR_OF_MARRIAGE <> m.MARRIAGE_YEAR and x.PARENTS_YEAR_OF_MARRIAGE = m.MARRIAGE_YEAR and y.PARENTS_YEAR_OF_MARRIAGE = m.MARRIAGE_YEAR MERGE (y)-[r:DELETED { provenance: \"m_pred\",actors: \"Child-Child\" } ]-(z)";
    private final String BB_ISO = "MATCH (b1:Birth)-[:SIBLING]-(b2:Birth)-[:SIBLING]-(b3:Birth),\n" +
            "(d1:Death)-[:SIBLING]-(d2:Death)-[:SIBLING]-(d3:Death),\n" +
            "(d1)-[:SIBLING]-(d3),\n" +
            "(b1)-[:ID]-(d1),\n" +
            "(b2)-[:ID]-(d2),\n" +
            "(b3)-[:ID]-(d3)\n" +
            "WHERE NOT (b1)-[:SIBLING]-(b3)\n" +
            "MERGE (b1)-[r:SIBLING { provenance: \"bb_iso\",actors: \"Child-Child\" } ]-(b3)";

    //Names of predicates to be used as prov
    private final String[] creationPredicates = {"match_m_date", "bb_iso"};
    private final String[] deletionPredicates = {"max_age_range", "min_b_interval", "birthplace_mode", "bad_m_date", "msed" , "m_pred"};

    public static void main(String[] args){
        String sourceRepo = args[0]; // e.g. umea
        String numberOfRecords = args[1]; // e.g. EVERYTHING or 10000 etc.

        if(args.length != 2){
            throw new IllegalArgumentException("Invalid number of arguments");
        }

        try {
            new BirthBirthOpenTriangleResolver(sourceRepo, numberOfRecords);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BirthBirthOpenTriangleResolver(String sourceRepo, String numberOfRecords) throws InterruptedException  {
        super(sourceRepo);

        final StringMeasure base_measure = Constants.LEVENSHTEIN;
        final LXPMeasure composite_measure_date = getCompositeMeasureDate(base_measure);
        IBucket births = record_repository.getBucket("birth_records");
        BirthSiblingLinkageRecipe recipe = new BirthSiblingLinkageRecipe(sourceRepo, numberOfRecords, BirthSiblingBundleBuilder.class.getName());
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Birth"); //get number of triangles before resolution
        new BirthBirthSiblingAccuracy(bridge);

        //Run all graph predicates
        System.out.println("Running graph predicates...");
        String[] graphPredicates = {BB_SIBLING_WITH_PARENTS, BB_ISO};
        for (String graphPredicate : graphPredicates) {
            try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction()) {
                tx.run(graphPredicate);
                tx.commit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Locating triangles...");
        List<OpenTriangleClusterBB> triangles = findIllegalBirthBirthSiblingTriangles(bridge, sourceRepo); //get all open triangles in their clusters
        System.out.println("Triangle clusters found: " + triangles.size());

        System.out.println("Resolving triangles with MSED...");
        for (OpenTriangleClusterBB triangle : triangles) {
            executorService.submit(() ->
                {
                    try {
                        resolveTrianglesMSED(triangle.getTriangleChain(), triangle.x, recipe, deletionPredicates[4], Birth.STANDARDISED_ID, BB_SIBLING_QUERY_DEL_PROV);
                    } catch (BucketException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
        }

        System.out.println("Resolving triangles with predicates...");
        for (OpenTriangleClusterBB cluster : triangles) { //loop through each triangle cluster
            executorService.submit(() ->
                {
                    try {
                        resolveTrianglesPredicates(cluster, births, composite_measure_date);
                    } catch (BucketException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);

        System.out.println("After");
        System.out.println("\n");
        PredicateEfficacy pef = new PredicateEfficacy(); //get efficacy of each predicate
        pef.countSiblingEfficacy(creationPredicates, deletionPredicates, "Birth", "Birth");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Birth"); //count number of open triangles after resolution
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
    private void resolveTrianglesPredicates(OpenTriangleClusterBB cluster, IBucket births, LXPMeasure composite_measure_date) throws BucketException {
        for (List<String> chain : cluster.getTriangleChain()){ //loop through each chain of open triangles in cluster
            LXP[] tempKids = {(LXP) births.getObjectById(cluster.x), (LXP) births.getObjectById(chain.get(0)), (LXP) births.getObjectById(chain.get(1))}; //get node objects
            String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
            String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
            String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

            cluster.getYearStatistics(); //get statistics for brith years
            boolean hasChanged = false; //prevent resolution if chain has already been resolved

            //1. Check age of child not outside of max difference
            hasChanged = maxRangePredicate(cluster, tempKids, hasChanged, 0);

            //2. check DOB at least 9 months away from rest
            hasChanged = minBirthIntervalPredicate(cluster, tempKids, hasChanged, 1);

            //3. Get mode of birthplace
            hasChanged = mostCommonBirthPlacePredicate(cluster, hasChanged, tempKids, 2);

            //4. If same marriage date and pass other checks, create link
            if(!hasChanged && getDistance(cluster.x, chain.get(1), composite_measure_date, births) < DATE_THRESHOLD &&
                    !Objects.equals(tempKids[0].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----") &&
                    !Objects.equals(tempKids[2].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----")){
                createLink(bridge, std_id_x, std_id_z, creationPredicates[0], BB_SIBLING_QUERY);
            }else{
                if(!hasChanged && getDistance(cluster.x, chain.get(0), composite_measure_date, births) > DATE_THRESHOLD &&
                        !Objects.equals(tempKids[0].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----") &&
                        !Objects.equals(tempKids[1].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----")){
                    deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[3], BB_SIBLING_QUERY_DEL_PROV);
                }

                if (!hasChanged && getDistance(chain.get(0), chain.get(1), composite_measure_date, births) > DATE_THRESHOLD &&
                        !Objects.equals(tempKids[1].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----") &&
                        !Objects.equals(tempKids[2].getString(Birth.PARENTS_YEAR_OF_MARRIAGE), "----")){
                    deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[3], BB_SIBLING_QUERY_DEL_PROV);
                }
            }
        }
    }

    /**
     * Method to locate all open triangles in the database
     *
     * @param bridge Neo4j Bridge
     * @return List of open triangle clusters
     */
    private List<OpenTriangleClusterBB> findIllegalBirthBirthSiblingTriangles(NeoDbCypherBridge bridge, String recordRepo) {
        final String BIRTH_SIBLING_TRIANGLE_QUERY = "MATCH (x:Birth)-[:SIBLING]-(y:Birth)-[:SIBLING]-(z:Birth)\n" +
                "WHERE NOT (x)-[:SIBLING]-(z) AND NOT (x)-[:DELETED]-(y) AND NOT (z)-[:DELETED]-(y)\n" +
                "RETURN x, collect([y, z]) AS openTriangles";

        //run query to get all open triangles
        Result result = bridge.getNewSession().run(BIRTH_SIBLING_TRIANGLE_QUERY);
        List<OpenTriangleClusterBB> clusters = new ArrayList<>();
        List<List<String>> temp = new ArrayList<>();

        //loop through each cluster
        result.stream().forEach(r -> {
            String x = ((Node) r.asMap().get("x")).get("STORR_ID").asString();
            List<List<Node>> openTrianglesNodes = (List<List<Node>>) r.asMap().get("openTriangles");

            for (List<Node> innerList : openTrianglesNodes) {
                List<String> openTriangleList = innerList.stream()
                        .map(obj -> {
                            if (obj instanceof Node) {
                                return ((Node) obj).get("STORR_ID").asString();
                            } else {
                                throw new IllegalArgumentException("Expected a Node but got: " + obj.getClass());
                            }
                        })
                        .collect(Collectors.toList());

                temp.add(openTriangleList); //add triangles to a temporary list

                if (temp.size() == 360) { //limit number of triangles in cluster
                    clusters.add(new OpenTriangleClusterBB(x, new ArrayList<>(temp), recordRepo));
                    temp.clear();
                }
            }

            if (!temp.isEmpty()) { //if not reached limit, create a cluster object with whatever is left
                clusters.add(new OpenTriangleClusterBB(x, new ArrayList<>(temp), recordRepo));
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
        String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        //Check if record x is outside of range
        if(!Objects.equals(tempKids[0].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Birth.BIRTH_YEAR), "----") &&
                (Math.abs(cluster.getYearMedian() - Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE ||
                Math.abs(Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR)) - Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE)){
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber], BB_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;

        //Check if record z is outside of range
        } else if (!Objects.equals(tempKids[2].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Birth.BIRTH_YEAR), "----") &&
                (Math.abs(cluster.getYearMedian() - Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE ||
                Math.abs(Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR)) - Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE)){
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber], BB_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;

        //Check if record y is outside of range compared to x
        } else if (!Objects.equals(tempKids[0].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Birth.BIRTH_YEAR), "----") &&
                (Math.abs(cluster.getYearMedian() - Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE ||
                Math.abs(Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR)) - Integer.parseInt(tempKids[0].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE)) {
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber], BB_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;

        //Check if record y is outside of range compared to z
        } else if (!Objects.equals(tempKids[2].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Birth.BIRTH_YEAR), "----") &&
                (Math.abs(cluster.getYearMedian() - Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE ||
                Math.abs(Integer.parseInt(tempKids[1].getString(Birth.BIRTH_YEAR)) - Integer.parseInt(tempKids[2].getString(Birth.BIRTH_YEAR))) > MAX_AGE_DIFFERENCE)){
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber], BB_SIBLING_QUERY_DEL_PROV);
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
        String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        for (int i = 0; i < tempKids.length; i+=2) {
            try{
                LocalDate childDate = getBirthdayAsDate(tempKids[i], false); //get birth date of node being analysed
                LocalDate dateY = getBirthdayAsDate(tempKids[1], false); //get birth date of middle node
                if(!hasChanged && Math.abs(ChronoUnit.DAYS.between(dateY, childDate)) < BIRTH_INTERVAL && Math.abs(ChronoUnit.DAYS.between(dateY, childDate)) > 2){
                    if(i == 0){
                        deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber], BB_SIBLING_QUERY_DEL_PROV);
                    }else{
                        deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber], BB_SIBLING_QUERY_DEL_PROV);
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
        int MIN_FAMILY_SIZE = 3; //delete link only if cluster contains more children than threshold
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        //check on x
        if(!hasChanged && !Objects.equals(tempKids[1].getString(Birth.BIRTH_ADDRESS), "----") && !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), "----") &&
                !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), tempKids[1].getString(Birth.BIRTH_ADDRESS)) && !Objects.equals(tempKids[0].getString(Birth.BIRTH_ADDRESS), cluster.getMostCommonBirthplace()) && cluster.getNumOfChildren() > MIN_FAMILY_SIZE ){
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber], BB_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;

        //check on z
        } else if (!hasChanged && !Objects.equals(tempKids[1].getString(Birth.BIRTH_ADDRESS), "----") && !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), "----") &&
                !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), tempKids[1].getString(Birth.BIRTH_ADDRESS)) && !Objects.equals(tempKids[2].getString(Birth.BIRTH_ADDRESS), cluster.getMostCommonBirthplace()) && cluster.getNumOfChildren() > MIN_FAMILY_SIZE) {
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber], BB_SIBLING_QUERY_DEL_PROV);
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
        int[] fields = {Birth.FATHER_FORENAME, Birth.MOTHER_FORENAME, Birth.FATHER_SURNAME, Birth.MOTHER_MAIDEN_SURNAME};

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
            for (int j = 0; j < fields.length - 3; j++) {
                matcher = pattern.matcher(triangle.get(i).getString(fields[j]));

                if(matcher.find()) {
                    String substringX = triangle.get(0).getString(fields[j]).length() >= matcher.end() - 1 ? triangle.get(0).getString(fields[j]).substring(matcher.start(), matcher.end() - 1) : triangle.get(0).getString(j);
                    String substringY = triangle.get(1).getString(fields[j]).length() >= matcher.end() - 1 ? triangle.get(1).getString(fields[j]).substring(matcher.start(), matcher.end() - 1) : triangle.get(1).getString(j);
                    String substringZ = triangle.get(2).getString(fields[j]).length() >= matcher.end() - 1 ? triangle.get(2).getString(fields[j]).substring(matcher.start(), matcher.end() - 1) : triangle.get(2).getString(j);

                    if (i == 0 && substringX.equals(substringY) && substringX.equals(substringZ)) {
                        triangle.get(0).put(fields[j], triangle.get(0).getString(fields[j]).replace(".", ""));
                        triangle.get(1).put(fields[j], triangle.get(0).getString(fields[j]).substring(matcher.start(), matcher.end() - 1));
                        triangle.get(2).put(fields[j], triangle.get(0).getString(fields[j]).substring(matcher.start(), matcher.end() - 1));
                    } else if (i == 1 && substringY.equals(substringX) && substringY.equals(substringZ)) {
                        triangle.get(1).put(fields[j], triangle.get(1).getString(fields[j]).replace(".", ""));
                        triangle.get(0).put(fields[j], triangle.get(1).getString(fields[j]).substring(matcher.start(), matcher.end() - 1));
                        triangle.get(2).put(fields[j], triangle.get(1).getString(fields[j]).substring(matcher.start(), matcher.end() - 1));
                    } else if (i == 2 && substringZ.equals(substringX) && substringZ.equals(substringY)) {
                        triangle.get(2).put(fields[j], triangle.get(2).getString(fields[j]).replace(".", ""));
                        triangle.get(0).put(fields[j], triangle.get(2).getString(fields[j]).substring(matcher.start(), matcher.end() - 1));
                        triangle.get(1).put(fields[j], triangle.get(2).getString(fields[j]).substring(matcher.start(), matcher.end() - 1));
                    }
                }
            }

            //3. Middle names and double barrel surnames
            for (int field : fields) {
                if (triangle.get(i).getString(field).contains(" ")) {
                    if (i == 0 && !triangle.get(2).getString(field).contains(" ")) {
                        String[] names = triangle.get(0).getString(field).split("\\s+");
                        for (String name : names) {
                            if (name.equals(triangle.get(2).getString(field))) {
                                triangle.get(0).put(field, name);
                                break;
                            }
                        }
                    } else if(i == 1 && (!triangle.get(0).getString(field).contains(" ") || !triangle.get(2).getString(field).contains(" "))) {
                        String[] names = triangle.get(1).getString(field).split("\\s+");
                        for (String name : names) {
                            if (name.equals(triangle.get(0).getString(field))) {
                                triangle.get(1).put(field, name);
                                break;
                            }
                        }
                        for (String name : names) {
                            if (name.equals(triangle.get(2).getString(field))) {
                                triangle.get(1).put(field, name);
                                break;
                            }
                        }
                    } else if(i == 2 && !triangle.get(0).getString(field).contains(" ")) {
                        String[] names = triangle.get(2).getString(field).split("\\s+");
                        for (String name : names) {
                            if (name.equals(triangle.get(0).getString(field))) {
                                triangle.get(2).put(field, name);
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
                matcher = pattern.matcher(triangle.get(i).getString(field));

                if (matcher.find() && matcher.start() > 0) {
                    String newString = triangle.get(i).getString(field).substring(0, matcher.start()).strip();
                    triangle.get(i).put(field, newString);
                }
            }
        }
    }

    /**
     * Method to get birth objects based on storr IDs
     *
     * @param sibling_ids ids of records to find
     * @param record_repository repository of where records stored
     * @return list of birth objects
     * @throws BucketException
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<LXP> getRecords(List<String> sibling_ids, RecordRepository record_repository) throws BucketException {
        IBucket<LXP> births = (IBucket<LXP>) record_repository.getBucket("birth_records");
        ArrayList<LXP> bs = new ArrayList<>();
        for( String id : sibling_ids) {
            bs.add(births.getObjectById(id));
        }
        return bs;
    }
}

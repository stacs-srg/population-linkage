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
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthBirthSiblingAccuracy;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.resolver.msed.Binomials;
import uk.ac.standrews.cs.population_linkage.resolver.msed.MSED;
import uk.ac.standrews.cs.population_linkage.resolver.msed.OrderedList;
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

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe.list;

public class BirthBirthOpenTriangleResolver extends SiblingOpenTriangleResolver {
    //Cypher queries used in predicates
    private final String BB_SIBLING_QUERY = "MATCH (a:Birth), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:SIBLING { provenance: $prov, actors: \"Child-Child\" } ]-(b)";
    private final String BB_SIBLING_QUERY_DEL_PROV = "MATCH (a:Birth), (b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:DELETED { provenance: $prov, actors: \"Child-Child\" } ]-(b)";
    private final String BB_SIBLING_WITH_PARENTS = "MATCH (x:Birth)-[:SIBLING]-(y:Birth)-[:SIBLING]-(z:Birth),\n" +
            "(x)-[s:ID]-(m:Marriage),\n" +
            "(y)-[t:ID]-(m)\n" +
            "WHERE (s.actors = \"Child-Father\" or s.actors = \"Child-Mother\") and (t.actors = \"Child-Father\" or t.actors = \"Child-Mother\") and NOT (x)-[:SIBLING]-(z) and NOT (z)-[:ID]-(m) and z.PARENTS_YEAR_OF_MARRIAGE <> m.MARRIAGE_YEAR and x.PARENTS_YEAR_OF_MARRIAGE = m.MARRIAGE_YEAR and y.PARENTS_YEAR_OF_MARRIAGE = m.MARRIAGE_YEAR MERGE (y)-[r:DELETED { provenance: \"m_pred\",actors: \"Child-Child\" } ]-(z)";

    //Names of predicates to be used as prov
    private final String[] creationPredicates = {"match_m_date", "match_fixed_name", "msed"};
    private final String[] deletionPredicates = {"max_age_range", "min_b_interval", "birthplace_mode", "bad_m_date", "msed"};

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

    public BirthBirthOpenTriangleResolver(String sourceRepo, String numberOfRecords) throws BucketException, InterruptedException  {
        super(sourceRepo);

        final StringMeasure base_measure = Constants.LEVENSHTEIN;
        final LXPMeasure composite_measure_date = getCompositeMeasureDate(base_measure);
        IBucket births = record_repository.getBucket("birth_records");
        BirthSiblingLinkageRecipe recipe = new BirthSiblingLinkageRecipe(sourceRepo, numberOfRecords, BirthSiblingBundleBuilder.class.getName());

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Birth"); //get number of triangles before resolution
        new BirthBirthSiblingAccuracy(bridge);

        System.out.println("Running graph predicates...");
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            tx.run(BB_SIBLING_WITH_PARENTS); //run birth-marriage graph pattern
            tx.commit();
        }

        System.out.println("Locating triangles...");
        List<OpenTriangleClusterBB> triangles = findIllegalBirthBirthSiblingTriangles(bridge, sourceRepo); //get all open triangles in their clusters
        System.out.println("Triangle clusters found: " + triangles.size());

        System.out.println("Resolving triangles with MSED...");
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);

//        for (OpenTriangleClusterBB triangle : triangles) {
//            executorService.submit(() ->
//                    {
//                        try {
//                            resolveTrianglesMSED(triangle.getTriangleChain(), triangle.x, recipe, 2, 4);
//                        } catch (BucketException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//            );
//        }

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
        executorService.awaitTermination(12, TimeUnit.HOURS);

        System.out.println("After");
        System.out.println("\n");
        PredicateEfficacy pef = new PredicateEfficacy(); //get efficacy of each predicate
        pef.countSiblingEfficacy(creationPredicates, deletionPredicates, "Birth", "Birth");
        PatternsCounter.countOpenTrianglesToString(bridge, "Birth", "Birth"); //count number of open triangles after resolution
        new BirthBirthSiblingAccuracy(bridge);
    }

    private void resolveTrianglesPredicates(OpenTriangleClusterBB cluster, IBucket births, LXPMeasure composite_measure_date) throws BucketException {
        for (List<Long> chain : cluster.getTriangleChain()){ //loop through each chain of open triangles in cluster
            LXP[] tempKids = {(LXP) births.getObjectById(cluster.x), (LXP) births.getObjectById(chain.get(0)), (LXP) births.getObjectById(chain.get(1))}; //get node objects
            String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
            String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
            String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

            cluster.getYearStatistics(); //get statistics for brith years
            boolean hasChanged = false; //prevent resolution if chain has already been resolved

//                    String toFind = "417705";
//                    if(Objects.equals(std_id_z, toFind) || Objects.equals(std_id_y, toFind) || Objects.equals(std_id_x, toFind)){
//                        System.out.println("fsd");
//                    }

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
    public boolean maxRangePredicate(OpenTriangleCluster cluster, LXP[] tempKids, boolean hasChanged, int predNumber) {
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        //TODO, maybe make it an or instead?
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
    public boolean minBirthIntervalPredicate(OpenTriangleCluster cluster, LXP[] tempKids, boolean hasChanged, int predNumber) {
        String std_id_x = tempKids[0].getString(Birth.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Birth.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Birth.STANDARDISED_ID);

        for (int i = 0; i < tempKids.length; i+=2) {
            try{
                LocalDate childDate = getBirthdayAsDate(tempKids[i]);
                LocalDate dateY = getBirthdayAsDate(tempKids[1]);
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

    private LocalDate getBirthdayAsDate(LXP child){
        int day = 1;

        //if missing day, set to first of month
        if(!Objects.equals(child.getString(Birth.BIRTH_DAY), "--")){
            day = Integer.parseInt(child.getString(Birth.BIRTH_DAY));
        }

        //get date
        return LocalDate.of(Integer.parseInt(child.getString(Birth.BIRTH_YEAR)), Integer.parseInt(child.getString(Birth.BIRTH_MONTH)), day);
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
    public boolean mostCommonBirthPlacePredicate(OpenTriangleCluster cluster, boolean hasChanged, LXP[] tempKids, int predNumber) {
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

    public void resolveTrianglesMSED(List<List<Long>> triangleChain, Long x, LinkageRecipe recipe, int cPred, int dPred) throws BucketException {
        double THRESHOLD = 0.04;
        double TUPLE_THRESHOLD = 0.02;

        List<Set<LXP>> familySets = new ArrayList<>();
        List<List<LXP>> toDelete = new ArrayList<>();
        int[] fields = {Birth.FATHER_FORENAME, Birth.MOTHER_FORENAME, Birth.FATHER_SURNAME, Birth.MOTHER_MAIDEN_SURNAME};

        for (List<Long> chain : triangleChain){
            List<Long> listWithX = new ArrayList<>(Arrays.asList(x));
            listWithX.addAll(chain);
            List<LXP> bs = getBirths(listWithX, record_repository);

            for (int i = 0; i < bs.size(); i++) {
                //1. DOTTER/SON
                String dotterRegex = "D[.:ORT](?!.*D[.:RT])";
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
                for (int j = 0; j < fields.length - 3; j++) {
                    matcher = pattern.matcher(bs.get(i).getString(fields[j]));

                    if(matcher.find()) {
                        String substringX = bs.get(0).getString(fields[j]).length() >= matcher.end() - 1 ? bs.get(0).getString(fields[j]).substring(matcher.start(), matcher.end() - 1) : bs.get(0).getString(j);
                        String substringY = bs.get(1).getString(fields[j]).length() >= matcher.end() - 1 ? bs.get(1).getString(fields[j]).substring(matcher.start(), matcher.end() - 1) : bs.get(1).getString(j);
                        String substringZ = bs.get(2).getString(fields[j]).length() >= matcher.end() - 1 ? bs.get(2).getString(fields[j]).substring(matcher.start(), matcher.end() - 1) : bs.get(2).getString(j);

                        if (i == 0 && substringX.equals(substringY) && substringX.equals(substringZ)) {
                            bs.get(0).put(fields[j], bs.get(0).getString(fields[j]).replace(".", ""));
                            bs.get(1).put(fields[j], bs.get(0).getString(fields[j]).substring(matcher.start(), matcher.end() - 1));
                            bs.get(2).put(fields[j], bs.get(0).getString(fields[j]).substring(matcher.start(), matcher.end() - 1));
                        } else if (i == 1 && substringY.equals(substringX) && substringY.equals(substringZ)) {
                            bs.get(1).put(fields[j], bs.get(1).getString(fields[j]).replace(".", ""));
                            bs.get(0).put(fields[j], bs.get(1).getString(fields[j]).substring(matcher.start(), matcher.end() - 1));
                            bs.get(2).put(fields[j], bs.get(1).getString(fields[j]).substring(matcher.start(), matcher.end() - 1));
                        } else if (i == 2 && substringZ.equals(substringX) && substringZ.equals(substringY)) {
                            bs.get(2).put(fields[j], bs.get(2).getString(fields[j]).replace(".", ""));
                            bs.get(0).put(fields[j], bs.get(2).getString(fields[j]).substring(matcher.start(), matcher.end() - 1));
                            bs.get(1).put(fields[j], bs.get(2).getString(fields[j]).substring(matcher.start(), matcher.end() - 1));
                        }
                    }
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
                        } else if(i == 1 && (!bs.get(0).getString(field).contains(" ") || !bs.get(2).getString(field).contains(" "))) {
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

            double distance = getMSEDForCluster(bs, recipe);
            double distanceXY = getMSEDForCluster(bs.subList(0, 2), recipe);
            double distanceZY = getMSEDForCluster(bs.subList(1, 3), recipe);

            if(distance < THRESHOLD) {
                addFamilyMSED(familySets, bs);
            }else if(distance > THRESHOLD){
                toDelete.add(bs);
                if(distanceXY < TUPLE_THRESHOLD){
                    addFamilyMSED(familySets, bs.subList(0, 2));
                }
                if (distanceZY < TUPLE_THRESHOLD){
                    addFamilyMSED(familySets, bs.subList(1, 3));
                }
            }
        }

        List<Set<LXP>> setsToRemove = new ArrayList<>();
        List<Set<LXP>> setsToAdd = new ArrayList<>();

        for (Set<LXP> fSet : familySets) {
            int k = 3;
            if (fSet.size() >= k) {
                OrderedList<List<LXP>,Double> familySetMSED = getMSEDForK(fSet, k, recipe);
                List<Double> distances = familySetMSED.getComparators();
                List<List<LXP>> births = familySetMSED.getList();
                List<Set<LXP>> newSets = new ArrayList<>();

                newSets.add(new HashSet<>(births.get(0)));

                for (int i = 1; i < distances.size(); i++) {
                    if ((distances.get(i) - distances.get(i - 1)) / distances.get(i - 1) > 0.5 || distances.get(i) > 0.01) {
                        break;
                    } else {
                        boolean familyFound = false;
                        for (Set<LXP> nSet : newSets) {
                            if (familyFound) {
                                break;
                            }
                            for (int j = 0; j < births.get(i).size(); j++) {
                                if (nSet.contains(births.get(i).get(j))) {
                                    nSet.addAll(births.get(i));
                                    familyFound = true;
                                    break;
                                }
                            }
                        }

                        if (!familyFound) {
                            newSets.add(new HashSet<>(births.get(i)));
                        }
                    }
                }

                setsToRemove.add(fSet);
                setsToAdd.addAll(newSets);
            }
        }

        familySets.removeAll(setsToRemove);
        familySets.addAll(setsToAdd);

        for (List<LXP> triangleToDelete : toDelete) {
//            String toFind = "244425";
//            String toFind2 = "235074";
//            if((Objects.equals(triangleToDelete.get(0).getString(Birth.STANDARDISED_ID), toFind) || Objects.equals(triangleToDelete.get(1).getString(Birth.STANDARDISED_ID), toFind) || Objects.equals(triangleToDelete.get(2).getString(Birth.STANDARDISED_ID), toFind)) && familySets.size() > 0 &&
//                    (Objects.equals(triangleToDelete.get(0).getString(Birth.STANDARDISED_ID), toFind2) || Objects.equals(triangleToDelete.get(1).getString(Birth.STANDARDISED_ID), toFind2) || Objects.equals(triangleToDelete.get(2).getString(Birth.STANDARDISED_ID), toFind2))) {
//                System.out.println("fsd");
//            }

            for(Set<LXP> fSet : familySets) {
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
                        deleteLink(bridge, triangleToDelete.get(0).getString(Birth.STANDARDISED_ID), triangleToDelete.get(1).getString(Birth.STANDARDISED_ID), deletionPredicates[dPred], BB_SIBLING_QUERY_DEL_PROV);
                        break;
                    } else if (kidsIndex.get(0) == 2) {
                        deleteLink(bridge, triangleToDelete.get(2).getString(Birth.STANDARDISED_ID), triangleToDelete.get(1).getString(Birth.STANDARDISED_ID), deletionPredicates[dPred], BB_SIBLING_QUERY_DEL_PROV);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Method to get composite measure for dates to calculate distance
     *
     * @param base_measure base measure to be used
     * @return composite measure
     */
    protected LXPMeasure getCompositeMeasureDate(StringMeasure base_measure) {
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
    private double getDistance(long id1, long id2, LXPMeasure composite_measure, IBucket births) throws BucketException {
        LXP b1 = (LXP) births.getObjectById(id1);
        LXP b2 = (LXP) births.getObjectById(id2);
        return composite_measure.distance(b1, b2);
    }

    public double getMSEDForCluster(List<LXP> choices, BirthSiblingLinkageRecipe recipe) {
        /* Calculate the MESD for the cluster represented by the indices choices into bs */
        List<String> fields_from_choices = new ArrayList<>(); // a list of the concatenated linkage fields from the selected choices.
        List<Integer> linkage_fields = recipe.getLinkageFieldsMSED(); // the linkage field indexes to be used
        for (LXP a_birth : choices) {
            StringBuilder sb = new StringBuilder();              // make a string of values for this record drawn from the recipe linkage fields
            for (int field_selector : linkage_fields) {
                sb.append(a_birth.get(field_selector) + "/");
            }
            fields_from_choices.add(sb.toString()); // add the linkage fields for this choice to the list being assessed
        }
        return MSED.distance(fields_from_choices);
    }

    protected OrderedList<List<LXP>,Double> getMSEDForK(Set<LXP> family, int k, BirthSiblingLinkageRecipe recipe) throws BucketException {
        OrderedList<List<LXP>,Double> all_mseds = new OrderedList<>(Integer.MAX_VALUE); // don't want a limit!
        List<LXP> bs = new ArrayList<>(family);

        List<List<Integer>> indices = Binomials.pickAll(bs.size(), k);
        for (List<Integer> choices : indices) {
            List<LXP> births = getRecordsFromChoices(bs, choices);
            double distance = getMSEDForCluster(births, recipe);
            all_mseds.add(births,distance);
        }
        return all_mseds;
    }

    /**
     * Method to get birth objects based on storr IDs
     *
     * @param sibling_ids ids of records to find
     * @param record_repository repository of where records stored
     * @return list of birth objects
     * @throws BucketException
     */
    private List<LXP> getBirths(List<Long> sibling_ids, RecordRepository record_repository) throws BucketException {
        IBucket<LXP> births = record_repository.getBucket("birth_records");
        ArrayList<LXP> bs = new ArrayList();
        for( long id : sibling_ids) {
            bs.add(births.getObjectById(id));
        }
        return bs;
    }
}

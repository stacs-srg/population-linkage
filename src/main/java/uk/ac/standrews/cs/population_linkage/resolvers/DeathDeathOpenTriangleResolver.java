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
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.DeathSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.DeathDeathSiblingAccuracy;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Death;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DeathDeathOpenTriangleResolver extends SiblingOpenTriangleResolver {
    //Cypher queries used in predicates
    private final String DD_SIBLING_QUERY_DEL_PROV = "MATCH (a:Death), (b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:DELETED { provenance: $prov } ]-(b)";
    private final String DD_ISO = "MATCH (b1:Birth)-[:SIBLING]-(b2:Birth)-[:SIBLING]-(b3:Birth),\n" +
            "(d1:Death)-[:SIBLING]-(d2:Death)-[:SIBLING]-(d3:Death),\n" +
            "(b1)-[:SIBLING]-(b3),\n" +
            "(b1)-[:ID]-(d1),\n" +
            "(b2)-[:ID]-(d2),\n" +
            "(b3)-[:ID]-(d3)\n" +
            "WHERE NOT (d1)-[:SIBLING]-(d3)\n" +
            "MERGE (d1)-[r:SIBLING { provenance: \"dd_iso\",actors: \"Deceased-Deceased\" } ]-(d3)";
    private final String DD_SIB_ISO = "MATCH (b1:Death)-[:SIBLING]-(d:Birth),\n" +
            "(b2:Death)-[:SIBLING]-(d),\n" +
            "(b1:Death)-[:ID]-(d1:Birth),\n" +
            "(b2:Death)-[:ID]-(d2:Birth),\n" +
            "(d)-[:SIBLING]-(d1)-[:SIBLING]-(d2)\n" +
            "WHERE NOT (b1)-[:SIBLING]-(b2) and not (b1)-[:SIBLING]-(d1) and not (b2)-[:SIBLING]-(d2)\n" +
            "MERGE (b1)-[r:SIBLING { provenance: \"dd_sib_iso\", actors: \"Deceased-Deceased\" } ]-(b2)";

    //Names of predicates to be used as prov
    private final String[] creationPredicates = {"dd_iso", "dd_sib_iso"};
    private final String[] deletionPredicates = {"max_age_range", "min_b_interval", "birthplace_mode", "bad_m_date", "msed"};

    public static void main(String[] args) throws BucketException {
        String sourceRepo = args[0]; // e.g. umea
        String numberOfRecords = args[1]; // e.g. EVERYTHING or 10000 etc.

        if(args.length != 2){
            throw new IllegalArgumentException("Invalid number of arguments");
        }

        try {
            new DeathDeathOpenTriangleResolver(sourceRepo, numberOfRecords);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public DeathDeathOpenTriangleResolver(String sourceRepo, String numberOfRecords) throws BucketException, InterruptedException {
        super(sourceRepo);
        IBucket deaths = record_repository.getBucket("death_records");
        DeathSiblingLinkageRecipe recipe = new DeathSiblingLinkageRecipe(sourceRepo, numberOfRecords, DeathSiblingBundleBuilder.class.getName());
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToString(bridge, "Death", "Death");
        new DeathDeathSiblingAccuracy(bridge);

        //Run all graph predicates
        System.out.println("Running graph predicates...");
        String[] graphPredicates = {DD_ISO, DD_SIB_ISO};
        for (int i = 0; i < graphPredicates.length; i++) {
            try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
                tx.run(graphPredicates[i]);
                tx.commit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Locating triangles...");
        List<OpenTriangleClusterDD> triangles = findIllegalDeathDeathSiblingTriangles(bridge, sourceRepo);
        System.out.println("Triangle clusters found: " + triangles.size());

        System.out.println("Resolving triangles with MSED...");
        for (OpenTriangleClusterDD cluster : triangles) {
            executorService.submit(() ->
                {
                    try {
                        resolveTrianglesMSED(cluster.getTriangleChain(), cluster.x, recipe, deletionPredicates[4], Death.STANDARDISED_ID, DD_SIBLING_QUERY_DEL_PROV);
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
                        resolveTrianglesPredicates(cluster, deaths);
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
        System.out.println("Death-Death");
        pef.countSiblingEfficacy(creationPredicates, deletionPredicates, "Death", "Death");
        PatternsCounter.countOpenTrianglesToString(bridge, "Death", "Death");
        new DeathDeathSiblingAccuracy(bridge);
    }

    /**
     * Method to resolve open triangles using logical predicates
     *
     * @param cluster cluster of open triangles to resolve
     * @param deaths deaths bucket
     * @throws BucketException
     */
    private void resolveTrianglesPredicates(OpenTriangleCluster cluster, IBucket deaths) throws BucketException {
        for (List<Long> chain : cluster.getTriangleChain()){ //loop through each chain of open triangles in cluster
            LXP[] tempKids = {(LXP) deaths.getObjectById(cluster.x), (LXP) deaths.getObjectById(chain.get(0)), (LXP) deaths.getObjectById(chain.get(1))};

            cluster.getYearStatistics();
            boolean hasChanged = false;

            //1. Check age of child not outside of max difference
            hasChanged = maxRangePredicate(cluster, tempKids, hasChanged, 0);

            //2. check DOB at least 9 months away from rest
            hasChanged = minBirthIntervalPredicate(cluster, tempKids, hasChanged, 1);

            //3. Get mode of birthplace
            hasChanged = mostCommonBirthPlacePredicate(cluster, hasChanged, tempKids, 2);
        }
    }

    /**
     * Method to locate all open triangles in the database
     *
     * @param bridge Neo4j Bridge
     * @return List of open triangle clusters
     */
    private List<OpenTriangleClusterDD> findIllegalDeathDeathSiblingTriangles(NeoDbCypherBridge bridge, String recordRepo) {
        final String DEATH_SIBLING_TRIANGLE_QUERY = "MATCH (x:Death)-[:SIBLING]-(y:Death)-[:SIBLING]-(z:Death)\n"+
                "WHERE NOT (x)-[:SIBLING]-(z) AND NOT (x)-[:DELETED]-(y) AND NOT (z)-[:DELETED]-(y)\n" +
                "RETURN x, collect([y, z]) AS openTriangles";

        //run query to get all open triangles
        Result result = bridge.getNewSession().run(DEATH_SIBLING_TRIANGLE_QUERY);
        List<OpenTriangleClusterDD> clusters = new ArrayList<>();
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
                    clusters.add(new OpenTriangleClusterDD(x, new ArrayList<>(temp), recordRepo));
                    temp.clear();
                }
            }

            if (!temp.isEmpty()) { //if not reached limit, create a cluster object with whatever is left
                clusters.add(new OpenTriangleClusterDD(x, new ArrayList<>(temp), recordRepo));
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
        String std_id_x = tempKids[0].getString(Death.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Death.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Death.STANDARDISED_ID);

        //Check if record x is outside of range
        if(!Objects.equals(tempKids[0].getString(Death.DATE_OF_BIRTH), "--/--/----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                (Math.abs(cluster.getYearMedian() - Integer.parseInt(tempKids[0].getString(Death.DATE_OF_BIRTH).substring(6))) > MAX_AGE_DIFFERENCE ||
                Math.abs(Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) - Integer.parseInt(tempKids[0].getString(Death.DATE_OF_BIRTH).substring(6))) > MAX_AGE_DIFFERENCE)){
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber], DD_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;

        //Check if record z is outside of range
        } else if (!Objects.equals(tempKids[2].getString(Death.DATE_OF_BIRTH), "--/--/----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                (Math.abs(cluster.getYearMedian() - Integer.parseInt(tempKids[2].getString(Death.DATE_OF_BIRTH).substring(6))) > MAX_AGE_DIFFERENCE ||
                Math.abs(Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6))- Integer.parseInt(tempKids[2].getString(Death.DATE_OF_BIRTH).substring(6))) > MAX_AGE_DIFFERENCE)){
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber], DD_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;

        //Check if record y is outside of range compared to x
        } else if (!Objects.equals(tempKids[0].getString(Death.DATE_OF_BIRTH), "--/--/----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----")  &&
                (Math.abs(cluster.getYearMedian() - Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6))) > MAX_AGE_DIFFERENCE ||
                Math.abs(Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) - Integer.parseInt(tempKids[0].getString(Death.DATE_OF_BIRTH).substring(6))) > MAX_AGE_DIFFERENCE)) {
            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber], DD_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;

        //Check if record y is outside of range compared to z
        } else if (!Objects.equals(tempKids[2].getString(Death.DATE_OF_BIRTH), "--/--/----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----")  &&
                (Math.abs(cluster.getYearMedian() - Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6))) > MAX_AGE_DIFFERENCE ||
                Math.abs(Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) - Integer.parseInt(tempKids[2].getString(Death.DATE_OF_BIRTH).substring(6))) > MAX_AGE_DIFFERENCE)){
            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber], DD_SIBLING_QUERY_DEL_PROV);
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
        String std_id_x = tempKids[0].getString(Death.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Death.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Death.STANDARDISED_ID);

        for (int i = 0; i < tempKids.length; i+=2) {
            try{
                LocalDate childDate = getBirthdayAsDate(tempKids[i], true); //get birth date of node being analysed
                LocalDate dateY = getBirthdayAsDate(tempKids[1], true); //get birth date of middle node
                if(!hasChanged && Math.abs(ChronoUnit.DAYS.between(dateY, childDate)) < BIRTH_INTERVAL && Math.abs(ChronoUnit.DAYS.between(dateY, childDate)) > 2){
                    if(i == 0){
                        deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber], DD_SIBLING_QUERY_DEL_PROV);
                    }else{
                        deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber], DD_SIBLING_QUERY_DEL_PROV);
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
        String std_id_x = tempKids[0].getString(Death.STANDARDISED_ID);
        String std_id_y = tempKids[1].getString(Death.STANDARDISED_ID);
        String std_id_z = tempKids[2].getString(Death.STANDARDISED_ID);

        if(!hasChanged &&
                !Objects.equals(tempKids[1].getString(Death.PLACE_OF_DEATH), "----") &&
                !Objects.equals(tempKids[0].getString(Death.PLACE_OF_DEATH), "----") &&
                ((!Objects.equals(tempKids[1].getString(Death.AGE_AT_DEATH), "") && Integer.parseInt(tempKids[1].getString(Death.AGE_AT_DEATH)) < cluster.getAgeRange() / 2) ||
                (!Objects.equals(tempKids[1].getString(Death.DEATH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                        Integer.parseInt(tempKids[1].getString(Death.DEATH_YEAR)) - Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) < cluster.getAgeRange() / 2)) &&
                ((!Objects.equals(tempKids[0].getString(Death.AGE_AT_DEATH), "") && Integer.parseInt(tempKids[0].getString(Death.AGE_AT_DEATH)) < cluster.getAgeRange() / 2) ||
                (!Objects.equals(tempKids[0].getString(Death.DEATH_YEAR), "----") && !Objects.equals(tempKids[0].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                    Integer.parseInt(tempKids[0].getString(Death.DEATH_YEAR)) - Integer.parseInt((tempKids[0].getString(Death.DATE_OF_BIRTH)).substring(6)) < cluster.getAgeRange() / 2)) &&
                !Objects.equals(tempKids[0].getString(Death.PLACE_OF_DEATH), tempKids[1].getString(Death.PLACE_OF_DEATH)) && !Objects.equals(tempKids[0].getString(Death.PLACE_OF_DEATH), cluster.getMostCommonBirthplace()) && cluster.getNumOfChildren() > MIN_FAMILY_SIZE){

            deleteLink(bridge, std_id_x, std_id_y, deletionPredicates[predNumber], DD_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;
        } else if (!hasChanged &&
        !Objects.equals(tempKids[1].getString(Death.PLACE_OF_DEATH), "----") &&
                !Objects.equals(tempKids[2].getString(Death.PLACE_OF_DEATH), "----") &&
                ((!Objects.equals(tempKids[1].getString(Death.AGE_AT_DEATH), "") && Integer.parseInt(tempKids[1].getString(Death.AGE_AT_DEATH)) < cluster.getAgeRange() / 2) ||
                        (!Objects.equals(tempKids[1].getString(Death.DEATH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                                Integer.parseInt(tempKids[1].getString(Death.DEATH_YEAR)) - Integer.parseInt((tempKids[1].getString(Death.DATE_OF_BIRTH)).substring(6)) < cluster.getAgeRange() / 2)) &&
                ((!Objects.equals(tempKids[2].getString(Death.AGE_AT_DEATH), "") && Integer.parseInt(tempKids[2].getString(Death.AGE_AT_DEATH)) < cluster.getAgeRange() / 2) ||
                        (!Objects.equals(tempKids[2].getString(Death.DEATH_YEAR), "----") && !Objects.equals(tempKids[2].getString(Death.DATE_OF_BIRTH), "--/--/----") &&
                                Integer.parseInt(tempKids[2].getString(Death.DEATH_YEAR)) - Integer.parseInt((tempKids[2].getString(Death.DATE_OF_BIRTH)).substring(6)) < cluster.getAgeRange() / 2)) &&
                !Objects.equals(tempKids[2].getString(Death.PLACE_OF_DEATH), tempKids[1].getString(Death.PLACE_OF_DEATH)) && !Objects.equals(tempKids[2].getString(Death.PLACE_OF_DEATH), cluster.getMostCommonBirthplace()) && cluster.getNumOfChildren() > MIN_FAMILY_SIZE){

            deleteLink(bridge, std_id_z, std_id_y, deletionPredicates[predNumber], DD_SIBLING_QUERY_DEL_PROV);
            hasChanged = true;
        }

        return hasChanged;
    }

    /**
     * Method to clean and standardise strings
     *
     * @param bs triangle to clean
     */
    @Override
    protected void cleanStrings(List<LXP> bs) {
        int[] fields = {Death.FATHER_FORENAME, Death.MOTHER_FORENAME, Death.FATHER_SURNAME, Death.MOTHER_MAIDEN_SURNAME};

        for (int i = 0; i < bs.size(); i++) {
            //1. DOTTER/SON
            String dotterRegex = "D[.:ORT](?!.*D[.:RT])";
            Pattern pattern = Pattern.compile(dotterRegex);
            Matcher matcher = pattern.matcher(bs.get(i).getString(Death.MOTHER_MAIDEN_SURNAME));
            if (matcher.find()) {
                String newString = bs.get(i).getString(Death.MOTHER_MAIDEN_SURNAME).substring(0, matcher.start()) + "DOTTER";
                bs.get(i).put(Death.MOTHER_MAIDEN_SURNAME, newString);
            }

            String sonRegex = "S[.]";
            pattern = Pattern.compile(sonRegex);
            matcher = pattern.matcher(bs.get(i).getString(Death.FATHER_SURNAME));
            if (matcher.find()) {
                String newString = bs.get(i).getString(Death.FATHER_SURNAME).substring(0, matcher.start()) + "SON";
                bs.get(i).put(Death.FATHER_SURNAME, newString);
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
    }

    /**
     * Method to get death objects based on storr IDs
     *
     * @param sibling_ids ids of records to find
     * @param record_repository repository of where records stored
     * @return list of death objects
     * @throws BucketException
     */
    @Override
    protected List<LXP> getRecords(List<Long> sibling_ids, RecordRepository record_repository) throws BucketException {
        IBucket<Death> deaths = record_repository.getBucket("death_records");
        ArrayList<LXP> bs = new ArrayList();

        for (int i = 0; i < sibling_ids.size(); i++) {
            bs.add(deaths.getObjectById(sibling_ids.get(i)));
        }

        return bs;
    }
}

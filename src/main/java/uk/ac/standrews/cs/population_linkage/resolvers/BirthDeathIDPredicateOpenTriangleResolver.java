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
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthOwnDeathAccuracy;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class BirthDeathIDPredicateOpenTriangleResolver extends IdentityOpenTriangleResolver {
    private final String BD_BAD_DATE = "MATCH (a:Birth)-[r:ID]-(d:Death)-[:ID]-(b:Birth) \n" +
            "WHERE a.BIRTH_YEAR <> right(d.DATE_OF_BIRTH, 4)\n" +
            "MERGE (a)-[:DELETED { provenance: $prov, actors: \"Child-Deceased\" } ]-(d)";
    private final String BD_BORN_AFER = "MATCH (a:Birth)-[r:ID]-(d:Death)-[:ID]-(b:Birth) \n" +
            "WHERE toInteger(a.BIRTH_YEAR) > toInteger(d.DEATH_YEAR)\n" +
            "MERGE (a)-[:DELETED { provenance: $prov, actors: \"Child-Deceased\" } ]-(d)";
    private final String BD_SIBLING = "MATCH (a:Birth)-[:ID]-(d:Death)-[:ID]-(b:Birth) \n" +
            "WHERE NOT (a)-[:SIBLING]-(d) and (b)-[:SIBLING]-(d)\n" +
            "MERGE (b)-[:DELETED { provenance: \"sibling\", actors: \"Child-Deceased\" } ]-(d)";

    private static final String BM_ID_QUERY_DEL_PROV = "MATCH (a:Birth), (b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:DELETED { provenance: $prov, actors: $actor } ]-(b)";


    //Names of predicates to be used as prov
    private static final String[] deletionPredicates = {"bad_b_date", "born_after_death", "sibling"};

    public static void main(String[] args) throws BucketException {
        String sourceRepo = args[0]; // e.g. umea

        if(args.length != 1){
            throw new IllegalArgumentException("Invalid number of arguments");
        }

        try {
            new BirthDeathIDPredicateOpenTriangleResolver(sourceRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BirthDeathIDPredicateOpenTriangleResolver(String sourceRepo) throws BucketException, InterruptedException {
        super(sourceRepo);

        IBucket births = record_repository.getBucket("birth_records");
        IBucket deaths = record_repository.getBucket("death_records");

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Birth", "Death"); //get number of triangles before resolution
        new BirthOwnDeathAccuracy(bridge);

        System.out.println("Locating triangles...");
        List<String[]> triangles = findIllegalBirthDeathTriangles(bridge); //get all open triangles in their clusters
        System.out.println("Triangles found: " + triangles.size());

        System.out.println("Running graph predicates...");
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            tx.run(BD_SIBLING); //run birth-marriage graph pattern
            tx.commit();
        }

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);

        System.out.println("Resolving triangles with predicates...");
        for (String[] triangle : triangles) {
            executorService.submit(() ->
                    {
                        try {
                            resolveTriangle(triangle, births, deaths);
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
        pef.countIDEfficacyDel(deletionPredicates, "Birth", "Death", "Child-Deceased");
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Birth", "Death"); //get number of triangles before resolution
        new BirthOwnDeathAccuracy(bridge);
    }

    private void resolveTriangle(String[] triangle, IBucket births, IBucket deaths) throws BucketException {
        boolean isDeleted = false;
        LXP[] tempKids = {(LXP) births.getObjectById(triangle[0]), (LXP) deaths.getObjectById(triangle[1]), (LXP) births.getObjectById(triangle[2])};
        String[] stds = {tempKids[0].getString(Birth.STANDARDISED_ID), tempKids[1].getString(Death.STANDARDISED_ID), tempKids[2].getString(Birth.STANDARDISED_ID)};

        int day = 1;
        if(!Objects.equals(tempKids[1].getString(Death.DEATH_DAY), "--")){
            day = Integer.parseInt(tempKids[1].getString(Death.DEATH_DAY));
        }
        int month = 1;
        if(!Objects.equals(tempKids[1].getString(Death.DEATH_MONTH), "--")){
            month = Integer.parseInt(tempKids[1].getString(Death.DEATH_MONTH));
        }

        LocalDate dateD = null;
        if(!Objects.equals(tempKids[1].getString(Death.DEATH_YEAR), "----")){
            dateD = LocalDate.of(Integer.parseInt(tempKids[1].getString(Death.DEATH_YEAR)), month, day);
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
            if (!Objects.equals(tempKids[i].getString(Birth.BIRTH_YEAR), "----") && !Objects.equals(tempKids[1].getString(Death.DATE_OF_BIRTH).substring(6), "----")) {
                if (!Objects.equals(tempKids[i].getString(Birth.BIRTH_YEAR), tempKids[1].getString(Death.DATE_OF_BIRTH).substring(6))) {
                    deleteLink(bridge, stds[i], stds[1], "Child-Deceased", deletionPredicates[0], BM_ID_QUERY_DEL_PROV);
                    isDeleted = true;
                }
            }

            if (!isDeleted && date != null && dateD != null) { //2. If born after death
                if (date.isAfter(dateD)) {
                    deleteLink(bridge, stds[i], stds[1], "Child-Deceased", deletionPredicates[1], BM_ID_QUERY_DEL_PROV);
                    isDeleted = true;
                }
            }

//        } else if (ChronoUnit.YEARS.between(date, dateD) >= 0 && //4. If born way before death
//                ChronoUnit.YEARS.between(date, dateD)> MAX_MARRIAGE_AGE) {
//            deleteLink(bridge, stds[i], stds[1], "Child-Deceased", deletionPredicates[3], BM_ID_QUERY_DEL_PROV);
//            isDeleted = true;
//        }
        }
    }

    private List<String[]> findIllegalBirthDeathTriangles(NeoDbCypherBridge bridge) {
        final String BIRTH_DEATH_TRIANGLE_QUERY = "MATCH (x:Birth)-[:ID]-(y:Death)-[:ID]-(z:Birth)\n" +
                "WHERE id(x) < id(z) AND NOT (x)-[:DELETED]-(y) AND NOT (z)-[:DELETED]-(y)\n" +
                "RETURN x, y, z";

        //run query to get all open triangles
        Result result = bridge.getNewSession().run(BIRTH_DEATH_TRIANGLE_QUERY);
        List<String[]> triangles = new ArrayList<>();
        result.stream().forEach(r -> {
            String x = ((Node) r.asMap().get("x")).get("STORR_ID").asString();
            String y = ((Node) r.asMap().get("y")).get("STORR_ID").asString();
            String z = ((Node) r.asMap().get("z")).get("STORR_ID").asString();

            String[] tempList = {x, y, z};
            triangles.add(tempList);
        });

        return triangles;
    }
}

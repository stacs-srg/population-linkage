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
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthBrideOwnMarriageAccuracy;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthGroomOwnMarriageBundleAccuracy;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.*;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe.list;


public class BirthMarriageIDOpenTriangleResolver extends IdentityOpenTriangleResolver {
    private static final double NAME_THRESHOLD = 0.5;

    //Cypher queries used in predicates
    private static final String BM_ID_QUERY_DEL_PROV = "MATCH (a:Birth), (b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to MERGE (a)-[r:DELETED { provenance: $prov, actors: $actor } ]-(b)";

    private final String BM_BAD_DATE = "MATCH (a:Birth)-[r:ID {actors: \"Child-%1$s\"}]-(d:Marriage)-[s:ID {actors: \"Child-%1$s\"}]-(b:Birth) \n" +
            "WHERE a.BIRTH_YEAR <> right(d.%2$s_AGE_OR_DATE_OF_BIRTH, 4)\n" +
            "MERGE (a)-[:DELETED { provenance: $prov, actors: $actor } ]-(d)";
    private final String BM_BORN_AFER = "MATCH (a:Birth)-[r:ID {actors: \"Child-%1$s\"}]-(d:Marriage)-[s:ID {actors: \"Child-%1$s\"}]-(b:Birth) \n" +
            "WHERE toInteger(a.BIRTH_YEAR) > toInteger(d.MARRIAGE_YEAR)\n" +
            "MERGE (a)-[:DELETED { provenance: $prov, actors: $actor } ]-(d)";
    private final String BM_BORN_BEFORE = "MATCH (a:Birth)-[r:ID {actors: \"Child-%1$s\"}]-(d:Marriage)-[s:ID {actors: \"Child-%1$s\"}]-(b:Birth) \n" +
            "WHERE toInteger(d.MARRIAGE_YEAR) - toInteger(a.BIRTH_YEAR) < 16\n" +
            "MERGE (a)-[:DELETED { provenance: $prov, actors: $actor } ]-(d)";
    private final String BM_BORN_OLD = "MATCH (a:Birth)-[r:ID {actors: \"Child-%1$s\"}]-(d:Marriage)-[s:ID {actors: \"Child-%1$s\"}]-(b:Birth) \n" +
            "WHERE toInteger(d.MARRIAGE_YEAR) - toInteger(a.BIRTH_YEAR) > 60\n" +
            "MERGE (a)-[:DELETED { provenance: $prov, actors: $actor } ]-(d)";
    private final String BM_SIBLING = "MATCH (a:Birth)-[r:ID {actors: \"Child-%1$s\"}]-(d:Marriage)-[s:ID {actors: \"Child-%1$s\"}]-(b:Birth) \n" +
            "WHERE NOT (a)-[:SIBLING]-(d) and (b)-[:SIBLING]-(d)\n" +
            "MERGE (a)-[:DELETED { provenance: $prov, actors: $actor } ]-(d)";

    //Names of predicates to be used as prov
    private static final String[] deletionPredicates = {"diff_birth", "born_after", "too_young", "too_old", "siblings", "bad_name"};

    public static void main(String[] args) throws BucketException {
        String sourceRepo = args[0]; // e.g. umea

        if(args.length != 1){
            throw new IllegalArgumentException("Invalid number of arguments");
        }

        try {
            new BirthMarriageIDOpenTriangleResolver(sourceRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BirthMarriageIDOpenTriangleResolver(String sourceRepo) throws BucketException {
        super(sourceRepo);

        final StringMeasure base_measure = Constants.JENSEN_SHANNON;
        LXPMeasure composite_measure;
        IBucket births = record_repository.getBucket("birth_records");
        IBucket marriages = record_repository.getBucket("marriage_records");
        String[] partners = {"Groom", "Bride"};

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Birth", "Marriage"); //get number of triangles before resolution
        new BirthGroomOwnMarriageBundleAccuracy(bridge);
        new BirthBrideOwnMarriageAccuracy(bridge);

        //loop through each partner
        for (String partner : partners) {
            composite_measure = getCompositeMeasureBirthMarriage(base_measure, partner);
            System.out.println("Resolving " + partner);
            System.out.println("Locating triangles...");
            List<Long[]> triangles = findIllegalBirthMarriageTriangles(bridge, partner); //get all open triangles in their clusters
            System.out.println("Triangles found: " + triangles.size());

            //run through all graph predicates
            System.out.println("Running graph predicates...");
            String[] graphPredicates = {BM_BAD_DATE, BM_BORN_AFER, BM_BORN_BEFORE, BM_BORN_OLD, BM_SIBLING};
            for (int i = 0; i < graphPredicates.length; i++) {
                try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction()) {
                    Map<String, Object> parameters = getCreationParameterMap(null, null, deletionPredicates[i], "Child-" + partner);
                    if(i == 0){
                        tx.run(String.format(graphPredicates[i], partner, partner.toUpperCase()), parameters);
                    }else{
                        tx.run(String.format(graphPredicates[i], partner), parameters);
                    }
                    tx.commit();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            //run through all logical predicates
            System.out.println("Resolving triangles with predicates...");
            for (Long[] triangle : triangles) {
                resolveTriangle(partner, triangle, births, marriages, composite_measure);
            }
        }

        System.out.println("After");
        System.out.println("\n");
        PredicateEfficacy pef = new PredicateEfficacy(); //get efficacy of each predicate
        for (String partner : partners) {
            System.out.println("\n" + partner + " efficacy:");
            pef.countIDEfficacyDel(deletionPredicates, "Birth", "Marriage", "Child-" + partner);
        }
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Birth", "Marriage"); //count number of open triangles after resolution
        new BirthGroomOwnMarriageBundleAccuracy(bridge);
        new BirthBrideOwnMarriageAccuracy(bridge);
    }

    /**
     * Method to resolve triangle
     *
     * @param partner which partner is being resolved
     * @param triangle the triangle in resolution
     * @param births births bucket
     * @param marriages marriages bucket
     * @param composite_measure composite measure
     * @throws BucketException
     */
    private void resolveTriangle(String partner, Long[] triangle, IBucket births, IBucket marriages, LXPMeasure composite_measure) throws BucketException {
        boolean isDeleted = false;
        LXP[] tempKids = {(LXP) births.getObjectById(triangle[0]), (LXP) marriages.getObjectById(triangle[1]), (LXP) births.getObjectById(triangle[2])};
        String[] stds = {tempKids[0].getString(Birth.STANDARDISED_ID), tempKids[1].getString(Marriage.STANDARDISED_ID), tempKids[2].getString(Birth.STANDARDISED_ID)};

        for (int i = 0; i < triangle.length; i += 2) {
            //5. Check names
            if (!isDeleted && getDistance(tempKids[i], tempKids[1], composite_measure) > NAME_THRESHOLD) {
                deleteLink(bridge, stds[i], stds[1], "Child-" + partner, deletionPredicates[5], BM_ID_QUERY_DEL_PROV);
            }
        }
    }

    /**
     * Method to locate all open triangles in the database
     *
     * @param bridge Neo4j Bridge
     * @param partner which partner in marriage to get
     * @return List of open triangle clusters
     */
    private List<Long[]> findIllegalBirthMarriageTriangles(NeoDbCypherBridge bridge, String partner) {
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
     * Method to get distance between names of groom/bride
     *
     * @param base_measure base measure
     * @param partner which partner to measure distance
     * @return distance between birth and marriage
     */
    protected LXPMeasure getCompositeMeasureBirthMarriage(StringMeasure base_measure, String partner) {
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
}

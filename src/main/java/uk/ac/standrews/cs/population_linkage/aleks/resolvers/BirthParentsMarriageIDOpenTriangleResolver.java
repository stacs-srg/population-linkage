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
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthParentsMarriageAccuracy;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe.list;


public class BirthParentsMarriageIDOpenTriangleResolver extends IdentityOpenTriangleResolver {
    private final String BMP_SUPPORTED_TRIANGLE = "MATCH (a:Birth)-[:SIBLING]-(b:Birth)-[r:ID {actors: $actor}]-(m:Marriage),\n" +
            "(a)-[:ID {actors: $actor}]-(m1:Marriage)-[:ID {actors: $actor}]-(b)\n" +
            "WHERE NOT (a)-[:ID {actors: $actor}]-(m) AND a <> b AND m <> m1 AND NOT (m)-[:ID]-(m1)\n" +
            "MERGE (b)-[:DELETED { provenance: $prov, actors: $actor } ]-(m)";

    //Names of predicates to be used as prov
    private static final String[] creationPredicates = {"match_m_date"};
    private static final String[] deletionPredicates = {"supported_triangle"};

    public static void main(String[] args) throws BucketException {
        String sourceRepo = args[0]; // e.g. umea

        if(args.length != 1){
            throw new IllegalArgumentException("Invalid number of arguments");
        }

        try {
            new BirthParentsMarriageIDOpenTriangleResolver(sourceRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BirthParentsMarriageIDOpenTriangleResolver(String sourceRepo) throws BucketException {
        super(sourceRepo);

        final StringMeasure base_measure = Constants.JENSEN_SHANNON;
        LXPMeasure composite_measure;
        IBucket births = record_repository.getBucket("birth_records");
        IBucket marriages = record_repository.getBucket("marriage_records");
        String[] partners = {"Father", "Mother"};

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Birth", "Marriage"); //get number of triangles before resolution
        new BirthParentsMarriageAccuracy(bridge);

        System.out.println("Running graph predicates...");
        for (String partner : partners) {
            try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
                Map<String, Object> parameters = getCreationParameterMap(null, null, deletionPredicates[0], "Child-" + partner);
                tx.run(BMP_SUPPORTED_TRIANGLE, parameters);
                tx.commit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("After");
        System.out.println("\n");
        PredicateEfficacy pef = new PredicateEfficacy(); //get efficacy of each predicate
        for (String partner : partners) {
            System.out.println("\n" + partner + " efficacy:");
            pef.countIDEfficacy(deletionPredicates, "Birth", "Marriage", "Child-" + partner);
        }
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Birth", "Marriage"); //get number of triangles before resolution
        new BirthParentsMarriageAccuracy(bridge);
    }

    private void resolveTriangle(String partner, Long[] triangle, IBucket births, IBucket marriages, LXPMeasure composite_measure) throws BucketException {
    }

    /**
     * Method to locate all open triangles in the database
     *
     * @param bridge Neo4j Bridge
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
}

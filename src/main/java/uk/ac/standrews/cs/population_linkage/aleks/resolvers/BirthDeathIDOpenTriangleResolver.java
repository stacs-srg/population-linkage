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

import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BirthOwnDeathAccuracy;

import java.util.Map;


public class BirthDeathIDOpenTriangleResolver extends IdentityOpenTriangleResolver {
    private final String BD_BAD_DATE = "MATCH (a:Birth)-[r:ID]-(d:Death)-[:ID]-(b:Birth) \n" +
            "WHERE a.BIRTH_YEAR <> right(d.DATE_OF_BIRTH, 4)\n" +
            "MERGE (a)-[:DELETED { provenance: $prov, actors: \"Child-Deceased\" } ]-(d)";
    private final String BD_BORN_AFER = "MATCH (a:Birth)-[r:ID]-(d:Death)-[:ID]-(b:Birth) \n" +
            "WHERE toInteger(a.BIRTH_YEAR) > toInteger(d.DEATH_YEAR)\n" +
            "MERGE (a)-[:DELETED { provenance: $prov, actors: \"Child-Deceased\" } ]-(d)";
    private final String BD_SIBLING = "MATCH (a:Birth)-[:ID]-(d:Death)-[:ID]-(b:Birth) \n" +
            "WHERE NOT (a)-[:SIBLING]-(d) and (b)-[:SIBLING]-(d)\n" +
            "MERGE (b)-[:DELETED { provenance: $prov, actors: \"Child-Deceased\" } ]-(d)";

    //Names of predicates to be used as prov
    private static final String[] deletionPredicates = {"bad_b_date", "born_after_death", "sibling"};

    public static void main(String[] args) throws BucketException {
        String sourceRepo = args[0]; // e.g. umea

        if(args.length != 1){
            throw new IllegalArgumentException("Invalid number of arguments");
        }

        try {
            new BirthDeathIDOpenTriangleResolver(sourceRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BirthDeathIDOpenTriangleResolver(String sourceRepo) throws BucketException {
        super(sourceRepo);

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Birth", "Death"); //get number of triangles before resolution
        new BirthOwnDeathAccuracy(bridge);

        System.out.println("Running graph predicates...");
        String[] graphPredicates = {BD_BAD_DATE, BD_BORN_AFER, BD_SIBLING};
        for (int i = 0; i < graphPredicates.length; i++) {
            try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction()) {
                Map<String, Object> parameters = getCreationParameterMap(null, null, deletionPredicates[i], "Child-Deceased");
                tx.run(graphPredicates[i], parameters);
                tx.commit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("After");
        System.out.println("\n");
        PredicateEfficacy pef = new PredicateEfficacy(); //get efficacy of each predicate
        pef.countIDEfficacy(deletionPredicates, "Birth", "Death", "Child-Deceased");
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Birth", "Death"); //get number of triangles before resolution
        new BirthOwnDeathAccuracy(bridge);
    }
}

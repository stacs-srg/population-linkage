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
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.BrideMarriageParentsMarriageAccuracy;
import uk.ac.standrews.cs.population_linkage.linkageAccuracy.GroomMarriageParentsMarriageAccuracy;

import java.util.Map;

public class MarriageParentsMarriageIDOpenTriangleResolver extends IdentityOpenTriangleResolver {
    private final String MMP_ISO_B = "MATCH (b1:Birth)-[:SIBLING]-(b2:Birth)-[:SIBLING]-(b3:Birth),\n" +
            "(b3)-[:SIBLING]-(b1),\n" +
            "(m:Marriage)-[r:ID]-(b1),\n" +
            "(m)-[s:ID]-(b2),\n" +
            "(m)-[t:ID]-(b3),\n" +
            "(m1:Marriage)-[u:ID]-(b1),\n" +
            "(m2:Marriage)-[v:ID]-(b2),\n" +
            "(m3:Marriage)-[w:ID]-(b3),\n" +
            "(m1)-[:SIBLING]-(m2)-[:SIBLING]-(m3)\n" +
            "WHERE (r.actors = \"Child-Father\" OR r.actors = \"Child-Mother\")\n" +
            "AND (s.actors = \"Child-Father\" OR s.actors = \"Child-Mother\") AND (t.actors = \"Child-Father\"\n" +
            "or t.actors = \"Child-Mother\")\n" +
            "AND NOT (m1)-[:SIBLING]-(b1) AND (u.actors = \"Child-Groom\" OR u.actors = \"Child-Bride\")\n" +
            "AND NOT (m2)-[:SIBLING]-(b2) AND (v.actors = \"Child-Groom\" OR v.actors = \"Child-Bride\")\n" +
            "AND NOT (m3)-[:SIBLING]-(b3) AND (w.actors = \"Child-Bride\")\n" +
            "AND NOT (m3)-[:ID]-(m)\n" +
            "MERGE (m3)-[:ID { provenance: $prov, actors: $actor } ]-(m)";
    private final String MMP_ISO_G = "MATCH (b1:Birth)-[:SIBLING]-(b2:Birth)-[:SIBLING]-(b3:Birth),\n" +
            "(b3)-[:SIBLING]-(b1),\n" +
            "(m:Marriage)-[r:ID]-(b1),\n" +
            "(m)-[s:ID]-(b2),\n" +
            "(m)-[t:ID]-(b3),\n" +
            "(m1:Marriage)-[u:ID]-(b1),\n" +
            "(m2:Marriage)-[v:ID]-(b2),\n" +
            "(m3:Marriage)-[w:ID]-(b3),\n" +
            "(m1)-[:SIBLING]-(m2)-[:SIBLING]-(m3)\n" +
            "WHERE (r.actors = \"Child-Father\" OR r.actors = \"Child-Mother\")\n" +
            "AND (s.actors = \"Child-Father\" OR s.actors = \"Child-Mother\") AND (t.actors = \"Child-Father\"\n" +
            "or t.actors = \"Child-Mother\")\n" +
            "AND NOT (m1)-[:SIBLING]-(b1) AND (u.actors = \"Child-Groom\" OR u.actors = \"Child-Bride\")\n" +
            "AND NOT (m2)-[:SIBLING]-(b2) AND (v.actors = \"Child-Groom\" OR v.actors = \"Child-Bride\")\n" +
            "AND NOT (m3)-[:SIBLING]-(b3) AND (w.actors = \"Child-Groom\")\n" +
            "AND NOT (m3)-[:ID]-(m)\n" +
            "MERGE (m3)-[:ID { provenance: $prov, actors: $actor } ]-(m)";

    //Names of predicates to be used as prov
    private static final String[] creationPredicates = {"iso"};

    public static void main(String[] args) throws BucketException {
        String sourceRepo = args[0]; // e.g. umea

        if(args.length != 1){
            throw new IllegalArgumentException("Invalid number of arguments");
        }

        try {
            new MarriageParentsMarriageIDOpenTriangleResolver(sourceRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MarriageParentsMarriageIDOpenTriangleResolver(String sourceRepo) throws BucketException {
        super(sourceRepo);

        System.out.println("Before");
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Marriage", "Marriage"); //get number of triangles before resolution
        new GroomMarriageParentsMarriageAccuracy(bridge);
        new BrideMarriageParentsMarriageAccuracy(bridge);

        String[] partners = {"Groom", "Bride"};

        System.out.println("Running graph predicates...");
        String[] graphPredicates = {MMP_ISO_G, MMP_ISO_B};
        for (String partner : partners) {
            try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
                if(partner.equals("Groom")){
                    Map<String, Object> parameters = getCreationParameterMap(null, null, creationPredicates[0], partner + "-Couple");
                    tx.run(graphPredicates[0], parameters);
                }else{
                    Map<String, Object> parameters = getCreationParameterMap(null, null, creationPredicates[0], partner + "-Couple");
                    tx.run(graphPredicates[1], parameters);
                }
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
            pef.countIDEfficacyCreate(creationPredicates, "Marriage", "Marriage", partner + "-Couple");
        }
        PatternsCounter.countOpenTrianglesToStringID(bridge, "Marriage", "Marriage"); //get number of triangles before resolution
        new GroomMarriageParentsMarriageAccuracy(bridge);
        new BrideMarriageParentsMarriageAccuracy(bridge);
    }
}

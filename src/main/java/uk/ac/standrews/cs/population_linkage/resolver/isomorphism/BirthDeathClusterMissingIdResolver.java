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
package uk.ac.standrews.cs.population_linkage.resolver.isomorphism;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthDeathSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.resolver.util.TwoTriangles;
import uk.ac.standrews.cs.population_records.RecordRepository;

import java.util.stream.Stream;

public class BirthDeathClusterMissingIdResolver {

    private final NeoDbCypherBridge bridge;
    private final RecordRepository record_repository;
    private final IBucket deaths;
    private final IBucket births;

    private static String BirthDeathSiblingBundleBuilder_link_name = BirthDeathSiblingBundleBuilder.class.getName();

    private static  String MISSING_ID_QUERY = "MATCH (d1:Death)-[d12:SIBLING]-(d2:Death)-[d23:SIBLING]-(d3:Death)-[d31:SIBLING]-(d1:Death) "
                                           + " MATCH (b1:Birth)-[b12:SIBLING]-(b2:Birth)-[b23:SIBLING]-(b3:Birth)-[b31:SIBLING]-(b1:Birth) "
                    + "MATCH (d1:Death)-[id:" + BirthDeathSiblingBundleBuilder_link_name + "]-(b1:Birth) " +
                    " RETURN d1,d2,d3,b1,b2,b3,id";


//    MATCH p=(startnode)-[rel:CALLING*]->(endnode)
//    WHERE endnode.name = {name}
//    RETURN startnode.name AS s_name, endnode.name AS e_name, p;

    private static  String TWO_CLUSTERS_QUERY = "MATCH (d1:Death)-[id:BirthDeathSiblingBundleBuilder]-(b1:Birth) " +
            "MATCH (b1:Birth)-[b12:SIBLING*]-(b2:Birth) " +
            "MATCH (d1:Death)-[d12:SIBLING*]-(d2:Death) " +
            "RETURN b1,d1,b2,d2";


    public BirthDeathClusterMissingIdResolver(NeoDbCypherBridge bridge, String source_repo_name ) {
            this.bridge = bridge;
            this.record_repository = new RecordRepository(source_repo_name);
            this.deaths = record_repository.getBucket("death_records");
            this.births = record_repository.getBucket("birth_records");
    }

    public static void main(String[] args) {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {

            BirthDeathClusterMissingIdResolver resolver = new BirthDeathClusterMissingIdResolver( bridge,sourceRepo );
            resolver.resolve();

        } catch (Exception e) {
            System.out.println( "Exception closing bridge" );
        } finally {
            System.out.println( "Run finished" );
            System.exit(0); // Make sure it all shuts down properly.
        }
    }

    private void resolve() {
        Stream<TwoTriangles> oddballs = findMissingIdLinks();
        oddballs.forEach(this::process);
    }

    public java.util.stream.Stream<TwoTriangles> findMissingIdLinks() {
        Result result = bridge.getNewSession().run(MISSING_ID_QUERY); // returns d1,d2,d3 and b1,b2,b3 where d1b1 are connected.
        return result.stream().map(r -> {
                    return new TwoTriangles(
                            // RETURN d1,d2,d3,b1,b2,b3
                            ((Node) r.asMap().get("d1")).get("STORR_ID").asLong(),
                            ((Node) r.asMap().get("d2")).get("STORR_ID").asLong(),
                            ((Node) r.asMap().get("d3")).get("STORR_ID").asLong(),
                            ((Node) r.asMap().get("b1")).get("STORR_ID").asLong(),
                            ((Node) r.asMap().get("b2")).get("STORR_ID").asLong(),
                            ((Node) r.asMap().get("b3")).get("STORR_ID").asLong()
                    );
                }
        );
    }

    private void process(TwoTriangles db) { // death birth

        try {

            LXP d1 = (LXP) deaths.getObjectById(db.t1);
            LXP d2 = (LXP) deaths.getObjectById(db.t2);
            LXP d3 = (LXP) deaths.getObjectById(db.t3);

            LXP b1 = (LXP) births.getObjectById(db.s1);
            LXP b2 = (LXP) births.getObjectById(db.s2);
            LXP b3 = (LXP) births.getObjectById(db.s3);

            // Are the distances close enough - we know they are otherwise wouldn't have linked them!

//            String std_id_x = x.getString(Death.STANDARDISED_ID);
//            String std_id_y = y.getString(Death.STANDARDISED_ID);
//            String std_id_z = z.getString(Death.STANDARDISED_ID);

        } catch (BucketException e) {
            e.printStackTrace();
        }
    }
}

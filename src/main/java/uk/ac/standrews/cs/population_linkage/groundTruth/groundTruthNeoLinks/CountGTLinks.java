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
package uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.time.LocalDateTime;

/*
 * Establishes ground truth links in Neo4J for Umea data set
 *
 * @author al
 */
public class CountGTLinks {

    private static final String FATHER_OWN_BIRTH_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Child-Father\" } ]->(b) return count(r)";
    private static final String MOTHER_OWN_BIRTH_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Child-Mother\" } ]->(b) return count(r)";
    private static final String BIRTH_BIRTH_SIBLING = "MATCH (a)-[r:GT_SIBLING { actors: \"Child-Child\" } ]->(b) return count(r)";
    private static final String BIRTH_BIRTH_HALF_SIBLING = "MATCH (a)-[r:GT_HALF_SIBLING { actors: \"Child-Child\" } ]->(b) return count(r)";
    private static final String BIRTH_DEATH_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Child-Deceased\" } ]->(b) return count(r)";
    private static final String BIRTH_DEATH_SIBLING = "MATCH (a)-[r:GT_SIBLING { actors: \"Child-Deceased\" } ]->(b) return count(r)";
    private static final String BIRTH_DEATH_HALF_SIBLING = "MATCH (a)-[r:GT_HALF_SIBLING { actors: \"Child-Deceased\" } ]->(b) return count(r)";
    private static final String BIRTH_GROOM_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Child-Groom\" } ]->(b) return count(r)";
    private static final String BIRTH_BRIDE_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Child-Bride\" } ]->(b) return count(r)";
    private static final String FATHER_GROOM_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Father-Groom\" } ]->(b) return count(r)";
    private static final String MOTHER_BRIDE_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Mother-Bride\" } ]->(b) return count(r)";
    private static final String BIRTH_PARENTS_MARRIAGE_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Child-Couple\" } ]->(b) return count(r)";
    private static final String DEATH_PARENTS_MARRIAGE_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Deceased-Couple\" } ]->(b) return count(r)";
    private static final String BIRTH_GROOM_SIBLING = "MATCH (a)-[r:GT_SIBLING { actors: \"Child-Groom\" } ]->(b) return count(r)";
    private static final String BIRTH_BRIDE_SIBLING = "MATCH (a)-[r:GT_SIBLING { actors: \"Child-Bride\" } ]->(b) return count(r)";
    private static final String DEATH_GROOM_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Deceased-Groom\" } ]->(b) return count(r)";
    private static final String DEATH_BRIDE_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Deceased-Bride\" } ]->(b) return count(r)";
    private static final String DEATH_GROOM_SIBLING = "MATCH (a)-[r:GT_SIBLING { actors: \"Deceased-Groom\" } ]->(b) return count(r)";
    private static final String DEATH_BRIDE_SIBLING = "MATCH (a)-[r:GT_SIBLING { actors: \"Deceased-Bride\" } ]->(b) return count(r)";
    private static final String GROOM_GROOM_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Groom-Groom\" } ]->(b) return count(r)";
    private static final String BRIDE_BRIDE_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Bride-Bride\" } ]->(b) return count(r)";
    private static final String GROOM_PARENTS_MARRIAGE_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Groom-Couple\" } ]->(b) return count(r)";
    private static final String BRIDE_PARENTS_MARRIAGE_IDENTITY = "MATCH (a)-[r:GT_ID { actors: \"Bride-Couple\" } ]->(b) return count(r)";
    private static final String GROOM_GROOM_SIBLING = "MATCH (a)-[r:GT_SIBLING { actors: \"Groom-Groom\" } ]->(b) return count(r)";
    private static final String BRIDE_BRIDE_SIBLING = "MATCH (a)-[r:GT_SIBLING { actors: \"Bride-Bride\" } ]->(b) return count(r)";
    private static final String BRIDE_GROOM_SIBLING = "MATCH (a)-[r:GT_SIBLING { actors: \"Bride-Groom\" } ]->(b) return count(r)";
    private static final String DEATH_DEATH_SIBLING = "MATCH (a)-[r:GT_SIBLING { actors: \"Deceased-Deceased\" } ]->(b) return count(r)";
    private static final String GROOM_GROOM_HALF_SIBLING = "MATCH (a)-[r:GT_HALF_SIBLING { actors: \"Groom-Groom\" } ]->(b) return count(r)";
    private static final String BRIDE_BRIDE_HALF_SIBLING = "MATCH (a)-[r:GT_HALF_SIBLING { actors: \"Bride-Bride\" } ]->(b) return count(r)";
    private static final String BRIDE_GROOM_HALF_SIBLING = "MATCH (a)-[r:GT_HALF_SIBLING { actors: \"Bride-Groom\" } ]->(b) return count(r)";
    private static final String DEATH_DEATH_HALF_SIBLING = "MATCH (a)-[r:GT_HALF_SIBLING { actors: \"Deceased-Deceased\" } ]->(b) return count(r)";

    private static void doQuery(Session session, String title, String query) {

        Result r = session.run(query);
        Value x = r.stream().findFirst().get().get( "count(r)" );
        System.out.println( title + " : " + x );
    }

    public static void main(String[] args) {

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge(); Session session = bridge.getNewSession()) {

            System.out.println("Analysing sizes of relationships from GT @ " + LocalDateTime.now());

            doQuery(session, "Birth-birth sibling", BIRTH_BIRTH_SIBLING);
            doQuery(session, "Birth-birth half sibling", BIRTH_BIRTH_HALF_SIBLING);
            doQuery(session, "Birth-death identity", BIRTH_DEATH_IDENTITY);
            doQuery(session, "Birth-bride identity", BIRTH_BRIDE_IDENTITY);
            doQuery(session, "Birth-groom identity", BIRTH_GROOM_IDENTITY);
            doQuery(session, "Death-groom identity", DEATH_GROOM_IDENTITY);
            doQuery(session, "Death-bride identity", DEATH_BRIDE_IDENTITY);
            doQuery(session, "Birth-parents marriage identity", BIRTH_PARENTS_MARRIAGE_IDENTITY);
            doQuery(session, "Father-child identity", FATHER_OWN_BIRTH_IDENTITY);
            doQuery(session, "Mother-child identity", MOTHER_OWN_BIRTH_IDENTITY);
            doQuery(session, "Groom-groom sibling", GROOM_GROOM_SIBLING);
            doQuery(session, "Groom-groom half sibling", GROOM_GROOM_HALF_SIBLING);
            doQuery(session, "Bride-bride sibling", BRIDE_BRIDE_SIBLING);
            doQuery(session, "Bride-bride half sibling", BRIDE_BRIDE_HALF_SIBLING);
            doQuery(session, "Bride-groom sibling", BRIDE_GROOM_SIBLING);
            doQuery(session, "Bride-groom half sibling", BRIDE_GROOM_HALF_SIBLING);
            doQuery(session, "Death-death sibling", DEATH_DEATH_SIBLING);
            doQuery(session, "Death-death half sibling", DEATH_DEATH_HALF_SIBLING);
            doQuery(session, "Birth-death sibling", BIRTH_DEATH_SIBLING);
            doQuery(session, "Birth-death half sibling", BIRTH_DEATH_HALF_SIBLING);
            doQuery(session, "Birth-bride sibling", BIRTH_BRIDE_SIBLING);
            doQuery(session, "Birth-groom sibling", BIRTH_GROOM_SIBLING);
            doQuery(session, "Bride-bride identity", BRIDE_BRIDE_IDENTITY);
            doQuery(session, "Groom-groom identity", GROOM_GROOM_IDENTITY);
            doQuery(session, "Bride parents marriage", BRIDE_PARENTS_MARRIAGE_IDENTITY);
            doQuery(session, "Groom parents marriage", GROOM_PARENTS_MARRIAGE_IDENTITY);
            doQuery(session, "Death-bride sibling", DEATH_BRIDE_SIBLING);
            doQuery(session, "Death-groom sibling", DEATH_GROOM_SIBLING);
            doQuery(session, "Father-groom identity", FATHER_GROOM_IDENTITY);
            doQuery(session, "Mother-bride identity", MOTHER_BRIDE_IDENTITY);
            doQuery(session, "Death-parents marriage identity", DEATH_PARENTS_MARRIAGE_IDENTITY);

            System.out.println("Complete @ " + LocalDateTime.now());
        }
    }
}
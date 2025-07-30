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
package uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthGroomOwnMarriage;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.datasets.Umea;
import uk.ac.standrews.cs.population_linkage.FelligiSunter.ProcessNodes;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.Pair;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.standrews.cs.population_linkage.supportClasses.Constants.*;

public class DistanceDistributionsBirthGroomOwnMarriage extends ProcessNodes {

    protected final String repo_name;

    // Copied from Priors:

    private static final String BIRTH_GROOM_GT_IDENTITY_LINKS_QUERY = "MATCH (first_result:Birth)-[rel:GT_ID { actors: \"Child-Groom\" } ]-(second_result:Marriage) RETURN first_result,second_result";
    private static final String BIRTHS_SAMPLE_QUERY = "MATCH (result:Birth) WITH result WHERE rand() < 0.5 RETURN result"; // 1 in 10 chance of selection!
    private static final String MARRIAGES_SAMPLE_QUERY = "MATCH (result:Marriage) WITH result WHERE rand() < 0.5 RETURN result";  // 1 in 10 chance of selection!

    private DistanceDistributionsBirthGroomOwnMarriage(String repo_name) {

        this.repo_name = repo_name;
    }

    private static List<StringMeasure> measures = List.of( COSINE, JACCARD, JENSEN_SHANNON, SED); // true metrics returning between - and 1.

    @SuppressWarnings("unchecked")
    public void run() {

        try (RecordRepository record_repository = new RecordRepository(repo_name)) {

            int number_of_records_to_be_checked = 25000; // 25000;

            final IBucket<Birth> birth_records = (IBucket<Birth>) record_repository.getBucket("birth_records" );
            final IBucket<Marriage> marriage_records = (IBucket<Marriage>) record_repository.getBucket("marriage_records" );


            //for (StringMeasure measure : Constants.BASE_MEASURES) {
            StringMeasure base_measure = JACCARD;
            calculateDistances(birth_records, marriage_records, BirthGroomOwnMarriageBuilder.getRecipe(repo_name, String.valueOf(number_of_records_to_be_checked)).getCompositeMeasure(base_measure));
            //}
        }
    }

    private static final String BM_BIRTH_GROOM_GTEXISTS_QUERY = "MATCH (a:Birth)-[r:GTID { actors: \"Child-Groom\" }]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to RETURN r";

    public static boolean BMBirthGroomReferenceExists(NeoDbCypherBridge bridge, String standard_id_from, String standard_id_to) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        Result result = bridge.getNewSession().run(BM_BIRTH_GROOM_GTEXISTS_QUERY,parameters);
        List<Relationship> relationships = result.list(r -> r.get("r").asRelationship());
        if( relationships.size() == 0 ) {
            return false;
        }
        return true;
    }

    private void calculateDistances(IBucket<Birth> birth_records, IBucket<Marriage> marriage_records, LXPMeasure measure) {

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {

            List<Pair<Node, Node>> m_pairs = getPairs(bridge,BIRTH_GROOM_GT_IDENTITY_LINKS_QUERY);
            List<Pair<Node, Node>> u_pairs = getPairs(bridge,BIRTHS_SAMPLE_QUERY,MARRIAGES_SAMPLE_QUERY);

            showDistances(birth_records, marriage_records, measure, m_pairs, "m");
            showDistances(birth_records, marriage_records, measure, u_pairs, "u");
        }

    }

    private void showDistances(IBucket<Birth> birth_records, IBucket<Marriage> marriage_records, LXPMeasure measure, List<Pair<Node, Node>> pairs, String kind) {
        for( Pair<Node, Node> pair : pairs) {

            Node birth_node = pair.X();
            Node marriage_node = pair.Y();

            String birth_id = birth_node.get("STORR_ID").asString();
            String marriage_id = marriage_node.get("STORR_ID").asString();

            try {
                Birth birth = birth_records.getObjectById(birth_id);
                Marriage marriage = marriage_records.getObjectById(marriage_id);
                double distance = measure.distance(birth, marriage);
                System.out.println( kind + "\t" + distance );
            } catch (BucketException e) {
                e.printStackTrace();
            }
        }
    }

    private void calculateOLDDistances(final List<LXP> birth_records, List<LXP> marriage_records, LXPMeasure measure) {

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            for (LXP record1 : birth_records) {
                for (LXP record2 : marriage_records) {

                    String std_id_from = record1.getString(Birth.STANDARDISED_ID);
                    String std_id_to = record2.getString(Marriage.STANDARDISED_ID);

                    double distance = measure.distance(record1, record2);
                    if (BMBirthGroomReferenceExists(bridge, std_id_from, std_id_to)) {
                        System.out.println( "M\t" + distance );
                    } // else {
//                        System.out.println( "U\t" + distance );
//                    }
                }
            }
        }

    }

    public static void main(String[] args) {

        new DistanceDistributionsBirthGroomOwnMarriage(Umea.REPOSITORY_NAME).run();
    }
}

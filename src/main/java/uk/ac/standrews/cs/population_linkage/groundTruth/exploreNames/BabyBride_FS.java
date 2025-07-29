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
package uk.ac.standrews.cs.population_linkage.groundTruth.exploreNames;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.Pair;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.text.DecimalFormat;
import java.util.*;

public class BabyBride_FS {

    public static final int SEED = 14638657;
    private final int num_births;
    private final int num_marriages;
    private final IBucket<Birth> births;
    private final IBucket<Marriage> marriages;
    private final RecordRepository record_repository;
    //    private final long count_first_name_same_id_different;
//    private final long count_surname_same_id_different_count;
    private StringMeasure measure = Constants.JENSEN_SHANNON;
    private DecimalFormat df = new DecimalFormat("0.00");

    protected final NeoDbCypherBridge bridge;

    private long count_gt_first_same = 0;      // for GT matches
    private long count_gt_first_different = 0;// for GT matches
    private long count_gt_surname_same = 0;// for GT matches
    private long count_gt_surname_different = 0;// for GT matches
    private long count_gt_both_same = 0;
    private long count_bothnames_same_id_different = 0;
    private long count_first_name_same_id_different = 0;
    private long count_surname_same_id_different = 0;
    private long num_pairs = 0;
    private long count_unmatched = 0;
    private long gt_pairs_considered = 0;

    private TreeSet<String> baby_first_names = new TreeSet<>();
    private TreeSet<String> bride_first_names = new TreeSet<>();
    private TreeSet<String> all_first_names = new TreeSet<>();

    private TreeSet<String> baby_surnames = new TreeSet<>();
    private TreeSet<String> bride_surnames = new TreeSet<>();
    private TreeSet<String> all_surnames = new TreeSet<>();

    private static final String BIRTH_BRIDE_GT_IDENTITY_MATCH_QUERY = "MATCH (b:Birth)-[r:GT_ID {actors: \"Child-Bride\"}]-(m:Marriage) RETURN b,m";
//    private static final String DIFFERENT_ID_QUERY = "MATCH (b:Birth), (m:Marriage) WHERE b.SEX = \"F\" AND b.CHILD_IDENTITY <> \"\" AND m.BRIDE_IDENTITY <> \"\" AND b.CHILD_IDENTITY <> m.BRIDE_IDENTITY return b,m LIMIT 50000";


//    private static final String NAMES_SAME_ID_DIFFERENT_COUNT = "MATCH (b:Birth), (m:Marriage)       WHERE b.FORENAME = m.BRIDE_FORENAME AND b.SURNAME = m.BRIDE_SURNAME AND b.CHILD_IDENTITY <> \"\" AND m.BRIDE_IDENTITY <> \"\" AND b.CHILD_IDENTITY <> m.BRIDE_IDENTITY return count(b)";
//    private static final String FIRST_NAMES_SAME_ID_DIFFERENT_COUNT = "MATCH (b:Birth), (m:Marriage) WHERE b.FORENAME = m.BRIDE_FORENAME                                 AND b.CHILD_IDENTITY <> \"\" AND m.BRIDE_IDENTITY <> \"\" AND b.CHILD_IDENTITY <> m.BRIDE_IDENTITY return count(b)";
//    private static final String SURNAMES_SAME_ID_DIFFERENT_COUNT = "MATCH (b:Birth), (m:Marriage)    WHERE b.SURNAME  = m.BRIDE_SURNAME                                  AND b.CHILD_IDENTITY <> \"\" AND m.BRIDE_IDENTITY <> \"\" AND b.CHILD_IDENTITY <> m.BRIDE_IDENTITY return count(b)";

    public BabyBride_FS() throws BucketException {
        bridge = new NeoDbCypherBridge();
        record_repository = new RecordRepository("umea");
        births = record_repository.getBucket("birth_records");
        marriages = record_repository.getBucket("marriage_records");
        num_births = births.size();
        num_marriages = marriages.size();
    }

    public static List<Pair<Node, Node>> getPairs(NeoDbCypherBridge bridge, String query) {
        Result result = bridge.getNewSession().run(query);
        return result.list(r -> new Pair<>(r.get("b").asNode(), r.get("m").asNode()));
    }

    private void explore() {
        List<Pair<Node, Node>> match_gt_nodes = getPairs(bridge,BIRTH_BRIDE_GT_IDENTITY_MATCH_QUERY);
        analyse_gt(match_gt_nodes);
        num_pairs = num_births * num_marriages;
        count_unmatched = num_pairs - count_gt_both_same;
        gt_pairs_considered = match_gt_nodes.size();
        analyseUnMatched(gt_pairs_considered);
        examineNodes();
    }

    private void examineNodes() {

        System.out.println("Count linked in GT  : " + count_gt_both_same);
        System.out.println("Count linked in U: " + count_bothnames_same_id_different);
        System.out.println("GT pairs considered  : " + gt_pairs_considered );

        float p_m_prior = calculatePrior(count_gt_both_same, gt_pairs_considered ); // num_births,num_marriages );
        float p_u_prior = calculatePrior(count_bothnames_same_id_different, gt_pairs_considered ); // num_births,num_marriages );

        System.out.println();
        System.out.println("P(M)_prior: " + p_m_prior );
        System.out.println("Match Prior P(Unmatch)prior: " + p_u_prior );


        float records_considered = gt_pairs_considered * 1.0f;

        float p_first_given_match = ( count_gt_first_same * 1.0f ) / records_considered;
        float p_second_given_match = ( count_gt_surname_same * 1.0f ) / records_considered;
        float p_first_given_nonmatch = ( count_first_name_same_id_different * 1.0f ) / records_considered;
        float p_second_given_nonmatch = ( count_surname_same_id_different * 1.0f ) / records_considered;

        System.out.println();
        System.out.println( "p_first_given_match\t" + p_first_given_match );
        System.out.println( "p_second_given_match\t" + p_second_given_match );
        System.out.println();
        System.out.println( "p_first_given_nonmatch\t" + p_first_given_nonmatch );
        System.out.println( "p_second_given_nonmatch\t" + p_second_given_nonmatch );

        System.out.println();
        float match_weight_first = p_first_given_match / p_first_given_nonmatch;
        float match_weight_second = p_second_given_match / p_second_given_nonmatch;
        double log_match_weight_first = Math.log( match_weight_first );
        double log_match_weight_second = Math.log( match_weight_second );

        System.out.println( "Likelihood ratio for first name P(firstname/Match) / P(firstname/UnMatch)\t" + match_weight_first + "\tlog: " + log_match_weight_first );
        System.out.println( "Likelihood ratio for surname P(surname/Match) / P(surname/UnMatch)\t" + match_weight_second + "\tlog: " + log_match_weight_second  );

        float p_given_match = p_first_given_match * p_second_given_match;
        float p_given_unmatch = p_first_given_nonmatch * p_second_given_nonmatch;


        float prior_odds = p_m_prior / p_u_prior;
        float likelihood_ratio = p_given_match / p_given_unmatch;

        System.out.println();
        System.out.println( "prior odds\t" + prior_odds );
        System.out.println( "likelihood_ratio\t" + likelihood_ratio );

        float posterior_odds_ratio = likelihood_ratio * prior_odds;
        float p_posterior = posterior_odds_ratio / ( 1 + posterior_odds_ratio );

        System.out.println();
        System.out.println( "p_match_posterior (prob of any two records are a match)\t" + p_posterior );
    }


    /**
     * Initialises the count_first_different and count_surname_different variables
     * All the ids supplied in the pairs should be different i.e. non match in GT.
     */
    private void analyseUnMatched(long num_pairs_to_consider) {

        // All the ids of birth records
        String[] all_birth_ids = births.getObjectIds().toArray(new String[0]);
        String[] all_marriage_ids = marriages.getObjectIds().toArray(new String[0]);

        // The array indices that we have chosen already to be in the unmatched list
        TreeSet<Integer> picked_birth_ids = new TreeSet<>();
        TreeSet<Integer> picked_marriage_ids = new TreeSet<>();

        // random number to pick candidates
        Random random = new Random(SEED);

        int count = 0;

        while( count < num_pairs_to_consider ) { // pick another pair not picked before
            final int bid = random.nextInt(all_birth_ids.length);
            final int mid = random.nextInt(all_marriage_ids.length);
            if( picked_birth_ids.contains( bid ) || picked_marriage_ids.contains( mid ) ) {
                continue; // try next pair
            }
            try {
                Birth birth = births.getObjectById(all_birth_ids[bid]);
                Marriage marriage = marriages.getObjectById(all_marriage_ids[mid]);

                picked_birth_ids.add(bid);
                picked_marriage_ids.add(mid);
                //  Now have two random records that we have not picked already
                // Check that they are really an unmatched pair
                if (birth.get(Birth.CHILD_IDENTITY).equals(marriage.get(Marriage.BRIDE_IDENTITY))) {
                    continue; // try next pair
                }
                // Now we know the two records are unmatched in GT so do the counting
                count++;

                String baby_firstname = birth.getString(Birth.FORENAME);
                String baby_surname = birth.getString(Birth.SURNAME);
                String bride_firstname = marriage.getString(Marriage.BRIDE_FORENAME);
                String bride_surname = marriage.getString(Marriage.BRIDE_SURNAME);

                // System.out.println( count + " " + baby_firstname + " " + baby_surname + " " + bride_firstname + " " + bride_surname );

                if (baby_firstname.equals(bride_firstname)) {
                    count_first_name_same_id_different++;
                }
                if (baby_surname.equals(bride_surname)) {
                    count_surname_same_id_different++;
                }
                if (baby_firstname.equals(bride_firstname) && baby_surname.equals(bride_surname)) {
                    count_bothnames_same_id_different++;
                }
            } catch (BucketException e) {
                System.out.println("Bucket exception for pair " + all_birth_ids[bid] + " and " + all_marriage_ids[mid]);
                continue; // ignore that pair - should never happen!
            }
        }
    }

    private void analyse_gt(List<Pair<Node, Node>> match_gt_pairs) {
        for (Pair<Node, Node> p : match_gt_pairs) {
            Node birth = p.X();
            Node marriage = p.Y();
            String baby_firstname = birth.get("FORENAME").toString(); // this is in Node space from Cypher not LXP!
            String baby_surname = birth.get("SURNAME").toString();
            String bride_firstname = marriage.get("BRIDE_FORENAME").toString();
            String bride_surname = marriage.get("BRIDE_SURNAME").toString();

            baby_first_names.add( baby_firstname );
            all_first_names.add( baby_firstname );
            baby_surnames.add( baby_surname );
            all_surnames.add( baby_surname );

            bride_first_names.add( bride_firstname );
            all_first_names.add( bride_firstname );
            bride_surnames.add( bride_surname );
            all_surnames.add( bride_surname );

            if ( baby_firstname.equals(bride_firstname)) {
                count_gt_first_same++;
            } else {
                count_gt_first_different++;
            }

            if ( baby_surname.equals(bride_surname)) {
                count_gt_surname_same++;
            } else {
                count_gt_surname_different++;
            }

            if (baby_firstname.equals(bride_firstname) && baby_surname.equals(bride_surname)) {
                count_gt_both_same++;
            }
        }
    }

    private float calculatePrior(long expected_matches, long considered) {
        return ( expected_matches * 1.0f ) / considered;
    }

    protected long doQuery(String query_string) {
        Map<String, Object> parameters = new HashMap<>();
        Result result = bridge.getNewSession().run(query_string, parameters);
        long value = result.list(r -> r.get("count(r)").asInt()).get(0);
        return value;
    }

    private void showPair(int counter, String label, String baby_firstname, String baby_surname, String baby_id, String bride_firstname, String bride_surname, String bride_id) {
        if (!baby_id.equals(bride_id)) {
            System.out.println("GT MATCH ERROR");
            System.exit(-1);
        }
        if (counter < 10) {
            System.out.println("*** " + label + ":");
            System.out.println("baby:  " + baby_firstname + "/" + baby_surname);
            System.out.println("bride: " + bride_firstname + "/" + bride_surname);
            double fnd = measure.distance(baby_firstname, bride_firstname);
            double snd = measure.distance(baby_surname, bride_surname);
            System.out.print("Firstname distance = " + df.format(fnd));
            System.out.print(" Surname distance = " + df.format(snd));
            System.out.println(" Combined distance = " + df.format((fnd + snd) / 2));
        }
    }

    public static void main(String[] args) throws BucketException {

        BabyBride_FS en = null;
        try {
            en = new BabyBride_FS();
            en.explore();
        } finally {
            en.bridge.close();
            en.record_repository.close();
        }
    }
}

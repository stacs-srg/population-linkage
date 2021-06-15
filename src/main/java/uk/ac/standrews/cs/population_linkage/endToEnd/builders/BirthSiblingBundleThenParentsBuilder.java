/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.builders;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.endToEnd.runners.BitBlasterSubsetOfDataEndtoEndSiblingBundleLinkageRunner;
import uk.ac.standrews.cs.population_linkage.endToEnd.subsetRecipes.BirthSiblingSubsetLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.graph.model.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.ParentsMarriageBirthLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_linkage.supportClasses.Sigma;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.PercentageProgressIndicator;
import uk.ac.standrews.cs.utilities.ProgressIndicator;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

import static uk.ac.standrews.cs.population_linkage.endToEnd.util.Util.closeTo;
import static uk.ac.standrews.cs.population_linkage.endToEnd.util.Util.getBirthSiblings;

/**
 * This class attempts to perform birth-birth sibling linkage.
 * It creates a Map of families indexed (at the momement TODO) from birth ids to families
 */
public class BirthSiblingBundleThenParentsBuilder {

    private static final double COMBINED_AVERAGE_DISTANCE_THRESHOLD = 0.2;
    public static final int PREFILTER_REQUIRED_FIELDS = 8;
    private static final double DISTANCE_THRESHOLD = 0.67;

    public static void main(String[] args) throws Exception {

        BitBlasterSearchStructure<Marriage> bb = null;        // initialised in try block - decl is here so we can finally shut it.

        try(NeoDbCypherBridge bridge = new NeoDbCypherBridge(); ) {
            String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
            String resultsRepo = args[1]; // e.g. synth_results

            BirthSiblingSubsetLinkageRecipe bb_recipe = new BirthSiblingSubsetLinkageRecipe(sourceRepo, resultsRepo, bridge,BirthSiblingBundleThenParentsBuilder.class.getCanonicalName() );

            final BitBlasterSubsetOfDataEndtoEndSiblingBundleLinkageRunner runner1 = new BitBlasterSubsetOfDataEndtoEndSiblingBundleLinkageRunner();

            int linkage_fields = bb_recipe.ALL_LINKAGE_FIELDS;
            int half_fields = linkage_fields - (linkage_fields / 2 ) + 1;

            while( linkage_fields >= half_fields ) {
                bb_recipe.setNumberLinkageFieldsRequired(linkage_fields);

                LinkageResult lr = runner1.run(bb_recipe, new JensenShannon(2048), false, false, false, false);
                HashMap<Long, List<Link>> families = runner1.getFamilyBundles(); // from LXP Id to Links.
                ParentsMarriageBirthLinkageRecipe parents_recipe = new ParentsMarriageBirthLinkageRecipe(sourceRepo, resultsRepo, BirthSiblingBundleThenParentsBuilder.class.getCanonicalName());
                LinkageConfig.numberOfROs = 20;
                Iterable<LXP> marriage_records = parents_recipe.getStoredRecords();
                StringMetric baseMetric = new JensenShannon(2048);
                Metric<LXP> composite_metric = getCompositeMetric(parents_recipe, baseMetric);
                bb = new BitBlasterSearchStructure(composite_metric, marriage_records);
                int[] number_of_marriages_per_family = new int[20]; // 20 is far too big!
                List<String> seen_already = new ArrayList<>(); // keys of the sibling-marriage pairs we have already seen.
                // This is inefficient but safe - consider doing something else?
                System.out.println("Forming family bundles @ " + LocalDateTime.now().toString());
                Collection<List<Link>> all_families = families.values(); // all the families: links from siblings to marriages.
                ProgressIndicator progress_indicator = new PercentageProgressIndicator(100);
                progress_indicator.setTotalSteps(all_families.size());
                processFamilies(bridge, bb, parents_recipe, number_of_marriages_per_family, all_families, progress_indicator, seen_already);
                printFamilyStats(number_of_marriages_per_family);
                linkage_fields--;
            }
        } finally {
            System.out.println( "Finishing" );
            if( bb != null ) { bb.terminate(); } // shut down the metric search threads
            System.out.println( "Run finished" );
            System.exit(0); // make sure it all shuts down.
        }
    }

    private static void printFamilyStats(int[] number_of_marriages_per_family) {
        int sum = 0;
        for (int i = 1; i < number_of_marriages_per_family.length; i++) {
            final int no_of_marriages = number_of_marriages_per_family[i];
            if (no_of_marriages != 0) {
                System.out.println("Number of marriages per family for family of size " + i + " = " + no_of_marriages);
                sum = sum + no_of_marriages;
            }
        }
        System.out.println("Total number of families = " + sum);
    }

    private static void processFamilies(NeoDbCypherBridge bridge, BitBlasterSearchStructure<Marriage> bb, LinkageRecipe parents_recipe, int[] number_of_marriages_per_family, Collection<List<Link>> all_families, ProgressIndicator progress_indicator, List<String> seen_already) throws BucketException, RepositoryException {
        for( List<Link> siblings : all_families) {

            Set<LXP> sib_records = getBirthSiblings(siblings);

            Set<SiblingParentsMarriage> sibling_parents_marriages = new TreeSet<>();

            for (LXP sibling : sib_records) {
                Marriage search_record = (Marriage) parents_recipe.convertToOtherRecordType(sibling);

                List<DataDistance<Marriage>> distances = bb.findWithinThreshold(search_record, DISTANCE_THRESHOLD);

                sibling_parents_marriages.add(new SiblingParentsMarriage(sibling, distances));
            }

            int count = countDifferentMarriagesInGrouping(sibling_parents_marriages);
            if (count > 1) {
                adjustMarriagesInGrouping(sibling_parents_marriages);
            }
            System.out.println( "num families in group = " + count );

            addChildParentsMarriageToNeo4J(bridge, sibling_parents_marriages,seen_already);

            number_of_marriages_per_family[count]++;
            progress_indicator.progressStep();
        }
    }

    /**
     * Adds a 'family' to Neo4J; this involves:
     *      adding a link from each child to the siblings
     *      adding a link from each child to all the parents marriage records
     * @param bridge - the neo4J session currently open
     * @param sibling_parents_marriages - a record containing 1 sibling the Marriage records of that sibling.
     * @param seen_already
     * @throws Exception
     */
    private static void addChildParentsMarriageToNeo4J(NeoDbCypherBridge bridge, Set<SiblingParentsMarriage> sibling_parents_marriages, List<String> seen_already) {

        String provenance = getThisClassName();

        for (SiblingParentsMarriage spm : sibling_parents_marriages) {

            final LXP sibling = spm.sibling;

            for (DataDistance<Marriage> distance : spm.parents_marriages) {
                final LXP marriage = distance.value;
                double dist = distance.distance;
                String sibling_std_id = sibling.getString( Birth.STANDARDISED_ID );
                String marriage_std_id = marriage.getString( Marriage.STANDARDISED_ID );

                String pair_key = sibling_std_id+marriage_std_id;
                if( ! seen_already.contains(pair_key) ) {
                    Query.createBMMotherReference(bridge, sibling_std_id, marriage_std_id, provenance, PREFILTER_REQUIRED_FIELDS, dist);
                    Query.createBMFatherReference(bridge, sibling_std_id, marriage_std_id, provenance, PREFILTER_REQUIRED_FIELDS, dist);
                    seen_already.add(pair_key);
                }
            }
        }
    }

    private static double getDistance(LXP sibling1, LXP sibling2, List<Link> siblings) {
        for( Link link : siblings ) {
            if( link.getRecord1().equals( sibling1 ) && link.getRecord2().equals( sibling2 ) ||
                    link.getRecord2().equals( sibling1 ) && link.getRecord1().equals( sibling2 ) ) {
                return link.getDistance();
            }
        }
        return 1.0; // didn't find pair - should never happen.
    }

    private static String getThisClassName() {
        StackTraceElement[] stack = Thread.currentThread ().getStackTrace ();
        StackTraceElement main = stack[stack.length - 2];
        return main.getClassName();
    }

    private static int countDifferentMarriagesInGrouping(Set<SiblingParentsMarriage> sibling_parents_marriages) {

        Map<LXP, Integer> counts = new HashMap<>();
        for (SiblingParentsMarriage spm : sibling_parents_marriages) {
            for (DataDistance<Marriage> distance : spm.parents_marriages) {
                LXP marriage = distance.value;
                if (counts.containsKey(marriage)) {
                    counts.put(marriage, counts.get(marriage) + 1);
                } else {
                    counts.put(marriage, 1);
                }
            }
        }

        return counts.keySet().size();
    }

    private static Set<SiblingParentsMarriage> adjustMarriagesInGrouping(Set<SiblingParentsMarriage> sibling_parents_marriages) throws BucketException {

        TreeMap<String, Integer> marriage_counts = new TreeMap<>();     // map from the marriages to number of occurances.

        int sibling_index = 0;

        // build a map from marriage id to number of occurences of that marriage in the set.

        for (SiblingParentsMarriage spm : sibling_parents_marriages) {

            for (DataDistance<Marriage> distance_and_marriage : spm.parents_marriages) {
                final LXP marriage = distance_and_marriage.value;
                String id = marriage.getString(Marriage.STANDARDISED_ID);
                if (marriage_counts.keySet().contains(id)) {
                    int count = marriage_counts.get(id);
                    marriage_counts.put(id, count + 1);
                } else {
                    marriage_counts.put(id, 1);
                }
            }
        }

        // if there is one set of parents they all agree, then on use that.

        int num_siblings = sibling_parents_marriages.size();
        if( marriage_counts.values().contains( num_siblings ) ) {
            // then there is at least one set of parents on which they all agree!
            // so find them.
            List<String> all_agree_ids = new ArrayList<>();
            for( Map.Entry<String, Integer> entry: marriage_counts.entrySet() ) {
                if( entry.getValue() == num_siblings ) {
                    all_agree_ids.add(entry.getKey());
                }
            }
            // Hopefully there is only 1;
            if( all_agree_ids.size() == 1 ) {
                // all the siblings can agree on exactly one parent pair.
                String standard_agreed_id = all_agree_ids.get(0);

                return replaceAllInExcept( standard_agreed_id,sibling_parents_marriages );

            } else {

                return findLowestCombinedIfExistsOrAll( all_agree_ids,sibling_parents_marriages );
                // TODO Need more code here not sure what!!! - the above method will return multiples if there is no agreement - maybe this is OK.
            }

        } else {
            System.out.println( "There are not any parents on which the siblings agree" );
            // TODO Need more code here not sure what!!! - the above method will return multiples if there is no agreement - maybe this is OK.
        }

        //TODO if we get here there is no one list of siblings that share a single set of parents - WHAT TO DO?? - MAYBE OK.
        return sibling_parents_marriages;
    }

    /**
     * Tries to find a list of sibling marriages on which the siblings all agree, and whose combined distance is close to zero.
     * @param all_agree_ids
     * @param sibling_parents_marriages
     * @return a list of sibling marriages that are the best and close to zero
     */
    private static Set<SiblingParentsMarriage> findLowestCombinedIfExistsOrAll(List<String> all_agree_ids, Set<SiblingParentsMarriage> sibling_parents_marriages) {

        // try and find the lowest combined distance in the family.
        double lowest = Double.MAX_VALUE;
        SiblingParentsMarriage best_so_far = null;

        for (SiblingParentsMarriage spm : sibling_parents_marriages) {
            LXP sibling = spm.sibling;
            double total = 0.0;
            for (DataDistance<Marriage> distance : spm.parents_marriages) {
                total += distance.distance;
            }
            if (total < lowest) {
                lowest = total;
                best_so_far = spm;
            }
        }

        // See if there is a winner!

        double average_distance = lowest / best_so_far.parents_marriages.size();

        if (  average_distance < COMBINED_AVERAGE_DISTANCE_THRESHOLD ) {

            System.out.println("Found clear winner for set of parents !!!! - average distance = " + average_distance);
            TreeSet<SiblingParentsMarriage> result = new TreeSet<>();
            result.add(best_so_far);
            return result;

        } else {
            System.out.println("Could not find a clear winner for set of parents ##### - average distance = " + average_distance);
            return sibling_parents_marriages;
        }
    }

    /**
     * Filters out all of the marriages from the various lists except from the one one which they all agree
     * @param standard_agreed_id
     * @param sibling_parents_marriages
     * @return
     */
    private static Set<SiblingParentsMarriage> replaceAllInExcept(String standard_agreed_id, Set<SiblingParentsMarriage> sibling_parents_marriages) {
        TreeSet<SiblingParentsMarriage> result = new TreeSet<>();
        for( SiblingParentsMarriage spm : sibling_parents_marriages ) {
            LXP sibling = spm.sibling;
            for( DataDistance<Marriage> distance : spm.parents_marriages ) {
                final Marriage marriage = distance.value;
                String id = marriage.getString(Marriage.STANDARDISED_ID);
                if(standard_agreed_id.equals(id)) {
                    List<DataDistance<Marriage>> new_list = new ArrayList<>();
                    new_list.add( new DataDistance<>(marriage,distance.distance) );
                    SiblingParentsMarriage new_entry = new SiblingParentsMarriage( sibling,new_list );
                    result.add( new_entry );
                }
            }
        }
        return result;
    }


    private static void showMarriagesInGrouping(List <SiblingParentsMarriage> sibling_parents_marriages) throws BucketException {

        DecimalFormat df = new DecimalFormat("0.00" );

        HashMap<String, Integer> marriage_counts = new HashMap<>();     // map from the marriages to number of occurances.
        List<DataDistance<Marriage>> distances = new ArrayList<>();          // all the distances from all the siblings - siblings can have multiple parents

        System.out.println("Family unit:");
        System.out.println("Number of children in bundle: " + sibling_parents_marriages.size());

        int sibling_index = 0;

        for (SiblingParentsMarriage spm : sibling_parents_marriages) {

            System.out.println( sibling_index++ + " has " + spm.parents_marriages.size() + " parents' marriages");

            for (DataDistance<Marriage> distance : spm.parents_marriages) {
                final LXP marriage = distance.value;
                String id = marriage.getString(Marriage.STANDARDISED_ID);
                if (marriage_counts.keySet().contains(id)) {
                    int count = marriage_counts.get(id);
                    marriage_counts.put(id, count + 1);
                } else {
                    marriage_counts.put(id, 1);
                    distances.add(distance);
                }
            }
        }

        System.out.println("Number of different parents marriages in bundle: " + distances.size());
        for (DataDistance<Marriage> distance : distances) {
            String perfect_match = closeTo( distance.distance,0.0 ) ? "  ********": "";
            System.out.println("Distance = " + df.format(distance.distance) + perfect_match );
            LXP marriage = distance.value;
            String id = marriage.getString(Marriage.STANDARDISED_ID);
            boolean all_agree = marriage_counts.get(id) == sibling_parents_marriages.size();
            System.out.println("Marriage occurances = " + marriage_counts.get(id) + " all agree = " + all_agree );
            DisplayMethods.showMarriage(marriage);
        }
        System.out.println("---");
    }


    protected static Metric<LXP> getCompositeMetric(final LinkageRecipe linkageRecipe, StringMetric baseMetric) {
        return new Sigma(baseMetric, linkageRecipe.getLinkageFields(), 0);
    }
}

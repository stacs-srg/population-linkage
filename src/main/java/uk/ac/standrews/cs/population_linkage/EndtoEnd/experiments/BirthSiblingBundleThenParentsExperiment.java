/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.experiments;

import org.neo4j.ogm.session.Session;
import uk.ac.standrews.cs.population_linkage.EndtoEnd.runners.BitBlasterSubsetOfDataEndtoEndSiblingBundleLinkageRunner;
import uk.ac.standrews.cs.population_linkage.graph.model.Reference;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbBridge;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthParentsMarriageLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.ParentsMarriageBirthLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_linkage.supportClasses.Sigma;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.text.DecimalFormat;
import java.util.*;

import static uk.ac.standrews.cs.population_linkage.EndtoEnd.runners.Util.getBirthSiblings;

/**
 * This class attempts to perform birth-birth sibling linkage.
 * It creates a Map of families indexed (at the momement TODO) from birth ids to families
 */
public class BirthSiblingBundleThenParentsExperiment {

    private static final double THRESHOLD = 0.0000001;
    private static final double COMBINED_AVERAGE_DISTANCE_THRESHOLD = 0.2;
    public static final int PREFILTER_REQUIRED_FIELDS = 8;

    private static int sibling_references_made = 0;    // nasty hack
    private static int mother_references_made = 0; // nasty hack
    private static int father_references_made = 0; // nasty hack

    public static void main(String[] args) throws Exception {

        BitBlasterSearchStructure bb = null;        // initialised in try block - decl is here so we can finally shut it.
        NeoDbBridge bridge = new NeoDbBridge();
        Session session = bridge.getSession();

        try {
            String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
            String resultsRepo = args[1]; // e.g. synth_results

            LinkageRecipe bb_recipe = new BirthSiblingLinkageRecipe(sourceRepo, resultsRepo, BirthSiblingLinkageRecipe.LINKAGE_TYPE + "-links");

            final BitBlasterSubsetOfDataEndtoEndSiblingBundleLinkageRunner runner1 = new BitBlasterSubsetOfDataEndtoEndSiblingBundleLinkageRunner();
            LinkageResult lr = runner1.run(bb_recipe, new JensenShannon(2048), 0.67, true, PREFILTER_REQUIRED_FIELDS, true, false, false, false);

            HashMap<Long, List<Link>> families = runner1.getFamilyBundles(); // from LXP Id to Links.

            /**
             *  Map<String, Collection<Link>> al_lynx = lr.getMapOfLinks();  // from ORIGINAL_ID to Links - not currently built.
             *
             * This is pretty much the same as the families map above - could fold in and make the runners return maps
             * TODO I am not happy about this filtering thing and how the runners actually get the source records - don't know what to do.
             * I would also like more composibility of these runners - it is not clear that this is possible (at least at the moment)
             * but all the use of BB below is a bit messy.
             * Would be nice to abstract out more code.
             * The recipies have makeLinksPersistent which could be used to link in neo persistence stuff (with more parameters).
             *
             **/

            LinkageRecipe parents_recipe = new ParentsMarriageBirthLinkageRecipe(sourceRepo, resultsRepo, BirthParentsMarriageLinkageRecipe.LINKAGE_TYPE + "-links");

            LinkageConfig.numberOfROs = 20;

            Iterable<LXP> marriage_records = parents_recipe.getStoredRecords();

            StringMetric baseMetric = new JensenShannon(2048);

            Metric<LXP> composite_metric = getCompositeMetric(parents_recipe, baseMetric);

            bb = new BitBlasterSearchStructure(composite_metric, marriage_records);

            int[] number_of_marriages_per_family = new int[20]; // 20 is far too big!

            Set<Long> birth_keys = families.keySet();
            for (long key : birth_keys) {

                List<Link> siblings = families.get(key);
                Set<LXP> sib_records = getBirthSiblings(siblings);

                List<SiblingParentsMarriage> sibling_parents_marriages = new ArrayList<>();

                for (LXP sibling : sib_records) {
                    LXP search_record = parents_recipe.convertToOtherRecordType(sibling);

                    List<DataDistance<LXP>> distances = bb.findWithinThreshold(search_record, 0.67);
                    sibling_parents_marriages.add(new SiblingParentsMarriage(sibling, distances));
                }

                int count = countDifferentMarriagesInGrouping(sibling_parents_marriages);
                if (count > 1) {
                    adjustMarriagesInGrouping(sibling_parents_marriages);
                }

                addChildParentsMarriageToNeo4J(session, siblings, sib_records, sibling_parents_marriages);


                number_of_marriages_per_family[count]++;
            }

            System.out.println("Sibling references made = " + sibling_references_made);
            System.out.println("Mother references made = " + mother_references_made);
            System.out.println("Father references made = " + father_references_made);

            bb.terminate(); // shut down the metric search threads
            bridge.close();

            int sum = 0;
            for (int i = 1; i < number_of_marriages_per_family.length; i++) {
                final int no_of_marriages = number_of_marriages_per_family[i];
                if (no_of_marriages != 0) {
                    System.out.println("Number of marriages per family for size " + i + " = " + no_of_marriages);
                    sum = sum + no_of_marriages;
                }
            }
            System.out.println("Total number of families = " + sum);
        } finally {
            if( bb != null ) { bb.terminate(); } // shut down the metric search threads
            if( bridge != null ) { bridge.close(); }
        }
    }

    /**
     * Adds a 'family' to Neo4J; this involves:
     *      adding a link from each child to the siblings
     *      adding a link from each child to all the parents marriage records
     * @param session - the neo4J session currently open
     * @param siblings - the links containing the birth records for all babies in this family.
     * @param sib_records
     * @param sibling_parents_marriages - a record containing 1 sibling the Marriage records of that sibling.
     * @throws Exception
     */
    private static void addChildParentsMarriageToNeo4J(Session session, List<Link> siblings, Set<LXP> sib_records, List<SiblingParentsMarriage> sibling_parents_marriages) throws Exception {

        List<LXP> processed_siblings = new ArrayList<>();

        for (SiblingParentsMarriage spm : sibling_parents_marriages) {

            final LXP sibling = spm.sibling;
            processed_siblings.add(sibling);

            String provenance = getThisClassName(); // TODO This is not really enough but will do for now, need tied to git version and some narrative - JSON perhaps?

            for (DataDistance<LXP> distance : spm.parents_marriages) {
                final LXP marriage = distance.value;
                double dist = distance.distance;
                String id = marriage.getString(Marriage.STANDARDISED_ID);

                mother_references_made += Reference.createMotherReference(session, sibling.getString( Birth.STANDARDISED_ID ), marriage.getString( Marriage.STANDARDISED_ID ), provenance, PREFILTER_REQUIRED_FIELDS,  dist);
                father_references_made += Reference.createFatherReference(session, sibling.getString( Birth.STANDARDISED_ID ), marriage.getString( Marriage.STANDARDISED_ID ), provenance, PREFILTER_REQUIRED_FIELDS,  dist);
            }

            for( LXP other_sibling : sib_records ) {
                if( ! processed_siblings.contains( other_sibling ) ) { // if we haven't already processed the other sibling
                    // add a sibling link.
                    // TODO this is one way - does that work ok in neo?
                    // TODO what happens in neo OMG with direction of references?
                    sibling_references_made += Reference.createSiblingReference( session, sibling.getString( Birth.STANDARDISED_ID ), other_sibling.getString( Birth.STANDARDISED_ID ), provenance, PREFILTER_REQUIRED_FIELDS, getDistance(sibling,other_sibling,siblings)  );

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

    private static int countDifferentMarriagesInGrouping(List<SiblingParentsMarriage> sibling_parents_marriages) {

        Map<LXP, Integer> counts = new HashMap<>();
        for (SiblingParentsMarriage spm : sibling_parents_marriages) {
            for (DataDistance<LXP> distance : spm.parents_marriages) {
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

    private static List<SiblingParentsMarriage> adjustMarriagesInGrouping(List <SiblingParentsMarriage> sibling_parents_marriages) throws BucketException {

        TreeMap<String, Integer> marriage_counts = new TreeMap<>();     // map from the marriages to number of occurances.

        int sibling_index = 0;

        // build a map from marriage id to number of occurences of that marriage in the set.

        for (SiblingParentsMarriage spm : sibling_parents_marriages) {

            for (DataDistance<LXP> distance : spm.parents_marriages) {
                final LXP marriage = distance.value;
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
     * TODO this is policy!
     * @param all_agree_ids
     * @param sibling_parents_marriages
     * @return a list of sibling marriages that are the best and close to zero
     */
    private static List<SiblingParentsMarriage> findLowestCombinedIfExistsOrAll(List<String> all_agree_ids, List<SiblingParentsMarriage> sibling_parents_marriages) {

        // try and find the lowest combined distance in the family.
        double lowest = Double.MAX_VALUE;
        SiblingParentsMarriage best_so_far = null;

        for (SiblingParentsMarriage spm : sibling_parents_marriages) {
            LXP sibling = spm.sibling;
            double total = 0.0;
            for (DataDistance<LXP> distance : spm.parents_marriages) {
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
            List<SiblingParentsMarriage> result = new ArrayList<>();
            result.add(best_so_far);
            return result;

        } else {
            System.out.println("Could not find a clear winner for set of parents ##### - average distance = " + average_distance); // TODO now what???????????
            return sibling_parents_marriages;
        }
    }

    /**
     * Filters out all of the marriages from the various lists except from the one one which they all agree
     * @param standard_agreed_id
     * @param sibling_parents_marriages
     * @return
     */
    private static List<SiblingParentsMarriage> replaceAllInExcept(String standard_agreed_id, List<SiblingParentsMarriage> sibling_parents_marriages) {
        List<SiblingParentsMarriage> result = new ArrayList<>();
        for( SiblingParentsMarriage spm : sibling_parents_marriages ) {
            LXP sibling = spm.sibling;
            for( DataDistance<LXP> distance : spm.parents_marriages ) {
                final LXP marriage = distance.value;
                String id = marriage.getString(Marriage.STANDARDISED_ID);
                if(standard_agreed_id.equals(id)) {
                    List<DataDistance<LXP>> new_list = new ArrayList<>();
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
        List<DataDistance<LXP>> distances = new ArrayList<>();          // all the distances from all the siblings - siblings can have multiple parents

        System.out.println("Family unit:");
        System.out.println("Number of children in bundle: " + sibling_parents_marriages.size());

        int sibling_index = 0;

        for (SiblingParentsMarriage spm : sibling_parents_marriages) {

            System.out.println( sibling_index++ + " has " + spm.parents_marriages.size() + " parents' marriages");

            for (DataDistance<LXP> distance : spm.parents_marriages) {
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
        for (DataDistance<LXP> distance : distances) {
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

    private static boolean closeTo(double distance, double target) {
        return Math.abs(target - distance) < THRESHOLD;
    }

    protected static Metric<LXP> getCompositeMetric(final LinkageRecipe linkageRecipe, StringMetric baseMetric) {
        return new Sigma(baseMetric, linkageRecipe.getLinkageFields(), 0);
    }
}

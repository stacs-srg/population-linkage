/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.builders;

import uk.ac.standrews.cs.neoStorr.impl.DynamicLXP;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.endToEnd.runners.BitBlasterSubsetOfDataEndtoEndSiblingBundleLinkageRunner;
import uk.ac.standrews.cs.population_linkage.endToEnd.subsetRecipes.BirthSiblingSubsetLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.graph.model.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathBirthIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_linkage.supportClasses.Sigma;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.text.DecimalFormat;
import java.util.*;

import static uk.ac.standrews.cs.population_linkage.endToEnd.util.Util.getBirthSiblings;
import static uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathBirthIdentityLinkageRecipe.trueMatch;

/**
 * This class attempts to perform birth-birth sibling linkage.
 * It creates a Map of families indexed (at the moment) from birth ids to families
 */
public class BirthSiblingBundleThenBirthDeathBuilder {

    private static final double THRESHOLD = 0.0000001;
    private static final double COMBINED_AVERAGE_DISTANCE_THRESHOLD = 0.2;
    public static final int PREFILTER_REQUIRED_FIELDS = 8;
    public static final double DISTANCE_THRESHOLD = 0.45;

    public static void main(String[] args) throws Exception {

        BitBlasterSearchStructure bb = null;        // initialised in try block - decl is here so we can finally shut it.

        try(NeoDbCypherBridge bridge = new NeoDbCypherBridge(); ) {
            String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
            String resultsRepo = args[1]; // e.g. synth_results

            LinkageRecipe bb_recipe = new BirthSiblingSubsetLinkageRecipe(sourceRepo, resultsRepo, bridge, BirthSiblingBundleThenBirthDeathBuilder.class.getCanonicalName());

            final BitBlasterSubsetOfDataEndtoEndSiblingBundleLinkageRunner runner1 = new BitBlasterSubsetOfDataEndtoEndSiblingBundleLinkageRunner();
            LinkageResult lr = runner1.run(bb_recipe, new JensenShannon(2048), false, false, false, false);

            HashMap<Long, List<Link>> families = runner1.getFamilyBundles(); // from LXP Id to Links.

            LinkageRecipe death_birth_recipe = new DeathBirthIdentityLinkageRecipe(sourceRepo, resultsRepo, BirthSiblingBundleThenBirthDeathBuilder.class.getCanonicalName());

            LinkageConfig.numberOfROs = 20;

            Iterable<LXP> death_records = death_birth_recipe.getStoredRecords();  // TODO We have no requirement on number of fields here - we should have!

            StringMetric baseMetric = new JensenShannon(2048);

            Metric<LXP> composite_metric = getCompositeMetric(death_birth_recipe, baseMetric);

            bb = new BitBlasterSearchStructure(composite_metric, death_records);

            int tp = 0;
            int fp = 0;
            int unknown = 0;

            List<String> seen_already = new ArrayList<>(); // keys of the sibling-marriage pairs we have already seen.
            // TODO This is inefficient but safe - consider doing something else?

            Set<Long> birth_keys = families.keySet();
            for (long key : birth_keys) {

                List<Link> siblings = families.get(key);
                Set<LXP> sib_births = getBirthSiblings(siblings);

                System.out.println( siblings.size() + " siblings in family" );

                List<SiblingDeath> sibling_deaths = new ArrayList<>();

                for (LXP sibling : sib_births) {
                    LXP search_deathrecord = convert(death_birth_recipe, sibling);  // converts birth to death

                    List<DataDistance<LXP>> distances = bb.findWithinThreshold(search_deathrecord, DISTANCE_THRESHOLD); // Finds deaths record based on ID on each sibling
                    System.out.println("Seaching for birth, child_id:" + sibling.getString(Birth.CHILD_IDENTITY) + " B: " + sibling.getString(Birth.FORENAME) + " " + sibling.getString(Birth.SURNAME) + " M: " +
                            sibling.getString(Birth.MOTHER_FORENAME) + " " + sibling.getString(Birth.MOTHER_MAIDEN_SURNAME) + " F: " +
                            sibling.getString(Birth.FATHER_FORENAME) + " " + sibling.getString(Birth.FATHER_SURNAME));
                    System.out.println("  Found " + distances.size() + " matching deaths found: ");
                    distances = restrictDeaths( distances ); // there should be only 1 - returning 3 is conservative
                    System.out.println("  Found " + distances.size() + " matching deaths after restricting deaths found: ");

                    for (DataDistance<LXP> d : distances) {
                        String dist = String.format("At distance = %.2f", d.distance);
                        LXP deceased = d.value;
                        System.out.println(dist + " found deceased_id: " + deceased.getString(Death.DECEASED_IDENTITY) + " D: " +
                                deceased.getString(Death.FORENAME) + " " + deceased.getString(Death.SURNAME) + " M: " +
                                deceased.getString(Death.MOTHER_FORENAME) + " " + deceased.getString(Death.MOTHER_MAIDEN_SURNAME) + " F: " +
                                deceased.getString(Death.FATHER_FORENAME) + " " + deceased.getString(Death.FATHER_SURNAME));
                    }
                    sibling_deaths.add(new SiblingDeath(sibling, distances));
                }

                // We expect the siblings in this group to have the same set of parents on death records - so try and get agreement.

                int count = countDifferentMarriagesInGrouping(sibling_deaths);
                System.out.println( "Got " + count + " different marriages in bundle" );
                if (count > 1) {
                    adjustMarriagesInGrouping(sibling_deaths);
                }

                for (SiblingDeath sb : sibling_deaths) {

                    LXP birth = sb.sibling_birth_record;

                    for (DataDistance<LXP> dd_death : sb.deaths) {

                        LXP death = dd_death.value;
                        final LinkStatus linkStatus = trueMatch(death,birth);

                        switch (linkStatus) {
                            case TRUE_MATCH:
                                tp++;
                                break;
                            case NOT_TRUE_MATCH:
                                fp++;
                                break;
                            default:
                                unknown++;
                        }
                    }
                }

               addBirthDeathToNeo4J(bridge, sibling_deaths,seen_already);

            }

            bb.terminate(); // shut down the metric search threads
            bridge.close();

            System.out.println( "Final Birth-Death Quality" );
            System.out.println( "Num TP  links = " + tp );
            System.out.println( "Num FP  links = " + fp ); // fields to do full linkage quality are buried in other runner.
            System.out.println( "Num unknown links = " + unknown );
            int total = tp + fp + unknown;
            System.out.println( "Total = " + total );

        } finally {
            if( bb != null ) { bb.terminate(); } // shut down the metric search threads
            System.out.println( "Run complete" );
            System.exit(0);
        }
    }

    /**
     * if there is are exact matches use only them
     * If there are more than three take top 3.
     * @param distances - a list of data distances
     * @return that list with only exact matches or top 3 preserved.
     */
    private static List<DataDistance<LXP>> restrictDeaths(List<DataDistance<LXP>> distances) {
        List<DataDistance<LXP>> results = new ArrayList<>();
        boolean found_zero = false;

        for( DataDistance<LXP> dd : distances ) {
            if (found_zero) { // already found a distance of zero
                if (closeTo(dd.distance, 0.0)) {
                    results = new ArrayList<>(); // get rid of any we have already found
                    results.add(dd);
                }
            } else {
                if (closeTo(dd.distance, 0.0)) {
                    results = new ArrayList<>(); // get rid of any we have already found
                    results.add(dd);
                    found_zero = true;
                } else { // add this distance if one of the three closest.
                    if (results.size() < 3) {
                        results.add(dd);
                    } else {
                        double highest = highestIn(results);
                        if (dd.distance < highest) {
                            removeDistanceWith(highest,results);
                            results.add(dd);
                        }
                    }
                }
            }
        }
        return results;
    }

    private static void removeDistanceWith(double some_val, List<DataDistance<LXP>> list) {
        for( int i = 0; i < list.size(); i++ ) {
            if( list.get(i).distance == some_val) {
                list.remove( i );
                return;
            }
        }
    }

    private static double highestIn(List<DataDistance<LXP>> list) {
        double highest = Double.MIN_VALUE;
        for( DataDistance<LXP> dd : list ) {
            highest = Double.max(highest, dd.distance);
        }
        return highest;
    }



    /**
     * performs conversion from birth to death and is tolerant of DynamicLXPs which are created during linkage.
     * @param death_birth_recipe - the recipe being used
     * @param birth - a record to convert
     * @return a death record
     */
    private static LXP convert(LinkageRecipe death_birth_recipe, LXP birth) {
        if( birth instanceof Birth ) {
            return death_birth_recipe.convertToOtherRecordType(birth);
        } else if( birth instanceof DynamicLXP) {
            uk.ac.standrews.cs.neoStorr.impl.LXP result = new Death();
            for (int i = 0; i < death_birth_recipe.getLinkageFields().size(); i++) {
                result.put(death_birth_recipe.getLinkageFields().get(i), birth.get(death_birth_recipe.getQueryMappingFields().get(i)));
            }
            return result;
        } else {
            throw new RuntimeException( "convert encountered an unexpected LXP type." );
        }
    }

    /**
     * Adds a 'family' to Neo4J; this involves:
     *      adding a link from each child to the siblings
     *      adding a link from each child to all the parents marriage records
     * @param bridge - a neo4J bridge
     * @param sibling_deaths - a record containing the sibling Death records of that sibling.
     * @throws Exception
     */
    private static void addBirthDeathToNeo4J(NeoDbCypherBridge bridge, List<SiblingDeath> sibling_deaths, List<String> seen_already) {

        String provenance = getThisClassName(); // TODO This is not really enough but will do for now, need tied to git version and some narrative - JSON perhaps?

        for (SiblingDeath sd : sibling_deaths) {

            LXP birth = sd.sibling_birth_record;

            for( DataDistance<LXP> dd_death : sd.deaths ) { // TODO could get closest and filter out far ones?

                LXP death = dd_death.value;
                double dist = dd_death.distance;
                String sibling_std_id = birth.getString( Birth.STANDARDISED_ID );
                String death_std_id = death.getString( Marriage.STANDARDISED_ID );

                String pair_key = sibling_std_id+death_std_id;
                if( ! seen_already.contains(pair_key) ) {
                    Query.createBDReference(bridge, birth.getString(Birth.STANDARDISED_ID), death.getString(Death.STANDARDISED_ID), provenance, PREFILTER_REQUIRED_FIELDS, dist);
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

    private static int countDifferentMarriagesInGrouping(List<SiblingDeath> sibling_deaths) {

        Map<String, Integer> counts = new HashMap<>();
        for (SiblingDeath sd : sibling_deaths) {
            for (DataDistance<LXP> distance : sd.deaths) {
                LXP death = distance.value;

                // Now create a key of parental information.

                String key = createParentsKeyFromBirth( death );

                if (counts.containsKey(key)) {
                    counts.put(key, counts.get(key) + 1);
                } else {
                    counts.put(key, 1);
                }
            }
        }

        return counts.keySet().size();
    }

    private static String createParentsKeyFromBirth(LXP death) {
        StringBuilder sb = new StringBuilder();
        sb.append( death.getString(Death.FATHER_FORENAME ) );
        sb.append( "-" );
        sb.append( death.getString(Death.FATHER_SURNAME ) );
        sb.append( "-" );
        sb.append( death.getString(Death.MOTHER_FORENAME ) );
        sb.append( "-" );
        sb.append( death.getString(Death.MOTHER_SURNAME ) );
//        sb.append( "-" );
//        sb.append( death.getString(Death.FATHER_OCCUPATION ) );
        return sb.toString();
    }

    private static List<SiblingDeath> adjustMarriagesInGrouping(List<SiblingDeath> sibling_deaths) throws BucketException {

        TreeMap<String, Integer> marriage_counts = new TreeMap<>();     // map from the marriages to number of occurances.

        int sibling_index = 0;

        // build a map from marriage id to number of occurences of that marriage in the set.

        for (SiblingDeath sd : sibling_deaths) {  // TODO fix this - this code is repeated from countDifferentMarriagesInGrouping above.

            for (DataDistance<LXP> distance : sd.deaths) {
                final LXP death = distance.value;

                String id = createParentsKeyFromBirth( death );
                if (marriage_counts.keySet().contains(id)) {
                    int count = marriage_counts.get(id);
                    marriage_counts.put(id, count + 1);
                } else {
                    marriage_counts.put(id, 1);
                }
            }
        }

        // if there is one set of parents they all agree, then on use that.

        int num_siblings = sibling_deaths.size();
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

                System.out.println( "All agree on parents" );
                return replaceAllInExcept( standard_agreed_id,sibling_deaths );

            } else {

                return findLowestCombinedIfExistsOrAll( all_agree_ids,sibling_deaths );
                // TODO Need more code here not sure what!!! - the above method will return multiples if there is no agreement - maybe this is OK.
            }

        } else {
            System.out.println( "There are not any parents on which the siblings agree" );
            // TODO Need more code here not sure what!!! - the above method will return multiples if there is no agreement - maybe this is OK.
        }

        //TODO if we get here there is no one list of siblings that share a single set of parents - WHAT TO DO?? - MAYBE OK.
        return sibling_deaths;
    }

    /**
     * Tries to find a list of sibling marriages on which the siblings all agree, and whose combined distance is close to zero.
     * TODO this is policy!
     * @param all_agree_ids
     * @param sibling_deaths
     * @return a list of sibling marriages that are the best and close to zero
     */
    private static List<SiblingDeath> findLowestCombinedIfExistsOrAll(List<String> all_agree_ids, List<SiblingDeath> sibling_deaths) {

        // try and find the lowest combined distance in the family.
        double lowest = Double.MAX_VALUE;
        SiblingDeath best_so_far = null;

        for (SiblingDeath spm : sibling_deaths) {
            double total = 0.0;
            for (DataDistance<LXP> distance : spm.deaths) {
                total += distance.distance;
            }
            if (total < lowest) {
                lowest = total;
                best_so_far = spm;
            }
        }

        // See if there is a winner!

        double average_distance = lowest / best_so_far.deaths.size();

        if (  average_distance < COMBINED_AVERAGE_DISTANCE_THRESHOLD ) {

            System.out.println("Found clear winner for set of parents !!!! - average distance = " + average_distance);
            List<SiblingDeath> result = new ArrayList<>();
            result.add(best_so_far);
            return result;

        } else {
            System.out.println("Could not find a clear winner for set of parents ##### - average distance = " + average_distance); // TODO now what???????????
            return sibling_deaths;
        }
    }

    /**
     * Filters out all of the marriages from the various lists except from the one one which they all agree
     * @param standard_agreed_id
     * @param sibling_deaths
     * @return
     */
    private static List<SiblingDeath> replaceAllInExcept(String standard_agreed_id, List<SiblingDeath> sibling_deaths) {
        List<SiblingDeath> result = new ArrayList<>();
        for( SiblingDeath sd : sibling_deaths ) {
            LXP sibling = sd.sibling_birth_record;
            for( DataDistance<LXP> distance : sd.deaths ) {
                final LXP marriage = distance.value;
                String id = marriage.getString(Marriage.STANDARDISED_ID);
                if(standard_agreed_id.equals(id)) {
                    List<DataDistance<LXP>> new_list = new ArrayList<>();
                    new_list.add( new DataDistance<>(marriage,distance.distance) );
                    SiblingDeath new_entry = new SiblingDeath( sibling,new_list );
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

    private static boolean closeTo(double distance, double target) {
        return Math.abs(target - distance) < THRESHOLD;
    }

    protected static Metric<LXP> getCompositeMetric(final LinkageRecipe linkageRecipe, StringMetric baseMetric) {
        return new Sigma(baseMetric, linkageRecipe.getLinkageFields(), 0);
    }
}

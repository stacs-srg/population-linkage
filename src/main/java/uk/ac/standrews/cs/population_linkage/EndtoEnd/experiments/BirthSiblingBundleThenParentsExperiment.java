/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.experiments;

import uk.ac.standrews.cs.population_linkage.EndtoEnd.Siblings;
import uk.ac.standrews.cs.population_linkage.EndtoEnd.runners.BitBlasterSubsetOfDataEndtoEndSiblingBundleLinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthParentsMarriageLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.ParentsMarriageBirthLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.Sigma;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.text.DecimalFormat;
import java.util.*;

/**
 * This class attempts to perform birth-birth sibling linkage.
 * It creates a Map of families indexed (at the momement TODO) from birth ids to families
 */
public class BirthSiblingBundleThenParentsExperiment {

    private static final double THRESHOLD = 0.0000001;

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe bb_recipe = new BirthSiblingLinkageRecipe(sourceRepo, resultsRepo, BirthSiblingLinkageRecipe.LINKAGE_TYPE + "-links");

        final BitBlasterSubsetOfDataEndtoEndSiblingBundleLinkageRunner runner1 = new BitBlasterSubsetOfDataEndtoEndSiblingBundleLinkageRunner();
        runner1.run(bb_recipe, new JensenShannon(2048), 0.67, true, 8, false, false, false, false);
        HashMap<Long, Siblings> families = runner1.getFamilyBundles();


        //------

        LinkageRecipe bparents_recipe = new ParentsMarriageBirthLinkageRecipe(sourceRepo, resultsRepo, BirthParentsMarriageLinkageRecipe.LINKAGE_TYPE + "-links");

        LinkageConfig.numberOfROs = 20;

        Iterable<LXP> marriage_records = bparents_recipe.getStoredRecords();

        StringMetric baseMetric = new JensenShannon(2048);

        Metric<LXP> composite_metric = getCompositeMetric(bparents_recipe, baseMetric);

        BitBlasterSearchStructure bb = new BitBlasterSearchStructure(composite_metric, marriage_records );

        int[] number_of_marriages_per_family = new int[20]; // 20 is far too big!

        Set<Long> birth_keys = families.keySet();
        for( long key : birth_keys ) {

            Siblings siblings = families.get(key);
            Set<LXP> sib_records = siblings.getBirthSiblings();

            List<SiblingParentsMarriage> sibling_parents_marriages = new ArrayList<>();

            for( LXP sibling : sib_records ) {
                LXP search_record = bparents_recipe.convertToOtherRecordType(sibling);

                List<DataDistance<LXP>> distances = bb.findWithinThreshold(search_record, 0.67);
                sibling_parents_marriages.add( new SiblingParentsMarriage(search_record,distances) );
            }
            int count = countDifferentMarriagesInGrouping( sibling_parents_marriages );
            if( count > 1 ) {
                showDifferentMarriagesInGrouping( sibling_parents_marriages );
            }
            number_of_marriages_per_family[count]++;
        }

        int sum = 0;
        for( int i = 1; i < number_of_marriages_per_family.length; i++ ) {
            final int no_of_marriages = number_of_marriages_per_family[i];
            if( no_of_marriages != 0 ) {
                System.out.println( "Number of marriages per family for size " + i + " = " + no_of_marriages);
                sum = sum + no_of_marriages;
            }
        }
        System.out.println( "Total number of families = " + sum );
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


    private static void showDifferentMarriagesInGrouping( List <SiblingParentsMarriage> sibling_parents_marriages) throws BucketException {

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

class SiblingParentsMarriage {
    public LXP sibling;
    public List<DataDistance<LXP>> parents_marriages;

    public SiblingParentsMarriage(LXP sibling, List<DataDistance<LXP>> parents_marriages) {
        this.sibling = sibling;
        this.parents_marriages = parents_marriages;

    }
}

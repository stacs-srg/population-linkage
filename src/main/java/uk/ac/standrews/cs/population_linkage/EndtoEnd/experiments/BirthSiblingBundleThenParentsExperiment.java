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
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This class attempts to perform birth-birth sibling linkage.
 * It creates a Map of families indexed (at the momement TODO) from birth ids to families
 */
public class BirthSiblingBundleThenParentsExperiment {

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

        Set<Long> birth_keys = families.keySet();
        for( long key : birth_keys ) {

            Siblings siblings = families.get(key);
            Set<LXP> sib_records = siblings.getBirthSiblings();
            for( LXP sibling : sib_records ) {
                LXP search_record = bparents_recipe.convertToOtherRecordType(sibling);

                List<DataDistance<LXP>> distances = bb.findWithinThreshold(search_record, 0.67);
                System.out.println( "Found: " + distances.size() + " records for: " );
                DisplayMethods.showLXPBirth(sibling);
                System.out.println( "Marriage: " );
                for( DataDistance<LXP> dd : distances ) {
                    LXP marriage = dd.value;
                    DisplayMethods.showMarriage(marriage);
                }
            }
        }

    }

    protected static Metric<LXP> getCompositeMetric(final LinkageRecipe linkageRecipe, StringMetric baseMetric) {
        return new Sigma(baseMetric, linkageRecipe.getLinkageFields(), 0);
    }
}

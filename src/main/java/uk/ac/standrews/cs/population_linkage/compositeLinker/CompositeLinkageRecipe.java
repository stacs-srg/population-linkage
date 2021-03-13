/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.compositeLinker;

import java.nio.file.Paths;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.*;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathBrideOwnMarriageIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathGroomOwnMarriageIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.FatherGroomIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.Storr;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.EvaluationApproach;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.StandardEvaluationApproach;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.util.*;

public class CompositeLinkageRecipe {

    public static void main(String[] args) throws BucketException, PersistentObjectException {

        StringMetric metric = new JensenShannon(4096);

        String links_persistent_name = "";
        String results_repository_name = "";

        String source_repository_name = "synthetic-scotland_13k_1_corrupted_A";

        reversed(metric, links_persistent_name, results_repository_name, source_repository_name);

//        TreeMap<Double, LinkageQuality> thresholdResults = new BirthDeathIdentityLinkageRunner().evaluateThresholds(source_repository_name, metric, true, 5, 0.0, 0.1, 1.0);

//        print(thresholdResults);

    }

    private static void print(TreeMap<Double, LinkageQuality> thresholdResults) {

        for (Map.Entry<Double, LinkageQuality> thresholdResult : thresholdResults.entrySet()) {
            System.out.println(thresholdResult.getKey() + "," + thresholdResult.getValue().toCSV());
        }
    }

    private static LinkageQuality runIndirectBirthFatherLinkage(StringMetric metric, String links_persistent_name, String results_repository_name, String source_repository_name) throws BucketException, PersistentObjectException {
        LinkageConfig.birthCacheSize = 15000;
        LinkageConfig.marriageCacheSize = 15000;
        LinkageConfig.deathCacheSize = 15000;
        LinkageConfig.numberOfROs = 70;
        LinkageConfig.GT_COUNTS_FILE =  Paths.get("local-runs/gt-link-counts-new.csv");

        Storr storr = new Storr(source_repository_name, links_persistent_name, results_repository_name);
        BirthGroomIdentityLinkageRecipe a = new BirthGroomIdentityLinkageRecipe(storr);
        ReversedLinkageRecipe b = new ReversedLinkageRecipe(new FatherGroomIdentityLinkageRecipe(storr));

        BirthFatherIdentityLinkageRecipe gt = new BirthFatherIdentityLinkageRecipe(storr);
        EvaluationApproach eval = new StandardEvaluationApproach(gt);

        SinglePathIndirectLinkageRecipe recipe = new SinglePathIndirectLinkageRecipe(a, b, "father-birth-via-groom-id");
        recipe.runRecipe1(metric, 0.2, 6, true, true, false);
        recipe.runRecipe2(metric, 0.75, 5, true, true, false);

        return recipe.evaluateIndirectLinkage(eval);

    }

    private static LinkageQuality reversed(StringMetric metric, String links_persistent_name, String results_repository_name, String source_repository_name) throws BucketException, PersistentObjectException {
        Storr storr = new Storr(source_repository_name, links_persistent_name, results_repository_name);
        LinkageRecipe a = new ReversedLinkageRecipe(new BirthGroomIdentityLinkageRecipe(storr));
        LinkageRecipe b = new FatherGroomIdentityLinkageRecipe(storr);
        LinkageRecipe gt = new ReversedLinkageRecipe(new BirthFatherIdentityLinkageRecipe(storr));
        LinkageConfig.GT_COUNTS_FILE =  Paths.get("local-runs/gt-link-counts-new.csv");

        SinglePathIndirectLinkageRecipe recipe = new SinglePathIndirectLinkageRecipe(b, a, "father-birth-via-groom-id");
        recipe.runRecipe1(metric, 0.75, 5, true, true, false);
        recipe.runRecipe2(metric, 0.2, 6, true, true, false);

        return recipe.evaluateIndirectLinkage(gt.getEvaluationsApproaches().get(EvaluationApproach.Type.ALL));
    }

    private static LinkageQuality multipath(StringMetric metric, String links_persistent_name, String results_repository_name, String source_repository_name) throws BucketException, PersistentObjectException {
        Storr storr = new Storr(source_repository_name, links_persistent_name, results_repository_name);
        LinkageRecipe a1 = new DeathGroomOwnMarriageIdentityLinkageRecipe(storr);
        LinkageRecipe a2 = new ReversedLinkageRecipe(new BirthGroomIdentityLinkageRecipe(storr));
        SinglePathIndirectLinkageRecipe recipeA = new SinglePathIndirectLinkageRecipe(a1, a2, "death-birth-via-groom-id");

        LinkageRecipe b1 = new DeathBrideOwnMarriageIdentityLinkageRecipe(storr);
        LinkageRecipe b2 = new ReversedLinkageRecipe(new BirthBrideIdentityLinkageRecipe(storr));
        SinglePathIndirectLinkageRecipe recipeB = new SinglePathIndirectLinkageRecipe(b1, b2, "death-birth-via-bride-id");

        LinkageRecipe gt = new ReversedLinkageRecipe(new BirthDeathIdentityLinkageRecipe(storr));

        MultiPathIndirectLinkageRecipe recipe = new MultiPathIndirectLinkageRecipe(recipeA, recipeB);
        recipe.getRecipeA().runRecipe1(metric, 0.67, 5, true, true, false);
        recipe.getRecipeA().runRecipe2(metric, 0.67, 3, true, true, false);
        recipe.getRecipeB().runRecipe1(metric, 0.67, 3, true, true, false);
        recipe.getRecipeB().runRecipe2(metric, 0.67, 5, true, true, false);

        return recipe.evaluateIndirectLinkage(gt.getEvaluationsApproaches().get(EvaluationApproach.Type.ALL));
    }


}

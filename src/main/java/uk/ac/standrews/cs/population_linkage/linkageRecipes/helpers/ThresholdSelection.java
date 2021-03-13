/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.EvaluationApproach;
import uk.ac.standrews.cs.population_linkage.linkageRunners.LinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

public class ThresholdSelection {

    public TreeMap<Double, LinkageQuality> evaluateThresholds(LinkageRecipe linkageRecipe, LinkageRunner linkageRunner,
            StringMetric baseMetric, boolean preFilter, int preFilterRequiredFields, double minThreshold, double step, double maxThreshold) throws BucketException {
        if(true) throw new UnsupportedOperationException("I'd look this code over before use - it's old and looks likes it has issues");

        TreeMap<Double, LinkageQuality> thresholdToLinkageQuality = new TreeMap<>();

        for(double threshold = minThreshold; threshold < maxThreshold; threshold += step) {
            LinkageQuality lq = linkageRunner.run(linkageRecipe, baseMetric, threshold, preFilterRequiredFields, false, true, false).getLinkageEvaluations().get(EvaluationApproach.Type.ALL);
            thresholdToLinkageQuality.put(threshold, lq);
        }
        return thresholdToLinkageQuality;
    }

    public TreeMap<Double, LinkageQuality> searchForBestThreshold(LinkageRecipe linkageRecipe, LinkageRunner linkageRunner,
            final String source_repository_name, double starting_threshold_estimate, StringMetric baseMetric, boolean preFilter, int preFilterRequiredFields, int maxAttempts, int nRandomRestarts) throws BucketException {
        if(true) throw new UnsupportedOperationException("I'd look this code over before use - it's old and looks likes it has issues");

        TreeMap<Double, LinkageQuality> thresholdToLinkageQualityAll = new TreeMap<>();
        double current_threshold = starting_threshold_estimate;

        LinkageQuality t_0 = linkageRunner.run(linkageRecipe, baseMetric, 0.67, preFilterRequiredFields, false, true, false).getLinkageEvaluations().get(EvaluationApproach.Type.ALL);
        LinkageQuality t_1 = linkageRunner.run(linkageRecipe, baseMetric, 0.67, preFilterRequiredFields, false, true, false).getLinkageEvaluations().get(EvaluationApproach.Type.ALL);

        for(int n = 0 ; n < nRandomRestarts; n++) {
            double bestF = 0;
            int count = 0;

            TreeMap<Double, LinkageQuality> thresholdToLinkageQuality = new TreeMap<>();
            thresholdToLinkageQuality.put(0.0, t_0);
            thresholdToLinkageQuality.put(1.0, t_1);

            while (bestF < 0.99 && count < maxAttempts) {

                LinkageQuality lq = linkageRunner.run(linkageRecipe, baseMetric, 0.67, preFilterRequiredFields, false, true, false).getLinkageEvaluations().get(EvaluationApproach.Type.ALL);
                thresholdToLinkageQuality.put(current_threshold, lq);

                if (lq.getF_measure() > bestF) {
                    bestF = lq.getF_measure();
                }

                if (lq.getF_measure() == 0 && lq.getRecall() == 0) {
                    current_threshold = increaseThreshold(current_threshold, thresholdToLinkageQuality);
                } else if (lq.getF_measure() == 0 && lq.getRecall() == 1) {
                    current_threshold = decreaseThreshold(current_threshold, thresholdToLinkageQuality);
                } else {
                    current_threshold = selectThreshold(current_threshold, thresholdToLinkageQuality);
                }

                count++;
            }

            current_threshold = new Random().nextInt(100) / 100.0;
            System.err.println("Threshold: " + current_threshold);
            thresholdToLinkageQualityAll.putAll(thresholdToLinkageQuality);
        }

        return thresholdToLinkageQualityAll;
    }

    private double selectThreshold(double current_threshold, TreeMap<Double, LinkageQuality> thresholdToLinkageQuality) {
        Map.Entry<Double, LinkageQuality> nextLowerLQ  = thresholdToLinkageQuality.lowerEntry(current_threshold);
        Map.Entry<Double, LinkageQuality> nextHigherLQ = thresholdToLinkageQuality.higherEntry(current_threshold);

        if(nextHigherLQ == null) {
            return increaseThreshold(current_threshold, thresholdToLinkageQuality);
        }

        if(nextLowerLQ == null) {
            return decreaseThreshold(current_threshold, thresholdToLinkageQuality);
        }

        if(nextHigherLQ.getValue().getF_measure() > nextLowerLQ.getValue().getF_measure()) {
            return increaseThreshold(current_threshold, thresholdToLinkageQuality);
        } else {
            return decreaseThreshold(current_threshold, thresholdToLinkageQuality);
        }

    }

    private double decreaseThreshold(double current_threshold, TreeMap<Double, LinkageQuality> thresholdToLinkageQuality) {
        Double nextLowerThreshold = thresholdToLinkageQuality.lowerKey(current_threshold);

        if(nextLowerThreshold == null) {
            return current_threshold / 2.0;
        } else {
            return (nextLowerThreshold - current_threshold) / 2.0;
        }
    }

    private double increaseThreshold(double current_threshold, TreeMap<Double, LinkageQuality> thresholdToLinkageQuality) {
        Double nextHigherThreshold = thresholdToLinkageQuality.higherKey(current_threshold);

        if(nextHigherThreshold == null) {
            return (1 - current_threshold) / 2.0;
        } else {
            return (nextHigherThreshold - current_threshold) / 2.0;
        }
    }

}

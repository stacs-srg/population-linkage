/*
 * ************************************************************************
 *
 * Copyright 2021 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 * ************************************************************************
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.job;

import java.util.Map;
import uk.ac.standrews.cs.population_linkage.compositeLinker.SinglePathIndirectLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.EvaluationApproach;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

public class IndirectLinkageRecipeHelper {
    public static Map<EvaluationApproach.Type, LinkageQuality> getRecipeResults(String linkagePhase, SinglePathIndirectLinkageRecipe compositeLinkageRecipe) {
        switch (linkagePhase) {
            case "1":
                return compositeLinkageRecipe.getRecipe1EvaluationResults();
            case "2":
                return compositeLinkageRecipe.getRecipe2EvaluationResults();
            default:
                throw new UnsupportedOperationException("Linkage phase get recipe command not defined");
        }
    }

    public static LinkageRecipe getRecipe(String linkagePhase, SinglePathIndirectLinkageRecipe compositeLinkageRecipe) {
        switch (linkagePhase) {
            case "1":
                return compositeLinkageRecipe.getRecipe1();
            case "2":
                return compositeLinkageRecipe.getRecipe2();
            default:
                throw new UnsupportedOperationException("Linkage phase get recipe command not defined");
        }
    }

    public static void runRecipe(String linkagePhase, SinglePathIndirectLinkageRecipe compositeLinkageRecipe, StringMetric chosenMetric, double threshold, int preFilterRequiredFields, boolean reversed, boolean evaluateQuality, boolean persistLinks) throws BucketException {
        switch (linkagePhase) {
            case "1":
                compositeLinkageRecipe.runRecipe1(chosenMetric, threshold, preFilterRequiredFields, reversed, evaluateQuality, persistLinks);
                break;
            case "2":
                compositeLinkageRecipe.runRecipe2(chosenMetric, threshold, preFilterRequiredFields, reversed, evaluateQuality, persistLinks);
                break;
            default:
                throw new UnsupportedOperationException("Linkage phase run recipe command not defined");
        }
    }
}

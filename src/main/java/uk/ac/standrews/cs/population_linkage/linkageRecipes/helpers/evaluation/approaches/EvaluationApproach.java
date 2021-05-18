package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches;/*
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

import java.util.Map;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;

public interface EvaluationApproach {

    enum Type {
        ALL, // would have been better named STANDARD. I'll rename this once I've run my thesis experiments
        PARENTAL_STATUS
    }

    Type getEvaluationDescription();
    Map<String, LinkageQuality> calculateLinkageQuality();
    void evaluateLink(Link proposedLink);

    LinkageRecipe getLinkageRecipe();

}

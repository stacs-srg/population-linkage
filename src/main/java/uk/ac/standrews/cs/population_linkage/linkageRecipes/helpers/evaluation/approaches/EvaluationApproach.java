/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches;

import java.util.Map;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;

public interface EvaluationApproach {

    enum Type {
        ALL, // would have been better named STANDARD. I'll rename this once I've run my thesis experiments
        PARENTAL_STATUS,
        EMPLOYMENT_DATA
    }

    Type getEvaluationDescription();
    Map<String, LinkageQuality> calculateLinkageQuality();
    void evaluateLink(Link proposedLink);

    LinkageRecipe getLinkageRecipe();

}

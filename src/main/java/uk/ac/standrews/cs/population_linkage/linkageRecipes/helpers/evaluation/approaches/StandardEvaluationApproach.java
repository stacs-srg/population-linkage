/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;

public class StandardEvaluationApproach extends EvaluationApproach {

    public StandardEvaluationApproach(LinkageRecipe linkageRecipe) {
        super(linkageRecipe);
    }

    @Override
    public EvaluationApproach.Type getEvaluationDescription() {
        return Type.ALL;
    }

}

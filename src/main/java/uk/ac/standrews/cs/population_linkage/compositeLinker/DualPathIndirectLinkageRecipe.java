/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.compositeLinker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

public class DualPathIndirectLinkageRecipe extends IndirectLinkageRecipe {

    private final SinglePathIndirectLinkageRecipe recipeA;
    private final SinglePathIndirectLinkageRecipe recipeB;
    private Map<String, Collection<DoubleLink>> combinedPotentialLinks = null;

    public DualPathIndirectLinkageRecipe(SinglePathIndirectLinkageRecipe recipeA, SinglePathIndirectLinkageRecipe recipeB) {
        this.recipeA = recipeA;
        this.recipeB = recipeB;
    }

    public SinglePathIndirectLinkageRecipe getRecipeA() {
        return recipeA;
    }

    public SinglePathIndirectLinkageRecipe getRecipeB() {
        return recipeB;
    }

    @Override
    public Map<String, Collection<DoubleLink>> getPotentialLinks() throws BucketException {
        if(combinedPotentialLinks == null) {
            combinedPotentialLinks = new HashMap<>();
            combinedPotentialLinks.putAll(recipeA.getPotentialLinks());
            combinedPotentialLinks.putAll(recipeB.getPotentialLinks());
        }
        return combinedPotentialLinks;
    }
}

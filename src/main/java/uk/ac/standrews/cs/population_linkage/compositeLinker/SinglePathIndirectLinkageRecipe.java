/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.compositeLinker;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

public class SinglePathIndirectLinkageRecipe extends IndirectLinkageRecipe {

    private final LinkageRecipe recipe1;
    private final LinkageRecipe recipe2;
    private final String linkType; // a textual description of the indirect link approach - e.g. "death-birth-via-groom-id"

    private LinkageResult recipe1Result = null;
    private LinkageResult recipe2Result = null;

    private Map<String, Collection<DoubleLink>> potentialLinks = null;

    public SinglePathIndirectLinkageRecipe(LinkageRecipe recipe1, LinkageRecipe recipe2, String linkType) {
        this.recipe1 = recipe1;
        this.recipe2 = recipe2;
        this.linkType = linkType;
    }

    public void runRecipe1(StringMetric baseMetric, double threshold,
            int prefilterRequiredFields, boolean generateMapOfLinks, boolean evaluateQuality,
            boolean persistLinks) throws BucketException {
         recipe1Result = new BitBlasterLinkageRunner().run(recipe1,
                 baseMetric, threshold, prefilterRequiredFields, generateMapOfLinks,
                 evaluateQuality, persistLinks);
    }

    public void runRecipe2(StringMetric baseMetric, double threshold,
            int prefilterRequiredFields, boolean generateMapOfLinks, boolean evaluateQuality,
            boolean persistLinks) throws BucketException {
        recipe2Result = new BitBlasterLinkageRunner().run(recipe2,
                baseMetric, threshold, prefilterRequiredFields, generateMapOfLinks,
                evaluateQuality, persistLinks);
    }

    public LinkageQuality evaluateRecipe1() {
        return recipe1Result.getLinkageEvaluations().get("ALL");
    }

    public LinkageQuality evaluateRecipe2() {
        return recipe2Result.getLinkageEvaluations().get("ALL");
    }

    public Map<String, Collection<DoubleLink>> getPotentialLinks() throws BucketException {
        if(recipe1Result == null || recipe2Result == null) {
            throw new InvalidUsageException("Both linkage recipes must be ran prior calculating potential links");
        }

        if(potentialLinks == null) {
            potentialLinks = combineLinks(recipe1Result.getMapOfLinks(), recipe2Result.getMapOfLinks(), linkType);
        }
        return potentialLinks;
    }
}

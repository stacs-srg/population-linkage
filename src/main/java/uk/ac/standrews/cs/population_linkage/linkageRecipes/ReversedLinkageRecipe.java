/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import java.util.List;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.StandardEvaluationApproach;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;

public class ReversedLinkageRecipe extends LinkageRecipe {

    private final LinkageRecipe originalLinkageRecipe;

    public ReversedLinkageRecipe(LinkageRecipe originalLinkageRecipe) {
        this.originalLinkageRecipe = originalLinkageRecipe;
        this.storr = originalLinkageRecipe.getStorr();
        addEvaluationsApproach(new StandardEvaluationApproach(this));
    }

    @Override
    public String getLinkageType() {
        return "reversed-" + originalLinkageRecipe.getLinkageType();
    }

    @Override
    public boolean isSiblingLinkage() {
        return originalLinkageRecipe.isSiblingLinkage();
    }

    @Override
    public Class<? extends LXP> getStoredType() {
        return originalLinkageRecipe.getSearchType();
    }

    @Override
    public Class<? extends LXP> getSearchType() {
        return originalLinkageRecipe.getStoredType();
    }

    @Override
    public String getStoredRole() {
        return originalLinkageRecipe.getSearchRole();
    }

    @Override
    public String getSearchRole() {
        return originalLinkageRecipe.getStoredRole();
    }

    @Override
    public List<Integer> getLinkageFields() {
        return originalLinkageRecipe.getSearchMappingFields();
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return originalLinkageRecipe.isViableLink(new RecordPair(proposedLink.record2, proposedLink.record1, proposedLink.distance));
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return originalLinkageRecipe.getLinkageFields();
    }

    @Override
    public List<List<Pair>> getTrueMatchMappings() {
        return originalLinkageRecipe.getTrueMatchMappings();
    }

    public String getOriginalLinkageClassCanonicalName() {
        return originalLinkageRecipe.getClass().getCanonicalName();
    }
}

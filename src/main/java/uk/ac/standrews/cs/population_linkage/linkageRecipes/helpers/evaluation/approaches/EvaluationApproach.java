/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.helpers.GroundTruthLinkCounterFileHandler;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.ReversedLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.Utils;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.GroundTruthLinkCounter;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

public abstract class EvaluationApproach {

    public enum Type {
        ALL
    }

    private long tp = 0;
    private long fp = 0;

    private final LinkageRecipe linkageRecipe;
    private final boolean reversed;

    public abstract Type getEvaluationDescription();

    public EvaluationApproach(LinkageRecipe linkageRecipe) {
        this.linkageRecipe = linkageRecipe;
        this.reversed = linkageRecipe instanceof ReversedLinkageRecipe;
    }

    public long getNumberOfGroundTruthTrueLinks() {
        if(reversed) {
            return GroundTruthLinkCounter.countGroundTruthLinksStandard(linkageRecipe.getTrueMatchMappings(),
                    linkageRecipe.getSearchRecords(), linkageRecipe.getStoredRecords(),
                    linkageRecipe.isLinkageSymmetric(), linkageRecipe.isSiblingLinkage());
        } else {
            return GroundTruthLinkCounter.countGroundTruthLinksStandard(linkageRecipe.getTrueMatchMappings(),
                    linkageRecipe.getStoredRecords(), linkageRecipe.getSearchRecords(),
                    linkageRecipe.isLinkageSymmetric(), linkageRecipe.isSiblingLinkage());
        }

    }

    public long getNumberOfGroundTruthTrueLinksPostFilter() {
        if(reversed) {
            return GroundTruthLinkCounter.countGroundTruthLinksStandard(linkageRecipe.getTrueMatchMappings(),
                    linkageRecipe.getPreFilteredSearchRecords(), linkageRecipe.getPreFilteredStoredRecords(),
                    linkageRecipe.isLinkageSymmetric(), linkageRecipe.isSiblingLinkage());
        } else {
            return GroundTruthLinkCounter.countGroundTruthLinksStandard(linkageRecipe.getTrueMatchMappings(),
                    linkageRecipe.getPreFilteredStoredRecords(), linkageRecipe.getPreFilteredSearchRecords(),
                    linkageRecipe.isLinkageSymmetric(), linkageRecipe.isSiblingLinkage());
        }
    }

    public void evaluateLink(Link proposedLink) {
        switch (doesGTSayIsTrue(proposedLink)) {
            case TRUE_MATCH:
                tp++;
                break;
            case NOT_TRUE_MATCH:
                fp++;
                break;
            case UNKNOWN:
            case EXCLUDED:
                break;
        }
    }

    public LinkageQuality calculateLinkageQuality() {
        long calcTp = tp;
        long calcFp = fp;

        if(linkageRecipe.isLinkageSymmetric()) {
            // if the linkageRecipe is a dataset to itself (i.e birth-birth) we should not be rewarded or penalised
            // for making the link in both direction - thus divide by two
            calcTp = tp/2;
            calcFp = fp/2;
        }

        long numberOfGroundTruthTrueLinks = optimisedGetNumberOfGroundTruthTrueLinks();
        long fn = numberOfGroundTruthTrueLinks - calcTp;

        long missedLinks = numberOfGroundTruthTrueLinks - getNumberOfGroundTruthTrueLinksPostFilter();

        LinkageQuality lq = new LinkageQuality(calcTp, calcFp, fn);
        lq.setLinksLostOnPrefilter(missedLinks);
        lq.print(System.out);
        return lq;
    }

    private long optimisedGetNumberOfGroundTruthTrueLinks() {
        return new GroundTruthLinkCounterFileHandler(LinkageConfig.GT_COUNTS_FILE).getOrCalcCount(this);
    }

    public boolean isSymmetric() {
        return linkageRecipe.isLinkageSymmetric();
    }

    public String getDataSetName() {
        return linkageRecipe.getStorr().getSourceRepositoryName();
    }

    public String getLinkageRecipeClassName() {
        return Utils.getLinkageClassName(linkageRecipe);
    }

    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return Evaluation.trueMatch(record1, record2, linkageRecipe.getTrueMatchMappings(), linkageRecipe.getExcludedMatchMappings(), reversed);
    }

    private LinkStatus doesGTSayIsTrue(Link linkage_says_true_link) {
        try {
            return isTrueMatch(
                    linkage_says_true_link.getRecord1().getReferend(),
                    linkage_says_true_link.getRecord2().getReferend());
        } catch (BucketException e) {
            throw new RuntimeException("Bucket exception from accessing referend - bucket no longer contains expected records", e);
        }
    }
}

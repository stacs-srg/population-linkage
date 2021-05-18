/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.helpers.GroundTruthLinkCounterFileHandler;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.ReversedLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.GroundTruthLinkCounter;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

public class StandardEvaluationApproach implements EvaluationApproach {

    private long tp = 0;
    private long fp = 0;
    private Function<Iterable<LXP>, Iterable<LXP>> storedRecordsFilter = (records) -> records;
    private Function<Iterable<LXP>, Iterable<LXP>> searchRecordsFilter = (records) -> records;
    private boolean customFilters = false;

    protected final LinkageRecipe linkageRecipe;

    private final boolean reversed;

    public StandardEvaluationApproach(LinkageRecipe linkageRecipe) {
        this.linkageRecipe = linkageRecipe;
        this.reversed = linkageRecipe instanceof ReversedLinkageRecipe;
    }

    @Override
    public EvaluationApproach.Type getEvaluationDescription() {
        return Type.ALL;
    }

    public Map<String, LinkageQuality> calculateLinkageQuality() {
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

        return mapOf("all", lq);
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

    public Function<Iterable<LXP>, Iterable<LXP>> getStoredRecordsFilter() {
        return storedRecordsFilter;
    }

    public Function<Iterable<LXP>, Iterable<LXP>> getSearchRecordsFilter() {
        return searchRecordsFilter;
    }

    public void setStoredRecordsFilter(Function<Iterable<LXP>, Iterable<LXP>> storedRecordsFilter) {
        customFilters = true;
        this.storedRecordsFilter = storedRecordsFilter;
    }

    public void setSearchRecordsFilter(Function<Iterable<LXP>, Iterable<LXP>> searchRecordsFilter) {
        customFilters = true;
        this.searchRecordsFilter = searchRecordsFilter;
    }

    public long getNumberOfGroundTruthTrueLinks() {
        if(reversed) {
            return GroundTruthLinkCounter.countGroundTruthLinksStandard(linkageRecipe.getTrueMatchMappings(),
                    getSearchRecordsFilter().apply(linkageRecipe.getSearchRecords()),
                    getStoredRecordsFilter().apply(linkageRecipe.getStoredRecords()),
                    linkageRecipe.isLinkageSymmetric(), linkageRecipe.isSiblingLinkage());
        } else {
            return GroundTruthLinkCounter.countGroundTruthLinksStandard(linkageRecipe.getTrueMatchMappings(),
                    getStoredRecordsFilter().apply(linkageRecipe.getStoredRecords()),
                    getSearchRecordsFilter().apply(linkageRecipe.getSearchRecords()),
                    linkageRecipe.isLinkageSymmetric(), linkageRecipe.isSiblingLinkage());
        }

    }

    public long getNumberOfGroundTruthTrueLinksPostFilter() {
        if(reversed) {
            return GroundTruthLinkCounter.countGroundTruthLinksStandard(linkageRecipe.getTrueMatchMappings(),
                    getSearchRecordsFilter().apply(linkageRecipe.getPreFilteredSearchRecords()),
                    getStoredRecordsFilter().apply(linkageRecipe.getPreFilteredStoredRecords()),
                    linkageRecipe.isLinkageSymmetric(), linkageRecipe.isSiblingLinkage());
        } else {
            return GroundTruthLinkCounter.countGroundTruthLinksStandard(linkageRecipe.getTrueMatchMappings(),
                    getStoredRecordsFilter().apply(linkageRecipe.getPreFilteredStoredRecords()),
                    getSearchRecordsFilter().apply(linkageRecipe.getPreFilteredSearchRecords()),
                    linkageRecipe.isLinkageSymmetric(), linkageRecipe.isSiblingLinkage());
        }
    }

    private Map<String, LinkageQuality> mapOf(String evaluationGroup, LinkageQuality result) {
        Map<String, LinkageQuality> map = new HashMap<>();
        map.put(evaluationGroup, result);
        return map;
    }

    // Method should really be private but I've built the Indirect Linkage Evaluation Code with a dependancy on this - really
    // the indirect code should use the evaluation processor code (i.e. this package) to drive its evaluation and not
    // have much code of its own for such
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return Evaluation.trueMatch(record1, record2, linkageRecipe.getTrueMatchMappings(), linkageRecipe.getExcludedMatchMappings(), reversed);
    }

    protected LinkStatus doesGTSayIsTrue(Link linkage_says_true_link) {
        try {
            return isTrueMatch(
                    linkage_says_true_link.getRecord1().getReferend(),
                    linkage_says_true_link.getRecord2().getReferend());
        } catch (BucketException e) {
            throw new RuntimeException("Bucket exception from accessing referend - bucket no longer contains expected records", e);
        }
    }

    public String getDataSetName() {
        return linkageRecipe.getStorr().getSourceRepositoryName();
    }

    public String getLinkageRecipeClassName() {
        return linkageRecipe.getLinkageClassCanonicalName();
    }

    public LinkageRecipe getLinkageRecipe() {
        return linkageRecipe;
    }

    protected long optimisedGetNumberOfGroundTruthTrueLinks() {
        if(customFilters) {
            return getNumberOfGroundTruthTrueLinks();
        }
        return new GroundTruthLinkCounterFileHandler(LinkageConfig.GT_COUNTS_FILE).getOrCalcCount(this);
    }
}

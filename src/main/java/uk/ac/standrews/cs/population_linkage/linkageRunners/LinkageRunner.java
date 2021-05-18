/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRunners;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.Utils;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.EvaluationApproach;
import uk.ac.standrews.cs.population_linkage.helpers.memorylogger.MemoryLogger;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.linkers.SimilaritySearchLinker;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.*;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.time.LocalDateTime;
import java.util.*;

public abstract class LinkageRunner {

    private static final int DEFAULT_NUMBER_OF_PROGRESS_UPDATES = 100;
    private StringMetric baseMetric;
    private Linker linker;
    private LinkageRecipe linkageRecipe;

    abstract SearchStructureFactory<LXP> getSearchFactory(final Metric<LXP> composite_metric);

    public LinkageResult run(LinkageRecipe linkageRecipe, StringMetric baseMetric, double threshold,
            int prefilterRequiredFields, boolean generateMapOfLinks,
            boolean evaluateQuality, boolean persistLinks) throws BucketException {
        MemoryLogger.update();
        this.baseMetric = baseMetric;
        this.linkageRecipe = linkageRecipe;

        linker = getLinker(threshold, this.linkageRecipe);

        Utils.setCacheSizes(linkageRecipe.getStorr().getRecordRepository());
        MemoryLogger.update();

        LinkageResult result = link(persistLinks, evaluateQuality, prefilterRequiredFields, generateMapOfLinks);

        linker.terminate();
        return result;
    }

    public LinkageResult link(boolean persist_links, boolean evaluate_quality, int prefilterRequiredFields, boolean generateMapOfLinks) throws BucketException {

        System.out.println("Adding records into linker @ " + LocalDateTime.now().toString());

        if( prefilterRequiredFields >= 0 ) {
            linkageRecipe.setPreFilteringRequiredPopulatedLinkageFields(prefilterRequiredFields);
            linker.addRecords(linkageRecipe.getPreFilteredStoredRecords(), linkageRecipe.getPreFilteredSearchRecords());
        } else {
            linker.addRecords(linkageRecipe.getStoredRecords(), linkageRecipe.getSearchRecords());
        }

        MemoryLogger.update();
        System.out.println("Constructing link iterable @ " + LocalDateTime.now().toString());

        Iterable<Link> links = linker.getLinks();
        LocalDateTime time_stamp = LocalDateTime.now();

        MemoryLogger.update();

        Map<String, Collection<Link>> linksByRecordID = new HashMap<>(); // this is for the map of links if requested

        System.out.println("Entering persist and evaluate loop @ " + LocalDateTime.now().toString());

        try {
            for (Link linkage_says_true_link : links) {

                if (persist_links) linkageRecipe.getStorr().makeLinkPersistent(linkage_says_true_link);

                if (generateMapOfLinks) {
                    String originalID = Utilities.originalId(linkage_says_true_link.getRecord1().getReferend());
                    linksByRecordID.computeIfAbsent(originalID, k -> new LinkedList<>());
                    linksByRecordID.get(originalID).add(linkage_says_true_link);
                }
                if (evaluate_quality) {
                    linkageRecipe.getEvaluationsApproaches().values()
                            .forEach(evaluationApproach -> evaluationApproach.evaluateLink(linkage_says_true_link));
                }
            }
        } catch(NoSuchElementException ignore) {}

        System.out.println("Exiting persist and evaluate loop @ " + LocalDateTime.now().toString());

        MemoryLogger.update();
        Utils.nextTimeStamp(time_stamp, "perform and evaluate linkageRecipe");

        Map<String, LinkageQuality> lq = new HashMap<>();

        for(EvaluationApproach evaluationApproach : linkageRecipe.getEvaluationsApproaches().values()) {
            evaluationApproach.calculateLinkageQuality().forEach((evaluationGroup, result) -> {
                lq.put(linkageRecipe.getLinkageType() + "." + evaluationApproach.getEvaluationDescription() + "." + evaluationGroup, result);
            });
        }

        if(generateMapOfLinks) {
            return new LinkageResult(lq, linksByRecordID);
        } else {
            return new LinkageResult(lq);
        }
    }

    private Linker getLinker(final double match_threshold, LinkageRecipe linkageRecipe) {
        Metric<LXP> compositeMetric = getCompositeMetric(linkageRecipe);
        return new SimilaritySearchLinker(getSearchFactory(compositeMetric), compositeMetric, match_threshold, DEFAULT_NUMBER_OF_PROGRESS_UPDATES,
                linkageRecipe.getLinkageType(), "threshold match at " + match_threshold, linkageRecipe.getStoredRole(), linkageRecipe.getSearchRole(), linkageRecipe::isViableLink, linkageRecipe);
    }

    private Metric<LXP> getCompositeMetric(final LinkageRecipe linkageRecipe) {
        return new Sigma(baseMetric, linkageRecipe.getLinkageFields(), 0);
    }
}

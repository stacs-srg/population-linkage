/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRunners;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.population_linkage.helpers.MemoryLogger;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.linkers.SimilaritySearchLinker;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.*;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.time.LocalDateTime;
import java.util.*;

import static uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering.filter;

public class BitBlasterLinkageRunner extends LinkageRunner{

    @Override
    public LinkageRecipe getLinkageRecipe(String links_persistent_name, String source_repository_name, String results_repository_name, RecordRepository record_repository) {
        return linkageRecipe;
    }

    public Linker getLinker(LinkageRecipe linkageRecipe, List<LXP> reference_points ) {
        Metric<LXP> compositeMetric = getCompositeMetric(linkageRecipe);
        return new SimilaritySearchLinker(getSearchFactory(compositeMetric,reference_points), compositeMetric, linkageRecipe.getThreshold(), getNumberOfProgressUpdates(),
                linkageRecipe.getLinkageType(), "threshold match at ", linkageRecipe.getStoredRole(), linkageRecipe.getQueryRole(), linkageRecipe::isViableLink, linkageRecipe);
    }

    public SearchStructureFactory<LXP> getSearchFactory(Metric<LXP> composite_metric, List<LXP> reference_points) {
        return new BitBlasterSearchStructureFactory<LXP>(composite_metric, reference_points);
    }

    @Override
    protected List<LXP> getReferencePoints() {
        ArrayList<LXP> candidates = filter(linkageRecipe.getLinkageFields().size(), LinkageRecipe.EVERYTHING, linkageRecipe.getStoredRecords(), linkageRecipe.getLinkageFields()); // all populated records
        List<LXP> result = BitBlasterSearchStructure.chooseRandomReferencePoints(candidates, LinkageConfig.numberOfROs);
        return result;
    }

    public LinkageResult link(boolean persist_links, boolean evaluate_quality, int numberOfGroundTruthTrueLinks, boolean generateMapOfLinks, boolean reverseMap) throws BucketException, RepositoryException {

        System.out.println("Adding records into linker @ " + LocalDateTime.now().toString());

        ((SimilaritySearchLinker)linker).addRecords(linkageRecipe.getStoredRecords(), linkageRecipe.getQueryRecords(),getReferencePoints());

        MemoryLogger.update();
        System.out.println("Constructing link iterable @ " + LocalDateTime.now().toString());

        Iterable<Link> links = linker.getLinks();
        LocalDateTime time_stamp = LocalDateTime.now();

        MemoryLogger.update();
        int tp = 0; // these are counters with which we use if evaluating
        int fp = 0;

        Map<String, Collection<Link>> linksByRecordID = new HashMap<>(); // this is for the map of links if requested

        System.out.println("Entering persist and evaluate loop @ " + LocalDateTime.now().toString());

        // Map<String, Link> groundTruthLinks = linkageRecipe.getGroundTruthLinks();
        // Don't really need this - delete the method - al.
        System.out.println("Al has temporarily removed getGroundTruthLinks() and references to groundTruthLinks from LinkageRunner.link");

        try {
            for (Link linkage_says_true_link : links) {

                // groundTruthLinks.remove(linkageRecipe.toKey(linkage_says_true_link.getRecord1().getReferend(), linkage_says_true_link.getRecord2().getReferend()));
                // Don't really need this - delete the method - al.

                if (persist_links) {
                    linkageRecipe.makeLinkPersistent(linkage_says_true_link);
                }
                if (generateMapOfLinks) {
                    String originalID;
                    if (reverseMap) { // defined if the map should be based on the ID of the stored or the search records
                        originalID = Utilities.originalId(linkage_says_true_link.getRecord2().getReferend());
                    } else {
                        originalID = Utilities.originalId(linkage_says_true_link.getRecord1().getReferend());
                    }
                    linksByRecordID.computeIfAbsent(originalID, k -> new LinkedList<>());
                    linksByRecordID.get(originalID).add(linkage_says_true_link);
                }
                if (evaluate_quality) {
                    if (doesGTSayIsTrue(linkage_says_true_link)) {
                        tp++;
                    } else {
//                        final boolean printFPs = false;
//                        if(printFPs) printLink(linkage_says_true_link, "FP");
                        fp++;
                    }
                }
            }
        } catch (NoSuchElementException ignore) {
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        System.out.println("Exiting persist and evaluate loop @ " + LocalDateTime.now().toString());

        MemoryLogger.update();
        nextTimeStamp(time_stamp, "perform and evaluate linkageRecipe");

        if (evaluate_quality) {
            System.out.println("Evaluating ground truth @ " + LocalDateTime.now().toString());
            numberOfGroundTruthTrueLinks = linkageRecipe.getNumberOfGroundTruthTrueLinks();
            System.out.println("Number of GroundTruth true Links = " + numberOfGroundTruthTrueLinks);
            LinkageQuality lq = getLinkageQuality(evaluate_quality, numberOfGroundTruthTrueLinks, tp, fp);
            lq.print(System.out);
            return new LinkageResult(lq);
        } else {
            return new LinkageResult(null); // TODO What should this return in this case?
        }


    }
}

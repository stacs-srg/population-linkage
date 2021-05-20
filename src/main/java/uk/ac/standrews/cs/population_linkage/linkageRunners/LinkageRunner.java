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
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.*;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus.TRUE_MATCH;

public abstract class LinkageRunner {

    private static final int DEFAULT_NUMBER_OF_PROGRESS_UPDATES = 100;
    private StringMetric baseMetric;
    protected Linker linker;
    protected LinkageRecipe linkageRecipe;

    public LinkageResult run(LinkageRecipe linkageRecipe, StringMetric baseMetric,
                             boolean generateMapOfLinks, boolean reverseMap,
                             boolean evaluateQuality, boolean persistLinks) throws BucketException, RepositoryException {

        MemoryLogger.update();
        this.baseMetric = baseMetric;
        this.linkageRecipe = linkageRecipe;

        linker = getLinker(this.linkageRecipe);

        linkageRecipe.setCacheSizes(LinkageConfig.birthCacheSize,LinkageConfig.deathCacheSize,LinkageConfig.marriageCacheSize);

        int numberOGroundTruthLinks = 0;
        if(evaluateQuality) {
            System.out.println("Evaluating ground truth @ " + LocalDateTime.now().toString());
            numberOGroundTruthLinks = linkageRecipe.getNumberOfGroundTruthTrueLinks();
        }

        MemoryLogger.update();

        LinkageResult result = link(persistLinks, evaluateQuality, numberOGroundTruthLinks, generateMapOfLinks, reverseMap);

        linker.terminate();

        return result;
    }

    public TreeMap<Double, LinkageQuality> evaluateThresholds(String source_repository_name, StringMetric baseMetric, boolean preFilter, int preFilterRequiredFields, double minThreshold, double step, double maxThreshold) throws BucketException, RepositoryException {

        TreeMap<Double, LinkageQuality> thresholdToLinkageQuality = new TreeMap<>();

        for(double threshold = minThreshold; threshold < maxThreshold; threshold += step) {
            LinkageQuality lq = run(linkageRecipe, baseMetric, false, false, true, false).getLinkageQuality();
            thresholdToLinkageQuality.put(threshold, lq);
        }
        return thresholdToLinkageQuality;
    }

    public TreeMap<Double, LinkageQuality> searchForBestThreshold(final String source_repository_name, double starting_threshold_estimate, StringMetric baseMetric, int maxAttempts, int nRandomRestarts) throws BucketException, RepositoryException {

        TreeMap<Double, LinkageQuality> thresholdToLinkageQualityAll = new TreeMap<>();
        double current_threshold = starting_threshold_estimate;

        LinkageQuality t_0 = run(linkageRecipe, baseMetric, false, false, true, false).getLinkageQuality();
        LinkageQuality t_1 = run(linkageRecipe, baseMetric, false, false, true, false).getLinkageQuality();

        for(int n = 0 ; n < nRandomRestarts; n++) {
            double bestF = 0;
            int count = 0;

            TreeMap<Double, LinkageQuality> thresholdToLinkageQuality = new TreeMap<>();
            thresholdToLinkageQuality.put(0.0, t_0);
            thresholdToLinkageQuality.put(1.0, t_1);

            while (bestF < 0.99 && count < maxAttempts) {

                LinkageQuality lq = run(linkageRecipe, baseMetric, false, false, true, false).getLinkageQuality();
                thresholdToLinkageQuality.put(current_threshold, lq);

                if (lq.getF_measure() > bestF) {
                    bestF = lq.getF_measure();
                }

                if (lq.getF_measure() == 0 && lq.getRecall() == 0) {
                    current_threshold = increaseThreshold(current_threshold, thresholdToLinkageQuality);
                } else if (lq.getF_measure() == 0 && lq.getRecall() == 1) {
                    current_threshold = decreaseThreshold(current_threshold, thresholdToLinkageQuality);
                } else {
                    current_threshold = selectThreshold(current_threshold, thresholdToLinkageQuality);
                }

                count++;
            }

            current_threshold = new Random().nextInt(100) / 100.0;
            System.err.println("Threshold: " + current_threshold);
            thresholdToLinkageQualityAll.putAll(thresholdToLinkageQuality);
        }

        return thresholdToLinkageQualityAll;
    }

    private double selectThreshold(double current_threshold, TreeMap<Double, LinkageQuality> thresholdToLinkageQuality) {
        Map.Entry<Double, LinkageQuality> nextLowerLQ  = thresholdToLinkageQuality.lowerEntry(current_threshold);
        Map.Entry<Double, LinkageQuality> nextHigherLQ = thresholdToLinkageQuality.higherEntry(current_threshold);

        if(nextHigherLQ == null) {
            return increaseThreshold(current_threshold, thresholdToLinkageQuality);
        }

        if(nextLowerLQ == null) {
            return decreaseThreshold(current_threshold, thresholdToLinkageQuality);
        }

        if(nextHigherLQ.getValue().getF_measure() > nextLowerLQ.getValue().getF_measure()) {
            return increaseThreshold(current_threshold, thresholdToLinkageQuality);
        } else {
            return decreaseThreshold(current_threshold, thresholdToLinkageQuality);
        }

    }

    private double decreaseThreshold(double current_threshold, TreeMap<Double, LinkageQuality> thresholdToLinkageQuality) {
        Double nextLowerThreshold = thresholdToLinkageQuality.lowerKey(current_threshold);

        if(nextLowerThreshold == null) {
            return current_threshold / 2.0;
        } else {
            return (nextLowerThreshold - current_threshold) / 2.0;
        }
    }

    private double increaseThreshold(double current_threshold, TreeMap<Double, LinkageQuality> thresholdToLinkageQuality) {
        Double nextHigherThreshold = thresholdToLinkageQuality.higherKey(current_threshold);

        if(nextHigherThreshold == null) {
            return (1 - current_threshold) / 2.0;
        } else {
            return (nextHigherThreshold - current_threshold) / 2.0;
        }
    }

    public LinkageResult link(boolean persist_links, boolean evaluate_quality, int numberOfGroundTruthTrueLinks, boolean generateMapOfLinks, boolean reverseMap) throws BucketException, RepositoryException {

        System.out.println("Adding records into linker @ " + LocalDateTime.now().toString());

        linker.addRecords(linkageRecipe.getStoredRecords(), linkageRecipe.getQueryRecords());

        MemoryLogger.update();
        System.out.println("Constructing link iterable @ " + LocalDateTime.now().toString());

        Iterable<Link> links = linker.getLinks();
        LocalDateTime time_stamp = LocalDateTime.now();

        MemoryLogger.update();
        int tp = 0; // these are counters with which we use if evaluating
        int fp = 0;

        Map<String, Collection<Link>> linksByRecordID = new HashMap<>(); // this is for the map of links if requested

        System.out.println("Entering persist and evaluate loop @ " + LocalDateTime.now().toString());

        Map<String, Link> groundTruthLinks = linkageRecipe.getGroundTruthLinks();

        try {
            for (Link linkage_says_true_link : links) {

                groundTruthLinks.remove(linkageRecipe.toKey(linkage_says_true_link.getRecord1().getReferend(), linkage_says_true_link.getRecord2().getReferend()));

                if (persist_links) {
                    linkageRecipe.makeLinkPersistent(linkage_says_true_link);
                }
                if (generateMapOfLinks) {
                    String originalID;
                    if(reverseMap) { // defined if the map should be based on the ID of the stored or the search records
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
                        final boolean printFPs = false;
                        if(printFPs) printLink(linkage_says_true_link, "FP");
                        fp++;
                    }
                }
            }
        } catch(NoSuchElementException ignore) {} catch (RepositoryException e) {
            e.printStackTrace();
        }

        System.out.println("Exiting persist and evaluate loop @ " + LocalDateTime.now().toString());

        MemoryLogger.update();
        nextTimeStamp(time_stamp, "perform and evaluate linkageRecipe");

        LinkageQuality lq = getLinkageQuality(evaluate_quality, numberOfGroundTruthTrueLinks, tp, fp);
        lq.print( System.out );

        final boolean printFNs = false; // this does not appear to work - values yields an empty set!!!
        if(printFNs) {
            for (Link missingLink : groundTruthLinks.values()) {
                printLink(missingLink, "FN");
            }
        }

        if(generateMapOfLinks) {
            return new LinkageResult(lq, linksByRecordID);
        } else {
            return new LinkageResult(lq);
        }
    }

    private LinkageQuality getLinkageQuality(boolean evaluate_quality, int numberOfGroundTruthTrueLinks, int tp, int fp) {
        if(evaluate_quality) {
            if(linkageRecipe.isSymmetric()) {
                // if the linkageRecipe is a dataset to itself (i.e birth-birth) we should not be rewarded or penalised
                // for making the link in both direction - thus divide by two
                tp = tp /2;
                fp = fp /2;
            }
            int fn = numberOfGroundTruthTrueLinks - tp;
            return new LinkageQuality(tp, fp, fn);
        } else {
            return new LinkageQuality("Evaluation not requested");
        }
    }


    protected boolean doesGTSayIsTrue(Link link) {
        try {
            return linkageRecipe.isTrueMatch(
                    link.getRecord1().getReferend(),
                    link.getRecord2().getReferend())
                    .equals(TRUE_MATCH);
        } catch (BucketException e) {
            throw new RuntimeException("Bucket exception from accessing referend - bucket no longer contains expected records (TD)", e);
        } catch (RepositoryException e) {
            throw new RuntimeException("Bucket exception from accessing referend - bucket no longer contains expected records (TD)", e);
        }
    }

    ///////////////////////////// I/O /////////////////////////////

    private static String prettyPrint(Duration duration) {

        return String.format("%sh %sm %ss",
                duration.toHours(),
                duration.toMinutes() - TimeUnit.HOURS.toMinutes(duration.toHours()),
                duration.getSeconds() - TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
    }

    ///////////////////////////// Private methods /////////////////////////////

    protected LocalDateTime nextTimeStamp(final LocalDateTime previous_time_stamp, final String step_description) {

        LocalDateTime next = LocalDateTime.now();
        System.out.println(prettyPrint(Duration.between(previous_time_stamp, next)) + " to " + step_description);
        return next;
    }

    private void showLink(Link calculated_link) {

        try {
            LXP person1 = calculated_link.getRecord1().getReferend();
            LXP person2 = calculated_link.getRecord2().getReferend();

            System.out.println("B1: " + person1.getString(Birth.FORENAME) + " " + person1.getString(Birth.SURNAME) + " // "
                    + "B1F: " + person1.getString(Birth.FATHER_FORENAME) + " " + person1.getString(Birth.FATHER_SURNAME) + " " + person1.getString(Birth.FAMILY) + " -> " +
                    "B2: " + person2.getString(Birth.FORENAME) + " " + person2.getString(Birth.SURNAME) + " // " +
                    "B2F: " + person2.getString(Birth.FATHER_FORENAME) + " " + person2.getString(Birth.FATHER_SURNAME) + " " + person2.getString(Birth.FAMILY));

        } catch (Exception e) {}
    }

    protected void printLink(Link link, String classification) {

        try {
            LXP person1 = link.getRecord1().getReferend();
            LXP person2 = link.getRecord2().getReferend();

            System.out.printf("-%s------------------------------------------------------------------------------------------------------------\n", classification);

            for(int i = 0 ; i < linkageRecipe.getLinkageFields().size(); i++) {
                String r1FieldName = Utilities.getLabels(person1).get(linkageRecipe.getLinkageFields().get(i));
                String r2FieldName = Utilities.getLabels(person2).get(linkageRecipe.getQueryMappingFields().get(i));

                String r1FieldContent = person1.getString(linkageRecipe.getLinkageFields().get(i));
                String r2FieldContent = person2.getString(linkageRecipe.getQueryMappingFields().get(i));

                String isEquals = "≠";
                if(r1FieldContent.equals(r2FieldContent)) isEquals = "=";

                System.out.printf("%30s | %20s |%s| %-20s | %-30s %.2f \n", r1FieldName, r1FieldContent, isEquals, r2FieldContent, r2FieldName, link.getDistance());
            }

            System.out.println("---------------------------------------------------------------------------------------------------------------");

        } catch (Exception ignored) { }
    }

    public Linker getLinker( LinkageRecipe linkageRecipe ) {
        Metric<LXP> compositeMetric = getCompositeMetric(linkageRecipe);
        return new SimilaritySearchLinker(getSearchFactory(compositeMetric), compositeMetric, linkageRecipe.getTheshold(), getNumberOfProgressUpdates(),
                linkageRecipe.getLinkageType(), "threshold match at ", linkageRecipe.getStoredRole(), linkageRecipe.getQueryRole(), linkageRecipe::isViableLink, linkageRecipe);

    }

    public abstract LinkageRecipe getLinkageRecipe(final String links_persistent_name, final String source_repository_name, final String results_repository_name, final RecordRepository record_repository);

    protected Metric<LXP> getCompositeMetric(final LinkageRecipe linkageRecipe) {
        return new Sigma(getBaseMetric(), linkageRecipe.getLinkageFields(), 0);
    }

    abstract SearchStructureFactory<LXP> getSearchFactory(final Metric<LXP> composite_metric);

    protected int getNumberOfProgressUpdates() {
        return DEFAULT_NUMBER_OF_PROGRESS_UPDATES;
    }

    protected StringMetric getBaseMetric() {
        return baseMetric;
    }

    public void setBaseMetric(StringMetric baseMetric) {
        this.baseMetric = baseMetric;
    }
}

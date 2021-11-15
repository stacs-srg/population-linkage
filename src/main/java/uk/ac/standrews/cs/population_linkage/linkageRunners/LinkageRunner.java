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
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.*;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus.TRUE_MATCH;

public abstract class LinkageRunner {

    private static final int DEFAULT_NUMBER_OF_PROGRESS_UPDATES = 100;
    protected StringMetric baseMetric;
    protected Linker linker;
    protected LinkageRecipe linkage_recipe;

    public LinkageResult run(LinkageRecipe linkage_recipe,
                             MakePersistent make_persistent,
                             boolean generateMapOfLinks, boolean reverseMap,
                             boolean evaluateQuality, boolean persistLinks)  throws BucketException, RepositoryException { // TODO AL $$$ throws BucketException, RepositoryException { // throws Exception

        this.linkage_recipe = linkage_recipe;
        MemoryLogger.update();
        this.baseMetric = linkage_recipe.getMetric();
        this.linkage_recipe = linkage_recipe;

        linker = getLinker(linkage_recipe,getReferencePoints());

        linkage_recipe.setCacheSizes(LinkageConfig.birthCacheSize,LinkageConfig.deathCacheSize,LinkageConfig.marriageCacheSize);

        int numberOGroundTruthLinks = 0;
//        if(evaluateQuality) {
//            System.out.println("Evaluating ground truth @ " + LocalDateTime.now().toString());
//            numberOGroundTruthLinks = linkageRecipe.getNumberOfGroundTruthTrueLinks();
//            System.out.println( "Number of GroundTruth true Links = " + numberOGroundTruthLinks );
//        }

        MemoryLogger.update();

        LinkageResult result = link(persistLinks, make_persistent, evaluateQuality, numberOGroundTruthLinks, generateMapOfLinks, reverseMap);

        linker.terminate();

        return result;
    }

    protected abstract List<LXP> getReferencePoints();

//    public TreeMap<Double, LinkageQuality> evaluateThresholds(String source_repository_name, StringMetric baseMetric, boolean preFilter, int preFilterRequiredFields, double minThreshold, double step, double maxThreshold) throws BucketException, RepositoryException {
//
//        TreeMap<Double, LinkageQuality> thresholdToLinkageQuality = new TreeMap<>();
//
//        for(double threshold = minThreshold; threshold < maxThreshold; threshold += step) {
//            LinkageQuality lq = run(linkage_recipe, false, false, true, false).getLinkageQuality();
//            thresholdToLinkageQuality.put(threshold, lq);
//        }
//        return thresholdToLinkageQuality;
//    }
//
//    public TreeMap<Double, LinkageQuality> searchForBestThreshold(final String source_repository_name, double starting_threshold_estimate, StringMetric baseMetric, int maxAttempts, int nRandomRestarts) throws BucketException, RepositoryException {
//
//        TreeMap<Double, LinkageQuality> thresholdToLinkageQualityAll = new TreeMap<>();
//        double current_threshold = starting_threshold_estimate;
//
//        LinkageQuality t_0 = run(linkage_recipe, false, false, true, false).getLinkageQuality();
//        LinkageQuality t_1 = run(linkage_recipe, false, false, true, false).getLinkageQuality();
//
//        for(int n = 0 ; n < nRandomRestarts; n++) {
//            double bestF = 0;
//            int count = 0;
//
//            TreeMap<Double, LinkageQuality> thresholdToLinkageQuality = new TreeMap<>();
//            thresholdToLinkageQuality.put(0.0, t_0);
//            thresholdToLinkageQuality.put(1.0, t_1);
//
//            while (bestF < 0.99 && count < maxAttempts) {
//
//                LinkageQuality lq = run(linkage_recipe, false, false, true, false).getLinkageQuality();
//                thresholdToLinkageQuality.put(current_threshold, lq);
//
//                if (lq.getF_measure() > bestF) {
//                    bestF = lq.getF_measure();
//                }
//
//                if (lq.getF_measure() == 0 && lq.getRecall() == 0) {
//                    current_threshold = increaseThreshold(current_threshold, thresholdToLinkageQuality);
//                } else if (lq.getF_measure() == 0 && lq.getRecall() == 1) {
//                    current_threshold = decreaseThreshold(current_threshold, thresholdToLinkageQuality);
//                } else {
//                    current_threshold = selectThreshold(current_threshold, thresholdToLinkageQuality);
//                }
//
//                count++;
//            }
//
//            current_threshold = new Random().nextInt(100) / 100.0;
//            System.err.println("Threshold: " + current_threshold);
//            thresholdToLinkageQualityAll.putAll(thresholdToLinkageQuality);
//        }
//
//        return thresholdToLinkageQualityAll;
//    }
//
//    private double selectThreshold(double current_threshold, TreeMap<Double, LinkageQuality> thresholdToLinkageQuality) {
//        Map.Entry<Double, LinkageQuality> nextLowerLQ  = thresholdToLinkageQuality.lowerEntry(current_threshold);
//        Map.Entry<Double, LinkageQuality> nextHigherLQ = thresholdToLinkageQuality.higherEntry(current_threshold);
//
//        if(nextHigherLQ == null) {
//            return increaseThreshold(current_threshold, thresholdToLinkageQuality);
//        }
//
//        if(nextLowerLQ == null) {
//            return decreaseThreshold(current_threshold, thresholdToLinkageQuality);
//        }
//
//        if(nextHigherLQ.getValue().getF_measure() > nextLowerLQ.getValue().getF_measure()) {
//            return increaseThreshold(current_threshold, thresholdToLinkageQuality);
//        } else {
//            return decreaseThreshold(current_threshold, thresholdToLinkageQuality);
//        }
//
//    }
//
//    private double decreaseThreshold(double current_threshold, TreeMap<Double, LinkageQuality> thresholdToLinkageQuality) {
//        Double nextLowerThreshold = thresholdToLinkageQuality.lowerKey(current_threshold);
//
//        if(nextLowerThreshold == null) {
//            return current_threshold / 2.0;
//        } else {
//            return (nextLowerThreshold - current_threshold) / 2.0;
//        }
//    }
//
//    private double increaseThreshold(double current_threshold, TreeMap<Double, LinkageQuality> thresholdToLinkageQuality) {
//        Double nextHigherThreshold = thresholdToLinkageQuality.higherKey(current_threshold);
//
//        if(nextHigherThreshold == null) {
//            return (1 - current_threshold) / 2.0;
//        } else {
//            return (nextHigherThreshold - current_threshold) / 2.0;
//        }
//    }

    public abstract LinkageResult link(boolean persist_links, MakePersistent make_persistent, boolean evaluate_quality, int numberOfGroundTruthTrueLinks, boolean generateMapOfLinks, boolean reverseMap) throws BucketException, RepositoryException; // TODO AL $$$ throws BucketException, RepositoryException;

    protected LinkageQuality getLinkageQuality(boolean evaluate_quality, int numberOfGroundTruthTrueLinks, int tp, int fp) {
        if(evaluate_quality) {
            if(linkage_recipe.isSymmetric()) {
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
            return linkage_recipe.isTrueMatch(
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

            for(int i = 0; i < linkage_recipe.getLinkageFields().size(); i++) {
                String r1FieldName = Utilities.getLabels(person1).get(linkage_recipe.getLinkageFields().get(i));
                String r2FieldName = Utilities.getLabels(person2).get(linkage_recipe.getQueryMappingFields().get(i));

                String r1FieldContent = person1.getString(linkage_recipe.getLinkageFields().get(i));
                String r2FieldContent = person2.getString(linkage_recipe.getQueryMappingFields().get(i));

                String isEquals = "≠";
                if(r1FieldContent.equals(r2FieldContent)) isEquals = "=";

                System.out.printf("%30s | %20s |%s| %-20s | %-30s %.2f \n", r1FieldName, r1FieldContent, isEquals, r2FieldContent, r2FieldName, link.getDistance());
            }

            System.out.println("---------------------------------------------------------------------------------------------------------------");

        } catch (Exception ignored) { }
    }

    public abstract Linker getLinker( LinkageRecipe linkageRecipe, List<LXP> reference_points ); // TODO ref points shouldn't be in here might not have any! But doesn't work otherwise!

    public abstract LinkageRecipe getLinkageRecipe(final String links_persistent_name, final String source_repository_name, final String results_repository_name, final RecordRepository record_repository);

    protected Metric<LXP> getCompositeMetric(final LinkageRecipe linkageRecipe) {
        return new Sigma(getBaseMetric(), linkageRecipe.getLinkageFields(), 0);
    }

    abstract SearchStructureFactory<LXP> getSearchFactory(final Metric<LXP> composite_metric, List<LXP> reference_objects);

    protected int getNumberOfProgressUpdates() {
        return DEFAULT_NUMBER_OF_PROGRESS_UPDATES;
    }

    protected StringMetric getBaseMetric() {
        return baseMetric;
    }


}

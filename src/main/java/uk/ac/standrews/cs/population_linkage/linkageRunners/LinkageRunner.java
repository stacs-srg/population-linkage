package uk.ac.standrews.cs.population_linkage.linkageRunners;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.linkageRecipies.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.helpers.GroundTruthLinkCounter;
import uk.ac.standrews.cs.population_linkage.helpers.MemoryLogger;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import static uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus.TRUE_MATCH;

public abstract class LinkageRunner {

    private static final int DEFAULT_NUMBER_OF_PROGRESS_UPDATES = 100;
    private StringMetric baseMetric;
    private Linker linker;
    private LinkageRecipe linkageRecipe;

    public abstract String getLinkageType();

    private boolean printFNs = false;
    private Path gtLinksCountFile = Paths.get("gt-link-counts.csv"); // TODO put this in the application properties?

    public LinkageResult run(final String links_persistent_name, final String source_repository_name,
                              final String results_repository_name, double match_threshold, StringMetric baseMetric,
                              boolean prefilter, boolean persistLinks, boolean evaluateQuality, int prefilterRequiredFields, boolean generateMapOfLinks, boolean reverseMap) throws BucketException {

        this.baseMetric = baseMetric;

        MemoryLogger.update();

        final Path store_path = ApplicationProperties.getStorePath();
        final RecordRepository record_repository = new RecordRepository(store_path, source_repository_name);
        linkageRecipe = getLinkageRecipe(links_persistent_name, source_repository_name, results_repository_name, record_repository);

        linker = getLinker(match_threshold, linkageRecipe);

        setCacheSizes(record_repository);

        int numberOGroundTruthLinks = 0;
        if(evaluateQuality)
            numberOGroundTruthLinks = new GroundTruthLinkCounter(source_repository_name, gtLinksCountFile).count(this);

        MemoryLogger.update();

        LinkageResult lr = link(prefilter, persistLinks, evaluateQuality, numberOGroundTruthLinks, prefilterRequiredFields, generateMapOfLinks, reverseMap);

        record_repository.stopStoreWatcher();
        linker.terminate();

        return lr;

    }

    // This method is used when we are not going to persist the links made - i.e. we will always and only evaluate the linkageRecipe quality
    public LinkageResult run(final String source_repository_name, double match_threshold, StringMetric baseMetric, boolean preFilter, int preFilterRequiredFields, boolean generateMapOfLinks, boolean reverseMap) throws BucketException {

        return run("", source_repository_name, "",
                match_threshold, baseMetric, preFilter, false, true, preFilterRequiredFields, generateMapOfLinks, reverseMap);
    }

    public TreeMap<Double, LinkageQuality> evaluateThresholds(String source_repository_name, StringMetric baseMetric, boolean preFilter, int preFilterRequiredFields, double minThreshold, double step, double maxThreshold) throws BucketException {

        TreeMap<Double, LinkageQuality> thresholdToLinkageQuality = new TreeMap<>();

        for(double threshold = minThreshold; threshold < maxThreshold; threshold += step) {
            LinkageQuality lq = run(source_repository_name, threshold, baseMetric, preFilter, preFilterRequiredFields, false, false).getLinkageQuality();
            thresholdToLinkageQuality.put(threshold, lq);
        }
        return thresholdToLinkageQuality;
    }

    public TreeMap<Double, LinkageQuality> searchForBestThreshold(final String source_repository_name, double starting_threshold_estimate, StringMetric baseMetric, boolean preFilter, int preFilterRequiredFields, int maxAttempts, int nRandomRestarts) throws BucketException {

        TreeMap<Double, LinkageQuality> thresholdToLinkageQualityAll = new TreeMap<>();
        double current_threshold = starting_threshold_estimate;

        LinkageQuality t_0 = run(source_repository_name, 0, baseMetric, preFilter, preFilterRequiredFields, false, false).getLinkageQuality();
        LinkageQuality t_1 = run(source_repository_name, 1, baseMetric, preFilter, preFilterRequiredFields, false, false).getLinkageQuality();

        for(int n = 0 ; n < nRandomRestarts; n++) {
            double bestF = 0;
            int count = 0;

            TreeMap<Double, LinkageQuality> thresholdToLinkageQuality = new TreeMap<>();
            thresholdToLinkageQuality.put(0.0, t_0);
            thresholdToLinkageQuality.put(1.0, t_1);

            while (bestF < 0.99 && count < maxAttempts) {

                LinkageQuality lq = run(source_repository_name, current_threshold, baseMetric, preFilter, preFilterRequiredFields, false, false).getLinkageQuality();
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

    public void setCacheSizes(RecordRepository record_repository) {
        record_repository.setBirthsCacheSize(LinkageConfig.birthCacheSize);
        record_repository.setDeathsCacheSize(LinkageConfig.deathCacheSize);
        record_repository.setMarriagesCacheSize(LinkageConfig.marriageCacheSize);
    }

    public int countNumberOfGroundTruthLinks(final String source_repository_name) {

        final Path store_path = ApplicationProperties.getStorePath();
        final RecordRepository record_repository = new RecordRepository(store_path, source_repository_name);
        final LinkageRecipe linkageRecipe = getLinkageRecipe(null, source_repository_name, null, record_repository);

        int numberOfGroundTruthLinks = linkageRecipe.getNumberOfGroundTruthTrueLinks();
        record_repository.stopStoreWatcher();

        return numberOfGroundTruthLinks;
    }

    public LinkageResult link(boolean pre_filter, boolean persist_links, boolean evaluate_quality, int numberOfGroundTruthTrueLinks, int prefilterRequiredFields, boolean generateMapOfLinks, boolean reverseMap) throws BucketException {

        System.out.println("Adding records into linker @ " + LocalDateTime.now().toString());

        int missedLinks = 0;

        if( pre_filter ) {
            linkageRecipe.setPreFilteringRequiredPopulatedLinkageFields(prefilterRequiredFields);
            linker.addRecords(linkageRecipe.getPreFilteredStoredRecords(), linkageRecipe.getPreFilteredSearchRecords());
            missedLinks = numberOfGroundTruthTrueLinks - linkageRecipe.getNumberOfGroundTruthTrueLinksPostFilter();
        } else {
            linker.addRecords(linkageRecipe.getStoredRecords(), linkageRecipe.getSearchRecords());
        }

        MemoryLogger.update();
        System.out.println("Constructing link iterable @ " + LocalDateTime.now().toString());

        Iterable<Link> links = linker.getLinks();
        LocalDateTime time_stamp = LocalDateTime.now();

        MemoryLogger.update();
        int tp = 0; // these are counters with which we use if evaluating
        int fp = 0;

        Map<String, Collection<Link>> linksByRecordID = new HashMap<>(); // this is for the map of links if requested

        System.out.println("Entering persist and evaluate loop @ " + LocalDateTime.now().toString());

        try {
            for (Link linkage_says_true_link : links) {
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
                        if(printFNs) printLink(linkage_says_true_link);
                        fp++;
                    }
                }
            }
        } catch(NoSuchElementException ignore) {}

        System.out.println("Exiting persist and evaluate loop @ " + LocalDateTime.now().toString());

        MemoryLogger.update();
        nextTimeStamp(time_stamp, "perform and evaluate linkageRecipe");

        System.out.printf("%d links lost due to pre-filtering\n", missedLinks);

        LinkageQuality lq;

        if(evaluate_quality) {

            if(linkageRecipe.isSymmetric()) {
                // if the linkageRecipe is a dataset to itself (i.e birth-birth) we should not be rewarded or penalised
                // for making the link in both direction - thus divide by two
                tp = tp/2;
                fp = fp/2;
            }

            int fn = numberOfGroundTruthTrueLinks - tp;
            lq = new LinkageQuality(tp, fp, fn);
            lq.print(System.out);
        } else {
            lq = new LinkageQuality("Evaluation not requested");
        }

        if(generateMapOfLinks) {
            return new LinkageResult(lq, linksByRecordID);
        } else {
            return new LinkageResult(lq);
        }
    }


    private boolean doesGTSayIsTrue(Link linkage_says_true_link) {
        try {
            return linkageRecipe.isTrueMatch(
                    linkage_says_true_link.getRecord1().getReferend(),
                    linkage_says_true_link.getRecord2().getReferend())
                    .equals(TRUE_MATCH);
        } catch (BucketException e) {
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

    private LocalDateTime nextTimeStamp(final LocalDateTime previous_time_stamp, final String step_description) {

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

    private void printLink(Link link) {

        try {
            LXP person1 = link.getRecord1().getReferend();
            LXP person2 = link.getRecord2().getReferend();

            System.out.println("---------------------------------------------------------------------------------------------------------------");

            for(int i = 0 ; i < linkageRecipe.getLinkageFields().size(); i++) {
                String r1FieldName = Utilities.getLabels(person1).get(linkageRecipe.getLinkageFields().get(i));
                String r2FieldName = Utilities.getLabels(person2).get(linkageRecipe.getSearchMappingFields().get(i));

                String r1FieldContent = person1.getString(linkageRecipe.getLinkageFields().get(i));
                String r2FieldContent = person2.getString(linkageRecipe.getSearchMappingFields().get(i));

                String isEquals = "≠";
                if(r1FieldContent.equals(r2FieldContent)) isEquals = "=";

                System.out.printf("%30s | %20s |%s| %-20s | %-30s \n", r1FieldName, r1FieldContent, isEquals, r2FieldContent, r2FieldName);
            }

            System.out.println("---------------------------------------------------------------------------------------------------------------");

        } catch (Exception ignored) { }

    }


    public abstract Linker getLinker(final double match_threshold, LinkageRecipe linkageRecipe);

    public abstract LinkageRecipe getLinkageRecipe(final String links_persistent_name, final String source_repository_name, final String results_repository_name, final RecordRepository record_repository);

    protected abstract Metric<LXP> getCompositeMetric(final LinkageRecipe linkageRecipe);

    protected abstract SearchStructureFactory<LXP> getSearchFactory(final Metric<LXP> composite_metric);

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

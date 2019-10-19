package uk.ac.standrews.cs.population_linkage.linkageRunners;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.linkageRecipies.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.helpers.GroundTruthLinkCounter;
import uk.ac.standrews.cs.population_linkage.helpers.MemoryLogger;
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

    private Path gtLinksCountFile = Paths.get("gt-link-counts.csv"); // TODO put this in the application properties?

    public LinkageQuality run(final String links_persistent_name, final String source_repository_name,
                              final String results_repository_name, double match_threshold, StringMetric baseMetric,
                              boolean prefilter, boolean persistLinks, boolean evaluateQuality, int prefilterRequiredFields) {

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

        LinkageQuality lq = link(prefilter, persistLinks, evaluateQuality, numberOGroundTruthLinks, prefilterRequiredFields);

        record_repository.stopStoreWatcher();
        linker.terminate();

        return lq;

    }

    // This method is used when we are not going to persist the links made - i.e. we will always and only evaluate the linkageRecipe quality
    public LinkageQuality run(final String source_repository_name, double match_threshold, StringMetric baseMetric, boolean preFilter) {

        return run("", source_repository_name, "",
                match_threshold, baseMetric, preFilter, false, true, 0);
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

    public LinkageQuality link(boolean pre_filter, boolean persist_links, boolean evaluate_quality, int numberOfGroundTruthTrueLinks, int prefilterRequiredFields) {

        System.out.println("Adding records into linker @ " + LocalDateTime.now().toString());

        int missedLinks = 0;

        if( pre_filter ) {
            linkageRecipe.setPreFilteringRequiredPopulatedLinkageFields(prefilterRequiredFields);
            linker.addRecords(linkageRecipe.getPreFilteredSourceRecords1(), linkageRecipe.getPreFilteredSourceRecords2());
            missedLinks = numberOfGroundTruthTrueLinks - linkageRecipe.getNumberOfGroundTruthTrueLinksPostFilter();
        } else {
            linker.addRecords(linkageRecipe.getSourceRecords1(), linkageRecipe.getSourceRecords2());
        }

        MemoryLogger.update();
        System.out.println("Constructing link iterable @ " + LocalDateTime.now().toString());

        Iterable<Link> links = linker.getLinks();
        LocalDateTime time_stamp = LocalDateTime.now();

        MemoryLogger.update();
        int tp = 0; // these are counters with which we use if evaluating
        int fp = 0;

        System.out.println("Entering persist and evaluate loop @ " + LocalDateTime.now().toString());

        try {
            for (Link linkage_says_true_link : links) {
                if (persist_links) {
                    linkageRecipe.makeLinkPersistent(linkage_says_true_link);
                }
                if (evaluate_quality) {
                    if (doesGTSayIsTrue(linkage_says_true_link)) {
                        tp++;
                    } else {
                        fp++;
                    }
                }
            }
        } catch(NoSuchElementException ignore) {}

        System.out.println("Exiting persist and evaluate loop @ " + LocalDateTime.now().toString());

        MemoryLogger.update();
        nextTimeStamp(time_stamp, "perform and evaluate linkageRecipe");

        System.out.printf("%d links lost due to pre-filtering\n", missedLinks);

        if(evaluate_quality) {

            if(linkageRecipe.isSymmetric()) {
                // if the linkageRecipe is a dataset to itself (i.e birth-birth) we should not be rewarded or penalised
                // for making the link in both direction - thus divide by two
                tp = tp/2;
                fp = fp/2;
            }

            int fn = numberOfGroundTruthTrueLinks - tp;
            LinkageQuality lq = new LinkageQuality(tp, fp, fn);
            lq.print(System.out);
            return lq;
        } else
            return new LinkageQuality("Evaluation not requested");
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

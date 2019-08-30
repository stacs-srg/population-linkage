package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.helpers.GroundTruthLinkCounter;
import uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.helpers.MemoryLogger;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class LinkageRunner {

    private static final int DEFAULT_NUMBER_OF_PROGRESS_UPDATES = 100;
    private StringMetric baseMetric;

    private Path gtLinksCountFile = Paths.get("gt-link-counts.csv"); // TODO put this in the application properties?

    public LinkageQuality run(final String links_persistent_name, final String gt_persistent_name, final String source_repository_name,
                    final String results_repository_name, double match_threshold, StringMetric baseMetric,
                    boolean prefilter, boolean persistLinks, boolean evaluateQuality, boolean symmetricLinkage) {

        this.baseMetric = baseMetric;

        MemoryLogger.update();

        final Path store_path = ApplicationProperties.getStorePath();
        final RecordRepository record_repository = new RecordRepository(store_path, source_repository_name);
        final Linkage linkage = getLinkage(links_persistent_name, gt_persistent_name, source_repository_name, results_repository_name, record_repository);
        final Metric<LXP> composite_metric = getCompositeMetric(linkage);
        final SearchStructureFactory<LXP> search_factory = getSearchFactory(composite_metric);
        final Linker linker = getLinker(match_threshold, composite_metric, search_factory);

        setCacheSizes(record_repository);

        int numberOGroundTruthLinks = 0;
        if(evaluateQuality)
            numberOGroundTruthLinks= new GroundTruthLinkCounter(source_repository_name, gtLinksCountFile).count(this);

        MemoryLogger.update();

        LinkageQuality lq = new LinkageFramework(linkage, linker).link(prefilter, persistLinks, evaluateQuality, symmetricLinkage, numberOGroundTruthLinks);

        record_repository.stopStoreWatcher();
        linker.terminate();

        return lq;

    }

    public LinkageQuality evaluateOnly(final String source_repository_name, double match_threshold, StringMetric baseMetric, int numberOfGroundTruthLinks) {

        this.baseMetric = baseMetric;

        MemoryLogger.update();

        final Path store_path = ApplicationProperties.getStorePath();
        final RecordRepository record_repository = new RecordRepository(store_path, source_repository_name);

        setCacheSizes(record_repository);

        final Linkage linkage = getLinkage(null, null, source_repository_name, null, record_repository);
        final Metric<LXP> composite_metric = getCompositeMetric(linkage);
        final SearchStructureFactory<LXP> search_factory = getSearchFactory(composite_metric);
        final Linker linker = getLinker(match_threshold, composite_metric, search_factory);

        MemoryLogger.update();

        LinkageQuality lq = new LinkageFramework(linkage, linker).link(true, false, true, true, numberOfGroundTruthLinks);
        record_repository.stopStoreWatcher();
        linker.terminate();


        return lq;
    }



    public void setCacheSizes(RecordRepository record_repository) {
        record_repository.setBirthsCacheSize(15000);
        record_repository.setDeathsCacheSize(10000);
        record_repository.setMarriagesCacheSize(10000);
    }

    public int countNumberOfGroundTruthLinks(final String source_repository_name) {

        final Path store_path = ApplicationProperties.getStorePath();
        final RecordRepository record_repository = new RecordRepository(store_path, source_repository_name);
        final Linkage linkage = getLinkage(null, null, source_repository_name, null, record_repository);

        int numberOfGroundTruthLinks = linkage.numberOfGroundTruthTrueLinks();
        record_repository.stopStoreWatcher();

        return numberOfGroundTruthLinks;
    }



    protected abstract Linkage getLinkage(final String links_persistent_name, final String gt_persistent_name, final String source_repository_name, final String results_repository_name, final RecordRepository record_repository);

    protected abstract Linker getLinker(final double match_threshold, final Metric<LXP> composite_metric, final SearchStructureFactory<LXP> search_factory);

    protected abstract Metric<LXP> getCompositeMetric(final Linkage linkage);

    protected abstract SearchStructureFactory<LXP> getSearchFactory(final Metric<LXP> composite_metric);

    protected int getNumberOfProgressUpdates() {
        return DEFAULT_NUMBER_OF_PROGRESS_UPDATES;
    }

    protected StringMetric getBaseMetric() {
        return baseMetric;
    }
}

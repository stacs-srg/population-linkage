package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.MemoryLogger;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class LinkageRunner {

    private static final int DEFAULT_NUMBER_OF_PROGRESS_UPDATES = 100;
    private static final StringMetric DEFAULT_BASE_METRIC = Constants.JACCARD;
    private StringMetric baseMetric = DEFAULT_BASE_METRIC;

    public void run(final String links_persistent_name, final String gt_persistent_name, final String source_repository_name, final String results_repository_name, double match_threshold) {

        final Path store_path = ApplicationProperties.getStorePath();
        final RecordRepository record_repository = new RecordRepository(store_path, source_repository_name);
        final Linkage linkage = getLinkage(links_persistent_name, gt_persistent_name, source_repository_name, results_repository_name, record_repository);
        final Metric<LXP> composite_metric = getCompositeMetric(linkage);
        final SearchStructureFactory<LXP> search_factory = getSearchFactory(composite_metric);
        final Linker linker = getLinker(match_threshold, composite_metric, search_factory);

        new LinkageFramework(linkage, linker).link();

        linker.terminate();

    }

    public LinkageQuality evaluateOnly(final String source_repository_name, double match_threshold, StringMetric baseMetric, int numberOfGroundTruthLinks) {

        this.baseMetric = baseMetric;

        MemoryLogger.update();

        final Path store_path = ApplicationProperties.getStorePath();
        final RecordRepository record_repository = new RecordRepository(store_path, source_repository_name);
        final Linkage linkage = getLinkage(null, null, source_repository_name, null, record_repository);
        final Metric<LXP> composite_metric = getCompositeMetric(linkage);
        final SearchStructureFactory<LXP> search_factory = getSearchFactory(composite_metric);
        final Linker linker = getLinker(match_threshold, composite_metric, search_factory);

        MemoryLogger.update();

        LinkageQuality lq = new LinkageFramework(linkage, linker).linkForEvaluationOnly(numberOfGroundTruthLinks);
        record_repository.stopStoreWatcher();
        linker.terminate();


        return lq;
    }

    public int countNumberOfGroundTruthLinks(final String source_repository_name) {

        final Path store_path = ApplicationProperties.getStorePath();
        final RecordRepository record_repository = new RecordRepository(store_path, source_repository_name);
        final Linkage linkage = getLinkage(null, null, source_repository_name, null, record_repository);

        int numberOfGroundTruthLinks = linkage.numberOfGroundTruthTrueLinks();
        record_repository.stopStoreWatcher();

        return numberOfGroundTruthLinks;
    }

    public double calculateIntrinsicDimensionality(final String source_repository_name, StringMetric baseMetric, List<Integer> fields, int sampleN, int numberOfRecords) {

        final Path store_path = ApplicationProperties.getStorePath();
        final RecordRepository record_repository = new RecordRepository(store_path, source_repository_name);

        Sigma distanceFunction = new Sigma(baseMetric, fields);

        List<RecordPair> pairs = new ArrayList<>();

        int everyNthPair = (int) Math.pow(numberOfRecords, 2) / sampleN;

        long consideredPairs = 0;
        long sampledPairs = 0;
        double sumOfDistances = 0;

        for(LXP record1 : Utilities.getBirthRecords(record_repository)) {
            for(LXP record2 : Utilities.getBirthRecords(record_repository)) {

                if(toSample(consideredPairs, everyNthPair)) {
                    double distance = distanceFunction.calculateDistance(record1, record2);
                    pairs.add(new RecordPair(record1, record2, distance));

                    sampledPairs++;
                    sumOfDistances += distance;
                }

                consideredPairs++;
            }
        }

        double mean = sumOfDistances / sampledPairs;

        double cumalativeSum = 0;

        for(RecordPair pair : pairs) {
            cumalativeSum += Math.pow(pair.distance - mean, 2) / sampledPairs;
        }

        double standardDeviation = Math.sqrt(cumalativeSum);

        double intrinsicDimensionality = (mean*mean) / Math.pow(2*standardDeviation, 2);

        System.out.println("Sampled Pairs: " + sampledPairs);
        System.out.println("mean of distances: " + mean);
        System.out.println("standard deviation: " + standardDeviation);
        System.out.println("Intrinsic Dimensionality: " + intrinsicDimensionality);

        record_repository.stopStoreWatcher();

        return intrinsicDimensionality;

    }

    private boolean toSample(long consideredPairs, int everyNthPair) {
        if(everyNthPair <= 1) return true;
        return consideredPairs % everyNthPair == 0;
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

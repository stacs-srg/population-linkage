package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.*;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SSBirthDeathSiblingLinkageRunner extends LinkageRunner {

    @Override
    protected Linkage getLinkage(String links_persistent_name, String gt_persistent_name, String source_repository_name, String results_repository_name, RecordRepository record_repository) {
        return new SSBirthDeathSiblingLinkage(results_repository_name, links_persistent_name, gt_persistent_name, source_repository_name, record_repository);
    }

    @Override
    protected Linker getLinker(double match_threshold, Metric<LXP> composite_metric, SearchStructureFactory<LXP> search_factory) {
        return new SimilaritySearchSiblingBundlerOverBirthsAndDeaths(search_factory, match_threshold, composite_metric, getNumberOfProgressUpdates());
    }

    @Override
    protected Metric<LXP> getCompositeMetric(Linkage linkage) {
        return new Sigma2(getBaseMetric(), linkage.getLinkageFields1(), linkage.getLinkageFields2());
    }

    @Override
    protected SearchStructureFactory<LXP> getSearchFactory(Metric<LXP> composite_metric) {
        return new BitBlasterSearchStructureFactory<>(composite_metric, numberOfReferenceObjects);
    }

    private int birthsCacheSize;
    private int deathsCacheSize;

    private String populationName;
    private String populationSize;
    private String populationNumber;
    private String corruptionNumber;
    private Path resultsFile;
    private int numberOfReferenceObjects;

    private String sourceRepoName;

    static final String linkageApproach = "birth-death-sibling";

    public SSBirthDeathSiblingLinkageRunner(String populationName, String populationSize, String populationNumber,
                                            boolean corrupted, String corruptionNumber, Path resultsFile,
                                            int birthsCacheSize, int deathsCacheSize, int numberOfReferenceObjects) {
        this.populationName = populationName;
        this.populationSize = populationSize;
        this.populationNumber = populationNumber;
        this.corruptionNumber = corruptionNumber;
        this.resultsFile = resultsFile;
        this.birthsCacheSize = birthsCacheSize;
        this.deathsCacheSize = deathsCacheSize;

        if(corrupted)
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_corrupted_" + corruptionNumber;
        else {
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_clean";
            corruptionNumber = "0";
        }

        this.numberOfReferenceObjects = numberOfReferenceObjects;
    }

    public void link(double threshold, String stringMetric, int numberOfGroundTruthLinks, int maxSiblingGap) {

        StringMetric metric = Constants.get(stringMetric, 4096);
        LinkagePostFilter.setMaxSiblingGap(maxSiblingGap);

        System.out.println("Linking population: " + sourceRepoName);

        try {
            JobRunnerIO.setupResultsFile(resultsFile);

            long startTime = System.currentTimeMillis();
            LinkageQuality lq = evaluateOnly(sourceRepoName, threshold, metric, numberOfGroundTruthLinks);
            long timeTakenInSeconds = (System.currentTimeMillis() - startTime) / 1000;

            JobRunnerIO.appendToResultsFile(threshold, stringMetric, maxSiblingGap, lq, timeTakenInSeconds,
                    resultsFile, populationName, populationSize, populationNumber, corruptionNumber,
                    linkageApproach, numberOfReferenceObjects,
                    Constants.SIBLING_BUNDLING_BIRTH_TO_DEATH_LINKAGE_FIELDS_AS_STRINGS,
                    Constants.SIBLING_BUNDLING_DEATH_TO_BIRTH_LINKAGE_FIELDS_AS_STRINGS);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCacheSizes(RecordRepository record_repository) {
        record_repository.setBirthsCacheSize(birthsCacheSize);
        record_repository.setDeathsCacheSize(deathsCacheSize);
    }

    public static void main(String[] args) {

        String populationName = args[0];
        String populationSize = args[1];
        String populationNumber = args[2];
        boolean corrupted = args[3].equals("true");
        String corruptionNumber = args[4];
        double threshold = Double.valueOf(args[5]);
        String stringMetric = args[6];
        Path resultsFile = Paths.get(args[7]);
        int numberOfGroundTruthLinks = Integer.valueOf(args[8]);
        int maxSiblingGap = Integer.valueOf(args[9]);
        int birthsCacheSize = Integer.valueOf(args[10]);
        int deathsCacheSize = Integer.valueOf(args[11]);
        int numROs= Integer.valueOf(args[12]);

        new SSBirthDeathSiblingLinkageRunner(populationName, populationSize, populationNumber, corrupted,
                corruptionNumber, resultsFile, birthsCacheSize, deathsCacheSize, numROs)
                .link(threshold, stringMetric, numberOfGroundTruthLinks, maxSiblingGap);

    }



}

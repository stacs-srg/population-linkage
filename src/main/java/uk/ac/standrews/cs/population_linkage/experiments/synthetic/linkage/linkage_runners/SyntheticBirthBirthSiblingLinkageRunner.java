package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.linkage_runners;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.*;
import uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.LinkagePostFilter;
import uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.helpers.JobRunnerIO;
import uk.ac.standrews.cs.population_linkage.experiments.umea.linkage.UmeaBirthBirthSiblingLinkageRunner;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SyntheticBirthBirthSiblingLinkageRunner extends UmeaBirthBirthSiblingLinkageRunner {

    private int birthsCacheSize;

    private String populationName;
    private String populationSize;
    private String populationNumber;
    private String corruptionNumber;
    private Path resultsFile;
    private int numberOfReferenceObjects;

    private String sourceRepoName;

    public SyntheticBirthBirthSiblingLinkageRunner(String populationName, String populationSize, String populationNumber, boolean corrupted, String corruptionNumber, Path resultsFile, int cacheSize, int numberOfReferenceObjects) {
        this.populationName = populationName;
        this.populationSize = populationSize;
        this.populationNumber = populationNumber;
        this.corruptionNumber = corruptionNumber;
        this.resultsFile = resultsFile;
        this.birthsCacheSize = cacheSize;

        if(corrupted)
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_corrupted_" + corruptionNumber;
        else {
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_clean";
            corruptionNumber = "0";
        }

        this.numberOfReferenceObjects = numberOfReferenceObjects;
    }

    public static final String linkageApproach = "sibling-birth-bundler";

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
                    Constants.SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS_AS_STRINGS,
                    Constants.SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS_AS_STRINGS);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCacheSizes(RecordRepository record_repository) {
        record_repository.setBirthsCacheSize(birthsCacheSize);
    }

    protected SearchStructureFactory<LXP> getSearchFactory(final Metric<LXP> composite_metric) {
        return new BitBlasterSearchStructureFactory<>(composite_metric, numberOfReferenceObjects);
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
        int cacheSize = Integer.valueOf(args[10]);
        int numROs= Integer.valueOf(args[11]);

        new SyntheticBirthBirthSiblingLinkageRunner(populationName, populationSize, populationNumber, corrupted,
                corruptionNumber, resultsFile, cacheSize, numROs)
                .link(threshold, stringMetric, numberOfGroundTruthLinks, maxSiblingGap);

    }

}

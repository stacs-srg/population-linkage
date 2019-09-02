package uk.ac.standrews.cs.population_linkage.linkageRunners;

import uk.ac.standrews.cs.population_linkage.linkageRecipies.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipies.BirthDeathSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.linkers.SimilaritySearchLinker;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.*;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkagePostFilter;
import uk.ac.standrews.cs.population_linkage.helpers.JobRunnerIO;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BirthDeathSiblingLinkageRunner extends LinkageRunner {

    @Override
    protected LinkageRecipe getLinkage(String links_persistent_name, String gt_persistent_name, String source_repository_name, String results_repository_name, RecordRepository record_repository) {
        return new BirthDeathSiblingLinkageRecipe(results_repository_name, links_persistent_name, gt_persistent_name, source_repository_name, record_repository);
    }

    @Override
    protected Linker getLinker(double match_threshold, Metric<LXP> composite_metric, SearchStructureFactory<LXP> search_factory) {
        return new SimilaritySearchLinker(search_factory, composite_metric, match_threshold, getNumberOfProgressUpdates(),
                "birth-death-sibling", "threshold match at " + match_threshold, Birth.ROLE_BABY, Death.ROLE_DECEASED, LinkagePostFilter::isViableBDSiblingLink);
    }

    @Override
    protected Metric<LXP> getCompositeMetric(LinkageRecipe linkageRecipe) {
        return new Sigma2(getBaseMetric(), linkageRecipe.getLinkageFields1(), linkageRecipe.getLinkageFields2());
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

    public static final String linkageApproach = "birth-death-sibling";

    public BirthDeathSiblingLinkageRunner(String populationName, String populationSize, String populationNumber,
                                          boolean corrupted, String corruptionNumber, Path resultsFile,
                                          int birthsCacheSize, int deathsCacheSize, int numberOfReferenceObjects) {
        this.populationName = populationName;
        this.populationSize = populationSize;
        this.populationNumber = populationNumber;
        this.corruptionNumber = corruptionNumber;
        this.resultsFile = resultsFile;
        this.birthsCacheSize = birthsCacheSize;
        this.deathsCacheSize = deathsCacheSize;

        if (corrupted)
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
            LinkageQuality lq = run(sourceRepoName, threshold, metric, true, false);
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
        int numROs = Integer.valueOf(args[12]);

        new BirthDeathSiblingLinkageRunner(populationName, populationSize, populationNumber, corrupted,
                corruptionNumber, resultsFile, birthsCacheSize, deathsCacheSize, numROs)
                .link(threshold, stringMetric, numberOfGroundTruthLinks, maxSiblingGap);

    }


}

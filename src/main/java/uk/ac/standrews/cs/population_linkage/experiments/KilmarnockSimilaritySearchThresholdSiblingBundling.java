package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkage.MTreeSearchStructure;
import uk.ac.standrews.cs.population_linkage.linkage.SimilaritySearchSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.model.*;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.nio.file.Path;
import java.util.List;

public class KilmarnockSimilaritySearchThresholdSiblingBundling {

    private static final double MATCH_THRESHOLD = 2.0;
    private static final int NUMBER_OF_RECORDS_TO_CONSIDER = 2;
    private static final int NUMBER_OF_PROGRESS_UPDATES = 1000;

    private final Path store_path;
    private final String repo_name;


    public KilmarnockSimilaritySearchThresholdSiblingBundling(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        System.out.println("Kilmarnock sibling bundling using M-tree Levenshtein threshold 2.0");

        long t1 = System.currentTimeMillis();

        List<LXP> birth_sub_records = Utilities.getBirthLinkageSubRecords(record_repository);

        long t2 = System.currentTimeMillis();
        System.out.println((t2 - t1) / 1000 + "s to extract linkage records");

        Linker sibling_bundler = new SimilaritySearchSiblingBundlerOverBirths(makeSearchStructure(), MATCH_THRESHOLD, NUMBER_OF_RECORDS_TO_CONSIDER, NUMBER_OF_PROGRESS_UPDATES);

        Links sibling_links = sibling_bundler.link(birth_sub_records);

        long t3 = System.currentTimeMillis();
        System.out.println((t3 - t2) / 1000 + "s to link records");

        Links ground_truth_links = Utilities.getGroundTruthSiblingLinks(record_repository);

        long t4 = System.currentTimeMillis();
        System.out.println((t4 - t3) / 1000 + "s to get ground truth links");

        LinkageQuality linkage_quality = Utilities.evaluateLinkage(sibling_links, ground_truth_links);

        long t5 = System.currentTimeMillis();
        System.out.println((t5 - t4) / 1000 + "s to evaluate linkage");

        linkage_quality.print(System.out);
    }

    private SearchStructure<LXP> makeSearchStructure() throws InvalidWeightsException {

        return new MTreeSearchStructure<>(Utilities.weightedAverageLevenshteinOverBirths());
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = ApplicationProperties.getRepositoryName();

        new KilmarnockSimilaritySearchThresholdSiblingBundling(store_path, repository_name).run();
    }
}

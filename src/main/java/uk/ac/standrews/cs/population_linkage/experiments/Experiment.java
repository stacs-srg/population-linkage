package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.model.Link;
import uk.ac.standrews.cs.population_linkage.model.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_linkage.model.Links;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;

import java.util.List;

public abstract class Experiment {

    public void run() throws Exception {

        RecordRepository record_repository = getRecordRepository();

        printHeader();

        long t1 = System.currentTimeMillis();

        List<LXP> birth_sub_records = getRecords(record_repository);

        long t2 = System.currentTimeMillis();
        System.out.println((t2 - t1) / 1000 + "s to extract linkage records");

        Linker sibling_bundler = getLinker();

        Links sibling_links = sibling_bundler.link(birth_sub_records);
        System.out.println("links: " + sibling_links.size());

        long t3 = System.currentTimeMillis();
        System.out.println((t3 - t2) / 1000 + "s to link records");

        Links ground_truth_links = getGroundTruthLinks(record_repository);
        System.out.println("ground truth links: " + ground_truth_links.size());

        long t4 = System.currentTimeMillis();
        System.out.println((t4 - t3) / 1000 + "s to get ground truth links");

        LinkageQuality linkage_quality = evaluateLinkage(sibling_links, ground_truth_links);

        long t5 = System.currentTimeMillis();
        System.out.println((t5 - t4) / 1000 + "s to evaluate linkage");

        linkage_quality.print(System.out);
    }

    protected abstract RecordRepository getRecordRepository() throws Exception;
    protected abstract void printHeader();
    protected abstract List<LXP> getRecords(RecordRepository record_repository);
    protected abstract Linker getLinker();
    protected abstract Links getGroundTruthLinks(RecordRepository record_repository);

    private LinkageQuality evaluateLinkage(Links calculated_links, Links ground_truth_links) {

        int true_positives = countTruePositives(calculated_links, ground_truth_links);
        int false_positives = countFalsePositives(calculated_links, ground_truth_links);
        int false_negatives = countFalseNegatives(calculated_links, ground_truth_links);

        double precision = ClassificationMetrics.precision(true_positives, false_positives);
        double recall = ClassificationMetrics.recall(true_positives, false_negatives);
        double f_measure = ClassificationMetrics.F1(true_positives, false_positives, false_negatives);

        return new LinkageQuality(precision, recall, f_measure);
    }

    private static int countTruePositives(Links calculated_links, Links ground_truth_links) {

        int count = 0;

        for (Link calculated_link : calculated_links) {

            if (ground_truth_links.contains(calculated_link)) {
                count++;
            }
        }

        return count;
    }

    private static int countFalsePositives(Links calculated_links, Links ground_truth_links) {

        return countInFormerNotInLatter(calculated_links, ground_truth_links);
    }

    private static int countFalseNegatives(Links calculated_links, Links ground_truth_links) {

        return countInFormerNotInLatter(ground_truth_links, calculated_links);
    }

    private static int countInFormerNotInLatter(final Links set1, final Links set2) {

        int count = 0;

        for (Link calculated_link : set1) {

            if (!set2.contains(calculated_link)) {
                count++;
            }
        }

        return count;
    }
}

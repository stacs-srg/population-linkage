package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.model.Link;
import uk.ac.standrews.cs.population_linkage.model.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class Experiment {

    public void run() throws Exception {

        final RecordRepository record_repository = getRecordRepository();

        printHeader();

        final Iterable<LXP> birth_records = getRecords(record_repository);

        final Linker sibling_bundler = getLinker();

        sibling_bundler.addRecords(birth_records, birth_records);

        final Iterable<Link> sibling_links = sibling_bundler.getLinks();
        LocalDateTime time_stamp = LocalDateTime.now();

        final Set<Link> ground_truth_links = getGroundTruthLinks(record_repository);
        time_stamp = nextTimeStamp(time_stamp, "get ground truth links");

        final LinkageQuality linkage_quality = evaluateLinkage(sibling_links, ground_truth_links);
        nextTimeStamp(time_stamp, "perform and evaluate linkage");

        linkage_quality.print(System.out);
    }

    protected abstract RecordRepository getRecordRepository() throws Exception;

    protected abstract void printHeader();

    protected abstract Iterable<LXP> getRecords(RecordRepository record_repository);

    protected abstract Linker getLinker();

    protected abstract Set<Link> getGroundTruthLinks(RecordRepository record_repository);

    private LocalDateTime nextTimeStamp(final LocalDateTime previous_time_stamp, final String step_description) {

        LocalDateTime next = LocalDateTime.now();
        System.out.println(prettyPrint(Duration.between(previous_time_stamp, next)) + " to " + step_description);
        return next;
    }

    private LinkageQuality evaluateLinkage(Iterable<Link> calculated_links, Set<Link> ground_truth_links) {

        int true_positives = 0;
        int false_positives = 0;

        Set<Link> copy_of_ground_truth_links = new HashSet<>(ground_truth_links);

        for (Link calculated_link : calculated_links) {

            if (ground_truth_links.contains(calculated_link)) {
                true_positives++;
            } else {
                false_positives++;
            }

            copy_of_ground_truth_links.remove(calculated_link);
        }

        int false_negatives = copy_of_ground_truth_links.size();

        System.out.println("TP: " + true_positives);
        System.out.println("FP: " + false_positives);
        System.out.println("FN: " + false_negatives);

        double precision = ClassificationMetrics.precision(true_positives, false_positives);
        double recall = ClassificationMetrics.recall(true_positives, false_negatives);
        double f_measure = ClassificationMetrics.F1(true_positives, false_positives, false_negatives);

        return new LinkageQuality(precision, recall, f_measure);
    }

    private static String prettyPrint(Duration duration) {

        return String.format("%sh %sm %ss",
                duration.toHours(),
                duration.toMinutes() - TimeUnit.HOURS.toMinutes(duration.toHours()),
                duration.getSeconds() - TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
    }
}

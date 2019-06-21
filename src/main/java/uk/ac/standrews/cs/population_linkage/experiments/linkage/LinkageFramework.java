package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.population_linkage.model.Link;
import uk.ac.standrews.cs.population_linkage.model.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class LinkageFramework {

    private final Linker linker;
    private Linkage linkage;

    public LinkageFramework(Linkage linkage, Linker linker) {
        this.linkage = linkage;
        this.linker = linker;
    }

    public void link() {

        System.out.println("r1r2r3r4");

        linker.addRecords(linkage.getSourceRecords1(), linkage.getSourceRecords2());

        System.out.println("r5");
        final Iterable<Link> links = linker.getLinks();
        LocalDateTime time_stamp = LocalDateTime.now();

        linkage.makeLinksPersistent(links);

        System.out.println("r6");
        final Set<Link> ground_truth_links = linkage.getGroundTruthLinks();
        time_stamp = nextTimeStamp(time_stamp, "get ground truth links");

        linkage.makeGroundTruthPersistent(ground_truth_links);

        System.out.println("r7");
        final LinkageQuality linkage_quality = evaluateLinkage(links, ground_truth_links);
        nextTimeStamp(time_stamp, "perform and evaluate linkage");

        System.out.println("r8");
        linkage_quality.print(System.out);
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

    private LinkageQuality evaluateLinkage(Iterable<Link> calculated_links, Set<Link> ground_truth_links) {

        // NB this mutates the passed in ground truth set.

        int true_positives = 0;
        int false_positives = 0;

        for (Link calculated_link : calculated_links) {

            if (ground_truth_links.contains(calculated_link)) {
                true_positives++;
            } else {
                false_positives++;
            }

            ground_truth_links.remove(calculated_link);
        }

        int false_negatives = ground_truth_links.size();

        System.out.println("TP: " + true_positives);
        System.out.println("FP: " + false_positives);
        System.out.println("FN: " + false_negatives);

        double precision = ClassificationMetrics.precision(true_positives, false_positives);
        double recall = ClassificationMetrics.recall(true_positives, false_negatives);
        double f_measure = ClassificationMetrics.F1(true_positives, false_positives, false_negatives);

        return new LinkageQuality(precision, recall, f_measure);
    }
}

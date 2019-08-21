package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.MemoryLogger;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

        System.out.println("r5.5");

//        linkage.makeLinksPersistent(links);


        System.out.println("r6");
        final Map<String, Link> ground_truth_links = linkage.getGroundTruthLinks();
        time_stamp = nextTimeStamp(time_stamp, "get ground truth links");

        linkage.makeGroundTruthPersistent(ground_truth_links.values());

//        Iterator<Link> linksMade = linkage.getLinksMade();

        System.out.println("r7");
        final LinkageQuality linkage_quality = evaluateLinkage(links, ground_truth_links);
        nextTimeStamp(time_stamp, "perform and evaluate linkage");

        System.out.println("r8");
        linkage_quality.print(System.out);
    }

    public LinkageQuality linkForEvaluationOnly(int numberOfGroundTruthLinks) {

        System.out.println("Adding records into linker @ " + LocalDateTime.now().toString());

        // Adds two datasets into linker - these will be used to contruct the iterator shortly
        // In the birth sibling bundling case these are both births
        linker.addRecords(linkage.getPreFilteredSourceRecords1(), linkage.getPreFilteredSourceRecords2());

        MemoryLogger.update();

        System.out.println("Constructing link iterable @ " + LocalDateTime.now().toString());
        final Iterable<Link> links = linker.getLinks();

        MemoryLogger.update();

        LocalDateTime time_stamp = LocalDateTime.now();

        System.out.println("Evaluating links @ " + LocalDateTime.now().toString());
        LinkageQuality quality = linkage.evaluateWithoutPersisting(numberOfGroundTruthLinks, links);
        nextTimeStamp(time_stamp, "perform and evaluate linkage");

        MemoryLogger.update();

        quality.print(System.out);
        return quality;
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

    private LinkageQuality evaluateLinkage(Iterable<Link> calculated_links, Map<String, Link> ground_truth_links) {

        // NB this mutates the passed in ground truth set.

        int true_positives = 0;
        int count_gt_links = ground_truth_links.size(); //<<<<<<<<<<<<<<< these have been double counted
        int false_positives = 0;

        for (Link calculated_link : calculated_links) {

            if (ground_truth_links.get(toKey(calculated_link)) != null) {
                true_positives++;
            } else {
                showLink(calculated_link);
                false_positives++;
            }

//            ground_truth_links.remove(calculated_link.toString());
        }

        true_positives = true_positives / 2; //<<<<<<<<<<<<<<< these have been double counted
        false_positives = false_positives / 2; //<<<<<<<<<<<<<<< these have been double counted

        int false_negatives = count_gt_links - true_positives;


        System.out.println("GT Links: " + count_gt_links);

        return new LinkageQuality(true_positives, false_positives, false_negatives);
    }

    private void showLink(Link calculated_link) {

        try {
            LXP person1 = calculated_link.getRole1().getRecordId().getReferend();
            LXP person2 = calculated_link.getRole2().getRecordId().getReferend();



            System.out.println("B1: " + person1.getString(Birth.FORENAME) + " " + person1.getString(Birth.SURNAME) + " // "
                    + "B1F: " + person1.getString(Birth.FATHER_FORENAME) + " " + person1.getString(Birth.FATHER_SURNAME) + " " + person1.getString(Birth.FAMILY) + " -> " +
                    "B2: " + person2.getString(Birth.FORENAME) + " " + person2.getString(Birth.SURNAME) + " // " +
                    "B2F: " + person2.getString(Birth.FATHER_FORENAME) + " " + person2.getString(Birth.FATHER_SURNAME) + " " + person2.getString(Birth.FAMILY));

        } catch (Exception e) {}


    }

    private String toKey(Link link) {
        String s1 = null;
        try {
            s1 = link.getRole1().getRecordId().getReferend().getString(Birth.ORIGINAL_ID);
            String s2 = link.getRole2().getRecordId().getReferend().getString(Birth.ORIGINAL_ID);

            if(s1.compareTo(s2) < 0)
                return s1 + "-" + s2;
            else
                return s2 + "-" + s1;

        } catch (BucketException e) {
            e.printStackTrace();
            throw new Error(e);
        }

    }
}

package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.helpers.MemoryLogger;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static uk.ac.standrews.cs.population_linkage.experiments.characterisation.LinkStatus.TRUE_MATCH;

public class LinkageFramework {

    private final Linker linker;
    private Linkage linkage;

    public LinkageFramework(Linkage linkage, Linker linker) {
        this.linkage = linkage;
        this.linker = linker;
    }

    public LinkageQuality link( boolean pre_filter, boolean persist_links, boolean evaluate_quality, boolean symmetricLinkage, int numberOfGroundTruthTrueLinks ) {

        System.out.println("Adding records into linker @ " + LocalDateTime.now().toString());

        if( pre_filter ) {
            linker.addRecords(linkage.getPreFilteredSourceRecords1(), linkage.getPreFilteredSourceRecords2());
        } else {
            linker.addRecords(linkage.getSourceRecords1(), linkage.getSourceRecords2());
        }

        MemoryLogger.update();
        System.out.println("Constructing link iterable @ " + LocalDateTime.now().toString());

        Iterable<Link> links = linker.getLinks();
        LocalDateTime time_stamp = LocalDateTime.now();

        MemoryLogger.update();
        int tp = 0; // these are counters with which we use if evaluating
        int fp = 0;

        System.out.println("Entering persist and evaluate loop @ " + LocalDateTime.now().toString());

        for (Link linkage_says_true_link : links) {
            if( persist_links ) {
                linkage.makeLinkPersistent(linkage_says_true_link);
            }
            if( evaluate_quality ) {
                if (doesGTSayIsTrue(linkage_says_true_link)) {
                    tp++;
                } else {
                    fp++;
                }
            }
        }

        System.out.println("Exiting persist and evaluate loop @ " + LocalDateTime.now().toString());

        MemoryLogger.update();
        nextTimeStamp(time_stamp, "perform and evaluate linkage");

        if(evaluate_quality) {

            if(symmetricLinkage) {
                // if the linkage is a dataset to itself (i.e birth-birth) we should be rewarded for making the
                // link in both direction - thus divide by two
                tp = tp/2;
                fp = fp/2;
            }

            int fn = numberOfGroundTruthTrueLinks - tp;
            LinkageQuality lq = new LinkageQuality(tp, fp, fn);
            lq.print(System.out);
            return lq;
        } else
            return new LinkageQuality("Evaluation not requested");
    }


    private boolean doesGTSayIsTrue(Link linkage_says_true_link) {
        try {
            return linkage.isTrueMatch(
                    linkage_says_true_link.getRecord1().getReferend(),
                    linkage_says_true_link.getRecord2().getReferend())
                    .equals(TRUE_MATCH);
        } catch (BucketException e) {
            throw new RuntimeException("Bucket exception from accessing referend - bucket no longer contains expected records (TD)", e);
        }
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


    private void showLink(Link calculated_link) {

        try {
            LXP person1 = calculated_link.getRecord1().getReferend();
            LXP person2 = calculated_link.getRecord2().getReferend();

            System.out.println("B1: " + person1.getString(Birth.FORENAME) + " " + person1.getString(Birth.SURNAME) + " // "
                    + "B1F: " + person1.getString(Birth.FATHER_FORENAME) + " " + person1.getString(Birth.FATHER_SURNAME) + " " + person1.getString(Birth.FAMILY) + " -> " +
                    "B2: " + person2.getString(Birth.FORENAME) + " " + person2.getString(Birth.SURNAME) + " // " +
                    "B2F: " + person2.getString(Birth.FATHER_FORENAME) + " " + person2.getString(Birth.FATHER_SURNAME) + " " + person2.getString(Birth.FAMILY));

        } catch (Exception e) {}
    }

}

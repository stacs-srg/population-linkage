package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.model.Link;
import uk.ac.standrews.cs.population_linkage.model.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class Experiment {

    protected final Path store_path;
    protected final String repo_name;

    Experiment(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    private static String prettyPrint(Duration duration) {

        return String.format("%sh %sm %ss",
                duration.toHours(),
                duration.toMinutes() - TimeUnit.HOURS.toMinutes(duration.toHours()),
                duration.getSeconds() - TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
    }

    public void run() throws Exception {

        System.out.println("r1");

        final RecordRepository record_repository = getRecordRepository();

        System.out.println("r2");
        printHeader();

        final Iterable<LXP> birth_records = getRecords(record_repository);

        final Iterable<LXP> br2 = getRecords(record_repository);
        Iterator<LXP> iter = br2.iterator();
        for (int i = 0; i < 4; i++) {
            if (iter.hasNext()) {
                LXP xx = iter.next();
                System.out.println("Read birth: " + xx);
            } else {
                System.out.println("No more records at " + i);
                break;
            }
        }

        System.out.println("r3");
        final Linker sibling_bundler = getLinker();

        System.out.println("r4");
        sibling_bundler.addRecords(birth_records, birth_records);

        System.out.println("r5");
        final Iterable<Link> sibling_links = sibling_bundler.getLinks();
        LocalDateTime time_stamp = LocalDateTime.now();

        dumpToFile("links", sibling_links);

        System.out.println("r6");
        final Set<Link> ground_truth_links = getGroundTruthLinks(record_repository);
        time_stamp = nextTimeStamp(time_stamp, "get ground truth links");

        dumpToFile("ground_truth", ground_truth_links);

        System.out.println("r7");
        final LinkageQuality linkage_quality = evaluateLinkage(sibling_links, ground_truth_links);
        nextTimeStamp(time_stamp, "perform and evaluate linkage");

        System.out.println("r8");
        linkage_quality.print(System.out);
    }

    private void dumpToFile(String filename, Iterable<Link> links) throws IOException {

        File f = new File(filename);
        if (!f.exists()) {
            f.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        for (Link l : links) {
            bw.write("Role1:\t" + l.getRole1().getRoleType() + "\tRole2:\t" + l.getRole2().getRoleType() + "\tid1:\t" + l.getRole1().getRecordId() + "\tid2:\t" + l.getRole2().getRecordId());
            bw.newLine();
            bw.flush();
        }
        bw.close();
    }

    protected abstract RecordRepository getRecordRepository();

    protected abstract void printHeader();

    protected abstract Iterable<LXP> getRecords(RecordRepository record_repository);

    protected abstract Linker getLinker();

    protected abstract NamedMetric<String> getBaseMetric();

    protected abstract NamedMetric<LXP> getCompositeMetric();

    protected abstract List<Integer> getMatchFields();

    protected abstract double getMatchThreshold();

    protected abstract int getNumberOfProgressUpdates();

    protected abstract Set<Link> getGroundTruthLinks(RecordRepository record_repository);

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

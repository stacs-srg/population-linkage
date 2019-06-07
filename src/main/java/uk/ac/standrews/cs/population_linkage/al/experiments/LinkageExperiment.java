package uk.ac.standrews.cs.population_linkage.al.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkage.SimilaritySearchSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_linkage.model.Link;
import uk.ac.standrews.cs.population_linkage.model.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_linkage.model.Role;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LinkageExperiment {

    protected final Path store_path;
    protected final String repo_name;
    private final List<Integer> match_fields;
    private final NamedMetric<String> base_metric;
    private final NamedMetric<LXP> composite_metric;
    private final List<Integer> ground_truth_fields;

    private static final int NUMBER_OF_PROGRESS_UPDATES = 100;

    private final Iterable source_records1_iterable;
    private final Iterable source_records2_iterable;
    private final RecordRepository record_repository;
    private final BitBlasterSearchFactory search_structure_factory;
    private final double match_threshold;

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = "umea";

        double match_threshold = 2.03; // from R metric power table [FRobustness2).
        NamedMetric<String> base_metric = Utilities.JACCARD;
        List<Integer>  match_fields = Utilities.SIBLING_BUNDLING_BIRTH_MATCH_FIELDS;
        List<Integer> ground_truth_fields = Collections.singletonList(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);
        NamedMetric<LXP> composite_metric = new Sigma(base_metric, match_fields);

        BitBlasterSearchFactory search_factory = new BitBlasterSearchFactory( composite_metric );

        new LinkageExperiment( store_path, repository_name, "births", "births", match_threshold, match_fields, base_metric, composite_metric, ground_truth_fields, search_factory ).run();
    }

    LinkageExperiment(Path store_path, String repo_name, String source_bucketname1, String source_bucketname2, double match_threshold,
                      List<Integer> match_fields, NamedMetric<String> base_metric, NamedMetric<LXP> composite_metric, List<Integer> ground_truth_fields,
                      BitBlasterSearchFactory factory) {

        this.store_path = store_path;
        this.repo_name = repo_name;
        this.match_fields = match_fields;
        this.base_metric = base_metric;
        this.composite_metric = composite_metric;
        this.match_threshold = match_threshold;

        this.ground_truth_fields = ground_truth_fields;
        this.search_structure_factory = factory;

        record_repository = getRecordRepository();

        source_records1_iterable = getRecords( source_bucketname1 );
        source_records2_iterable = getRecords( source_bucketname2 );
    }

    public void run() throws Exception {

        System.out.println("r1");

        System.out.println("r2");
        printHeader();

        Iterator<LXP> iter = source_records2_iterable.iterator();
        for (int i = 0; i < 4; i++) {
            if (iter.hasNext()) {
                LXP xx = iter.next();
                System.out.println("Read record: " + xx);
            } else {
                System.out.println("No more records at " + i);
                break;
            }
        }

        System.out.println("r3");
        final Linker linker = getLinker();

        System.out.println("r4");
        linker.addRecords(source_records1_iterable, source_records1_iterable);

        System.out.println("r5");
        final Iterable<Link> links = linker.getLinks();
        LocalDateTime time_stamp = LocalDateTime.now();

        dumpToFile("links", links);

        System.out.println("r6");
        final Set<Link> ground_truth_links = getGroundTruthLinks(record_repository);
        time_stamp = nextTimeStamp(time_stamp, "get ground truth links");

        dumpToFile("ground_truth", ground_truth_links);

        System.out.println("r7");
        final LinkageQuality linkage_quality = evaluateLinkage(links, ground_truth_links);
        nextTimeStamp(time_stamp, "perform and evaluate linkage");

        System.out.println("r8");
        linkage_quality.print(System.out);
    }

    protected Set<Link> getGroundTruthLinks(final RecordRepository record_repository) {

        final Set<Link> links = new HashSet<>();

        final List<LXP> records = new ArrayList<>();

        for (LXP lxp : record_repository.getBirths()) {
            records.add(lxp);
        }

        final int number_of_records = records.size();

        for (int i = 0; i < number_of_records; i++) {

            for (int j = i + 1; j < number_of_records; j++) {

                LXP record1 = records.get(i);
                LXP record2 = records.get(j);

                try {
                    if (areGroundTruthSiblings(record1, record2)) {
                        links.add(new Link(makeRole(record1), makeRole(record2), 1.0f, "ground truth"));
                    }
                } catch (PersistentObjectException e) {
                    ErrorHandling.error( "PersistentObjectException adding getGroundTruthLinks" );
                }
            }
        }

        return links;
    }

    protected Linker getLinker() {

        return new SimilaritySearchSiblingBundlerOverBirths(search_structure_factory, match_threshold, composite_metric, getNumberOfProgressUpdates());
    }

    protected RecordRepository getRecordRepository()  {
        System.out.println( "Using RecordRepository named - " + repo_name + " from " + store_path );
        return new RecordRepository(store_path, repo_name);
    }

    protected int getNumberOfProgressUpdates() {

        return NUMBER_OF_PROGRESS_UPDATES;
    }

    ///////////////////////////// I/O /////////////////////////////


    private void dumpToFile(String filename, Iterable<Link> links) throws IOException {

        File f = new File(filename);
        if (!f.exists()) {
            f.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        for (Link l : links) {
            bw.write("Role1:\t" + l.getRole1().getRoleType() + "\tRole2:\t" + l.getRole2().getRoleType() + "\tid1:\t" + l.getRole1().getRecordId() + "\tid2:\t" + l.getRole2().getRecordId() + "\tprovenance:\t" + combineProvenance(l.getProvenance()));
            bw.newLine();
            bw.flush();
        }
        bw.close();
    }


    protected void printHeader() {

        System.out.println("Sibling bundling using " + search_structure_factory.getSearchStructureType() + ", " + base_metric.getMetricName() + " with threshold " + match_threshold + " from repository: " + repo_name);
    }

    private static String prettyPrint(Duration duration) {

        return String.format("%sh %sm %ss",
                duration.toHours(),
                duration.toMinutes() - TimeUnit.HOURS.toMinutes(duration.toHours()),
                duration.getSeconds() - TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
    }

    ///////////////////////////// Private methods /////////////////////////////

    private Iterable getRecords(String source_bucketname) {
        if( source_bucketname.equals( "births" ) ) {
            return Utilities.getBirthRecords(record_repository);
        }
        if( source_bucketname.equals( "deaths" ) ) {
            return Utilities.getDeathRecords(record_repository);
        }
        if( source_bucketname.equals( "marriages" ) ) {
            return Utilities.getMarriageRecords(record_repository);
        }
        throw new RuntimeException( "Illegal source bucketname passed to getRecords: " + source_bucketname );
    }

    private String combineProvenance(final List<String> provenance) {

        final StringBuilder builder = new StringBuilder();

        for (String s: provenance) {
            if (builder.length() > 0) builder.append("/");
            builder.append(s);
        }

        return builder.toString();
    }

    private LocalDateTime nextTimeStamp(final LocalDateTime previous_time_stamp, final String step_description) {

        LocalDateTime next = LocalDateTime.now();
        System.out.println(prettyPrint(Duration.between(previous_time_stamp, next)) + " to " + step_description);
        return next;
    }

    private Role makeRole(final LXP record) throws PersistentObjectException {

        return new Role(record.getThisRef(), Birth.ROLE_BABY);
    }

    private boolean areGroundTruthSiblings(LXP record1, LXP record2) {

        if (record1 == record2) return false;

        for (int field : ground_truth_fields) {

            String field1 = record1.getString(field);
            if (field1.equals("") || !field1.equals(record2.getString(field))) return false;
        }

        return true;
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

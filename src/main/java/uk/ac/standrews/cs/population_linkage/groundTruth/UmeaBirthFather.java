package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.Constants;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma2;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* Performs linkage analysis on data from births.
 * It compares the baby's names on a birth certificate with the father's names on another birth certificate.
 * The fields used for comparison are listed in getComparisonFields() and getComparisonFields2().
 * This is identity linkage between the baby on one record and the father on another record.
 * The ground truth is listed in isTrueLink.
 **/
public class UmeaBirthFather extends AsymmetricSingleSourceLinkageAnalysis {

    private UmeaBirthFather(Path store_path, String repo_name, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(store_path, repo_name, getLinkageResultsFilename(), getDistanceResultsFilename(), number_of_records_to_be_checked, number_of_runs);
    }

    UmeaBirthFather(Path store_path, String repo_name, String linkage_results_filename, String distance_results_filename, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(store_path, repo_name, linkage_results_filename, distance_results_filename, number_of_records_to_be_checked, number_of_runs);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getBirthRecords(record_repository);
    }

    @Override
    protected LinkStatus isTrueMatch(LXP record1, LXP record2) {

        final String child_id = record1.getString(Birth.CHILD_IDENTITY);
        final String father_id = record2.getString(Birth.FATHER_IDENTITY);

        if (child_id.isEmpty() || father_id.isEmpty()) return LinkStatus.UNKNOWN;

        return child_id.equals(father_id) ? LinkStatus.TRUE_MATCH : LinkStatus.NOT_TRUE_MATCH;
    }

    @Override
    String getDatasetName() {
        return "Umea";
    }

    @Override
    String getLinkageType() {
        return "identity linkage between baby on birth record and father on birth record";
    }

    @Override
    protected String getSourceType() {
        return "births";
    }

    @Override
    public List<Integer> getComparisonFields() {
        return Arrays.asList(
                Birth.FORENAME,
                Birth.SURNAME);
    }

    @Override
    public List<Integer> getComparisonFields2() {
        return Arrays.asList(
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME);
    }

    @Override
    protected List<Metric<LXP>> getCombinedMetrics() {

        final List<Metric<LXP>> result = new ArrayList<>();

        for (final StringMetric base_metric : Constants.BASE_METRICS) {
            result.add(new Sigma2(base_metric, getComparisonFields(), getComparisonFields2()));
        }
        return result;
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";
        int NUMBER_OF_RUNS = 1;

        new UmeaBirthFather(store_path, repo_name, DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED, NUMBER_OF_RUNS).run();
    }
}

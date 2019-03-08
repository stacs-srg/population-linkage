package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma2;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* This class performs linkage analysis on data from birth certificates.
 * It compares the baby's names on a birth certificate with the father's names on another birth certificate.
 * It attempts to find the a person in the roles of baby and father on two different certificates, and thus is identity linkage
 * The fields used for comparison are listed in getComparisonFields() and getComparisonFields2().
 * The ground truth is listed in isTrueLink.
 **/

public class UmeaBirthFather extends AsymmetricSingleSourceLinkageAnalysis {

    public UmeaBirthFather(Path store_path, String repo_name, String linkage_results_filename, final String distance_results_filename, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(store_path,repo_name,linkage_results_filename, distance_results_filename,number_of_records_to_be_checked,number_of_runs);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getBirthRecords( record_repository );
    }

    @Override
    protected LinkStatus isTrueLink(LXP record1, LXP record2) {

        final String record_id1 = record1.getString(Birth.CHILD_IDENTITY);
        final String record_id2 = record2.getString(Birth.FATHER_IDENTITY);

        if (record_id1.isEmpty() || record_id2.isEmpty()) return LinkStatus.UNKNOWN;

        return record_id1.equals(record_id2) ? LinkStatus.TRUE_LINK : LinkStatus.NOT_TRUE_LINK;
    }

    @Override
    protected String getSourceType() {
        return "births";
    }

    @Override
    public List<Integer> getComparisonFields() {
        return Arrays.asList(
                Birth.FORENAME,
                Birth.SURNAME );
    }

    @Override
    public List<Integer> getComparisonFields2() {
        return Arrays.asList(
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME );
    }

    @Override
    protected List<NamedMetric<LXP>> getCombinedMetrics() {

        final List<NamedMetric<LXP>> result = new ArrayList<>();

        for (final NamedMetric<String> base_metric : Utilities.BASE_METRICS) {
            result.add(new Sigma2(base_metric, getComparisonFields(), getComparisonFields2()));
        }
        return result;
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";
        int NUMBER_OF_RUNS = 1;

        new UmeaBirthFather(store_path, repo_name, getLinkageResultsFilename(), getDistanceResultsFilename(), DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED,NUMBER_OF_RUNS).run();
    }
}

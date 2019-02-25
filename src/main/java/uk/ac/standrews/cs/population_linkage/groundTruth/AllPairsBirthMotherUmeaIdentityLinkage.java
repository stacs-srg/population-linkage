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
 * It compares the baby's names on a birth certificate with the mother's names on another birth certificate.
 * It attempts to find the a person in the roles of baby and mother on two different certificates, and thus is identity linkage
 * The fields used for comparison are listed in getComparisonFields() and getComparisonFields2().
 * The ground truth is listed in isTrueLink.
 **/

public class AllPairsBirthMotherUmeaIdentityLinkage extends AllPairsSameSourceLinkageAnalysis {

    private static final int NUMBER_OF_RUNS = 1;

    public AllPairsBirthMotherUmeaIdentityLinkage(Path store_path, String repo_name, String linkage_results_filename, final String distance_results_filename, int number_of_records_to_be_checked) throws IOException {
        super(store_path,repo_name,linkage_results_filename, distance_results_filename,number_of_records_to_be_checked,NUMBER_OF_RUNS);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getBirthRecords( record_repository );
    }

    @Override
    protected LinkStatus isTrueLink(LXP record1, LXP record2) {

        final String b1_parent_id = record1.getString(Birth.CHILD_IDENTITY);
        final String b2_parent_id = record2.getString(Birth.MOTHER_BIRTH_RECORD_IDENTITY);

        if (b1_parent_id.isEmpty() || b2_parent_id.isEmpty()) return LinkStatus.UNKNOWN;

        return b1_parent_id.equals(b2_parent_id) ? LinkStatus.TRUE_LINK : LinkStatus.NOT_TRUE_LINK;
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

    public List<Integer> getComparisonFields2() {
        return Arrays.asList(
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME );
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

        new AllPairsBirthMotherUmeaIdentityLinkage(store_path, repo_name, "UmeaBirthMotherIdentityLinkage", "UmeaThresholdBirthMotherIdentityDistances",DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED).run();
    }


}

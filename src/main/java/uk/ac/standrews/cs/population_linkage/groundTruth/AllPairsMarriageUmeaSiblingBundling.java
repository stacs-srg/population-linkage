package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma2;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllPairsMarriageUmeaSiblingBundling extends AllPairsSameSourceSiblingBundling {

    public AllPairsMarriageUmeaSiblingBundling(Path store_path, String repo_name, String linkage_results_filename, final String distance_results_filename) throws IOException {
        super(store_path,repo_name,linkage_results_filename, distance_results_filename);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getMarriageRecords( record_repository );
    }

    @Override
    protected LinkStatus isTrueLink(LXP record1, LXP record2) {

        final String m1_groom_id = record1.getString(Marriage.GROOM_BIRTH_RECORD_IDENTITY);
        final String m2_groom_id = record2.getString(Marriage.GROOM_BIRTH_RECORD_IDENTITY);
        final String m1_bride_id = record1.getString(Marriage.BRIDE_BIRTH_RECORD_IDENTITY);
        final String m2_bride_id = record2.getString(Marriage.BRIDE_BIRTH_RECORD_IDENTITY);

        if (m1_groom_id.isEmpty() || m2_groom_id.isEmpty() || m1_bride_id.isEmpty() || m2_bride_id.isEmpty() ) {
            return LinkStatus.UNKNOWN;
        }

        return m1_bride_id.equals(m2_bride_id) && m1_groom_id.equals(m2_groom_id) ? LinkStatus.TRUE_LINK : LinkStatus.NOT_TRUE_LINK;
    }

    @Override
    protected String getSourceType() {
        return "marriages";
    }

    @Override
    public List<Integer> getComparisonFields() {
        return Arrays.asList(
                Marriage.GROOM_FORENAME,
                Marriage.GROOM_FATHER_SURNAME,
                Marriage.GROOM_MOTHER_FORENAME,
                Marriage.GROOM_MOTHER_MAIDEN_SURNAME);
    }

    public List<Integer> getComparisonFields2() {
        return Arrays.asList(
                Marriage.BRIDE_FORENAME,
                Marriage.BRIDE_FATHER_SURNAME,
                Marriage.BRIDE_MOTHER_FORENAME,
                Marriage.BRIDE_MOTHER_MAIDEN_SURNAME);
    }

    @Override
    protected List<NamedMetric<LXP>> getCombinedMetrics() {

        final List<NamedMetric<LXP>> result = new ArrayList<>();

        for (final NamedMetric<String> base_metric : Utilities.BASE_METRICS) {
            result.add(new Sigma2(base_metric, getComparisonFields(), getComparisonFields()));
            result.add(new Sigma2(base_metric, getComparisonFields2(), getComparisonFields2()));
            result.add(new Sigma2(base_metric, getComparisonFields(), getComparisonFields2()));
            result.add(new Sigma2(base_metric, getComparisonFields2(), getComparisonFields()));
        }
        return result;
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        new AllPairsMarriageUmeaSiblingBundling(store_path, repo_name, "UmeaThresholdMarriageSiblingLinkage", "UmeaThresholdMarriageSiblingDistances").run();
    }


}

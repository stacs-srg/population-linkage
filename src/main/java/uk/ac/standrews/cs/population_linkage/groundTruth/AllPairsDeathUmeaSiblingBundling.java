package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * This class performs sibling bundling linkage analysis on data from deaths.
 * It compares two sets of parental names from two death certificates
 * The fields used for comparison are listed in getComparisonFields().
 * The ground truth is listed in isTrueLink.
 **/

public class AllPairsDeathUmeaSiblingBundling extends AllPairsSameSourceLinkageAnalysis {

    public AllPairsDeathUmeaSiblingBundling(Path store_path, String repo_name, String linkage_results_filename, final String distance_results_filename, int number_of_records_to_be_checked) throws IOException {
        super(store_path,repo_name,linkage_results_filename, distance_results_filename, number_of_records_to_be_checked);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getDeathRecords( record_repository );
    }

    @Override
    protected LinkStatus isTrueLink(LXP record1, LXP record2) {

        final String d1_parent_id = record1.getString(Death.PARENT_MARRIAGE_RECORD_IDENTITY);
        final String d2_parent_id = record2.getString(Death.PARENT_MARRIAGE_RECORD_IDENTITY);

        if (d1_parent_id.isEmpty() || d2_parent_id.isEmpty()) return LinkStatus.UNKNOWN;

        return d1_parent_id.equals(d2_parent_id) ? LinkStatus.TRUE_LINK : LinkStatus.NOT_TRUE_LINK;
    }

    @Override
    protected String getSourceType() {
        return "deaths";
    }

    @Override
    public List<Integer> getComparisonFields() {
        return Arrays.asList(
                Death.FATHER_FORENAME,
                Death.FATHER_SURNAME,
                Death.MOTHER_FORENAME,
                Death.MOTHER_MAIDEN_SURNAME);
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        new AllPairsDeathUmeaSiblingBundling(store_path, repo_name, "UmeaThresholdDeathSiblingLinkage", "UmeaThresholdDeathSiblingDistances",DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED).run();
    }


}

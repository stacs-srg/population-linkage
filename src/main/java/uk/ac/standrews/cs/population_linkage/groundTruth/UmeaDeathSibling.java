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

/* Performs linkage analysis on data from deaths.
 * It compares the parents' names on two death records.
 * The fields used for comparison are listed in getComparisonFields().
 * This is indirect sibling linkage between the deceaseds on two death records.
 * The ground truth is listed in isTrueLink.
 **/
public class UmeaDeathSibling extends SymmetricSingleSourceLinkageAnalysis {

    private UmeaDeathSibling(Path store_path, String repo_name, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(store_path, repo_name, getLinkageResultsFilename(), getDistanceResultsFilename(), number_of_records_to_be_checked, number_of_runs);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getDeathRecords(record_repository);
    }

    @Override
    protected LinkStatus isTrueMatch(LXP record1, LXP record2) {

        final String d1_parent_id = record1.getString(Death.PARENT_MARRIAGE_RECORD_IDENTITY);
        final String d2_parent_id = record2.getString(Death.PARENT_MARRIAGE_RECORD_IDENTITY);

        if (d1_parent_id.isEmpty() || d2_parent_id.isEmpty()) return LinkStatus.UNKNOWN;

        return d1_parent_id.equals(d2_parent_id) ? LinkStatus.TRUE_MATCH : LinkStatus.NOT_TRUE_MATCH;
    }

    @Override
    String getDatasetName() {
        return "Umea";
    }

    @Override
    String getLinkageType() {
        return "sibling bundling between deceaseds on death records";
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
        int NUMBER_OF_RUNS = 1;

        new UmeaDeathSibling(store_path, repo_name, DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED, NUMBER_OF_RUNS).run();
    }
}

package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/* Performs linkage analysis on data from marriages.
 * It compares the brides' parents' names on two marriage records.
 * The fields used for comparison are listed in getComparisonFields().
 * This is indirect sibling linkage between the brides on two marriage records.
 * The ground truth is listed in isTrueLink.
 **/
public class UmeaBrideBrideSibling extends SymmetricSingleSourceLinkageAnalysis {

    private UmeaBrideBrideSibling(Path store_path, String repo_name, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(store_path, repo_name, getLinkageResultsFilename(), getDistanceResultsFilename(), number_of_records_to_be_checked, number_of_runs);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getMarriageRecords(record_repository);
    }

    @Override
    protected LinkStatus isTrueMatch(LXP record1, LXP record2) {

        final String m1_bride_id = record1.getString(Marriage.BRIDE_IDENTITY);
        final String m1_bride_father_id = record1.getString(Marriage.BRIDE_FATHER_IDENTITY);
        final String m1_bride_mother_id = record1.getString(Marriage.BRIDE_MOTHER_IDENTITY);
        final String m2_bride_id = record2.getString(Marriage.BRIDE_IDENTITY);
        final String m2_bride_father_id = record2.getString(Marriage.BRIDE_FATHER_IDENTITY);
        final String m2_bride_mother_id = record2.getString(Marriage.BRIDE_MOTHER_IDENTITY);

        if (m1_bride_id.isEmpty() || m2_bride_id.isEmpty() || m1_bride_father_id.isEmpty() || m2_bride_father_id.isEmpty() || m1_bride_mother_id.isEmpty() || m2_bride_mother_id.isEmpty()) {
            return LinkStatus.UNKNOWN;
        }

        // Exclude matches for multiple marriages of the same bride.
        return !m1_bride_id.equals(m2_bride_id) && m1_bride_father_id.equals(m2_bride_father_id) && m1_bride_mother_id.equals(m2_bride_mother_id) ? LinkStatus.TRUE_MATCH : LinkStatus.NOT_TRUE_MATCH;
    }

    @Override
    String getDatasetName() {
        return "Umea";
    }

    @Override
    String getLinkageType() {
        return "sibling bundling between brides on marriage records";
    }

    @Override
    protected String getSourceType() {
        return "marriages";
    }

    @Override
    public List<Integer> getComparisonFields() {
        return Arrays.asList(
                Marriage.BRIDE_FATHER_FORENAME,
                Marriage.BRIDE_FATHER_SURNAME,
                Marriage.BRIDE_MOTHER_FORENAME,
                Marriage.BRIDE_MOTHER_MAIDEN_SURNAME);
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        int NUMBER_OF_RUNS = 1;

        new UmeaBrideBrideSibling(store_path, repo_name, DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED, NUMBER_OF_RUNS).run();
    }
}

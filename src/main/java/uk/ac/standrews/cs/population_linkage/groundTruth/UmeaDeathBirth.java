package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;


/* This class performs linkage analysis on data from births and deaths.
 * It compares the baby and parent's names on a birth certificate with the deceased's names and the deceased's parents names from a death certificate.
 * This is identity linkage on the baby and the deceased.
 * The fields used for comparison are listed in getComparisonFields() and getComparisonFields2().
 * The ground truth is listed in isTrueLink.
 **/

public class UmeaDeathBirth extends TwoSourcesLinkageAnalysis {


    public UmeaDeathBirth(Path store_path, String repo_name, String linkage_results_filename, final String distance_results_filename, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(store_path,repo_name,linkage_results_filename, distance_results_filename, number_of_records_to_be_checked,number_of_runs);
    }


    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getDeathRecords( record_repository );
    }

    @Override
    public Iterable<LXP> getSourceRecords2(RecordRepository record_repository) {
        return Utilities.getBirthRecords( record_repository );
    }


    @Override
    protected LinkStatus isTrueLink(LXP record1, LXP record2) {

        final String b2_parent_id = record1.getString(Death.DECEASED_IDENTITY);
        final String b1_parent_id = record2.getString(Birth.CHILD_IDENTITY);

        if (b1_parent_id.isEmpty() || b2_parent_id.isEmpty()) return LinkStatus.UNKNOWN;

        return b1_parent_id.equals(b2_parent_id) ? LinkStatus.TRUE_LINK : LinkStatus.NOT_TRUE_LINK;
    }

    @Override
    protected String getSourceType() {
        return "deaths";
    }

    @Override
    protected String getSourceType2() {
        return "births";
    }


    @Override
    public List<Integer> getComparisonFields() {
        return Arrays.asList(
                Death.FORENAME,
                Death.SURNAME,  // <<<<<<<<<<<<<<<<<<<<, is this surname or Maiden if female??
                Death.FATHER_FORENAME,
                Death.FATHER_SURNAME,
                Death.MOTHER_FORENAME,
                Death.MOTHER_MAIDEN_SURNAME
                // father's occupation omitted
        );

    }

    @Override
    public List<Integer> getComparisonFields2() {
        return Arrays.asList(
                Birth.FORENAME,
                Birth.SURNAME,
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME
                // father's occupation omitted
                );
    }


    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        int NUMBER_OF_RUNS = 1;

        new UmeaDeathBirth(store_path, repo_name, getLinkageResultsFilename(), getDistanceResultsFilename(), DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED,NUMBER_OF_RUNS).run();

    }
}

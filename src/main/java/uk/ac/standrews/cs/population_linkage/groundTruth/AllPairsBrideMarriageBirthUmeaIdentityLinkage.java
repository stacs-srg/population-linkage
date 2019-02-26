package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;


/**
 * This class performs linkage analysis on data from births and marriages.
 * It compares the baby's and parent's names on a birth certificate with the brides and her parents names from a marriage certificate.
 * This is identity linkage using birth and marriage records.
 * The fields used for comparison are listed in getComparisonFields() and getComparisonFields2().
 * The ground truth is listed in isTrueLink.
 **/

public class AllPairsBrideMarriageBirthUmeaIdentityLinkage extends AllPairs2SourcesLinkageAnalysis {

    private static final int NUMBER_OF_RUNS = 1;

    public AllPairsBrideMarriageBirthUmeaIdentityLinkage(Path store_path, String repo_name, String linkage_results_filename, final String distance_results_filename, int number_of_records_to_be_checked) throws IOException {
        super(store_path,repo_name,linkage_results_filename, distance_results_filename,number_of_records_to_be_checked,NUMBER_OF_RUNS);
    }

    @Override
    public Iterable<LXP> getSourceRecords1(RecordRepository record_repository) {
        return Utilities.getMarriageRecords( record_repository );
    }

    @Override
    public Iterable<LXP> getSourceRecords2(RecordRepository record_repository) {
        return Utilities.getBirthRecords( record_repository );
    }


    @Override
    protected LinkStatus isTrueLink(LXP record1, LXP record2) {

        final String m_parent_id = record1.getString(Marriage.BRIDE_BIRTH_RECORD_IDENTITY);
        final String b_parent_id = record2.getString(Birth.CHILD_IDENTITY);

        if (b_parent_id.isEmpty() || m_parent_id.isEmpty() ) return LinkStatus.UNKNOWN;

        return b_parent_id.equals(m_parent_id) ? LinkStatus.TRUE_LINK : LinkStatus.NOT_TRUE_LINK;
    }

    @Override
    protected String getSourceType1() {
        return "marriages";
    }

    @Override
    protected String getSourceType2() {
        return "births";
    }

    @Override
    public List<Integer> getComparisonFields() {
        return Arrays.asList(
                Marriage.BRIDE_FORENAME,
                Marriage.BRIDE_SURNAME,
                Marriage.BRIDE_FATHER_FORENAME,
                Marriage.BRIDE_FATHER_SURNAME,
                Marriage.BRIDE_MOTHER_FORENAME,
                Marriage.BRIDE_MOTHER_MAIDEN_SURNAME );
    }

    @Override
    public List<Integer> getComparisonFields2() {
        return Arrays.asList(
                Birth.FORENAME,
                Birth.SURNAME,
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME );
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        new AllPairsBrideMarriageBirthUmeaIdentityLinkage(store_path, repo_name,"UmeaThresholdBirthBridesBrideMarriageIdentityLinkage", "UmeaThresholdBirthBrideMarriageIdentityDistances",DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED).run();
    }
}

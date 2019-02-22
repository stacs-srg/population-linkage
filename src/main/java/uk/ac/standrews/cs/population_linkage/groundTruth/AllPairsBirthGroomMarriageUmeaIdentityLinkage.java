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
 * It compares the baby's and parent's names on a birth certificate with the grooms and his parents names from a marriage certificate.
 * The fields used for comparison are listed in getComparisonFields() and getComparisonFields2().
 * This is identity linkage between the baby and the groom.
 * The ground truth is listed in isTrueLink.
 **/

public class AllPairsBirthGroomMarriageUmeaIdentityLinkage extends AllPairs2SourcesLinkageAnalysis {

    public AllPairsBirthGroomMarriageUmeaIdentityLinkage(Path store_path, String repo_name, String linkage_results_filename, final String distance_results_filename, int number_of_records_to_be_checked) throws IOException {
        super(store_path,repo_name,linkage_results_filename, distance_results_filename,number_of_records_to_be_checked);
    }

    @Override
    public Iterable<LXP> getSourceRecords1(RecordRepository record_repository) {
        return Utilities.getBirthRecords( record_repository );
    }

    @Override
    public Iterable<LXP> getSourceRecords2(RecordRepository record_repository) {
        return Utilities.getDeathRecords( record_repository );
    }

    @Override
    protected LinkStatus isTrueLink(LXP record1, LXP record2) {

        final String b_parent_id = record1.getString(Birth.CHILD_IDENTITY);
        final String m_parent_id = record2.getString(Marriage.GROOM_BIRTH_RECORD_IDENTITY);

        if (b_parent_id.isEmpty() || m_parent_id.isEmpty() ) return LinkStatus.UNKNOWN;

        return b_parent_id.equals(m_parent_id) ? LinkStatus.TRUE_LINK : LinkStatus.NOT_TRUE_LINK;
    }

    @Override
    protected String getSourceType1() {
        return "births";
    }

    @Override
    protected String getSourceType2() {
        return "marriages";
    }

    @Override
    public List<Integer> getComparisonFields() {
        return Arrays.asList(
                Birth.FORENAME,
                Birth.SURNAME,
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME );
    }

    @Override
    protected List<Integer> getComparisonFields2() {
        return Arrays.asList(
                Marriage.GROOM_FORENAME,
                Marriage.GROOM_SURNAME,
                Marriage.GROOM_FATHER_FORENAME,
                Marriage.GROOM_FATHER_SURNAME,
                Marriage.GROOM_MOTHER_FORENAME,
                Marriage.GROOM_MOTHER_MAIDEN_SURNAME );
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        new AllPairsBirthGroomMarriageUmeaIdentityLinkage(store_path, repo_name,"UmeaThresholdBirthBridesGroomMarriageIdentityLinkage", "UmeaThresholdBirthGroomMarriageIdentityDistances",DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED).run();
    }
}

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

public class AllPairsBirthGroomsParentsMarriageUmeaIdentityLinkage extends AllPairs2SourcesLinkageAnalysis {

    public AllPairsBirthGroomsParentsMarriageUmeaIdentityLinkage(Path store_path, String repo_name, String linkage_results_filename, final String distance_results_filename) throws IOException {
        super(store_path,repo_name,linkage_results_filename, distance_results_filename);
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

        final String b_parent_id1 = record1.getString(Birth.FATHER_BIRTH_RECORD_IDENTITY);
        final String b_parent_id2 = record1.getString(Birth.MOTHER_BIRTH_RECORD_IDENTITY);
        final String m_parent_id1 = record2.getString(Marriage.GROOM_FATHER_BIRTH_RECORD_IDENTITY);
        final String m_parent_id2 = record2.getString(Marriage.GROOM_MOTHER_BIRTH_RECORD_IDENTITY);

        if (b_parent_id1.isEmpty() || b_parent_id2.isEmpty() ||
            m_parent_id1.isEmpty() || m_parent_id2.isEmpty() ) return LinkStatus.UNKNOWN;

        return b_parent_id1.equals(m_parent_id1) && b_parent_id2.equals(m_parent_id2) ? LinkStatus.TRUE_LINK : LinkStatus.NOT_TRUE_LINK;
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
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME );
    }

    @Override
    protected List<Integer> getComparisonFields2() {
        return Arrays.asList(
                Marriage.GROOM_FATHER_FORENAME,
                Marriage.GROOM_FATHER_SURNAME,
                Marriage.GROOM_MOTHER_FORENAME,
                Marriage.GROOM_MOTHER_MAIDEN_SURNAME );
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        new AllPairsBirthGroomsParentsMarriageUmeaIdentityLinkage(store_path, repo_name,"UmeaThresholdBirthGroomsParentsMarriageIdentityLinkage", "UmeaThresholdBirthGroomsParentsMarriageIdentityDistances").run();
    }
}

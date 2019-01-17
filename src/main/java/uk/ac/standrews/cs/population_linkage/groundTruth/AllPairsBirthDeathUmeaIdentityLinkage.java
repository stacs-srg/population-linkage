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

public class AllPairsBirthDeathUmeaIdentityLinkage extends AllPairs2SourcesLinkageAnalysis {

    public AllPairsBirthDeathUmeaIdentityLinkage(Path store_path, String repo_name, String linkage_results_filename, final String distance_results_filename) throws IOException {
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

        final String b1_parent_id = record1.getString(Birth.CHILD_IDENTITY);
        final String b2_parent_id = record2.getString(Death.DECEASED_IDENTITY);

        if (b1_parent_id.isEmpty() || b2_parent_id.isEmpty()) return LinkStatus.UNKNOWN;

        return b1_parent_id.equals(b2_parent_id) ? LinkStatus.TRUE_LINK : LinkStatus.NOT_TRUE_LINK;
    }

    @Override
    protected String getSourceType1() {
        return "births";
    }

    @Override
    protected String getSourceType2() {
        return "deaths";
    }


    @Override
    public List<Integer> getComparisonFields() {
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

    @Override
    protected List<Integer> getComparisonFields2() {
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

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        new AllPairsBirthDeathUmeaIdentityLinkage(store_path, repo_name,"UmeaThresholdBirthDeathIdentityLinkage", "UmeaThresholdBirthDeathIdentityDistances").run();
    }
}

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

public class AllPairsMarriageUmeaSiblingBundling extends AllPairsSameSourceUmeaSiblingBundling {

    public AllPairsMarriageUmeaSiblingBundling(Path store_path, String repo_name, String linkage_results_filename, final String distance_results_filename) throws IOException {
        super(store_path,repo_name,linkage_results_filename, distance_results_filename);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getMarriageRecords( record_repository );
    }

    @Override
    protected LinkStatus isTrueLink(LXP record1, LXP record2) {

        final String b1_parent_id = record1.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);
        final String b2_parent_id = record2.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);

        if (b1_parent_id.isEmpty() || b2_parent_id.isEmpty()) return LinkStatus.UNKNOWN;

        return b1_parent_id.equals(b2_parent_id) ? LinkStatus.TRUE_LINK : LinkStatus.NOT_TRUE_LINK;
    }

    @Override
    protected String getSourceType() {
        return "marriages";
    }

    @Override
    public List<Integer> getComparisonFields() {
        return Arrays.asList(
                Marriage.FATHER_FORENAME,
                Marriage.FATHER_SURNAME,
                Marriage.MOTHER_FORENAME,
                Marriage.MOTHER_MAIDEN_SURNAME,
                Marriage.PARENTS_PLACE_OF_MARRIAGE,
                Marriage.PARENTS_DAY_OF_MARRIAGE,
                Marriage.PARENTS_MONTH_OF_MARRIAGE,
                Marriage.PARENTS_YEAR_OF_MARRIAGE);
//
//\item Marriage.GROOM\_FATHER\_FORENAME,
//\item Marriage.GROOM\_FATHER\_SURNAME,
//\item Marriage.GROOM\_MOTHER\_FORENAME,
//\item Marriage.GROOM\_MOTHER\_MAIDEN\_SURNAME
// and symmetrically with the bride's mother and father.
// Note that the bride's mother/father may be matched with the groom's mother/father
// on another marriage record due to the asymmetry with brothers and sisters getting married.

    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        new AllPairsMarriageUmeaSiblingBundling(store_path, repo_name, "UmeaThresholdMarriageSiblingLinkage", "UmeaThresholdMarriageSiblingDistances").run();
    }


}

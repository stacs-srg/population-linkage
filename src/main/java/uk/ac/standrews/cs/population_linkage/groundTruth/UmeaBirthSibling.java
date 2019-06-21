package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.Constants;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Utilities;
import uk.ac.standrews.cs.population_linkage.experiments.umea.calibration.GroundTruth;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/* Performs linkage analysis on data from births.
 * It compares the parents' names, date and place of marriage on two birth records.
 * The fields used for comparison are listed in getComparisonFields().
 * This is indirect sibling linkage between the babies on two birth records.
 * The ground truth is listed in isTrueLink.
 **/
public class UmeaBirthSibling extends SymmetricSingleSourceLinkageAnalysis {

    private UmeaBirthSibling(Path store_path, String repo_name, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(store_path, repo_name, getLinkageResultsFilename(), getDistanceResultsFilename(), number_of_records_to_be_checked, number_of_runs);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getBirthRecords(record_repository);
    }

    @Override
    protected LinkStatus isTrueMatch(LXP record1, LXP record2) {

        return GroundTruth.isTrueMatchBirthSiblingUmea(record1, record2);
    }

    @Override
    String getDatasetName() {
        return "Umea";
    }

    @Override
    String getLinkageType() {
        return "sibling bundling between babies on birth records";
    }

    @Override
    protected String getSourceType() {
        return "births";
    }

    @Override
    public List<Integer> getComparisonFields() {
        return Constants.SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS;
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        final int NUMBER_OF_RUNS = 1;

        // number_of_records_to_be_checked = CHECK_ALL_RECORDS for exhaustive otherwise DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED or some other specific number.

        new UmeaBirthSibling(store_path, repo_name, CHECK_ALL_RECORDS, NUMBER_OF_RUNS).run();
    }
}

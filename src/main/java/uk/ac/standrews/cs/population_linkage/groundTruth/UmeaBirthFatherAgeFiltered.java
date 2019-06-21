package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.Constants;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma2BirthFatherAgeFiltered;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/* Performs linkage analysis on data from births.
 * It compares the baby's names on a birth certificate with the father's names on another birth certificate.
 * The fields used for comparison are listed in getComparisonFields() and getComparisonFields2().
 * This is identity linkage between the baby on one record and the father on another record.
 * The ground truth is listed in isTrueLink.
 **/
public class UmeaBirthFatherAgeFiltered extends UmeaBirthFather {

    private UmeaBirthFatherAgeFiltered(Path store_path, String repo_name, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(store_path, repo_name, getLinkageResultsFilename(), getDistanceResultsFilename(), number_of_records_to_be_checked, number_of_runs);
    }

    @Override
    String getLinkageType() {
        return "identity linkage between baby on birth record and father on birth record, with date plausibility checking";
    }

    @Override
    protected List<Metric<LXP>> getCombinedMetrics() {

        final List<Metric<LXP>> result = new ArrayList<>();

        for (final StringMetric base_metric : Constants.BASE_METRICS) {
            result.add(new Sigma2BirthFatherAgeFiltered(base_metric, getComparisonFields(), getComparisonFields2(), Birth.BIRTH_YEAR, Birth.BIRTH_YEAR));
        }
        return result;
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";
        int NUMBER_OF_RUNS = 1;

        new UmeaBirthFatherAgeFiltered(store_path, repo_name, DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED, NUMBER_OF_RUNS).run();
    }
}

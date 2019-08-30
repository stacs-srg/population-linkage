package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.Constants;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Utilities;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Sigma2;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class performs linkage analysis on data pulled from two different data sources, for example births and deaths.
 * Classes extending this class are required to implement the following methods:
 *     getSourceRecords(RecordRepository record_repository), which provides the records from the first data source
 *     <getSourceRecords2(RecordRepository record_repository), which provides the records from the second data source
 *     getSourceType(), which provides a textual description of the first data source, for example, "births"
 *     getSourceType2(), which provides a textual description of the first data source, for example, "deaths"
 *     LinkStatus isTrueLink(final LXP record1, final LXP record2), returns the ground truth about equivalence of datum's from source 1 and source 2
 *     getComparisonFields(), returns the set of fields to be used for distance comparison from data source 1 (note the name)
 *     getComparisonFields2(), returns the set of fields to be used for distance comparison from data source 2
 */
public abstract class TwoSourcesLinkageAnalysis extends ThresholdAnalysis {

    private List<LXP> source_records2;
    private int number_of_records2;

    protected TwoSourcesLinkageAnalysis(final Path store_path, final String repo_name, final String linkage_results_filename, final String distance_results_filename, int number_of_records_to_be_checked, int number_of_runs ) throws IOException {

        super(store_path,repo_name, linkage_results_filename,  distance_results_filename,  number_of_records_to_be_checked, number_of_runs );
    }

    protected abstract Iterable<LXP> getSourceRecords2(RecordRepository record_repository);

    protected abstract String getSourceType2();

    protected abstract List<Integer> getComparisonFields2();

    @Override
    protected List<Metric<LXP>> getCombinedMetrics() {

        final List<Metric<LXP>> result = new ArrayList<>();

        for (final StringMetric base_metric : Constants.BASE_METRICS) {
            result.add(new Sigma2(base_metric, getComparisonFields(), getComparisonFields2()));
        }
        return result;
    }

    @Override
    void setupRecords() {

        System.out.println("Reading records from repository: " + repo_name );

        final RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        final Iterable<LXP> records1 = getSourceRecords(record_repository);
        final Iterable<LXP> records2 = getSourceRecords2(record_repository);

        System.out.println("Randomising record order");

        source_records = Utilities.permute(records1, SEED);
        source_records2 = Utilities.permute(records2, SEED);

        number_of_records = number_of_records_to_be_checked == CHECK_ALL_RECORDS ? source_records.size() : number_of_records_to_be_checked;
        number_of_records2 = source_records2.size();
    }

    @Override
    void processRecord(final int record_index, final Metric<LXP> metric, final boolean increment_counts) {

        processRecord(record_index, number_of_records2, source_records, source_records2, metric, increment_counts);
    }

    @Override
    void printMetaData() {

        linkage_results_metadata_writer.println("Output file created: " + LocalDateTime.now());
        linkage_results_metadata_writer.println("Checking quality of linkage using various string similarity metrics and thresholds");
        linkage_results_metadata_writer.println("Dataset: " + getDatasetName());
        linkage_results_metadata_writer.println("LinkageRecipe type: " + getLinkageType());
        linkage_results_metadata_writer.println("Records: " + getSourceType() + ", " + getSourceType2());
        linkage_results_metadata_writer.flush();

        distance_results_metadata_writer.println("Output file created: " + LocalDateTime.now());
        distance_results_metadata_writer.println("Checking distributions of record pair distances using various string similarity metrics and thresholds");
        distance_results_metadata_writer.println("Dataset: " + getDatasetName());
        distance_results_metadata_writer.println("LinkageRecipe type: " + getLinkageType());
        distance_results_metadata_writer.println("Records: " + getSourceType() + ", " + getSourceType2());
        distance_results_metadata_writer.flush();
    }
}

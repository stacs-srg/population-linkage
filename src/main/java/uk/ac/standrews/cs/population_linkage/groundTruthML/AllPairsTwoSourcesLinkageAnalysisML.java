/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruthML;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * This class performs linkage analysis on data pulled from two different data sources, for example births and deaths.
 * Classes extending this class are required to implement the following methods:
 * getSourceRecords(RecordRepository record_repository), which provides the records from the first data source
 * getSourceRecords2(RecordRepository record_repository), which provides the records from the second data source
 * getSourceType(), which provides a textual description of the first data source, for example, "births"
 * getSourceType2(), which provides a textual description of the second data source, for example, "deaths"
 * LinkStatus isTrueLink(final LXP record1, final LXP record2), returns the ground truth about equivalence of datum's from source 1 and source 2
 * getComparisonFields(), returns the set of fields to be used for distance comparison from data source 1 (note the name)
 * getComparisonFields2(), returns the set of fields to be used for distance comparison from data source 2
 */
public abstract class AllPairsTwoSourcesLinkageAnalysisML extends AllPairsSameSourceLinkageAnalysisML {

    private List<LXP> source_records2;
    private int number_of_records2;

    protected AllPairsTwoSourcesLinkageAnalysisML(final Path store_path, final String repo_name, final String distance_results_filename) throws IOException {

        super(store_path,repo_name,distance_results_filename);
    }

//    protected AllPairsTwoSourcesLinkageAnalysisML(final Path store_path, final String repo_name, final String linkage_results_filename, final String distance_results_filename, final int number_of_records_to_be_checked, final int number_of_runs, final boolean allow_multiple_links) throws IOException {
//
//        super(store_path, repo_name, linkage_results_filename, distance_results_filename, number_of_records_to_be_checked, number_of_runs, allow_multiple_links);
//    }

    protected abstract Iterable<LXP> getSourceRecords2(RecordRepository record_repository);

    protected abstract String getSourceType2();

    protected abstract List<Integer> getComparisonFields2();

    protected abstract int getIdFieldIndex2();

//    @Override
//    public List<Metric<LXP>> getCombinedMetrics() {
//
//        final List<Metric<LXP>> result = new ArrayList<>();
//
//        for (final StringMetric base_metric : Constants.BASE_METRICS) {
//            result.add(new Sigma2(base_metric, getComparisonFields(), getComparisonFields2(), getIdFieldIndex(), getIdFieldIndex2()));
//        }
//        return result;
//    }

    protected void setupRecords() {

        final RecordRepository record_repository = new RecordRepository(repo_name);

        final Iterable<LXP> records1 = getSourceRecords(record_repository);
        final Iterable<LXP> records2 = getSourceRecords2(record_repository);

        source_records = Utilities.permute(records1, SEED);
        source_records2 = Utilities.permute(records2, SEED);

        number_of_records = source_records.size();
        number_of_records2 = source_records2.size();
    }

    public void run() throws Exception {

        printHeaders();
        printMetaData();

        final int number_of_blocks_to_be_checked = number_of_records / BLOCK_SIZE;

        for (int block_index = 0; block_index < number_of_blocks_to_be_checked; block_index++) {

            processBlock(block_index);

            System.out.println("finished block: checked " + (block_index + 1) * BLOCK_SIZE + " records");
            System.out.flush();
        }
    }

    private void processBlock(final int block_index) {

        final int start_index = block_index * BLOCK_SIZE;
        final int end_index = start_index + BLOCK_SIZE;

        for (int i = start_index; i < end_index; i++) {
            processRecords(i);
        }

    }

    private void processRecords(final int record_index) {

        LinkStatus last_status = LinkStatus.UNKNOWN;

        final LXP record1 = source_records.get(record_index);

        // These two weird loops are to try and get a better mix of records
        // If started at zero the same records would show up for negative matches
        // This mirrors what the SameSourceLinkage does.
        for (int j = record_index + 1; j < number_of_records2; j++) {

            last_status = tryMatch(record1,last_status,j);
            if( last_status == LinkStatus.TRUE_MATCH ) {
                return; // stop once we have a true match - may not get any negatives but unlikely!
            }
        }
        // Try from the low records if we didn't find a match
        for (int j = 0; j < Math.min(record_index,number_of_records2); j++) {

            last_status = tryMatch(record1,last_status,j);
            if( last_status == LinkStatus.TRUE_MATCH ) {
                return; // stop once we have a true match
            }
        }
    }

    public LinkStatus tryMatch(LXP record1, LinkStatus last_status, int index) {
        final LXP record2 = source_records2.get(index);

        final LinkStatus link_status = isTrueLink(record1, record2);

        if (link_status != LinkStatus.UNKNOWN) {

            if (shouldPrintResult(link_status, last_status)) {

                last_status = link_status;

                for (final StringMetric metric : metrics) {

                    List<Integer> cf1 = getComparisonFields();
                    List<Integer> cf2 = getComparisonFields2();

                    for (int field_selector = 0; field_selector < cf1.size(); field_selector++ ) {

                        final double distance = metric.distance(record1.getString(cf1.get(field_selector)),
                                                                record2.getString(cf2.get(field_selector)));
                        outputMeasurement(distance);
                    }
                }
                distance_results_writer.print(statusToPrintFormat(link_status));
                distance_results_writer.println();
                distance_results_writer.flush();
            }
        }
        return last_status;
    }

    @Override
    public void printMetaData() {

        distance_results_metadata_writer.println("Output file created: " + LocalDateTime.now());
        distance_results_metadata_writer.println("Checking distributions of record pair distances using various string similarity metrics and thresholds");
        distance_results_metadata_writer.println("Dataset: Umea");
        distance_results_metadata_writer.println("LinkageRecipe type: birth-death");
        distance_results_metadata_writer.println("Records: " + getSourceType() + ", " + getSourceType2());
        distance_results_metadata_writer.flush();
        distance_results_metadata_writer.close();
    }
}

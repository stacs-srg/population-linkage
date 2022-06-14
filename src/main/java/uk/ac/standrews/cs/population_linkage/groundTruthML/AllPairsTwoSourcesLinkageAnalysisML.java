/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruthML;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * This class performs linkage analysis on data pulled from two different data sources, for example births and deaths.
 */
public abstract class AllPairsTwoSourcesLinkageAnalysisML extends AllPairsSameSourceLinkageAnalysisML {

    private List<LXP> source_records2;
    private int number_of_records2;

    protected AllPairsTwoSourcesLinkageAnalysisML(final String repo_name, final String distance_results_filename) throws IOException {

        super(repo_name, distance_results_filename);
    }

    protected abstract Iterable<LXP> getSourceRecords2(RecordRepository record_repository);

    protected abstract String getSourceType2();

    protected abstract List<Integer> getComparisonFields2();

    protected abstract int getIdFieldIndex2();

    protected void setupRecords() {

        final RecordRepository record_repository = new RecordRepository(repo_name);

        final Iterable<LXP> records1 = getSourceRecords(record_repository);
        final Iterable<LXP> records2 = getSourceRecords2(record_repository);

        source_records = Utilities.permute(records1, new Random(SEED));
        source_records2 = Utilities.permute(records2, new Random(SEED));

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

            last_status = tryMatch(record1, last_status, j);
            if (last_status == LinkStatus.TRUE_MATCH) {
                return; // stop once we have a true match - may not get any negatives but unlikely!
            }
        }
        // Try from the low records if we didn't find a match
        for (int j = 0; j < Math.min(record_index, number_of_records2); j++) {

            last_status = tryMatch(record1, last_status, j);
            if (last_status == LinkStatus.TRUE_MATCH) {
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

                for (final StringMeasure measure : measures) {

                    List<Integer> cf1 = getComparisonFields();
                    List<Integer> cf2 = getComparisonFields2();

                    for (int field_selector = 0; field_selector < cf1.size(); field_selector++) {

                        final double distance = measure.distance(record1.getString(cf1.get(field_selector)),
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
        distance_results_metadata_writer.println("Checking distributions of record pair distances using various string similarity measures and thresholds");
        distance_results_metadata_writer.println("Dataset: Umea");
        distance_results_metadata_writer.println("LinkageRecipe type: birth-death");
        distance_results_metadata_writer.println("Records: " + getSourceType() + ", " + getSourceType2());
        distance_results_metadata_writer.flush();
        distance_results_metadata_writer.close();
    }
}

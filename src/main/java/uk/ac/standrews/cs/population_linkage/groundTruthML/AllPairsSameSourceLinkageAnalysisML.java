/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruthML;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class performs linkage analysis on data pulled from a single data sources, for example births.
 * <p>
 * Classes extending this class are required to implement the following methods:
 * getSourceRecords(RecordRepository record_repository), which provides the records from the first data source
 * getSourceType(), which provides a textual description of the first data source, for example, "births"
 * LinkStatus isTrueLink(final LXP record1, final LXP record2), returns the ground truth about equivalence of two datum's from the source
 * getComparisonFields(), returns the set of fields to be used for distance comparison from data source 1 (note the name)
 */

public abstract class AllPairsSameSourceLinkageAnalysisML extends ThresholdAnalysisML {

    protected final String repo_name;

    protected static final int BLOCK_SIZE = 100;
    protected static final String DELIMIT = ",";

    protected final PrintWriter distance_results_writer;
    protected final PrintWriter distance_results_metadata_writer;

    protected List<LXP> source_records;
    protected int number_of_records;

    DecimalFormat df = new DecimalFormat("#.###");

    protected AllPairsSameSourceLinkageAnalysisML(final String repo_name, final String distance_results_filename) throws IOException {

        super();

        this.repo_name = repo_name;

        distance_results_writer = new PrintWriter(new BufferedWriter(new FileWriter(distance_results_filename + ".csv", false)));
        distance_results_metadata_writer = new PrintWriter(new BufferedWriter(new FileWriter(distance_results_filename + ".meta", false)));

        setupRecords();
    }

    protected List<LXP> filter(int number_of_required_fields, int number_of_records_required, Iterable<LXP> records_to_filter, List<Integer> linkageFields) {

        final var filtered_source_records = new ArrayList<LXP>();

        for (LXP record : records_to_filter) {
            if (passesFilter(record, linkageFields, number_of_required_fields)) {
                filtered_source_records.add(record);
            }
            if (filtered_source_records.size() >= number_of_records_required) {
                break;
            }
        }
        return filtered_source_records;
    }

    public boolean passesFilter(LXP record, List<Integer> filterOn, int reqPopulatedFields) {

        int numberOfEmptyFieldsPermitted = filterOn.size() - reqPopulatedFields;
        int numberOfEmptyFields = 0;

        for (int attribute : filterOn) {
            String value = record.getString(attribute).toLowerCase().trim();
            if (RecordFiltering.isMissing(value)) {  // TODO could make this field specific
                numberOfEmptyFields++;
            }
        }

        return numberOfEmptyFields <= numberOfEmptyFieldsPermitted;
    }

    protected abstract Iterable<LXP> getSourceRecords(RecordRepository record_repository);

    protected abstract LinkStatus isTrueLink(final LXP record1, final LXP record2);

    protected abstract String getSourceType();

    protected void setupRecords() {

        System.out.println("Reading records from repository: " + repo_name);

        final RecordRepository record_repository = new RecordRepository(repo_name);

        final var records = getSourceRecords(record_repository);

        System.out.println("Randomising record order");

        source_records = Utilities.permute(records, SEED);
        number_of_records = source_records.size();
    }

    protected void run() throws Exception {

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
            processRecord(i);
        }
    }

    private void processRecord(final int record_index) {

        LinkStatus last_status = LinkStatus.UNKNOWN;

        final LXP record1 = source_records.get(record_index);

        for (int j = record_index + 1; j < number_of_records; j++) {

            final LXP record2 = source_records.get(j);

            final LinkStatus link_status = isTrueLink(record1, record2);

            if (link_status != LinkStatus.UNKNOWN) {

                if (shouldPrintResult(link_status, last_status)) {

                    last_status = link_status;

                    for (final StringMeasure measure : measures) {

                        for (int field_selector : getComparisonFields()) {

                            final double distance = measure.distance(record1.getString(field_selector), record2.getString(field_selector));
                            outputMeasurement(distance);
                        }
                    }
                    distance_results_writer.print(statusToPrintFormat(link_status));
                    distance_results_writer.println();
                    distance_results_writer.flush();
                }
            }
        }
    }

    protected boolean shouldPrintResult(LinkStatus link_status, LinkStatus last_status) {

        return last_status != LinkStatus.NOT_TRUE_MATCH || link_status != LinkStatus.NOT_TRUE_MATCH;
    }

    protected void outputMeasurement(double value) {
        distance_results_writer.print(df.format(value));
        distance_results_writer.print(DELIMIT);
    }

    protected void outputMeasurement(long value) {
        distance_results_writer.print(value);
        distance_results_writer.print(DELIMIT);
    }

    protected void printHeaders() {

        LXP a_source_record = source_records.get(0);

        for (final StringMeasure measure : measures) {

            String name = measure.getMeasureName();
            for (int field_selector : getComparisonFields()) {

                String label = name + "." + a_source_record.getMetaData().getFieldName(field_selector);  // measure name concatenated with the field selector name;
                distance_results_writer.print(label);
                distance_results_writer.print(DELIMIT);
            }
        }

        distance_results_writer.print("link_non-link");
        distance_results_writer.print(DELIMIT);

        distance_results_writer.println();
        distance_results_writer.flush();
    }

    protected void printMetaData() {

        distance_results_metadata_writer.println("Output file created: " + LocalDateTime.now());
        distance_results_metadata_writer.println("Checking quality of linkage for machine learning processing: cross products of measures and field distances");
        distance_results_metadata_writer.println("Dataset: Umea");
        distance_results_metadata_writer.println("LinkageRecipe type: sibling bundling");
        distance_results_metadata_writer.println("Records: " + getSourceType());
        distance_results_metadata_writer.flush();
        distance_results_metadata_writer.close();
    }

    protected String statusToPrintFormat(LinkStatus ls) {
        if (ls == LinkStatus.TRUE_MATCH) {
            return "1";
        } else if (ls == LinkStatus.NOT_TRUE_MATCH) {
            return "-1";
        } else {
            return "0";
        }
    }
}

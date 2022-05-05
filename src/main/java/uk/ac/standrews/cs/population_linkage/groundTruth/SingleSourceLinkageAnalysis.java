/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;

import java.io.IOException;

/**
 * This class performs linkage analysis on data pulled from a single data source, for example births.
 */
public abstract class SingleSourceLinkageAnalysis extends ThresholdAnalysis {

    protected SingleSourceLinkageAnalysis(final String repo_name, final String[] args, final String linkage_results_filename, final String distance_results_filename, final boolean allow_multiple_links) throws IOException {

        super(repo_name, args, linkage_results_filename, distance_results_filename, allow_multiple_links);
    }

    @Override
    public void setupRecords() {

        if (verbose) System.out.println("Reading records from repository: " + repo_name);

        final RecordRepository record_repository = new RecordRepository(repo_name);
        final Iterable<LXP> records = getSourceRecords(record_repository);

        if (verbose) System.out.println("Randomising record order");

        source_records = Utilities.permute(records, SEED);
        number_of_records = number_of_records_to_be_checked == CHECK_ALL_RECORDS ? source_records.size() : number_of_records_to_be_checked;
    }

    @Override
    public void processRecord(final int record_index, final LXPMeasure measure, final boolean increment_counts) {

        processRecord(record_index, number_of_records, source_records, source_records, measure, increment_counts);
    }
}

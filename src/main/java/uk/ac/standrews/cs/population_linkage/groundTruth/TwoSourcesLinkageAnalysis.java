/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.MeanOfFieldDistancesNormalised;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs linkage analysis on data pulled from two different data sources, for example births and deaths.
 */
public abstract class TwoSourcesLinkageAnalysis extends ThresholdAnalysis {

    private List<LXP> source_records2;
    private int number_of_records2;

    protected TwoSourcesLinkageAnalysis(final String repo_name, final String[] args, final String linkage_results_filename, final String distance_results_filename, final boolean allow_multiple_links) throws IOException {

        super(repo_name, args, linkage_results_filename, distance_results_filename, allow_multiple_links);
    }

    protected abstract Iterable<LXP> getSourceRecords2(RecordRepository record_repository);

    protected abstract List<Integer> getComparisonFields2();

    @Override
    public List<LXPMeasure> getCombinedMeasures() {

        final List<LXPMeasure> result = new ArrayList<>();

        for (final StringMeasure base_measure : Constants.BASE_MEASURES) {
            result.add(new MeanOfFieldDistancesNormalised(base_measure, getComparisonFields(), getComparisonFields2(), getNormalisationCutoff()));
        }
        return result;
    }

    @Override
    public void setupRecords() {

        if (verbose) System.out.println("Reading records from repository: " + repo_name);

        final RecordRepository record_repository = new RecordRepository(repo_name);

        final Iterable<LXP> records1 = getSourceRecords(record_repository);
        final Iterable<LXP> records2 = getSourceRecords2(record_repository);

        if (verbose) System.out.println("Randomising record order");

        source_records = Utilities.permute(records1, SEED);
        source_records2 = Utilities.permute(records2, SEED);

        number_of_records = number_of_records_to_be_checked == CHECK_ALL_RECORDS ? source_records.size() : number_of_records_to_be_checked;
        number_of_records2 = source_records2.size();
    }

    @Override
    public void processRecord(final int record_index, final LXPMeasure measure, final boolean increment_counts) {

        processRecord(record_index, number_of_records2, source_records, source_records2, measure, increment_counts);
    }
}

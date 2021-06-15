/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruthML;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs linkage analysis on data from births.
 * It compares the parents' names, date and place of marriage on two birth records.
 * The fields used for comparison are listed in getComparisonFields().
 * This is indirect sibling linkage between the babies on two birth records.
 *
 * This class performs linkage analysis on data pulled from a single data sources, for example births.
 *
 * Program takes a list of comma separated triples like this: distance.field=value, PLUS a threshold.
 * All params must have spaces between them
 *
 * e.g. Cosine.FATHER_FORENAME=0.3 Damerau-Levenshtein.MOTHER_FORENAME=0.7 0.62
 *
 * if multiple fields then like this:
 *
 * distance.field1.field2=value
 *
 * @author al
 *
 * 29/6/2020
 *
 */
public class UmeaBirthSiblingWeighted extends SingleSourceWeightedLinkageAnalysis {

    private final Metric<LXP> metric;

    UmeaBirthSiblingWeighted(Path store_path, String repo_name, final List<Integer> fields, final List<Metric<String>> metrics, final List<Float> weights, final int number_of_records_to_be_checked, final int number_of_runs,
                             double threshold) throws IOException {
        super(store_path, repo_name, getLinkageResultsFilename(), getDistanceResultsFilename(), number_of_records_to_be_checked, number_of_runs, true, threshold);
        this.metric = new SigmaWeighted(fields, metrics, weights, getIdFieldIndex());
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getBirthRecords(record_repository);
    }

    @Override
    public List<Integer> getComparisonFields() {
        return BirthSiblingLinkageRecipe.LINKAGE_FIELDS;
    }

    @Override
    public int getIdFieldIndex() {
        return BirthSiblingLinkageRecipe.ID_FIELD_INDEX;
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2);
    }

    public static LinkStatus trueMatch(LXP record1, LXP record2) {
        return BirthSiblingLinkageRecipe.trueMatch(record1, record2);
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return BirthSiblingLinkageRecipe.isViable(proposedLink);
    }

    @Override
    public String getDatasetName() {
        return "Umea";
    }

    @Override
    public String getLinkageType() {
        return "sibling bundling between babies on birth records";
    }

    @Override
    public String getSourceType() {
        return "births";
    }

    @Override
    public Metric<LXP> getMetric() { return metric; }

    /**
     * Splits a param list into separate fields
     * params are of the form: Cosine.FATHER_FORENAME=0.3, Damerau-Levenshtein.MOTHER_FORENAME=0.7, 0.62
     * @param args
     * @param fields - an empty list of the fields to be initialised.
     * @param metrics  - an empty list of the metrics to be initialised.
     * @param weights  - an empty list of the weights to be initialised.
     * @return the threshold for the program
     */
    public static double processParams(String[] args, List<Integer> fields, List<Metric<String>> metrics, List<Float> weights ) {

        if( args.length < 2 ) {
            throw new RuntimeException( "Error in args: expect list plus threshold like this: Cosine.FATHER_FORENAME=0.3 Damerau-Levenshtein.MOTHER_FORENAME=0.7 0.62");
        }

        String thresh_string = args[ args.length - 1 ];
        double threshold;
        try {
            threshold = Double.parseDouble( thresh_string );
        } catch( NumberFormatException e ) {
            throw new RuntimeException( "Cannot parse threshold as double: " + thresh_string );
        }
        System.out.println( "Threshold = " + threshold );

        for( int i = 0; i < args.length - 1; i++ ) { // parse the triples looks like this: Cosine.FATHER_FORENAME=0.3

            String[] split_front_weight = args[i].split("=");
            String[] metric_name_field = split_front_weight[0].split("\\.");

            Metric<String> m = Constants.get( metric_name_field[0] );
            int field_index = fieldNametoIndex( metric_name_field[1] );

            metrics.add( m );
            fields.add( field_index );
            weights.add(Float.parseFloat( split_front_weight[1] ) );
        }

        float total_weights = 0;
        for( float f: weights ) { total_weights += f; }

        if( total_weights > 1.00001 || total_weights < 0.99999 ) {
            throw new RuntimeException( "Weights must sum to 1, actually summed to: " + total_weights );
        }

        return threshold;
    }

    private static int fieldNametoIndex(String s) {

        return new Birth().getMetaData().getSlot(s);
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "Umea";

        List<Integer> fields = new ArrayList<>();
        List<Metric<String>> metrics = new ArrayList<>();
        List<Float> weights = new ArrayList<>();

        double threshold = processParams( args, fields, metrics, weights );

        final int NUMBER_OF_RUNS = 1;

        // number_of_records_to_be_checked = CHECK_ALL_RECORDS for exhaustive
        // otherwise DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED or some other specific number.

        new UmeaBirthSiblingWeighted(store_path, repo_name, fields, metrics, weights, 500, NUMBER_OF_RUNS, threshold).run();
//        new UmeaBirthSiblingWeighted(store_path, repo_name, fields, metrics, weights, DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED, NUMBER_OF_RUNS, threshold).run();
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;

public class PrintUtils {

    protected static DecimalFormat df = new DecimalFormat("#.###");
    protected static final String DELIMIT = ",";

    public final PrintWriter true_match_results_writer;
    public final PrintWriter random_negatives_results_writer;
    public final PrintWriter hard_negatives_results_writer;
    public final PrintWriter metadata_writer;

    private final List<StringMetric> metrics;
    private final List<Integer> comparison_fields;
    private final List<Integer> comparison_fields2;

    public PrintUtils(String true_match_filename, String random_negatives_filename,
                      String hard_negatives_filename, String metdata_filename,
                      List<Integer> comparison_fields, List<Integer> comparison_fields2 ) throws IOException {

        this.metrics = Constants.BASE_METRICS;
        this.comparison_fields = comparison_fields;
        this.comparison_fields2 = comparison_fields2;

        true_match_results_writer = new PrintWriter(new BufferedWriter(new FileWriter(true_match_filename + ".csv", false)));
        random_negatives_results_writer = new PrintWriter(new BufferedWriter(new FileWriter(random_negatives_filename + ".csv", false)));
        hard_negatives_results_writer = new PrintWriter(new BufferedWriter(new FileWriter(hard_negatives_filename + ".csv", false)));
        metadata_writer = new PrintWriter(new BufferedWriter(new FileWriter(metdata_filename + ".csv", false)));
    }

    public void printPairSameType(LXP record1, LXP record2, PrintWriter pw, LinkStatus ls ) {
        for (final StringMetric metric : metrics) {

            for (int field_selector : comparison_fields) {

                final double distance = metric.distance(record1.getString(field_selector), record2.getString(field_selector));
                outputMeasurement(distance,pw);
            }
        }
        pw.print(statusToPrintFormat(ls));
        pw.println();
        pw.flush();
    }

    public void printPairDiffType(LXP record1, LXP record2, PrintWriter pw, LinkStatus ls ) {
        for (final StringMetric metric : metrics) {

            for (int field_selector = 0; field_selector < comparison_fields.size(); field_selector++ ) {

                final double distance = metric.distance(record1.getString(comparison_fields.get(field_selector)),
                                                        record2.getString(comparison_fields2.get(field_selector)));

                outputMeasurement(distance,pw);
            }
        }
        pw.print(statusToPrintFormat(ls));
        pw.println();
        pw.flush();
    }

    protected String statusToPrintFormat( LinkStatus ls ) {
        if( ls == LinkStatus.TRUE_MATCH ) {
            return "1";
        } else if( ls == LinkStatus.NOT_TRUE_MATCH ) {
            return "-1";
        } else {
            return "0";
        }
    }

    public void outputMeasurement(double value, PrintWriter pw) {
        pw.print(df.format(value));
        pw.print(DELIMIT);
    }

    public void outputMeasurement(long value, PrintWriter pw) {
        pw.print(value);
        pw.print(DELIMIT);
    }

    public void printHeadersDiffTypes(PrintWriter pw, LXP example_record1,  LXP example_record2) {

//        LXP a_source_record = source_records.get(0);
//        LXP b_source_record = source_records.get(0);

        for (final StringMetric metric : metrics) {

            String name = metric.getMetricName();

            for (int field_selector = 0; field_selector < comparison_fields.size(); field_selector++ ) {

                String label = name + "." + example_record1.getMetaData().getFieldName(field_selector) + "-" +
                        example_record2.getMetaData().getFieldName(field_selector);  //metric name concatenated with the field selector names;
                pw.print(label);
                pw.print(DELIMIT);

            }
        }

        pw.print("link_non-link");
        pw.print(DELIMIT);

        pw.println();
        pw.flush();
    }

    public void printHeadersSameType(PrintWriter pw, LXP example_record) {

        for (final StringMetric metric : metrics) {

            String name = metric.getMetricName();
            for (int field_selector : comparison_fields) {

                String label = name + "." + example_record.getMetaData().getFieldName(field_selector);  //metric name concatenated with the field selector name;
                pw.print(label);
                pw.print(DELIMIT);

            }
        }

        pw.print("link_non-link");
        pw.print(DELIMIT);

        pw.println();
        pw.flush();
    }

    protected void printMetaData( PrintWriter pw ) {

        pw.println("Output file created: " + LocalDateTime.now());
        pw.println("Checking quality of linkage for machine learning processing: cross products of metrics and field distances");
        pw.println("Dataset: Umea");
        pw.flush();
        pw.close();
    }
}

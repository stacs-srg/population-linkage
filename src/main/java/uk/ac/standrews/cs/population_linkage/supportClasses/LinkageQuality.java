/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;

import uk.ac.standrews.cs.utilities.ClassificationMetrics;

import java.io.PrintStream;

public class LinkageQuality {

    private double precision;
    private double recall;
    private double f_measure;

    private long tp;
    private long fp;
    private long fn;

    private String message = null;

    public LinkageQuality(long tp, long fp, long fn) {
        this.tp = tp;
        this.fp = fp;
        this.fn = fn;

        precision = ClassificationMetrics.precision(tp, fp);
        recall = ClassificationMetrics.recall(tp, fn);
        f_measure = ClassificationMetrics.F1(tp, fp, fn);
    }

    public LinkageQuality(String message) {
        this.message = message;
    }

    // Switch names of print and print2 for csv output
    public void print(PrintStream out) {
        if(message == null) {
            out.println("TP: " + tp);
            out.println("FN: " + fn);
            out.println("FP: " + fp);

            out.printf("precision: %.2f%n", precision);
            out.printf("recall: %.2f%n", recall);
            out.printf("f measure: %.2f%n", f_measure);
        } else {
            out.println(message);
        }
    }

    // Switch names of print and print2 for textual output
    public void print2(PrintStream out) {
        out.println( toCSV() );
    }


    public String toCSV() {
        if(message == null)
            return tp +"," + fn + "," + fp + "," + String.format("%.4f", precision) + ","
                + String.format("%.4f", recall) + "," + String.format("%.4f", f_measure);
        else {
            return message + ",,,,";
        }
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getF_measure() {
        return f_measure;
    }
}

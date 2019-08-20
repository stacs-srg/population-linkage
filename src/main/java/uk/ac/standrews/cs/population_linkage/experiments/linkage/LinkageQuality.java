package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.utilities.ClassificationMetrics;

import java.io.PrintStream;

public class LinkageQuality {

    private double precision;
    private double recall;
    private double f_measure;

    private int tp;
    private int fp;
    private int fn;

    public LinkageQuality(double precision, double recall, double f_measure) {

        this.precision = precision;
        this.recall = recall;
        this.f_measure = f_measure;
    }

    public LinkageQuality(int tp, int fp, int fn) {
        this.tp = tp;
        this.fp = fp;
        this.fn = fn;

        precision = ClassificationMetrics.precision(tp, fp);
        recall = ClassificationMetrics.recall(tp, fn);
        f_measure = ClassificationMetrics.F1(tp, fp, fn);
    }

    public void print(PrintStream out) {

        out.println("TP: " + tp);
        out.println("FN: " + fn);
        out.println("FP: " + fp);

        out.printf("precision: %.2f%n", precision);
        out.printf("recall: %.2f%n", recall);
        out.printf("f measure: %.2f%n", f_measure);
    }

    public String toCSV() {
        return tp +"," + fp + "," + fn + "," + String.format("%.4f", precision) + ","
                + String.format("%.4f", recall) + "," + String.format("%.4f", f_measure);
    }
}

package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import java.io.PrintStream;

public class LinkageQuality {

    private double precision;
    private double recall;
    private double f_measure;

    public LinkageQuality(double precision, double recall, double f_measure) {

        this.precision = precision;
        this.recall = recall;
        this.f_measure = f_measure;
    }

    public void print(PrintStream out) {

        out.printf("precision: %.2f%n", precision);
        out.printf("recall: %.2f%n", recall);
        out.printf("f measure: %.2f%n", f_measure);
    }
}

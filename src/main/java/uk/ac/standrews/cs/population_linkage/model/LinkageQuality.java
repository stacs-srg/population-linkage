package uk.ac.standrews.cs.population_linkage.model;

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

        out.println("precision: " + precision);
        out.println("recall: " + recall);
        out.println("f measure: " + f_measure);
    }
}

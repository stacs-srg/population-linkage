/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
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

        setAll(tp,fp,fn);
        updatePRF();
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

    public long getTp() {
        return tp;
    }

    public void setTp(long tp) {
        this.tp = tp;
    }

    public long getFp() {
        return fp;
    }

    public void setFp(long fp) {
        this.fp = fp;
    }

    public long getFn() {
        return fn;
    }

    public void setFn(long fn) {
        this.fn = fn;
    }

    public void setAll(long tp, long fp, long fn) {
        this.tp = tp;
        this.fp = fp;
        this.fn = fn;
    }

    public void updatePRF() {
        precision = ClassificationMetrics.precision(tp, fp);
        recall = ClassificationMetrics.recall(tp, fn);
        f_measure = ClassificationMetrics.F1(tp, fp, fn);
    }

}

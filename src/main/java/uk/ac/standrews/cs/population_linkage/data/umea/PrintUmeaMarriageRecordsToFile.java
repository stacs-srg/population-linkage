/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.umea;

import uk.ac.standrews.cs.data.umea.UmeaMarriagesDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrintUmeaMarriageRecordsToFile {

    private final Path output_path;

    public PrintUmeaMarriageRecordsToFile(Path output_path) {

        this.output_path = output_path;
    }

    public void run() throws Exception {

        DataSet marriage_records = new UmeaMarriagesDataSet();
        marriage_records.print(new PrintStream(Files.newOutputStream(output_path)));
        System.out.println("Printed " + marriage_records.getRecords().size() + " marriage records");
    }

    public static void main(String[] args) throws Exception {

        new PrintUmeaMarriageRecordsToFile(Paths.get("/Users/al/Desktop/marriages.csv")).run();
    }
}

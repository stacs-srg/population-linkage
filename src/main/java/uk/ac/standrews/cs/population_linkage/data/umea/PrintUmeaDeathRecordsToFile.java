/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.umea;

import uk.ac.standrews.cs.data.umea.UmeaDeathsDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrintUmeaDeathRecordsToFile {

    private final Path output_path;

    private PrintUmeaDeathRecordsToFile(Path output_path) {

        this.output_path = output_path;
    }

    public void run() throws Exception {

        DataSet death_records = new UmeaDeathsDataSet();
        death_records.print(new PrintStream(Files.newOutputStream(output_path)));
        System.out.println("Printed " + death_records.getRecords().size() + " death records");
    }

    public static void main(String[] args) throws Exception {

        new PrintUmeaDeathRecordsToFile(Paths.get("/Users/al/Desktop/deaths.csv")).run();
    }
}

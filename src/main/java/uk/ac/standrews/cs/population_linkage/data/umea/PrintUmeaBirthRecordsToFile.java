/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.umea;

import uk.ac.standrews.cs.data.umea.UmeaBirthsDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrintUmeaBirthRecordsToFile {

    private final Path output_path;

    private PrintUmeaBirthRecordsToFile(Path output_path) {

        this.output_path = output_path;
    }

    public void run() throws Exception {

        DataSet birth_records = new UmeaBirthsDataSet();
        birth_records.print(new PrintStream(Files.newOutputStream(output_path)));
        System.out.println("Output " + birth_records.getRecords().size() + " birth records");
    }

    public static void main(String[] args) throws Exception {

        new PrintUmeaBirthRecordsToFile(Paths.get("/Users/graham/Desktop/births.csv")).run();
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.kilmarnock;

import uk.ac.standrews.cs.data.kilmarnock.KilmarnockDeathsDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrintKilmarnockDeathRecordsToFile {

    private final Path output_path;

    private PrintKilmarnockDeathRecordsToFile(Path output_path) {

        this.output_path = output_path;
    }

    public void run() throws Exception {

        DataSet death_records = new KilmarnockDeathsDataSet();
        death_records.print(new PrintStream(Files.newOutputStream(output_path)));
        System.out.println("Printed " + death_records.getRecords().size() + " death records");
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockDeathRecordsToFile(Paths.get("/Users/graham/Desktop/deaths.csv")).run();
    }
}

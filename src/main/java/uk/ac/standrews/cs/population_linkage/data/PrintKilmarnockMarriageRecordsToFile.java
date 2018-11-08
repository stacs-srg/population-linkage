package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.data.kilmarnock.KilmarnockMarriagesDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrintKilmarnockMarriageRecordsToFile {

    private final Path output_path;

    private PrintKilmarnockMarriageRecordsToFile(Path output_path) {

        this.output_path = output_path;
    }

    public void run() throws Exception {

        DataSet marriage_records = new KilmarnockMarriagesDataSet();
        marriage_records.print(new PrintStream(Files.newOutputStream(output_path)));
        System.out.println("Printed " + marriage_records.getRecords().size() + " marriage records");
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockMarriageRecordsToFile(Paths.get("/Users/graham/Desktop/marriages.csv")).run();
    }
}

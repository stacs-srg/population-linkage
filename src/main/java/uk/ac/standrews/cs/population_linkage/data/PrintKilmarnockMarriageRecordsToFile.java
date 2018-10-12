package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.data.kilmarnock.data.MarriagesDataSet;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PrintKilmarnockMarriageRecordsToFile {

    public void run() throws Exception {

        MarriagesDataSet marriage_records = new MarriagesDataSet();
        marriage_records.print(new PrintStream(Files.newOutputStream(Paths.get("/Users/graham/Desktop/marriages.csv"))));
        System.out.println("Printed " + marriage_records.getRecords().size() + " marriage records");
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockMarriageRecordsToFile().run();
    }
}

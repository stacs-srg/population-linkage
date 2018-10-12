package uk.ac.standrews.cs.population_linkage.actions;

import uk.ac.standrews.cs.data.kilmarnock.data.DeathsDataSet;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PrintKilmarnockDeathRecordsToFile {

    public void run() throws Exception {

        DeathsDataSet death_records = new DeathsDataSet();
        death_records.print(new PrintStream(Files.newOutputStream(Paths.get("/Users/graham/Desktop/deaths.csv"))));
        System.out.println("Printed " + death_records.getRecords().size() + " death records");
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockDeathRecordsToFile().run();
    }
}

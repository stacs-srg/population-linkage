package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.data.kilmarnock.data.BirthsDataSet;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrintKilmarnockBirthRecordsToFile {

    private final Path output_path;

    private PrintKilmarnockBirthRecordsToFile(Path output_path) {

        this.output_path = output_path;
    }

    public void run() throws Exception {

        BirthsDataSet birth_records = new BirthsDataSet();
        birth_records.print(new PrintStream(Files.newOutputStream(output_path)));
        System.out.println("Output " + birth_records.getRecords().size() + " birth records");
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockBirthRecordsToFile(Paths.get("/Users/graham/Desktop/births.csv")).run();
    }
}

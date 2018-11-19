package uk.ac.standrews.cs.population_linkage.data.skye;

import uk.ac.standrews.cs.data.skye.SkyeBirthsDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrintSkyeBirthRecordsToFile {

    private final Path output_path;

    private PrintSkyeBirthRecordsToFile(Path output_path) {

        this.output_path = output_path;
    }

    public void run() throws Exception {

        DataSet birth_records = new SkyeBirthsDataSet();
        birth_records.print(new PrintStream(Files.newOutputStream(output_path)));
        System.out.println("Output " + birth_records.getRecords().size() + " birth records");
    }

    public static void main(String[] args) throws Exception {

        new PrintSkyeBirthRecordsToFile(Paths.get("/Users/graham/Desktop/births.csv")).run();
    }
}

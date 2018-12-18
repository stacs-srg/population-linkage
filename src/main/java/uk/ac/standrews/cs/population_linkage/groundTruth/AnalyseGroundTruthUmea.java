package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.nio.file.Paths;
import java.util.List;

public class AnalyseGroundTruthUmea {

    private final String DELIMIT = ",";
    private final Iterable<String> labels;
    private final Iterable<List<String>> records;

    public AnalyseGroundTruthUmea(String filename) throws Exception {

        DataSet data = new DataSet(Paths.get(filename));
        labels = data.getColumnLabels();
        records = data.getRecords();

    }

    private void run() {

        for (String label : labels) {
            System.out.println(label);
        }


    }

    public static void main(String[] args) throws Exception {

        new AnalyseGroundTruthUmea("/Users/al/Desktop/distances.csv").run();

    }
}

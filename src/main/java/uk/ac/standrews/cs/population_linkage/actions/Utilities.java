package uk.ac.standrews.cs.population_linkage.actions;

import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.util.List;

public class Utilities {

    private final static int NUMBER_TO_PRINT = 5;

    public static void printSampleRecords(DataSet data_set, String record_type) {

        Utilities.printRow(data_set.getColumnLabels());
        List<List<String>> records = data_set.getRecords();

        for (int i = 0; i < NUMBER_TO_PRINT; i++) {
            Utilities.printRow(records.get(i));
        }

        System.out.println("Printed " + NUMBER_TO_PRINT + " of " + records.size() + " " + record_type + " records");
    }

    private static void printRow(List<String> row) {

        boolean first = true;
        for (String element : row) {
            if (!first) {
                System.out.print(",");
            }
            first = false;
            System.out.print(element);
        }
        System.out.println();
    }
}

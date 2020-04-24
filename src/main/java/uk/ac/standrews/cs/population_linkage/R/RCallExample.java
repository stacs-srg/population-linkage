/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.R;

import uk.ac.standrews.cs.population_linkage.data.umea.PrintUmeaMarriageRecordsToFile;

import java.nio.file.Paths;

public class RCallExample {


    private static String script_path = "src/main/scripts/R/example.R";
    private static String data_path = "/tmp/marriages.csv";
    private static String results_path = "/tmp/results.png";


    public RCallExample() throws Exception {

        new PrintUmeaMarriageRecordsToFile(Paths.get(data_path)).run();

        System.out.println( "Calling: " + "Rcaller.callR( " + script_path + "," + data_path + "," + results_path + " )" );
        int return_val = Rcaller.callR( script_path, data_path, results_path );
        System.out.println( "R call exited with value " + return_val );
    }

    public static void main(String[] args) throws Exception {
        new RCallExample();
    }


}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.R;

public class UmeaConvergence {

    private static String script_path = "src/main/scripts/R/plotUmea_convergence.R";
    private static String data_path = "/tmp/UmeaThresholdBirthSiblingLinkage.csv";
    private static String results_path = "/tmp/Umea-PRF.png";


    public UmeaConvergence() throws Exception {

        System.out.println( "Calling: " + "Rcaller.callR( " + script_path + "," + data_path + "," + results_path + " )" );
        int return_val = Rcaller.callR( script_path, data_path, results_path );
        System.out.println( "R call exited with value " + return_val );
    }

    public static void main(String[] args) throws Exception {
        new UmeaConvergence();
    }


}


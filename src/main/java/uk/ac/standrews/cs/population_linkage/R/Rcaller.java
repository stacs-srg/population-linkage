/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by al on 09/10/2017.
 */
public class Rcaller {

     public static int callR(String script_path, String data_path, String results_path ) throws IOException {

         String[] commands = {"Rscript", script_path, data_path, results_path};
         ProcessBuilder pb = new ProcessBuilder(commands);

         Process proc = pb.start();

         BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

         String next_line = null;

         while((next_line = stdInput.readLine())!=null) {
             System.out.println( "R message: " + next_line); // forward all R messages to stdout with a message
         }

         try {
             return proc.waitFor();
         } catch (InterruptedException e) {
             return 1; // fail
         }


     }
}

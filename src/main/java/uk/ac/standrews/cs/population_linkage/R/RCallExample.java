/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.R;

public class RCallExample {

    private static String script_path = "src/main/scripts/R/example.R";
    private static String data_path = "/tmp/marriages.csv";
    private static String results_path = "/tmp/results.png";

    public RCallExample() throws Exception {

        System.out.println( "Calling: " + "Rcaller.callR( " + script_path + "," + data_path + "," + results_path + " )" );
        int return_val = Rcaller.callR( script_path, data_path, results_path );
        System.out.println( "R call exited with value " + return_val );
    }

    public static void main(String[] args) throws Exception {
        new RCallExample();
    }
}

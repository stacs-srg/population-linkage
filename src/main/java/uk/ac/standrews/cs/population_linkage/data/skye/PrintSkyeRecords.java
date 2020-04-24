/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.skye;

public class PrintSkyeRecords {

    public void run() throws Exception {

        new PrintSkyeBirthRecords().run();
        new PrintSkyeDeathRecords().run();
        new PrintSkyeMarriageRecords().run();
    }

    public static void main(String[] args) throws Exception {

        new PrintSkyeRecords().run();
    }
}

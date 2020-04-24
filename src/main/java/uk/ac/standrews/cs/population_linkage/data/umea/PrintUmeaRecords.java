/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.umea;

public class PrintUmeaRecords {

    public void run() throws Exception {

        new PrintUmeaBirthRecords().run();
        new PrintUmeaDeathRecords().run();
        new PrintUmeaMarriageRecords().run();
    }

    public static void main(String[] args) throws Exception {

        new PrintUmeaRecords().run();
    }
}

/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.umea;

import uk.ac.standrews.cs.data.umea.UmeaMarriagesDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

public class PrintUmeaMarriageRecords {

    public void run() throws Exception {

        DataSet marriage_records = new UmeaMarriagesDataSet();
        marriage_records.print(System.out);
        System.out.println("Printed " + marriage_records.getRecords().size() + " marriage records");
    }

    public static void main(String[] args) throws Exception {

        new PrintUmeaMarriageRecords().run();
    }
}

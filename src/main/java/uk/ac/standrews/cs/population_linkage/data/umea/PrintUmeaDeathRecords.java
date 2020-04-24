/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.umea;

import uk.ac.standrews.cs.data.umea.UmeaDeathsDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

public class PrintUmeaDeathRecords {

    public void run() throws Exception {

        DataSet death_records = new UmeaDeathsDataSet();
        death_records.print(System.out);
        System.out.println("Printed " + death_records.getRecords().size() + " death records");
    }

    public static void main(String[] args) throws Exception {

        new PrintUmeaDeathRecords().run();
    }
}

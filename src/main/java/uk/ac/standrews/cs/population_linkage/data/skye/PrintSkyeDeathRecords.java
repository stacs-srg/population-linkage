/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.skye;

import uk.ac.standrews.cs.data.skye.SkyeDeathsDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

public class PrintSkyeDeathRecords {

    public void run() throws Exception {

        DataSet death_records = new SkyeDeathsDataSet();
        death_records.print(System.out);
        System.out.println("Printed " + death_records.getRecords().size() + " death records");
    }

    public static void main(String[] args) throws Exception {

        new PrintSkyeDeathRecords().run();
    }
}

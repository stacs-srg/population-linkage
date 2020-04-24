/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.skye;

import uk.ac.standrews.cs.data.skye.SkyeBirthsDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

public class PrintSkyeBirthRecords {

    public void run() throws Exception {

        DataSet birth_records = new SkyeBirthsDataSet();
        birth_records.print(System.out);
        System.out.println("Printed " + birth_records.getRecords().size() + " birth records");
    }

    public static void main(String[] args) throws Exception {

        new PrintSkyeBirthRecords().run();
    }
}

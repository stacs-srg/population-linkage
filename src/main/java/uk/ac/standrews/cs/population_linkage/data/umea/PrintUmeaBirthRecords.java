/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.umea;

import uk.ac.standrews.cs.data.umea.UmeaBirthsDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

public class PrintUmeaBirthRecords {

    public void run() throws Exception {

        DataSet birth_records = new UmeaBirthsDataSet();
        birth_records.print(System.out);
        System.out.println("Printed " + birth_records.getRecords().size() + " birth records");
    }

    public static void main(String[] args) throws Exception {

        new PrintUmeaBirthRecords().run();
    }
}

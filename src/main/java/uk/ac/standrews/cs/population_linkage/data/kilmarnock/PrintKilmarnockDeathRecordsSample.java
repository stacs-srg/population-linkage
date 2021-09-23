/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.kilmarnock;

import uk.ac.standrews.cs.data.kilmarnock.KilmarnockDeathsDataSet;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;

public class PrintKilmarnockDeathRecordsSample {

    public void run() throws Exception {

        Utilities.printSampleRecords(new KilmarnockDeathsDataSet(), "death", PrintKilmarnockRecordsFromStoreSample.NUMBER_TO_PRINT);
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockDeathRecordsSample().run();
    }
}
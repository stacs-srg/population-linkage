/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.kilmarnock;

import uk.ac.standrews.cs.data.kilmarnock.KilmarnockBirthsDataSet;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;

public class PrintKilmarnockBirthRecordsSample {

    public void run() throws Exception {

        Utilities.printSampleRecords(new KilmarnockBirthsDataSet(), "birth", PrintKilmarnockRecordsFromStoreSample.NUMBER_TO_PRINT);
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockBirthRecordsSample().run();
    }
}

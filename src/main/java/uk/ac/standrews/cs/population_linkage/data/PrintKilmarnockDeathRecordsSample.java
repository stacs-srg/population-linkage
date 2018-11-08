package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.data.kilmarnock.KilmarnockDeathsDataSet;

public class PrintKilmarnockDeathRecordsSample {

    public void run() throws Exception {

        Utilities.printSampleRecords(new KilmarnockDeathsDataSet(), "death", PrintKilmarnockRecordsFromStoreSample.NUMBER_TO_PRINT);
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockDeathRecordsSample().run();
    }
}

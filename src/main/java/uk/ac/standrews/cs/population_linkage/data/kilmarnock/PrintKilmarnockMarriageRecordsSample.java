package uk.ac.standrews.cs.population_linkage.data.kilmarnock;

import uk.ac.standrews.cs.data.kilmarnock.KilmarnockMarriagesDataSet;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;

public class PrintKilmarnockMarriageRecordsSample {

    public void run() throws Exception {

        Utilities.printSampleRecords(new KilmarnockMarriagesDataSet(), "marriage", PrintKilmarnockRecordsFromStoreSample.NUMBER_TO_PRINT);
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockMarriageRecordsSample().run();
    }
}

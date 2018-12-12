package uk.ac.standrews.cs.population_linkage.data.umea;

import uk.ac.standrews.cs.data.umea.UmeaMarriagesDataSet;
import uk.ac.standrews.cs.population_linkage.data.Utilities;

public class PrintUmeaMarriageRecordsSample {

    public void run() throws Exception {

        Utilities.printSampleRecords(new UmeaMarriagesDataSet(), "marriage", PrintUmeaRecordsFromStoreSample.NUMBER_TO_PRINT);
    }

    public static void main(String[] args) throws Exception {

        new PrintUmeaMarriageRecordsSample().run();
    }
}
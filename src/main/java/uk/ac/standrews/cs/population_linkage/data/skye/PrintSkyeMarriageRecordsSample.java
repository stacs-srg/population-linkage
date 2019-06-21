package uk.ac.standrews.cs.population_linkage.data.skye;

import uk.ac.standrews.cs.data.skye.SkyeMarriagesDataSet;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Utilities;

public class PrintSkyeMarriageRecordsSample {

    public void run() throws Exception {

        Utilities.printSampleRecords(new SkyeMarriagesDataSet(), "marriage", PrintSkyeRecordsFromStoreSample.NUMBER_TO_PRINT);
    }

    public static void main(String[] args) throws Exception {

        new PrintSkyeMarriageRecordsSample().run();
    }
}

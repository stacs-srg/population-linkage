package uk.ac.standrews.cs.population_linkage.data.skye;

import uk.ac.standrews.cs.data.skye.SkyeDeathsDataSet;
import uk.ac.standrews.cs.population_linkage.data.Utilities;

public class PrintSkyeDeathRecordsSample {

    public void run() throws Exception {

        Utilities.printSampleRecords(new SkyeDeathsDataSet(), "death", PrintSkyeRecordsFromStoreSample.NUMBER_TO_PRINT);
    }

    public static void main(String[] args) throws Exception {

        new PrintSkyeDeathRecordsSample().run();
    }
}

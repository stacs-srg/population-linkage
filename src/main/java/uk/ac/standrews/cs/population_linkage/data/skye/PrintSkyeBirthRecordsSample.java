package uk.ac.standrews.cs.population_linkage.data.skye;

import uk.ac.standrews.cs.data.skye.SkyeBirthsDataSet;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Utilities;

public class PrintSkyeBirthRecordsSample {

    public void run() throws Exception {

        Utilities.printSampleRecords(new SkyeBirthsDataSet(), "birth", PrintSkyeRecordsFromStoreSample.NUMBER_TO_PRINT);
    }

    public static void main(String[] args) throws Exception {

        new PrintSkyeBirthRecordsSample().run();
    }
}

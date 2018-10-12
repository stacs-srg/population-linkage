package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.data.kilmarnock.data.DeathsDataSet;

public class PrintKilmarnockDeathRecordsSample {

    public void run() throws Exception {

        Utilities.printSampleRecords(new DeathsDataSet(), "death");
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockDeathRecordsSample().run();
    }
}

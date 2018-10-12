package uk.ac.standrews.cs.population_linkage.actions;

import uk.ac.standrews.cs.data.kilmarnock.data.DeathsDataSet;

public class PrintSampleKilmarnockDeathRecords {

    public void run() throws Exception {

        Utilities.printSampleRecords(new DeathsDataSet(), "death");
    }

    public static void main(String[] args) throws Exception {

        new PrintSampleKilmarnockDeathRecords().run();
    }
}

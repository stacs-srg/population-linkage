package uk.ac.standrews.cs.population_linkage.actions;

import uk.ac.standrews.cs.data.kilmarnock.data.BirthsDataSet;

public class PrintSampleKilmarnockBirthRecords {

    public void run() throws Exception {

        Utilities.printSampleRecords(new BirthsDataSet(), "birth");
    }

    public static void main(String[] args) throws Exception {

        new PrintSampleKilmarnockBirthRecords().run();
    }
}

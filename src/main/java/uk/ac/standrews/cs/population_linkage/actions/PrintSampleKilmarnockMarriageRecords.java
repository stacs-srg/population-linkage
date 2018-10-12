package uk.ac.standrews.cs.population_linkage.actions;

import uk.ac.standrews.cs.data.kilmarnock.data.MarriagesDataSet;

public class PrintSampleKilmarnockMarriageRecords {

    public void run() throws Exception {

        Utilities.printSampleRecords(new MarriagesDataSet(), "marriage");
    }

    public static void main(String[] args) throws Exception {

        new PrintSampleKilmarnockMarriageRecords().run();
    }
}

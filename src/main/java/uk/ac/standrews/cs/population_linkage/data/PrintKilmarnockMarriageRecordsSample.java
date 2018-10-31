package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.data.kilmarnock.KilmarnockMarriagesDataSet;

public class PrintKilmarnockMarriageRecordsSample {

    public void run() throws Exception {

        Utilities.printSampleRecords(new KilmarnockMarriagesDataSet(), "marriage");
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockMarriageRecordsSample().run();
    }
}

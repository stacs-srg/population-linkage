package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.data.kilmarnock.data.BirthsDataSet;

public class PrintKilmarnockBirthRecordsSample {

    public void run() throws Exception {

        Utilities.printSampleRecords(new BirthsDataSet(), "birth");
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockBirthRecordsSample().run();
    }
}

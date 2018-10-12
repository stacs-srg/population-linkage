package uk.ac.standrews.cs.population_linkage.actions;

import uk.ac.standrews.cs.data.kilmarnock.data.Census1861DataSet;

public class PrintKilmarnockCensus1861Records {

    public void run() throws Exception {

        Census1861DataSet census_records = new Census1861DataSet();
        census_records.print(System.out);
        System.out.println("Printed " + census_records.getRecords().size() + " census records");
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockCensus1861Records().run();
    }
}

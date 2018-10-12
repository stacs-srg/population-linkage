package uk.ac.standrews.cs.population_linkage.actions;

import uk.ac.standrews.cs.data.kilmarnock.data.MarriagesDataSet;

public class PrintKilmarnockMarriageRecords {

    public void run() throws Exception {

        MarriagesDataSet marriage_records = new MarriagesDataSet();
        marriage_records.print(System.out);
        System.out.println("Printed " + marriage_records.getRecords().size() + " marriage records");
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockMarriageRecords().run();
    }
}
